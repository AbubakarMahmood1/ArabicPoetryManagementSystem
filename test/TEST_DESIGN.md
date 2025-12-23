# Test Design (CFG / EPC / TR)

Scope: representative non-trivial methods in DAL/BLL targeted for early tests.

## AuthenticationService.login
- CFG branches: user null; user inactive; password mismatch; success path -> updateLastLogin.
- TR (EPC): exercise each decision outcome; ensure updateLastLogin only on success.
- Paths/inputs:
  1) user not found -> expect null, no DAO calls beyond findByUsername.
  2) inactive user -> expect null, no updateLastLogin.
  3) password mismatch -> expect null, no updateLastLogin.
  4) password match + active -> expect user returned, updateLastLogin invoked.

## BookService.createBook/updateBook
- CFG: validate title non-empty; then delegate to DAO.
- TR: (a) empty/blank title -> IllegalArgumentException; (b) valid title -> DAO called.
- Inputs: title blank vs. "Sample".

## FrequencyService.*FrequenciesByBook / generate*IndexByBook
- CFG: loop over poems -> loop over verses -> loop over tokens -> loop over lemmas/roots; normalization branch (empty token skips).
- TR: ensure at least one verse with tokens; ensure repeated tokens accumulate counts; ensure positions recorded in indices; ignore empty/normalized-empty tokens.
- Paths: (a) non-empty tokens accumulate counts; (b) empty/normalized-empty tokens ignored.

## LinguisticAnalysisService.refresh/ensureAnalyzed
- CFG: initialized flag guard; rebuildIndices to populate caches; retry after refresh sets initialized=false then rebuilds.
- TR: call ensureAnalyzed twice (second should not rebuild); call refresh then ensureAnalyzed (should rebuild).

## ImportService.importFromFile
- CFG: loop over lines; branches for footnote delimiter/page delimiter/book title/poem title/verse parts; creation of book/poem/verse.
- TR: inputs covering (a) book line; (b) poem line; (c) verse lines; (d) footnote section skipped; (e) page delimiter resets footnote flag.
- Paths: happy path through all creations; skip paths through footnotes/page delimiters.

## ImportController.handleImport/runImport (JavaFX)
- CFG: empty path -> info dialog; non-file path -> error dialog; else async task -> success vs failure.
- TR: (a) blank input shows info and leaves buttons enabled; (b) missing/invalid file path shows error without starting task; (c) happy path drives status to "Import completed", re-enables buttons, and populates output area; (d) failure path logs error, shows alert, status shows "Import failed", and buttons are re-enabled.

## DAO CRUD paths
- BookDAOImpl: insert/find/delete (done).
- PoetDAOImpl: insert/findById/findByName/delete.
- PoemDAOImpl: insert with FK book/poet, find, delete.
- VerseDAOImpl: insert with FK poem, find, delete.
- UserDAOImpl: insert, findByUsername, delete.
- TR: each DAO exercises happy path insert+fetch+delete; negative fetch after delete -> null.

Planned test coverage mapping
- Unit (Mockito): AuthenticationService (done), BookService (done), FrequencyService (added), LinguisticAnalysisService (added), ImportService (added).
- Integration (test DB): BookDAOImpl (done), Poem/Poet/Verse/User DAOs (added).
