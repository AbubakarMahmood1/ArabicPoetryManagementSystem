using System;
using System.Diagnostics;
using System.IO;
using System.Text;

namespace ArabicPoetryInstaller
{
    internal static class ProvisionMySqlRunner
    {
        public static int Main(string[] args)
        {
            var logPath = ResolveLogPath();
            AppendLog(logPath, "=== ArabicPoetry MySQL provisioning runner ===");
            AppendLog(logPath, "Timestamp: " + DateTime.Now.ToString("O"));
            AppendLog(logPath, "BaseDirectory: " + AppContext.BaseDirectory);
            AppendLog(logPath, "Args: " + (args == null ? "<null>" : string.Join(" | ", args)));

            var mode = "install";
            var argIndex = 0;
            if (args != null && args.Length > 0 && !string.IsNullOrWhiteSpace(args[0]))
            {
                var cmd = args[0].Trim().ToLowerInvariant();
                if (cmd == "install" || cmd == "uninstall")
                {
                    mode = cmd;
                    argIndex = 1;
                }
            }

            // Legacy explicit-path mode: [scriptPath] [mysqlZipPath] [schemaSqlPath]
            var scriptPath = (args != null && args.Length > argIndex && !string.IsNullOrWhiteSpace(args[argIndex])) ? args[argIndex] : null;
            var mysqlZipPath = (args != null && args.Length > (argIndex + 1) && !string.IsNullOrWhiteSpace(args[argIndex + 1])) ? args[argIndex + 1] : null;
            var schemaSqlPath = (args != null && args.Length > (argIndex + 2) && !string.IsNullOrWhiteSpace(args[argIndex + 2])) ? args[argIndex + 2] : null;

            // Burn variable expansion for payload paths is easy to get wrong; support a robust
            // "no-args" mode by resolving payloads next to the cached runner EXE.
            if (string.IsNullOrWhiteSpace(scriptPath) ||
                string.IsNullOrWhiteSpace(mysqlZipPath) ||
                string.IsNullOrWhiteSpace(schemaSqlPath))
            {
                var baseDir = AppContext.BaseDirectory;
                scriptPath = Path.Combine(baseDir, "setup-mysql.ps1");
                mysqlZipPath = Path.Combine(baseDir, "mysql.zip");
                schemaSqlPath = Path.Combine(baseDir, "schema-install.sql");
            }

            AppendLog(logPath, "Mode: " + mode);
            AppendLog(logPath, "Resolved setup-mysql.ps1: " + scriptPath);
            AppendLog(logPath, "Resolved mysql.zip:       " + mysqlZipPath);
            AppendLog(logPath, "Resolved schema SQL:      " + schemaSqlPath);

            if (mode == "install" && (!File.Exists(scriptPath) || !File.Exists(mysqlZipPath) || !File.Exists(schemaSqlPath)))
            {
                Console.Error.WriteLine("Missing provisioning payload(s).");
                Console.Error.WriteLine("setup-mysql.ps1: " + scriptPath + " (exists=" + File.Exists(scriptPath) + ")");
                Console.Error.WriteLine("mysql.zip:       " + mysqlZipPath + " (exists=" + File.Exists(mysqlZipPath) + ")");
                Console.Error.WriteLine("schema SQL:      " + schemaSqlPath + " (exists=" + File.Exists(schemaSqlPath) + ")");
                AppendLog(logPath, "ERROR: Missing provisioning payload(s).");
                return 3;
            }

            if (mode == "uninstall" && !File.Exists(scriptPath))
            {
                Console.Error.WriteLine("Missing setup-mysql.ps1 payload: " + scriptPath);
                AppendLog(logPath, "ERROR: Missing setup-mysql.ps1 payload.");
                return 3;
            }

            var systemRoot = Environment.GetEnvironmentVariable("SystemRoot") ?? @"C:\Windows";
            var powershell = Path.Combine(systemRoot, @"System32\WindowsPowerShell\v1.0\powershell.exe");
            if (!File.Exists(powershell))
            {
                Console.Error.WriteLine("powershell.exe not found at: " + powershell);
                AppendLog(logPath, "ERROR: powershell.exe not found at: " + powershell);
                return 4;
            }

            var psi = new ProcessStartInfo
            {
                FileName = powershell,
                Arguments = BuildPowerShellArgs(mode, scriptPath, mysqlZipPath, schemaSqlPath),
                UseShellExecute = false,
                RedirectStandardOutput = true,
                RedirectStandardError = true,
                StandardOutputEncoding = Encoding.UTF8,
                StandardErrorEncoding = Encoding.UTF8,
                CreateNoWindow = true
            };

            try
            {
                using (var process = Process.Start(psi))
                {
                    if (process == null)
                    {
                        Console.Error.WriteLine("Failed to start powershell.exe.");
                        AppendLog(logPath, "ERROR: Failed to start powershell.exe.");
                        return 5;
                    }
                    var stdout = process.StandardOutput.ReadToEnd();
                    var stderr = process.StandardError.ReadToEnd();
                    process.WaitForExit();

                    if (!string.IsNullOrWhiteSpace(stdout))
                    {
                        AppendLog(logPath, "--- PowerShell STDOUT ---");
                        AppendLog(logPath, stdout.TrimEnd());
                    }
                    if (!string.IsNullOrWhiteSpace(stderr))
                    {
                        AppendLog(logPath, "--- PowerShell STDERR ---");
                        AppendLog(logPath, stderr.TrimEnd());
                    }

                    AppendLog(logPath, "PowerShell exit code: " + process.ExitCode);
                    return process.ExitCode;
                }
            }
            catch (Exception ex)
            {
                Console.Error.WriteLine("Provisioning failed: " + ex);
                AppendLog(logPath, "EXCEPTION: " + ex);
                return 6;
            }
        }

