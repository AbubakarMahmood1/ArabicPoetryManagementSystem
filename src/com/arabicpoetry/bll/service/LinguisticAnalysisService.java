package com.arabicpoetry.bll.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.arabicpoetry.model.Verse;
import com.arabicpoetry.model.linguistics.LinguisticMatch;
import com.arabicpoetry.model.linguistics.LinguisticSearchMode;
import com.arabicpoetry.model.linguistics.TokenAnalysis;
import com.arabicpoetry.model.linguistics.VerseAnalysis;
import com.arabicpoetry.util.ArabicTextUtils;
import com.arabicpoetry.util.WordAnalyzer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.oujda_nlp_team.entity.Result;
import net.oujda_nlp_team.entity.ResultList;

/**
 * Provides tokenization, lemmatization, root extraction, segmentation, and the
 * supporting query APIs for iteration 2 of the project.
 */
public class LinguisticAnalysisService {
    private static final Pattern ARABIC_TOKEN_PATTERN = Pattern.compile("[\\p{IsArabic}]+");
    private static final int SNIPPET_PADDING = 12;
    private static final Logger LOGGER = LogManager.getLogger(LinguisticAnalysisService.class);

    private static LinguisticAnalysisService instance;

    private VerseService verseService;
    private WordAnalyzer wordAnalyzer;

    private final Map<String, List<TokenAnalysis>> tokensByForm = new HashMap<>();
    private final Map<String, List<TokenAnalysis>> tokensByLemma = new HashMap<>();
    private final Map<String, List<TokenAnalysis>> tokensByRoot = new HashMap<>();
    private final Map<String, List<TokenAnalysis>> tokensBySegment = new HashMap<>();
    private final List<VerseAnalysis> verseAnalyses = new ArrayList<>();

    private boolean initialized;

    private LinguisticAnalysisService() throws SQLException {
        this.verseService = VerseService.getInstance();
        this.wordAnalyzer = WordAnalyzer.getInstance();
    }

    public static synchronized LinguisticAnalysisService getInstance() throws SQLException {
        if (instance == null) {
            instance = new LinguisticAnalysisService();
        }
        return instance;
    }

    // For tests
    public static synchronized void resetInstance() {
        instance = null;
    }

    // Package-private setters for tests
    void setVerseService(VerseService verseService) {
        this.verseService = verseService;
    }

    void setWordAnalyzer(WordAnalyzer wordAnalyzer) {
        this.wordAnalyzer = wordAnalyzer;
    }

    public List<String> getAllTokens() throws SQLException {
        ensureAnalyzed();
        return sortedKeys(tokensByForm);
    }

    public List<String> getAllLemmas() throws SQLException {
        ensureAnalyzed();
        return sortedKeys(tokensByLemma);
    }

    public List<String> getAllRoots() throws SQLException {
        ensureAnalyzed();
        return sortedKeys(tokensByRoot);
    }

    public List<String> getAllSegments() throws SQLException {
        ensureAnalyzed();
        return sortedKeys(tokensBySegment);
    }

