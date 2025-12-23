# Ensures a Windows .ico exists for packaging.
# If `assets/logo.ico` exists, it will be copied to `dist/logo/logo.ico`.
# Otherwise, generates a simple fallback icon so MSI builds still have a custom icon.

[CmdletBinding()]
param(
  [string]$RepoRoot
)

$ErrorActionPreference = "Stop"

if (-not $RepoRoot) {
  $RepoRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Definition)
}

$assetsIcon = Join-Path $RepoRoot "assets\\logo.ico"
$outDir = Join-Path $RepoRoot "dist\\logo"
$outIcon = Join-Path $outDir "logo.ico"

New-Item -ItemType Directory -Path $outDir -Force | Out-Null

if (Test-Path $assetsIcon) {
  Copy-Item $assetsIcon $outIcon -Force
  Write-Host "Using icon from: $assetsIcon"
  Write-Host "Icon copied to:  $outIcon"
  exit 0
}

Add-Type -AssemblyName System.Drawing | Out-Null

Add-Type @"
using System;
using System.Runtime.InteropServices;
public static class NativeIcon {
  [DllImport("user32.dll", SetLastError=true)]
  public static extern bool DestroyIcon(IntPtr hIcon);
}
"@ | Out-Null

$size = 256
$bmp = New-Object System.Drawing.Bitmap $size, $size, ([System.Drawing.Imaging.PixelFormat]::Format32bppArgb)
$g = [System.Drawing.Graphics]::FromImage($bmp)
try {
  $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
  $g.Clear([System.Drawing.Color]::FromArgb(255, 22, 24, 28))

  $brush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 46, 144, 255))
  $g.FillEllipse($brush, 20, 20, $size - 40, $size - 40)
  $brush.Dispose()

  $font = New-Object System.Drawing.Font "Segoe UI", 92, ([System.Drawing.FontStyle]::Bold), ([System.Drawing.GraphicsUnit]::Pixel)
  $textBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::White)
  $format = New-Object System.Drawing.StringFormat
  $format.Alignment = [System.Drawing.StringAlignment]::Center
  $format.LineAlignment = [System.Drawing.StringAlignment]::Center
  $g.DrawString("AP", $font, $textBrush, (New-Object System.Drawing.RectangleF 0, 0, $size, $size), $format)
  $format.Dispose()
  $textBrush.Dispose()
  $font.Dispose()

  $hIcon = $bmp.GetHicon()
  try {
    $icon = [System.Drawing.Icon]::FromHandle($hIcon)
    $fs = [System.IO.File]::Open($outIcon, [System.IO.FileMode]::Create, [System.IO.FileAccess]::Write)
    try {
      $icon.Save($fs)
    } finally {
      $fs.Dispose()
    }
  } finally {
    [NativeIcon]::DestroyIcon($hIcon) | Out-Null
  }
} finally {
  $g.Dispose()
  $bmp.Dispose()
}

Write-Host "Generated fallback icon at: $outIcon"