        private static string BuildPowerShellArgs(string mode, string scriptPath, string mysqlZipPath, string schemaSqlPath)
        {
            var prefix = "-NoProfile -STA -ExecutionPolicy Bypass -File " + Quote(scriptPath);
            if (string.Equals(mode, "uninstall", StringComparison.OrdinalIgnoreCase))
            {
                return prefix + " -Uninstall";
            }
            return prefix + " -MysqlZip " + Quote(mysqlZipPath) + " -SchemaSql " + Quote(schemaSqlPath);
        }

        private static string Quote(string value)
        {
            return "\"" + (value ?? string.Empty).Replace("\"", "\\\"") + "\"";
        }

        private static string ResolveLogPath()
        {
            var env = Environment.GetEnvironmentVariable("ARABIC_POETRY_INSTALL_LOG");
            if (!string.IsNullOrWhiteSpace(env))
            {
                TryEnsureDirectory(env);
                return env;
            }

            try
            {
                var desktop = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
                if (!string.IsNullOrWhiteSpace(desktop))
                {
                    var mapped = Path.Combine(desktop, "ArabicPoetryLogs");
                    if (Directory.Exists(mapped))
                    {
                        var p = Path.Combine(mapped, "ArabicPoetry-MySqlProvision.log");
                        TryEnsureDirectory(p);
                        return p;
                    }
                }
            }
            catch
            {
                // ignore
            }

            var commonAppData = Environment.GetFolderPath(Environment.SpecialFolder.CommonApplicationData);
            if (string.IsNullOrWhiteSpace(commonAppData))
            {
                commonAppData = @"C:\ProgramData";
            }
            var dir = Path.Combine(commonAppData, "ArabicPoetry", "install-logs");
            Directory.CreateDirectory(dir);
            return Path.Combine(dir, "ArabicPoetry-MySqlProvision.log");
        }

        private static void TryEnsureDirectory(string filePath)
        {
            try
            {
                var dir = Path.GetDirectoryName(filePath);
                if (!string.IsNullOrWhiteSpace(dir))
                {
                    Directory.CreateDirectory(dir);
                }
            }
            catch
            {
                // ignore
            }
        }

        private static void AppendLog(string path, string message)
        {
            try
            {
                File.AppendAllText(path, (message ?? string.Empty) + Environment.NewLine, Encoding.UTF8);
            }
            catch
            {
                // ignore
            }
        }
    }
}