    public List<LinguisticMatch> search(String query, LinguisticSearchMode mode) throws SQLException {
        ensureAnalyzed();
        if (mode == null) {
            return Collections.emptyList();
        }
        switch (mode) {
            case TOKEN:
                return wrapTokenMatches(lookup(tokensByForm, ArabicTextUtils.normalizeToken(query)), mode);
            case LEMMA:
                return wrapTokenMatches(lookup(tokensByLemma, ArabicTextUtils.normalizeToken(query)), mode);
            case ROOT:
                return wrapTokenMatches(lookup(tokensByRoot, ArabicTextUtils.normalizeToken(query)), mode);
            case SEGMENT:
                return wrapTokenMatches(lookup(tokensBySegment, ArabicTextUtils.normalizeToken(query)), mode);
            case STRING:
                return literalMatches(query);
            case REGEX:
                return regexMatches(query);
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Re-run the full corpus analysis. Useful after importing new poems or
     * editing verses without restarting the application.
     */
    public void refresh() throws SQLException {
        synchronized (this) {
            initialized = false;
        }
        LOGGER.info("Refreshing linguistic analyses cache");
        ensureAnalyzed();
    }

    /**
     * Get analysis for a specific verse.
     */
    public VerseAnalysis getVerseAnalysis(int verseId) throws SQLException {
        ensureAnalyzed();
        for (VerseAnalysis analysis : verseAnalyses) {
            if (analysis.getVerse().getVerseId() == verseId) {
                return analysis;
            }
        }
        return null;
    }

    /**
     * Get all verse analyses for a specific poem.
     */
    public List<VerseAnalysis> getVerseAnalysesByPoem(int poemId) throws SQLException {
        ensureAnalyzed();
        List<VerseAnalysis> result = new ArrayList<>();
        for (VerseAnalysis analysis : verseAnalyses) {
            if (analysis.getVerse().getPoemId() == poemId) {
                result.add(analysis);
            }
        }
        return result;
    }

    /**
     * Get all verse analyses.
     */
    public List<VerseAnalysis> getAllVerseAnalyses() throws SQLException {
        ensureAnalyzed();
        return new ArrayList<>(verseAnalyses);
    }

    private List<LinguisticMatch> wrapTokenMatches(List<TokenAnalysis> analyses, LinguisticSearchMode mode) {
        if (analyses.isEmpty()) {
            return Collections.emptyList();
        }
        List<LinguisticMatch> matches = new ArrayList<>();
        for (TokenAnalysis analysis : analyses) {
            matches.add(new LinguisticMatch(analysis.getVerse(), analysis, mode, analysis.getToken()));
        }
        return matches;
    }

    private List<LinguisticMatch> literalMatches(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String trimmed = text.trim();
        List<LinguisticMatch> matches = new ArrayList<>();
        for (VerseAnalysis analysis : verseAnalyses) {
            Verse verse = analysis.getVerse();
            if (verse.getText() == null) {
                continue;
            }
            int idx = verse.getText().indexOf(trimmed);
            if (idx >= 0) {
                matches.add(new LinguisticMatch(
                        verse,
                        null,
                        LinguisticSearchMode.STRING,
                        buildSnippet(verse.getText(), idx, trimmed.length())));
            }
        }
        return matches;
    }

    private List<LinguisticMatch> regexMatches(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            return Collections.emptyList();
        }
        Pattern pattern;
        try {
            pattern = Pattern.compile(regex);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid regular expression: " + ex.getMessage(), ex);
        }

        List<LinguisticMatch> matches = new ArrayList<>();
        for (VerseAnalysis analysis : verseAnalyses) {
            Verse verse = analysis.getVerse();
            if (verse.getText() == null) {
                continue;
            }
            Matcher matcher = pattern.matcher(verse.getText());
            while (matcher.find()) {
                matches.add(new LinguisticMatch(
                        verse,
                        null,
                        LinguisticSearchMode.REGEX,
                        buildSnippet(verse.getText(), matcher.start(), matcher.end() - matcher.start())));
            }
        }
        return matches;
    }

    private String buildSnippet(String verseText, int start, int length) {
        int left = Math.max(0, start - SNIPPET_PADDING);
        int right = Math.min(verseText.length(), start + length + SNIPPET_PADDING);
        String prefix = left > 0 ? "\u2026" : "";
        String suffix = right < verseText.length() ? "\u2026" : "";
        return prefix + verseText.substring(left, right) + suffix;
    }

    private List<TokenAnalysis> lookup(Map<String, List<TokenAnalysis>> index, String key) {
        if (key == null || key.isEmpty()) {
            return Collections.emptyList();
        }
        return index.getOrDefault(key, Collections.emptyList());
    }

    private List<String> sortedKeys(Map<String, ?> source) {
        List<String> keys = new ArrayList<>(source.keySet());
        keys.sort((a, b) -> a.compareToIgnoreCase(b));
        return keys;
    }

    private void ensureAnalyzed() throws SQLException {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (initialized) {
                return;
            }
            rebuildIndices();
            initialized = true;
            LOGGER.info("Initialized linguistic analyses for {} verses", verseAnalyses.size());
        }
    }

