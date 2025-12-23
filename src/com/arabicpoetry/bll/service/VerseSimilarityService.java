package com.arabicpoetry.bll.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.arabicpoetry.model.Verse;
import com.arabicpoetry.model.linguistics.VerseSimilarity;
import com.arabicpoetry.util.NGramUtil;

/**
 * Service for finding similar verses using n-gram based similarity.
 */
public class VerseSimilarityService {
    private static final int DEFAULT_N = 3;
    private static final double DEFAULT_THRESHOLD = 0.3;

    private static VerseSimilarityService instance;

    private final VerseService verseService;
    private final List<Verse> allVerses;
    private final List<Set<String>> precomputedNGrams;
    private final int n;

    private VerseSimilarityService() throws SQLException {
        this(DEFAULT_N);
    }

    private VerseSimilarityService(int n) throws SQLException {
        this.verseService = VerseService.getInstance();
        this.n = n;
        this.allVerses = new ArrayList<>();
        this.precomputedNGrams = new ArrayList<>();
        initialize();
    }

    public static synchronized VerseSimilarityService getInstance() throws SQLException {
        if (instance == null) {
            instance = new VerseSimilarityService();
        }
        return instance;
    }

    private void initialize() throws SQLException {
        List<Verse> verses = verseService.getAllVerses();
        for (Verse verse : verses) {
            if (verse.getText() != null && !verse.getText().trim().isEmpty()) {
                allVerses.add(verse);
                Set<String> nGrams = NGramUtil.toSet(NGramUtil.charNGrams(verse.getText(), n));
                precomputedNGrams.add(nGrams);
            }
        }
    }

    /**
     * Find similar verses to the query verse using default threshold.
     */
    public List<VerseSimilarity> findSimilar(String queryText) {
        return findSimilar(queryText, DEFAULT_THRESHOLD);
    }

    /**
     * Find similar verses to the query verse with configurable threshold.
     * @param queryText The input verse text
     * @param threshold Similarity threshold (0.0 to 1.0)
     * @return List of similar verses sorted by similarity score (highest first)
     */
    public List<VerseSimilarity> findSimilar(String queryText, double threshold) {
        if (queryText == null || queryText.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> queryNGrams = NGramUtil.toSet(NGramUtil.charNGrams(queryText, n));
        List<VerseSimilarity> results = new ArrayList<>();

        for (int i = 0; i < allVerses.size(); i++) {
            double score = NGramUtil.jaccardSimilarity(queryNGrams, precomputedNGrams.get(i));
            if (score >= threshold) {
                results.add(new VerseSimilarity(allVerses.get(i), score));
            }
        }

        Collections.sort(results);
        return results;
    }

    /**
     * Refresh the service after database changes.
     */
    public void refresh() throws SQLException {
        allVerses.clear();
        precomputedNGrams.clear();
        initialize();
    }

    public int getNGramSize() {
        return n;
    }
}
