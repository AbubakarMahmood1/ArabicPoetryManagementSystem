<#
Sets up a local MySQL instance for ArabicPoetry and writes the installed config.properties.

Designed to be run by the Windows bootstrapper after the application MSI is installed.
- Extracts a MySQL "noinstall" ZIP distribution into ProgramData
- Initializes a local data directory
- Installs and starts a Windows service bound to 127.0.0.1
- Applies database/schema-install.sql
- Creates an app DB user and writes app/config.properties accordingly

This script is intentionally idempotent for first-install scenarios.
#>

[CmdletBinding()]
param(
    [string]$MysqlZip,

    [string]$SchemaSql,

    [string]$ServiceName = "ArabicPoetryMySQL",
    [int]$PreferredPort = 3307,
    [string]$AppDbName = "arabic_poetry_db",
    [string]$AppDbUser = "arabic_poetry_app",

    [switch]$Uninstall,

    # Optional: pass a password non-interactively (not recommended since it may appear in logs/process list)
    [string]$AppDbPassword
)

$ErrorActionPreference = "Stop"

function New-RandomPassword {
    param([int]$Length = 20)
    $alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789-_."
    $bytes = New-Object byte[] $Length
    $rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try {
        $rng.GetBytes($bytes)
    } finally {
        if ($rng -is [System.IDisposable]) { $rng.Dispose() }
    }
    -join ($bytes | ForEach-Object { $alphabet[ $_ % $alphabet.Length ] })
}

function Prompt-AppPassword {
    if (-not [System.Environment]::UserInteractive) {
        return $null
    }

    try {
        Add-Type -AssemblyName System.Windows.Forms | Out-Null
        Add-Type -AssemblyName System.Drawing | Out-Null
    } catch {
        return $null
    }

    $form = New-Object System.Windows.Forms.Form
    $form.Text = "ArabicPoetry - Database Password"
    $form.StartPosition = "CenterScreen"
    $form.Width = 520
    $form.Height = 210
    $form.FormBorderStyle = "FixedDialog"
    $form.MaximizeBox = $false
    $form.MinimizeBox = $false
    $form.TopMost = $true

    $label = New-Object System.Windows.Forms.Label
    $label.Left = 12
    $label.Top = 12
    $label.Width = 490
    $label.Height = 40
    $label.Text = "Enter a password for the local database user '$AppDbUser'.`r`nLeave blank to auto-generate."
    $form.Controls.Add($label)

    $textBox = New-Object System.Windows.Forms.TextBox
    $textBox.Left = 12
    $textBox.Top = 60
    $textBox.Width = 490
    $textBox.UseSystemPasswordChar = $true
    $form.Controls.Add($textBox)

    $ok = New-Object System.Windows.Forms.Button
    $ok.Text = "OK"
    $ok.Left = 324
    $ok.Top = 105
    $ok.Width = 85
    $ok.DialogResult = [System.Windows.Forms.DialogResult]::OK
    $form.Controls.Add($ok)

    $cancel = New-Object System.Windows.Forms.Button
    $cancel.Text = "Cancel"
    $cancel.Left = 417
    $cancel.Top = 105
    $cancel.Width = 85
    $cancel.DialogResult = [System.Windows.Forms.DialogResult]::Cancel
    $form.Controls.Add($cancel)

    $form.AcceptButton = $ok
    $form.CancelButton = $cancel

    $result = $form.ShowDialog()
    if ($result -ne [System.Windows.Forms.DialogResult]::OK) {
        throw "Database setup canceled by user."
    }

    $value = $textBox.Text
    if ([string]::IsNullOrWhiteSpace($value)) { return $null }
    return $value
}

