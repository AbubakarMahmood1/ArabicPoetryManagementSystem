# Build (or rebuild) the fat JAR in ./dist/ArabicPoetry-all.jar
# Prereqs:
# - JDK on PATH (javac/jar/jpackage)
# - Dependencies present in ./lib (*.jar)
# - Source in ./src
# Optional: JavaFX natives in C:\JavaFX\javafx-sdk-22.0.2\bin (copied to dist/javafx-bin if missing)

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Definition)
Set-Location $repoRoot

$targetRelease = 21

$dist = Join-Path $repoRoot "dist"
$classes = Join-Path $dist "build-classes"
$fat     = Join-Path $dist "fat-build"
$jarOut  = Join-Path $dist "ArabicPoetry-all.jar"

# Prep directories
if (!(Test-Path $dist)) { New-Item -ItemType Directory -Path $dist | Out-Null }
if (Test-Path $classes) { Remove-Item $classes -Recurse -Force }
if (Test-Path $fat)     { Remove-Item $fat -Recurse -Force }
New-Item -ItemType Directory -Path $classes,$fat | Out-Null

# Classpath from lib jars
$cp = (Get-ChildItem (Join-Path $repoRoot "lib" | Join-Path -ChildPath "*.jar") | ForEach-Object { $_.FullName }) -join ';'
if (-not $cp) { throw "No jars found in lib/. Add dependencies first." }

# Compile sources
$javaFiles = Get-ChildItem -Recurse (Join-Path $repoRoot "src") -Filter *.java | ForEach-Object { $_.FullName }
if (-not $javaFiles) { throw "No Java sources found under src/." }
javac --release $targetRelease -d $classes -cp "$cp" $javaFiles

# Copy resources
$srcRoot = (Resolve-Path (Join-Path $repoRoot "src")).Path
Get-ChildItem -Path $srcRoot -Recurse -Include *.fxml,*.xml,*.properties,*.txt | ForEach-Object {
  $dest = $_.FullName.Replace($srcRoot, $classes)
  $destDir = Split-Path $dest -Parent
  if (!(Test-Path $destDir)) { New-Item -ItemType Directory -Path $destDir -Force | Out-Null }
  Copy-Item $_.FullName $dest -Force
}

# Stage compiled + resources
Copy-Item -Path (Join-Path $classes '*') -Destination $fat -Recurse -Force

# Unpack dependencies into staging
Push-Location $fat
Get-ChildItem (Join-Path $repoRoot "lib" | Join-Path -ChildPath "*.jar") | ForEach-Object { jar xf $_.FullName }
jar --create --file $jarOut --main-class com.arabicpoetry.Main .
Pop-Location

# Optional: copy JavaFX natives alongside the jar if present system-wide
$javafxBinSrc = "C:\JavaFX\javafx-sdk-22.0.2\bin"
$javafxBinDst = Join-Path $dist "javafx-bin"
if (!(Test-Path $javafxBinDst) -and (Test-Path $javafxBinSrc)) {
    Copy-Item $javafxBinSrc $javafxBinDst -Recurse -Force
}

Write-Host "Fat JAR built at: $jarOut"
Write-Host "If JavaFX natives are needed, ensure dist/javafx-bin exists (copied from $javafxBinSrc when available)."
