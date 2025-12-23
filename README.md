# Arabic Poetry Management System

JavaFX desktop app for managing classical Arabic poetry collections with built-in linguistic analysis, indexing, and import tools.

![Java](https://img.shields.io/badge/JDK-22+-orange)
![JavaFX](https://img.shields.io/badge/UI-JavaFX_22-blue)
![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-blue)
![Status](https://img.shields.io/badge/Status-JavaFX_port_complete-success)

## Table of Contents
- [Current Status](#current-status)
- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Running the App](#running-the-app)
- [Building the Installer (Windows)](#building-the-installer-windows)
- [Usage Guide](#usage-guide)
- [Testing](#testing)
- [Architecture & Patterns](#architecture--patterns)
- [Troubleshooting](#troubleshooting)
- [License & Notes](#license--notes)

## Current Status
- JavaFX is the primary UI; legacy Swing frames have been retired.
- All CRUD screens, importer, and analysis tools (linguistic workbench, frequency/index generation, verse similarity) are implemented.
- Manual classpath build is retained (`lib/` jars) with scripts for fat-jar and app-image packaging.
- Integration, unit, and UI tests cover services, DAOs, and controllers against a MySQL test schema.

## Features
- **Library Management:** CRUD and search for Books, Poets, Poems, and Verses with UTF-8/RTL support.
- **Secure Login:** SHA-256 password hashing; default admin user created by the schema.
- **Importer:** Parse formatted text files (see `Poem.txt`) into books/poems/verses with progress and logging.
- **Linguistic Workbench:** Tokenize, lemmatize, root/segment extraction (AlKhalilMorphoSys2), literal and regex verse search, cached index with on-demand refresh.
- **Analysis Tools:** Verse similarity (character n-grams), token/lemma/root frequency analysis, and per-book token/lemma/root index generation with occurrence drill-down.
- **Search Everywhere:** Filtered tables on every CRUD screen; literal/regex search inside the workbench.

## Project Structure
```
ArabicPoetryManagementSystem/
|-- src/com/arabicpoetry/
|   |-- Main.java                      # Launches JavaFX app
|   |-- model/                         # Entities (+ linguistics models)
|   |-- dal/dao/ (+impl/)              # JDBC DAOs and factory
|   |-- bll/service/                   # Auth, CRUD, importer, linguistics, similarity, frequency/index
|   |-- presentation/fx/               # JavaFX controllers + FXML views
|   |   |-- book | poet | poem | verse | importer
|   |   `-- analysis/                  # Linguistic workbench, frequency, index, similarity
|   `-- util/                          # DB config/connection, text utils, n-grams, analyzer wrapper
|-- test/                              # JUnit/TestFX tests + test schema config
|-- database/schema.sql                # Creates prod + test DBs and default admin user
|-- database/schema-install.sql        # Installer-safe prod schema (no DROP)
|-- lib/                               # JavaFX 22, Log4j2, JUnit 6 (M1), TestFX, Mockito, MySQL driver, AlKhalilMorphoSys2
|-- scripts/                           # build-fat-jar.ps1, build-app-image.ps1, make-msi.cmd
|-- installer/                         # WiX bootstrapper + MySQL provisioning script
|-- dist/                              # Build outputs (fat jar, resources, run-fatjar.ps1)
`-- config.properties                  # Auto-created DB config (override with -Ddb.config.file)
```

## Prerequisites
- **End users (Windows installer):** No Java/MySQL preinstall required; the bootstrapper bundles a runtime and provisions a local MySQL instance.
- **Developers (build from source):** JDK 22 (tested) or 21+.
- **Packaging (Windows):** WiX Toolset v3.11 + JavaFX SDK at `C:\JavaFX\javafx-sdk-22.0.2\lib` (build-time only for `jpackage`).
- **AlKhalil Morphological Analyzer:** Bundled in `lib/AlKhalilMorphoSys2.jar` and required for the linguistic workbench.
- **IDE:** Eclipse/IntelliJ with manual module/classpath (no Maven/Gradle).

## Installation & Setup
### Windows installer (recommended for evaluators)
1. Build `installer/output/ArabicPoetrySetup.exe` (see “Building the Installer” below) and run it on the target machine.
2. The installer:
   - Installs the app (bundled JRE, JavaFX natives, all jars).
   - Provisions a local MySQL Windows service `ArabicPoetryMySQL` bound to `127.0.0.1` (port `3307` or next free port).
   - Prompts for a password for the local DB user (blank = auto-generate), applies `database/schema-install.sql`, and writes the installed `config.properties`.
3. Login with default credentials: `admin` / `admin123`.

### Developer setup (system MySQL)
1. **Create Databases**
   ```bash
   mysql -u root -p < database/schema.sql
   ```
   - Creates `arabic_poetry_db` and `arabic_poetry_db_test`.
   - Default admin credentials: `admin` / `admin123`.

2. **Configure DB Connection**
   - For local development, create a private config file (don’t commit credentials) and run with `-Ddb.config.file=...` or env `DB_CONFIG_FILE=...`.
     ```properties
     db.url=jdbc:mysql://localhost:3306/arabic_poetry_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8&allowPublicKeyRetrieval=true
     db.username=root
     db.password=YOUR_PASSWORD
     db.driver=com.mysql.cj.jdbc.Driver
     ```
   - Tests use `config-test.properties` (points to `arabic_poetry_db_test`); provide credentials via `DB_CONFIG_FILE` or update locally.

3. **Import Project in Eclipse**
   - File -> Open Projects from File System -> choose repo root.
   - Ensure `lib/*.jar` stay on the module path; JRE set to JavaSE-22.
   - VM args for JavaFX: `--module-path "./lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web,javafx.swing`.

## Running the App
- **From IDE:** Run `com.arabicpoetry.Main` with the JavaFX VM args above.
- **From compiled classes (no fat jar):**
  ```bash
  java --module-path "./lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.web,javafx.swing ^
       -cp "bin;lib/*" com.arabicpoetry.Main
  ```
- **Build fat jar:** `pwsh scripts/build-fat-jar.ps1`
  - Output: `dist/ArabicPoetry-all.jar` (+ optional `dist/javafx-bin` if natives found).
- **Run fat jar (Windows helper):**
  ```bash
  pwsh dist/run-fatjar.ps1 [-SoftwareRendering] [-DbConfigPath path\to\config.properties]
  ```
- **Portable app image:** `pwsh scripts/build-app-image.ps1`
  - Requires `jpackage` and JavaFX SDK at `C:\JavaFX\javafx-sdk-22.0.2\lib`.
  - Produces `ArabicPoetry/ArabicPoetry.exe`; use `scripts/make-msi.cmd` if you need an MSI.
- **Bootstrapper installer (EXE):** `pwsh scripts/build-bootstrapper.ps1`
  - Produces `installer/output/ArabicPoetrySetup.exe` (bundles MSI + MySQL ZIP + DB provisioning).

## Building the Installer (Windows)
1. Build the MSI: run `scripts/make-msi.cmd` (uses `jpackage` + WiX).
2. Put a MySQL Windows “noinstall” ZIP at `installer/payloads/mysql.zip` (see `installer/payloads/README.md`).
3. Build the bootstrapper: `pwsh scripts/build-bootstrapper.ps1`.

## Usage Guide
- **Login:** Default `admin` / `admin123`.
- **Books/Poets/Poems/Verses:** Manage via JavaFX tables with inline search and validation; verses enforce unique (poem, verse_number).
- **Import Poems:** File -> Import Poems from File -> pick `Poem.txt` (UTF-8). Format:
  - Book line starts with a book marker (see sample file).
  - Poem title is bracketed; verses are `(hemistich1) (hemistich2)`.
  - Lines after `_________` are skipped; `==========` resets footnotes between pages.
- **Linguistic Workbench:** Browse tokens/lemmas/roots/segments; search by token/lemma/root/segment, literal text, or regex; click **Refresh Index** after imports/edits.
- **Frequency Analysis:** Token/lemma/root frequencies by poem or by book.
- **Book Index:** Generate per-book token/lemma/root indexes and inspect verse/position occurrences.
- **Verse Similarity:** Paste text to find similar verses via n-gram Jaccard score (default n=3, threshold 0.3).

## Testing
- **Stack:** JUnit Jupiter 6.1.0-M1, Mockito 5.20, TestFX 4.0.18, AssertJ.
- **DB:** Ensure `arabic_poetry_db_test` exists; run tests with `-Ddb.config.file=config-test.properties` or env `DB_CONFIG_FILE=config-test.properties`.
- **Headless UI tests:** Add `-Dtestfx.headless=true -Dprism.order=sw -Dprism.text=t2k -Dglass.platform=Monocle`.
- **Running:** Use your IDE's JUnit 5 runner with classpath `bin-test;bin;lib/*`. `test/log4j2-test.xml` configures test logging.
- **Coverage:** Service and DAO unit/integration tests, importer, linguistic analysis, frequency/index generation, and JavaFX controller smoke tests.

## Architecture & Patterns
- **3-layered:** JavaFX presentation -> BLL services -> JDBC DAOs.
- **Patterns:** Singleton (config, connection, services), Abstract Factory (DAOFactory), dependency injection via DAO interfaces, repository-style DAOs.
- **Linguistics:** AlKhalilMorphoSys2-driven token/lemma/root/segment extraction cached in memory with a refresh hook.
- **Similarity/Indexing:** Character n-gram similarity, frequency aggregation, and per-book term indexes for concordance views.

## Troubleshooting
- **DB connection:** For the bootstrapper install, ensure service `ArabicPoetryMySQL` is running; otherwise re-run the installer as admin.
- **Missing JavaFX natives:** For dev runs, populate `dist/javafx-bin` from your JavaFX SDK `bin` folder if needed.
- **Morph analyzer missing:** Ensure `lib/AlKhalilMorphoSys2.jar` stays on the classpath.
- **Regex errors in workbench:** Invalid patterns return a user-facing error; fix the regex and retry.
- **Import issues:** Confirm file is UTF-8 and follows the `Poem.txt` markers; check logs for line numbers.

## License & Notes
- Educational/hobby project; reuse freely for learning and personal experiments.
- **Version:** 1.1 (JavaFX + analysis suite)
- **Last Updated:** December 2025
