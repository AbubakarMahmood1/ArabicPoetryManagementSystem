package com.arabicpoetry.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.oujda_nlp_team.AlKhalil2Analyzer;
import net.oujda_nlp_team.entity.ResultList;

/**
 * Wraps the AlKhalil morphological analyzer and exposes a simple API that can
 * be memoized or re-used across the application layers. This follows the
 * Singleton pattern to avoid repeated and expensive instantiation of the
 * analyzer.
 */
public final class WordAnalyzer {
    private static final Logger LOGGER = LogManager.getLogger(WordAnalyzer.class);
    private static volatile WordAnalyzer instance;

    private final AlKhalil2Analyzer analyzer;
    private final Set<String> tokensWithAnalyzerBug;

    private WordAnalyzer() {
        this.analyzer = AlKhalil2Analyzer.getInstance();
        this.tokensWithAnalyzerBug = Collections.synchronizedSet(new HashSet<>());
    }

    public static WordAnalyzer getInstance() {
        WordAnalyzer localInstance = instance;
        if (localInstance == null) {
            synchronized (WordAnalyzer.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new WordAnalyzer();
                }
            }
        }
        return localInstance;
    }

    /**
     * Analyze the provided token using AlKhalil and return the full result list.
     * Returns an empty {@link ResultList} if the analyzer fails rather than
     * bubbling the exception to callers that primarily operate in the UI.
     */
    public ResultList analyzeToken(String token) {
        ResultList emptyResult = new ResultList();
        if (token == null || token.trim().isEmpty()) {
            return emptyResult;
        }

        String normalized = ArabicTextUtils.normalizeToken(token);
        if (normalized.isEmpty() || tokensWithAnalyzerBug.contains(normalized)) {
            return emptyResult;
        }

        try {
            if (analyzer == null) {
                LOGGER.warn("AlKhalil2Analyzer instance is null. Ensure the jar is on the classpath.");
                return emptyResult;
            }
            return analyzer.processToken(token);
        } catch (StringIndexOutOfBoundsException ex) {
            tokensWithAnalyzerBug.add(normalized);
            LOGGER.info(() -> "Skipping token due to analyzer limitation: " + token);
            return emptyResult;
        } catch (Exception ex) {
            LOGGER.warn("Failed to analyze token: {}", token, ex);
            return emptyResult;
        }
    }
}