function Find-InstallLocation {
    param([string]$DisplayName = "ArabicPoetry")

    function Normalize-PathCandidate {
        param([string]$Value)
        if (-not $Value) { return $null }
        $v = $Value.Trim()
        if ($v.StartsWith('"') -and $v.EndsWith('"')) {
            $v = $v.Trim('"')
        }

        # Common patterns:
        # - C:\Path\ArabicPoetry\  (InstallLocation)
        # - "C:\Path\ArabicPoetry\ArabicPoetry.exe",0  (DisplayIcon)
        # - C:\Path\ArabicPoetry\ArabicPoetry.exe,0
        if ($v -match '^[A-Za-z]:\\') {
            # If it's an .exe (optionally followed by ,0 or args), reduce to its parent folder.
            if ($v -match '([A-Za-z]:\\[^,"]+?\\.exe)') {
                $exe = $Matches[1]
                return (Split-Path $exe -Parent)
            }
            # Otherwise treat as folder path.
            return $v.TrimEnd('\')
        }

        # As a last resort, try to extract a drive-path substring.
        if ($v -match '([A-Za-z]:\\[^,"]+?)(?:,\\d+)?') {
            return $Matches[1].TrimEnd('\')
        }

        return $null
    }

    $uninstallRoots = @(
        "HKLM:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
        "HKLM:\\Software\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall",
        "HKCU:\\Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall"
    )

    foreach ($root in $uninstallRoots) {
        if (-not (Test-Path $root)) { continue }
        foreach ($key in (Get-ChildItem $root -ErrorAction SilentlyContinue)) {
            try {
                $p = Get-ItemProperty $key.PsPath -ErrorAction Stop
                if ($p.DisplayName -ne $DisplayName) { continue }

                $candidates = @()
                if ($p.InstallLocation) { $candidates += $p.InstallLocation }
                if ($p.DisplayIcon) { $candidates += $p.DisplayIcon }

                foreach ($c in $candidates) {
                    $resolved = Normalize-PathCandidate -Value $c
                    if ($resolved -and (Test-Path $resolved)) {
                        return $resolved
                    }
                }
            } catch { }
        }
    }

    $fallbacks = @(
        (Join-Path $env:ProgramFiles "ArabicPoetry"),
        (Join-Path ${env:ProgramFiles(x86)} "ArabicPoetry"),
        (Join-Path $env:LOCALAPPDATA "Programs\\ArabicPoetry")
    ) | Where-Object { $_ -and $_.Trim() -ne "" }

    foreach ($p in $fallbacks) {
        if (Test-Path $p) { return $p }
    }

    throw "Unable to locate installed ArabicPoetry directory (DisplayName='$DisplayName')."
}

function Resolve-AppConfigPath {
    param([string]$InstallLocation)
    if ($InstallLocation -is [System.Array]) {
        $InstallLocation = $InstallLocation | Select-Object -First 1
    }
    if ($InstallLocation) { $InstallLocation = $InstallLocation.Trim() }
    $candidates = @(
        (Join-Path $InstallLocation "app\\config.properties"),
        (Join-Path $InstallLocation "config.properties")
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { return $c }
    }
    $found = Get-ChildItem -Path $InstallLocation -Recurse -Filter "config.properties" -File -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($found) { return $found.FullName }
    throw "Could not find config.properties under '$InstallLocation'."
}

function Resolve-JavafxBin {
    param([string]$InstallLocation)
    if ($InstallLocation -is [System.Array]) {
        $InstallLocation = $InstallLocation | Select-Object -First 1
    }
    if ($InstallLocation) { $InstallLocation = $InstallLocation.Trim() }
    $candidates = @(
        (Join-Path $InstallLocation "app\\javafx-bin"),
        (Join-Path $InstallLocation "javafx-bin")
    )
    foreach ($c in $candidates) {
        if (Test-Path $c) { return $c }
    }
    return $null
}

function Find-MysqlBinaries {
    param([string]$SearchRoot)

    $mysqld = Get-ChildItem -Path $SearchRoot -Recurse -File -Filter "mysqld.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
    $mysql = Get-ChildItem -Path $SearchRoot -Recurse -File -Filter "mysql.exe" -ErrorAction SilentlyContinue | Select-Object -First 1
    if (-not $mysqld -or -not $mysql) {
        throw "MySQL binaries not found under '$SearchRoot'. Ensure MysqlZip is a MySQL Windows ZIP distribution."
    }

    return [PSCustomObject]@{
        Mysqld = $mysqld.FullName
        Mysql  = $mysql.FullName
        BinDir = (Split-Path $mysqld.FullName -Parent)
        Home   = (Split-Path (Split-Path $mysqld.FullName -Parent) -Parent)
    }
}

function Test-PortAvailable {
    param([int]$Port)
    try {
        $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $Port)
        $listener.Start()
        $listener.Stop()
        return $true
    } catch {
        return $false
    }
}

function Select-FreePort {
    param([int]$StartPort, [int]$MaxTries = 50)
    for ($p = $StartPort; $p -lt ($StartPort + $MaxTries); $p++) {
        if (Test-PortAvailable -Port $p) { return $p }
    }
    throw "No available TCP port found starting at $StartPort (tried $MaxTries ports)."
}

function TryRead-PortFromIni {
    param([string]$IniPath)
    if (-not (Test-Path $IniPath)) { return $null }
    $match = Get-Content -Path $IniPath -ErrorAction SilentlyContinue |
        Select-String -Pattern '^\s*port\s*=\s*(\d+)\s*$' |
        Select-Object -First 1
    if (-not $match) { return $null }
    return [int]$match.Matches[0].Groups[1].Value
}

function Expand-Zip {
    param(
        [Parameter(Mandatory = $true)][string]$ZipPath,
        [Parameter(Mandatory = $true)][string]$DestinationPath
    )

    # Prefer Expand-Archive when available, but fall back to .NET zip extraction for minimal images.
    $expandArchive = Get-Command Expand-Archive -ErrorAction SilentlyContinue
    if ($expandArchive) {
        Expand-Archive -Path $ZipPath -DestinationPath $DestinationPath -Force
        return
    }

    Add-Type -AssemblyName System.IO.Compression.FileSystem -ErrorAction Stop | Out-Null

    if (Test-Path $DestinationPath) {
        Remove-Item -Path $DestinationPath -Recurse -Force -ErrorAction SilentlyContinue
    }
    New-Item -ItemType Directory -Path $DestinationPath -Force | Out-Null

    [System.IO.Compression.ZipFile]::ExtractToDirectory($ZipPath, $DestinationPath)
}

function Copy-VcRuntimeIfPresent {
    param(
        [string]$JavafxBin,
        [string]$MysqlBin
    )
    if (-not $JavafxBin -or -not (Test-Path $JavafxBin)) { return }

    $dlls = @(
        "vcruntime140.dll",
        "vcruntime140_1.dll",
        "msvcp140.dll",
        "msvcp140_1.dll",
        "msvcp140_2.dll",
        "ucrtbase.dll"
    )

    foreach ($d in $dlls) {
        $src = Join-Path $JavafxBin $d
        $dst = Join-Path $MysqlBin $d
        if ((Test-Path $src) -and -not (Test-Path $dst)) {
            Copy-Item $src $dst -Force
        }
    }
}

function Write-AppConfig {
    param(
        [string]$ConfigPath,
        [int]$Port,
        [string]$DbName,
        [string]$DbUser,
        [string]$DbPassword
    )

    $jdbcParams = "useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true"
    $jdbcUrl = "jdbc:mysql://127.0.0.1:$Port/$DbName" + "?" + $jdbcParams

    $content = @"
# Arabic Poetry Management System - Database Configuration
db.driver=com.mysql.cj.jdbc.Driver
db.url=$jdbcUrl
db.username=$DbUser
db.password=$DbPassword
"@
    Set-Content -Path $ConfigPath -Value $content -Encoding ASCII
}

Write-Host "ArabicPoetry DB setup: starting..."

if ($Uninstall) {
    $programData = [System.Environment]::GetFolderPath('CommonApplicationData')
    if (-not $programData) { $programData = "C:\ProgramData" }
    $root = Join-Path $programData "ArabicPoetry"
    $mysqlExtractRoot = Join-Path $root "mysql"
    $mysqlData = Join-Path $root "mysql-data"
    $mysqlIni = Join-Path $root "my.ini"

    Write-Host "ArabicPoetry DB setup: uninstall requested."
    Write-Host "Stopping MySQL service '$ServiceName' (if present)..."
    try {
        $svc = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
        if ($svc) {
            try { Stop-Service -Name $ServiceName -Force -ErrorAction SilentlyContinue } catch { }
            try {
                $svc.Refresh()
                $svc.WaitForStatus('Stopped', [TimeSpan]::FromSeconds(30))
            } catch { }
        }
    } catch { }

    Write-Host "Removing MySQL service '$ServiceName' (if present)..."
    try {
        & sc.exe delete $ServiceName | Out-Null
    } catch { }

    # Remove provisioning marker (best effort).
    try {
        $serviceKey = "HKLM:\SYSTEM\CurrentControlSet\Services\$ServiceName"
        if (Test-Path $serviceKey) {
            Remove-ItemProperty -Path $serviceKey -Name "ArabicPoetryProvisioned" -ErrorAction SilentlyContinue
        }
    } catch { }

    Write-Host "Deleting local MySQL files under $root ..."
    foreach ($p in @($mysqlIni, $mysqlExtractRoot, $mysqlData, (Join-Path $root "install-logs"))) {
        try {
            if (Test-Path $p) { Remove-Item -Path $p -Recurse -Force -ErrorAction SilentlyContinue }
        } catch { }
    }

    # If nothing remains, remove the root folder too.
    try {
        if (Test-Path $root) {
            $remaining = Get-ChildItem -Path $root -Force -ErrorAction SilentlyContinue
            if (-not $remaining) { Remove-Item -Path $root -Force -ErrorAction SilentlyContinue }
        }
    } catch { }

    Write-Host "ArabicPoetry DB uninstall: completed."
    exit 0
}

if (-not (Test-Path $MysqlZip)) { throw "MysqlZip not found: $MysqlZip" }
if (-not (Test-Path $SchemaSql)) { throw "SchemaSql not found: $SchemaSql" }

$installLocation = Find-InstallLocation
$configPath = Resolve-AppConfigPath -InstallLocation $installLocation
$javafxBin = Resolve-JavafxBin -InstallLocation $installLocation
Write-Host "Install location: $installLocation"
Write-Host "Config path:      $configPath"
if ($javafxBin) { Write-Host "JavaFX bin:       $javafxBin" }

$programData = [System.Environment]::GetFolderPath('CommonApplicationData')
$root = Join-Path $programData "ArabicPoetry"
$mysqlExtractRoot = Join-Path $root "mysql"
$mysqlData = Join-Path $root "mysql-data"
$mysqlIni = Join-Path $root "my.ini"

New-Item -ItemType Directory -Path $mysqlExtractRoot -Force | Out-Null
New-Item -ItemType Directory -Path $mysqlData -Force | Out-Null

$service = Get-Service -Name $ServiceName -ErrorAction SilentlyContinue
if (-not $service) {
    $port = Select-FreePort -StartPort $PreferredPort
    Write-Host "Extracting MySQL to $mysqlExtractRoot ..."
    Expand-Zip -ZipPath $MysqlZip -DestinationPath $mysqlExtractRoot
} else {
    $existingPort = TryRead-PortFromIni -IniPath $mysqlIni
    $port = if ($existingPort) { $existingPort } else { Select-FreePort -StartPort $PreferredPort }
    Write-Host "MySQL service '$ServiceName' already exists; using port $port."
}

$bins = Find-MysqlBinaries -SearchRoot $mysqlExtractRoot
Copy-VcRuntimeIfPresent -JavafxBin $javafxBin -MysqlBin $bins.BinDir

if (-not (Test-Path $mysqlIni)) {
    $iniContent = @"
[mysqld]
basedir="$($bins.Home -replace '\\','/')"
datadir="$($mysqlData -replace '\\','/')"
port=$port
bind-address=127.0.0.1
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
max_allowed_packet=64M
skip_ssl=ON

[client]
host=127.0.0.1
port=$port
default-character-set=utf8mb4
"@
    Set-Content -Path $mysqlIni -Value $iniContent -Encoding ASCII
}

if (-not $service) {
    $systemDbMarker = Join-Path $mysqlData "mysql"
    if (-not (Test-Path $systemDbMarker)) {
        Write-Host "Initializing MySQL data directory at $mysqlData ..."
        & $bins.Mysqld "--defaults-file=$mysqlIni" "--initialize-insecure"
        if ($LASTEXITCODE -ne 0) { throw "mysqld --initialize-insecure failed with exit code $LASTEXITCODE" }
    }

    Write-Host "Installing Windows service '$ServiceName' ..."
    & $bins.Mysqld "--install" $ServiceName "--defaults-file=$mysqlIni"
    if ($LASTEXITCODE -ne 0) { throw "mysqld --install failed with exit code $LASTEXITCODE" }
    & sc.exe config $ServiceName start= auto | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "sc.exe config failed with exit code $LASTEXITCODE" }
}

