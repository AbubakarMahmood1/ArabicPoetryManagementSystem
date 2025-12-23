<#
Builds a single-file EXE bootstrapper (WiX Burn) that:
1) installs the jpackage MSI (bundled runtime + app)
2) provisions a local MySQL instance and applies schema-install.sql

Prereqs (build machine):
- WiX Toolset v3.11 (candle.exe/light.exe)
- MSI already built (see scripts/make-msi.cmd)
- MySQL Windows ZIP distribution copied to installer/payloads/mysql.zip
#>

[CmdletBinding()]
param(
    # Optional: path to a MySQL ZIP to copy into installer/payloads/mysql.zip
    [string]$MysqlZipSource
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Definition)
Set-Location $repoRoot

$payloadDir = Join-Path $repoRoot "installer\\payloads"
$outDir = Join-Path $repoRoot "installer\\output"
New-Item -ItemType Directory -Path $payloadDir -Force | Out-Null
New-Item -ItemType Directory -Path $outDir -Force | Out-Null

if ($MysqlZipSource) {
    if (-not (Test-Path $MysqlZipSource)) { throw "MysqlZipSource not found: $MysqlZipSource" }
    Copy-Item $MysqlZipSource (Join-Path $payloadDir "mysql.zip") -Force
}

# Copy latest MSI from dist/ into payloads/ArabicPoetry.msi
$msiFromDist = Get-ChildItem -Path (Join-Path $repoRoot "dist") -Filter *.msi -File -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTime -Descending |
    Select-Object -First 1

if (-not $msiFromDist) {
    throw "No MSI found under dist/. Build it first with scripts/make-msi.cmd (jpackage)."
}
Copy-Item $msiFromDist.FullName (Join-Path $payloadDir "ArabicPoetry.msi") -Force

if (-not (Test-Path (Join-Path $payloadDir "mysql.zip"))) {
    throw "Missing MySQL ZIP payload. Put it at installer/payloads/mysql.zip (see installer/payloads/README.md)."
}

# Ensure WiX tools are available (prefer default install location)
$wixBin = "C:\\Program Files (x86)\\WiX Toolset v3.11\\bin"
if (Test-Path $wixBin) {
    $env:PATH = "$wixBin;$env:PATH"
}
if (-not (Get-Command candle.exe -ErrorAction SilentlyContinue)) { throw "candle.exe not found. Install WiX Toolset v3.11 and/or add it to PATH." }
if (-not (Get-Command light.exe -ErrorAction SilentlyContinue)) { throw "light.exe not found. Install WiX Toolset v3.11 and/or add it to PATH." }

# Build the runner EXE (avoids bundling powershell.exe; uses system PowerShell at install-time)
$bootstrapperDir = Join-Path $repoRoot "installer\\bootstrapper"
$runnerCs = Join-Path $bootstrapperDir "ProvisionMySqlRunner.cs"
$runnerExe = Join-Path $bootstrapperDir "ProvisionMySqlRunner.exe"
if (-not (Test-Path $runnerCs)) { throw "Missing runner source: $runnerCs" }
$cscCandidates = @(
    "C:\\Windows\\Microsoft.NET\\Framework64\\v4.0.30319\\csc.exe",
    "C:\\Windows\\Microsoft.NET\\Framework\\v4.0.30319\\csc.exe"
)
$csc = $cscCandidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $csc) { throw "csc.exe not found. .NET Framework compiler is required to build the bootstrapper runner." }

Push-Location $bootstrapperDir
try {
    & $csc /nologo /target:exe /platform:x64 /optimize+ /out:$runnerExe $runnerCs
    if ($LASTEXITCODE -ne 0) { throw "csc.exe failed with exit code $LASTEXITCODE" }
} finally {
    Pop-Location
}

Push-Location (Join-Path $repoRoot "installer\\bootstrapper")
try {
    $wixobj = Join-Path $outDir "ArabicPoetryBundle.wixobj"
    $setupExe = Join-Path $outDir "ArabicPoetrySetup.exe"
    $setupExeTemp = Join-Path $env:TEMP ("ArabicPoetrySetup-" + [Guid]::NewGuid().ToString("N") + ".exe")

    & candle.exe -nologo -ext WixBalExtension -ext WixUtilExtension -out $wixobj "ArabicPoetryBundle.wxs"
    if ($LASTEXITCODE -ne 0) { throw "candle.exe failed with exit code $LASTEXITCODE" }
    # Build to a temp path first (OneDrive folders can transiently lock large files during layout/move)
    & light.exe -nologo -ext WixBalExtension -ext WixUtilExtension -out $setupExeTemp $wixobj
    if ($LASTEXITCODE -ne 0) { throw "light.exe failed with exit code $LASTEXITCODE" }

    Copy-Item $setupExeTemp $setupExe -Force
    Remove-Item $setupExeTemp -Force -ErrorAction SilentlyContinue

    Write-Host "Bootstrapper built at: $setupExe"
} finally {
    Pop-Location
}
