package com.arabicpoetry.bll.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.linguistics.FrequencyEntry;
import com.arabicpoetry.model.linguistics.IndexEntry;
import com.arabicpoetry.model.linguistics.TokenAnalysis;
import com.arabicpoetry.model.linguistics.VerseAnalysis;
import com.arabicpoetry.util.ArabicTextUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service for calculating frequencies and generating indices at poem and book levels.
 */
public class FrequencyService {
    private static FrequencyService instance;

    private LinguisticAnalysisService linguisticService;
    private PoemService poemService;
    private static final Logger LOGGER = LogManager.getLogger(FrequencyService.class);

    private FrequencyService() throws SQLException {
        this.linguisticService = LinguisticAnalysisService.getInstance();
        VerseService.getInstance();
        this.poemService = PoemService.getInstance();
    }

    public static synchronized FrequencyService getInstance() throws SQLException {
        if (instance == null) {
            instance = new FrequencyService();
        }
        return instance;
    }

    // For tests
    public static synchronized void resetInstance() {
        instance = null;
    }

    // Package-private setters for tests
    void setLinguisticService(LinguisticAnalysisService linguisticService) {
        this.linguisticService = linguisticService;
    }

    void setPoemService(PoemService poemService) {
        this.poemService = poemService;
    }

    /**
     * Get token frequencies for a specific poem.
     */
    public List<FrequencyEntry> getTokenFrequenciesByPoem(int poemId) throws SQLException {
        List<VerseAnalysis> analyses = linguisticService.getVerseAnalysesByPoem(poemId);
        Map<String, Integer> freqMap = new HashMap<>();

        for (VerseAnalysis analysis : analyses) {
            for (TokenAnalysis token : analysis.getTokens()) {
                String normalized = token.getNormalizedToken();
                freqMap.put(normalized, freqMap.getOrDefault(normalized, 0) + 1);
            }
        }

        return toFrequencyList(freqMap);
    }

    /**
     * Get lemma frequencies for a specific poem.
     */
    public List<FrequencyEntry> getLemmaFrequenciesByPoem(int poemId) throws SQLException {
        List<VerseAnalysis> analyses = linguisticService.getVerseAnalysesByPoem(poemId);
        Map<String, Integer> freqMap = new HashMap<>();

        for (VerseAnalysis analysis : analyses) {
            for (TokenAnalysis token : analysis.getTokens()) {
                for (String lemma : token.getLemmas()) {
                    String normalized = ArabicTextUtils.normalizeToken(lemma);
                    freqMap.put(normalized, freqMap.getOrDefault(normalized, 0) + 1);
                }
            }
        }

        return toFrequencyList(freqMap);
    }

    /**
     * Get root frequencies for a specific poem.
     */
    public List<FrequencyEntry> getRootFrequenciesByPoem(int poemId) throws SQLException {
        List<VerseAnalysis> analyses = linguisticService.getVerseAnalysesByPoem(poemId);
        Map<String, Integer> freqMap = new HashMap<>();

        for (VerseAnalysis analysis : analyses) {
            for (TokenAnalysis token : analysis.getTokens()) {
                for (String root : token.getRoots()) {
                    String normalized = ArabicTextUtils.normalizeToken(root);
                    freqMap.put(normalized, freqMap.getOrDefault(normalized, 0) + 1);
                }
            }
        }

        return toFrequencyList(freqMap);
    }

    /**
     * Get token frequencies for a specific book.
     */
    public List<FrequencyEntry> getTokenFrequenciesByBook(int bookId) throws SQLException {
        List<Poem> poems = poemService.getPoemsByBook(bookId);
        Map<String, Integer> freqMap = new HashMap<>();

        for (Poem poem : poems) {
            List<VerseAnalysis> analyses = linguisticService.getVerseAnalysesByPoem(poem.getPoemId());
            for (VerseAnalysis analysis : analyses) {
                for (TokenAnalysis token : analysis.getTokens()) {
                    String normalized = token.getNormalizedToken();
                    freqMap.put(normalized, freqMap.getOrDefault(normalized, 0) + 1);
                }
            }
        }

        return toFrequencyList(freqMap);
    }

