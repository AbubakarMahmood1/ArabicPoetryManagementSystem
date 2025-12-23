# Installer Payloads (not committed)

Place the following files in this folder before building the bootstrapper:

- `ArabicPoetry.msi`  
  Build it via `scripts/make-msi.cmd`, then copy/rename the produced MSI into this folder.

- `mysql.zip`  
  Download the **MySQL Community Server (Windows x64) ZIP** distribution (the "noinstall" ZIP), then copy/rename it to `mysql.zip`.

Then run `pwsh scripts/build-bootstrapper.ps1`.