Write-Host "Starting MySQL service '$ServiceName' ..."
Start-Service -Name $ServiceName

Write-Host "Waiting for MySQL to accept connections..."
for ($i = 0; $i -lt 60; $i++) {
    & $bins.Mysql "--defaults-file=$mysqlIni" -u root --execute "SELECT 1;" | Out-Null
    if ($LASTEXITCODE -eq 0) { break }
    Start-Sleep -Seconds 1
    if ($i -eq 59) { throw "MySQL did not become ready in time." }
}

if (-not $AppDbPassword) {
    $AppDbPassword = Prompt-AppPassword
}
if (-not $AppDbPassword) {
    $AppDbPassword = New-RandomPassword
}

Write-Host "Applying schema..."
Get-Content -Path $SchemaSql -Raw | & $bins.Mysql "--defaults-file=$mysqlIni" -u root --binary-mode=1 --default-character-set=utf8mb4
if ($LASTEXITCODE -ne 0) { throw "Applying schema failed with exit code $LASTEXITCODE" }

Write-Host "Creating application DB user..."
$safePassword = $AppDbPassword.Replace("'", "''")
$userSql = @"
-- Use mysql_native_password to avoid RSA public key retrieval issues with caching_sha2_password on fresh local installs.
CREATE USER IF NOT EXISTS '$AppDbUser'@'localhost' IDENTIFIED WITH mysql_native_password BY '$safePassword';
ALTER USER '$AppDbUser'@'localhost' IDENTIFIED WITH mysql_native_password BY '$safePassword';
CREATE USER IF NOT EXISTS '$AppDbUser'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY '$safePassword';
ALTER USER '$AppDbUser'@'127.0.0.1' IDENTIFIED WITH mysql_native_password BY '$safePassword';
GRANT ALL PRIVILEGES ON $AppDbName.* TO '$AppDbUser'@'localhost';
GRANT ALL PRIVILEGES ON $AppDbName.* TO '$AppDbUser'@'127.0.0.1';
FLUSH PRIVILEGES;
"@
& $bins.Mysql "--defaults-file=$mysqlIni" -u root --execute $userSql | Out-Null
if ($LASTEXITCODE -ne 0) { throw "Creating application DB user failed with exit code $LASTEXITCODE" }