    private void rebuildIndices() throws SQLException {
        tokensByForm.clear();
        tokensByLemma.clear();
        tokensByRoot.clear();
        tokensBySegment.clear();
        verseAnalyses.clear();

        List<Verse> verses = verseService.getAllVerses();
        for (Verse verse : verses) {
            List<TokenAnalysis> analyses = analyzeVerse(verse);
            verseAnalyses.add(new VerseAnalysis(verse, analyses));
            for (TokenAnalysis analysis : analyses) {
                indexToken(analysis);
            }
        }
    }

    private void indexToken(TokenAnalysis analysis) {
        addToIndex(tokensByForm, analysis.getNormalizedToken(), analysis);
        for (String lemma : analysis.getLemmas()) {
            addToIndex(tokensByLemma, ArabicTextUtils.normalizeToken(lemma), analysis);
        }
        for (String root : analysis.getRoots()) {
            addToIndex(tokensByRoot, ArabicTextUtils.normalizeToken(root), analysis);
        }
        for (String segment : analysis.getSegments()) {
            addToIndex(tokensBySegment, ArabicTextUtils.normalizeToken(segment), analysis);
        }
    }

    private void addToIndex(Map<String, List<TokenAnalysis>> index, String key, TokenAnalysis analysis) {
        if (key == null || key.isEmpty()) {
            return;
        }
        index.computeIfAbsent(key, k -> new ArrayList<>()).add(analysis);
    }

    private List<TokenAnalysis> analyzeVerse(Verse verse) {
        if (verse.getText() == null) {
            return Collections.emptyList();
        }
        Matcher matcher = ARABIC_TOKEN_PATTERN.matcher(verse.getText());
        List<TokenAnalysis> analyses = new ArrayList<>();
        int position = 0;
        while (matcher.find()) {
            String token = matcher.group();
            position++;
            TokenAnalysis analysis = analyzeToken(verse, token, position);
            if (analysis != null) {
                analyses.add(analysis);
            }
        }
        return analyses;
    }

    private TokenAnalysis analyzeToken(Verse verse, String token, int position) {
        String normalized = ArabicTextUtils.normalizeToken(token);
        if (normalized.isEmpty()) {
            return null;
        }

        ResultList resultList = wordAnalyzer.analyzeToken(token);
        Set<String> lemmaSet = new LinkedHashSet<>();
        Set<String> rootSet = new LinkedHashSet<>();
        Set<String> segments = new LinkedHashSet<>();
        String partOfSpeech = null;
        String stem = null;
        String proclitic = null;
        String enclitic = null;

        if (resultList != null && resultList.getAllResults() != null) {
            for (Result result : resultList.getAllResults()) {
                addIfNotBlank(lemmaSet, result.getLemma());
                addIfNotBlank(rootSet, result.getRoot());

                String cleanedStem = clean(result.getStem());
                stem = pickFirstNonEmpty(stem, cleanedStem);

                String posCandidate = clean(result.getPartOfSpeech());
                partOfSpeech = pickFirstNonEmpty(partOfSpeech, posCandidate);

                proclitic = pickFirstNonEmpty(proclitic, clean(result.getProclitic()));
                enclitic = pickFirstNonEmpty(enclitic, clean(result.getEnclitic()));

                if (proclitic != null && !proclitic.isEmpty()) {
                    segments.add(proclitic);
                }
                if (enclitic != null && !enclitic.isEmpty()) {
                    segments.add(enclitic);
                }
                if (cleanedStem != null && !cleanedStem.isEmpty()) {
                    segments.add(cleanedStem);
                }
            }
        }

        return new TokenAnalysis(
                verse,
                token,
                normalized,
                position,
                new ArrayList<>(lemmaSet),
                new ArrayList<>(rootSet),
                new ArrayList<>(segments),
                partOfSpeech,
                stem,
                proclitic,
                enclitic);
    }

    private String pickFirstNonEmpty(String current, String candidate) {
        if (current != null && !current.isEmpty()) {
            return current;
        }
        if (candidate == null || candidate.isEmpty()) {
            return current;
        }
        return candidate;
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private void addIfNotBlank(Set<String> target, String value) {
        String cleaned = clean(value);
        if (!cleaned.isEmpty()) {
            target.add(cleaned);
        }
    }
}
