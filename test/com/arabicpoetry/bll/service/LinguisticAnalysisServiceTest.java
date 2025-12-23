package com.arabicpoetry.bll.service;

import com.arabicpoetry.util.WordAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Collections;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LinguisticAnalysisServiceTest {

    private LinguisticAnalysisService service;
    private VerseService verseService;
    private WordAnalyzer wordAnalyzer;

    @BeforeEach
    void setUp() throws SQLException {
        com.arabicpoetry.testing.TestSupport.resetSingletons();
        service = LinguisticAnalysisService.getInstance();
        verseService = Mockito.mock(VerseService.class);
        wordAnalyzer = Mockito.mock(WordAnalyzer.class);
        service.setVerseService(verseService);
        service.setWordAnalyzer(wordAnalyzer);
    }

    @Test
    void ensureAnalyzedRunsOnceUntilRefresh() throws Exception {
        when(verseService.getAllVerses()).thenReturn(Collections.emptyList());

        service.getAllTokens();
        service.getAllTokens(); // second call should not trigger rebuild

        verify(verseService, times(1)).getAllVerses();

        service.refresh();
        service.getAllTokens(); // after refresh, should rebuild
        verify(verseService, times(2)).getAllVerses();
    }
}
