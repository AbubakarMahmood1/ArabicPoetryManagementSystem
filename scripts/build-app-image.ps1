# Rebuild the portable app image (ArabicPoetry folder with self-contained runtime/EXE).
# Requirements:
# - JDK with jpackage on PATH
# - JavaFX SDK at C:\JavaFX\javafx-sdk-22.0.2\lib (adjust $javafxLib if different)
# - dist/ArabicPoetry-all.jar already built, plus dist/config.properties, dist/Poem.txt, dist/javafx-bin/

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Definition)
Set-Location $repoRoot

$javafxLib = "C:\JavaFX\javafx-sdk-22.0.2\lib" # adjust if needed
$stage = Join-Path $repoRoot "jpackage-input"
$appImage = Join-Path $repoRoot "ArabicPoetry"

& (Join-Path $repoRoot "scripts\\ensure-logo.ps1") -RepoRoot $repoRoot | Out-Null

function Copy-IfExists($src, $dstDir) {
    if (Test-Path $src) { Copy-Item $src -Destination $dstDir -Recurse -Force }
    else { throw "Missing required file/folder: $src" }
}

# Prepare staging input for jpackage
if (Test-Path $stage) { Remove-Item $stage -Recurse -Force }
New-Item -ItemType Directory -Path $stage | Out-Null
Copy-IfExists (Join-Path $repoRoot "dist/ArabicPoetry-all.jar") $stage
Copy-IfExists (Join-Path $repoRoot "dist/config.properties") $stage
Copy-IfExists (Join-Path $repoRoot "dist/Poem.txt") $stage
Copy-IfExists (Join-Path $repoRoot "dist/javafx-bin") $stage

# Clean previous app image
if (Test-Path $appImage) { Remove-Item $appImage -Recurse -Force }

$javaOpts = @(
  "-Dprism.order=sw",
  "-Dprism.text=t2k",
  "-Djava.library.path=$APPDIR\javafx-bin",
  "-Ddb.config.file=$APPDIR\config.properties"
)

$args = @(
  "--name","ArabicPoetry",
  "--input",$stage,
  "--main-jar","ArabicPoetry-all.jar",
  "--main-class","com.arabicpoetry.Main",
  "--type","app-image",
  "--module-path",$javafxLib,
  "--add-modules","javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.web,javafx.swing,java.sql,java.naming,java.management,jdk.management,java.desktop",
  "--verbose"
)
$iconPath = Join-Path $repoRoot "dist\\logo\\logo.ico"
if (Test-Path $iconPath) {
  $args += @("--icon", $iconPath)
}
$javaOpts | ForEach-Object { $args += @("--java-options", $_) }

Write-Host "Building app image to $appImage ..."
& jpackage @args

# Overwrite launcher cfg to ensure required JVM options are present
$cfgPath = Join-Path $appImage "app/ArabicPoetry.cfg"
$cfgContent = @'
[Application]
app.classpath=$APPDIR\ArabicPoetry-all.jar
app.mainclass=com.arabicpoetry.Main

[JavaOptions]
java-options=-Djpackage.app-version=1.0
java-options=-Dprism.order=sw
java-options=-Dprism.text=t2k
java-options=-Djava.library.path=$APPDIR\javafx-bin
java-options=-Ddb.config.file=$APPDIR\config.properties
'@
Set-Content -Path $cfgPath -Value $cfgContent -Encoding ASCII

Write-Host "Done. Portable app image at: $repoRoot\$appImage (run ArabicPoetry.exe inside that folder)"