    /**
     * Get lemma frequencies for a specific book.
     */
    public List<FrequencyEntry> getLemmaFrequenciesByBook(int bookId) throws SQLException {
        List<Poem> poems = poemService.getPoemsByBook(bookId);
        Map<String, Integer> freqMap = new HashMap<>();

        for (Poem poem : poems) {
            List<VerseAnalysis> analyses = linguisticService.getVerseAnalysesByPoem(poem.getPoemId());
            for (VerseAnalysis analysis : analyses) {
                for (TokenAnalysis token : analysis.getTokens()) {
                    for (String lemma : token.getLemmas()) {
                        String normalized = ArabicTextUtils.normalizeToken(lemma);
                        freqMap.put(normalized, freqMap.getOrDefault(normalized, 0) + 1);
                    }
                }
            }
        }

        return toFrequencyList(freqMap);
    }

    /**
     * Get root frequencies for a specific book.
     */
    public List<FrequencyEntry> getRootFrequenciesByBook(int bookId) throws SQLException {
        List<Poem> poems = poemService.getPoemsByBook(bookId);
        Map<String, Integer> freqMap = new HashMap<>();

        for (Poem poem : poems) {
            List<VerseAnalysis> analyses = linguisticService.getVerseAnalysesByPoem(poem.getPoemId());
            for (VerseAnalysis analysis : analyses) {
                for (TokenAnalysis token : analysis.getTokens()) {
                    for (String root : token.getRoots()) {
                        String normalized = ArabicTextUtils.normalizeToken(root);
                        freqMap.put(normalized, freqMap.getOrDefault(normalized, 0) + 1);
                    }
                }
            }
        }

        return toFrequencyList(freqMap);
    }

    /**
     * Generate token index for a book.
     */
    public Map<String, List<IndexEntry>> generateTokenIndexByBook(int bookId) throws SQLException {
        Map<String, List<IndexEntry>> index = new HashMap<>();
        List<Poem> poems = poemService.getPoemsByBook(bookId);

        for (Poem poem : poems) {
            List<VerseAnalysis> analyses = linguisticService.getVerseAnalysesByPoem(poem.getPoemId());
            for (VerseAnalysis analysis : analyses) {
                for (TokenAnalysis token : analysis.getTokens()) {
                    String normalized = token.getNormalizedToken();
                    index.computeIfAbsent(normalized, k -> new ArrayList<>())
                         .add(new IndexEntry(normalized,
                                           analysis.getVerse().getVerseId(),
                                           analysis.getVerse().getPoemId(),
                                           token.getPosition()));
                }
            }
        }

        return index;
    }

    /**
     * Generate lemma index for a book.
     */
    public Map<String, List<IndexEntry>> generateLemmaIndexByBook(int bookId) throws SQLException {
        Map<String, List<IndexEntry>> index = new HashMap<>();
        List<Poem> poems = poemService.getPoemsByBook(bookId);

        for (Poem poem : poems) {
            List<VerseAnalysis> analyses = linguisticService.getVerseAnalysesByPoem(poem.getPoemId());
            for (VerseAnalysis analysis : analyses) {
                for (TokenAnalysis token : analysis.getTokens()) {
                    for (String lemma : token.getLemmas()) {
                        String normalized = ArabicTextUtils.normalizeToken(lemma);
                        index.computeIfAbsent(normalized, k -> new ArrayList<>())
                             .add(new IndexEntry(normalized,
                                               analysis.getVerse().getVerseId(),
                                               analysis.getVerse().getPoemId(),
                                               token.getPosition()));
                    }
                }
            }
        }

        return index;
    }

    /**
     * Generate root index for a book.
     */
    public Map<String, List<IndexEntry>> generateRootIndexByBook(int bookId) throws SQLException {
        Map<String, List<IndexEntry>> index = new HashMap<>();
        List<Poem> poems = poemService.getPoemsByBook(bookId);

        for (Poem poem : poems) {
            List<VerseAnalysis> analyses = linguisticService.getVerseAnalysesByPoem(poem.getPoemId());
            for (VerseAnalysis analysis : analyses) {
                for (TokenAnalysis token : analysis.getTokens()) {
                    for (String root : token.getRoots()) {
                        String normalized = ArabicTextUtils.normalizeToken(root);
                        index.computeIfAbsent(normalized, k -> new ArrayList<>())
                             .add(new IndexEntry(normalized,
                                               analysis.getVerse().getVerseId(),
                                               analysis.getVerse().getPoemId(),
                                               token.getPosition()));
                    }
                }
            }
        }

        return index;
    }

    private List<FrequencyEntry> toFrequencyList(Map<String, Integer> freqMap) {
        List<FrequencyEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : freqMap.entrySet()) {
            entries.add(new FrequencyEntry(entry.getKey(), entry.getValue()));
        }
        Collections.sort(entries);
        LOGGER.debug("Computed {} frequency entries", entries.size());
        return entries;
    }
}
