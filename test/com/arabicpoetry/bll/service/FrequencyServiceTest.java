package com.arabicpoetry.bll.service;

import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Verse;
import com.arabicpoetry.model.linguistics.FrequencyEntry;
import com.arabicpoetry.model.linguistics.IndexEntry;
import com.arabicpoetry.model.linguistics.TokenAnalysis;
import com.arabicpoetry.model.linguistics.VerseAnalysis;
import com.arabicpoetry.testing.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FrequencyServiceTest {

    private FrequencyService service;
    private LinguisticAnalysisService linguisticService;
    private PoemService poemService;

    @BeforeEach
    void setUp() throws SQLException {
        TestSupport.resetSingletons();
        service = FrequencyService.getInstance();
        linguisticService = Mockito.mock(LinguisticAnalysisService.class);
        poemService = Mockito.mock(PoemService.class);
        service.setLinguisticService(linguisticService);
        service.setPoemService(poemService);
    }

    @Test
    void tokenFrequenciesByBookAggregatesTokens() throws Exception {
        Poem poem = new Poem();
        poem.setPoemId(1);
        when(poemService.getPoemsByBook(10)).thenReturn(List.of(poem));

        Verse verse = new Verse();
        verse.setVerseId(5);
        verse.setPoemId(1);
        VerseAnalysis va = new VerseAnalysis(verse, tokenList("kitab", "kitab", "bait"));
        when(linguisticService.getVerseAnalysesByPoem(1)).thenReturn(List.of(va));

        List<FrequencyEntry> entries = service.getTokenFrequenciesByBook(10);

        assertEquals(2, entries.size());
        // Frequencies are sorted by descending count, then term.
        assertEquals("kitab", entries.get(0).getTerm());
        assertEquals(2, entries.get(0).getCount());
        assertEquals("bait", entries.get(1).getTerm());
        assertEquals(1, entries.get(1).getCount());
    }

    @Test
    void generateTokenIndexIncludesPositions() throws Exception {
        Poem poem = new Poem();
        poem.setPoemId(2);
        when(poemService.getPoemsByBook(11)).thenReturn(List.of(poem));

        Verse verse = new Verse();
        verse.setVerseId(6);
        verse.setPoemId(2);
        VerseAnalysis va = new VerseAnalysis(verse, tokenListAtPositions(
                new String[]{"ana", "anta", "ana"}, new int[]{1, 2, 3}));
        when(linguisticService.getVerseAnalysesByPoem(2)).thenReturn(List.of(va));

        Map<String, List<IndexEntry>> index = service.generateTokenIndexByBook(11);

        assertTrue(index.containsKey("ana"));
        List<IndexEntry> anaEntries = index.get("ana");
        assertEquals(2, anaEntries.size());
        assertEquals(1, anaEntries.get(0).getPosition());
        assertEquals(3, anaEntries.get(1).getPosition());
    }

    private List<TokenAnalysis> tokenList(String... tokens) {
        List<TokenAnalysis> list = new ArrayList<>();
        int pos = 0;
        for (String t : tokens) {
            pos++;
            list.add(new TokenAnalysis(new Verse(), t, t, pos, List.of(), List.of(), List.of(), null, null, null, null));
        }
        return list;
    }

    private List<TokenAnalysis> tokenListAtPositions(String[] tokens, int[] positions) {
        List<TokenAnalysis> list = new ArrayList<>();
        for (int i = 0; i < tokens.length; i++) {
            list.add(new TokenAnalysis(new Verse(), tokens[i], tokens[i], positions[i], List.of(), List.of(), List.of(), null, null, null, null));
        }
        return list;
    }
}