Write-Host "Verifying application DB user connection..."
$tempClientIni = Join-Path $root ("app-client-" + [Guid]::NewGuid().ToString("N") + ".ini")
try {
    $clientIniContent = @"
[client]
host=127.0.0.1
port=$port
user=$AppDbUser
password=$AppDbPassword
default-character-set=utf8mb4
"@
    # Use ASCII to avoid BOM (some MySQL builds are sensitive to UTF-8 BOM in option files).
    Set-Content -Path $tempClientIni -Value $clientIniContent -Encoding ASCII
    & $bins.Mysql "--defaults-extra-file=$tempClientIni" --execute "SELECT 1;" | Out-Null
    if ($LASTEXITCODE -ne 0) { throw "Application DB user connection test failed with exit code $LASTEXITCODE" }
} finally {
    Remove-Item $tempClientIni -Force -ErrorAction SilentlyContinue
}

Write-Host "Writing application config: $configPath"
Write-AppConfig -ConfigPath $configPath -Port $port -DbName $AppDbName -DbUser $AppDbUser -DbPassword $AppDbPassword

try {
    $desktopLogs = Join-Path $env:USERPROFILE "Desktop\ArabicPoetryLogs"
    if (Test-Path $desktopLogs) {
        $sanitizedConfig = Join-Path $desktopLogs "installed-config.properties"
        $sanitized = Get-Content -Path $configPath -ErrorAction Stop | ForEach-Object {
            if ($_ -match '^\s*db\.password=') { 'db.password=***' } else { $_ }
        }
        Set-Content -Path $sanitizedConfig -Value $sanitized -Encoding ASCII
        Copy-Item -Path $mysqlIni -Destination (Join-Path $desktopLogs "my.ini") -Force -ErrorAction SilentlyContinue
        Set-Content -Path (Join-Path $desktopLogs "mysql-port.txt") -Value "$port" -Encoding ASCII
    }
} catch {
    Write-Warning "Failed to write config snapshot to Desktop logs: $($_.Exception.Message)"
}

Write-Host "ArabicPoetry DB setup: completed."

# Write a marker so the bootstrapper can detect "provisioning complete" on re-runs.
try {
    $serviceKey = "HKLM:\SYSTEM\CurrentControlSet\Services\$ServiceName"
    if (-not (Test-Path $serviceKey)) { New-Item -Path $serviceKey -Force | Out-Null }
    New-ItemProperty -Path $serviceKey -Name "ArabicPoetryProvisioned" -Value 1 -PropertyType DWord -Force | Out-Null
    Write-Host "Provisioning marker written: $serviceKey\ArabicPoetryProvisioned=1"
} catch {
    Write-Warning "Failed to write provisioning marker: $($_.Exception.Message)"
}
