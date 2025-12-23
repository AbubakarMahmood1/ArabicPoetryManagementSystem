@echo off
REM Build MSI installer using local project paths
setlocal
set "REPO=%~dp0.."
cd /d "%REPO%"

set "PATH=C:\Program Files (x86)\WiX Toolset v3.11\bin;%PATH%"
set "JP_TEMP=%TEMP%\jptmp"
if exist "%JP_TEMP%" rmdir /s /q "%JP_TEMP%"
mkdir "%JP_TEMP%"

if exist ArabicPoetry rmdir /s /q ArabicPoetry
if not exist "%REPO%\jpackage-input" mkdir "%REPO%\jpackage-input"

REM Ensure an icon exists (assets/logo.ico preferred; fallback generated)
powershell -NoProfile -ExecutionPolicy Bypass -File "%REPO%\scripts\ensure-logo.ps1" -RepoRoot "%REPO%" >nul

REM Stage required inputs for jpackage (no secrets)
if exist "%REPO%\dist\ArabicPoetry-all.jar" copy /y "%REPO%\dist\ArabicPoetry-all.jar" "%REPO%\jpackage-input\ArabicPoetry-all.jar" >nul
if exist "%REPO%\config.properties" copy /y "%REPO%\config.properties" "%REPO%\jpackage-input\config.properties" >nul
if exist "%REPO%\Poem.txt" copy /y "%REPO%\Poem.txt" "%REPO%\jpackage-input\Poem.txt" >nul
if exist "%REPO%\dist\javafx-bin" xcopy /e /i /y "%REPO%\dist\javafx-bin" "%REPO%\jpackage-input\javafx-bin" >nul

set "ICON_ARG="
if exist "%REPO%\dist\logo\logo.ico" set ICON_ARG=--icon "%REPO%\dist\logo\logo.ico"

jpackage --name ArabicPoetry --input "%REPO%\jpackage-input" --main-jar ArabicPoetry-all.jar --main-class com.arabicpoetry.Main --type msi %ICON_ARG% --win-menu --win-menu-group "ArabicPoetry" --win-shortcut --module-path "C:\JavaFX\javafx-sdk-22.0.2\lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base,javafx.web,javafx.swing,java.sql,java.naming,java.management,jdk.management --java-options "-Dprism.order=sw -Dprism.text=t2k -Djava.library.path=$APPDIR\javafx-bin -Ddb.config.file=$APPDIR\config.properties" --temp "%JP_TEMP%" --dest "%REPO%\dist" --verbose

endlocal
exit /b %ERRORLEVEL%
