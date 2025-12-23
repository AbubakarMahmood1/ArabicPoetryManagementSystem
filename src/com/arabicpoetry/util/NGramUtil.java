package com.arabicpoetry.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility for generating n-grams from Arabic text.
 */
public class NGramUtil {

    /**
     * Generate character-level n-grams from text using code points
     * to properly handle Arabic text.
     */
    public static List<String> charNGrams(String text, int n) {
        if (text == null || text.isEmpty() || n <= 0) {
            return new ArrayList<>();
        }
        String normalized = ArabicTextUtils.normalizeToken(text);
        String clean = normalized.replaceAll("\\s+", " ").trim();

        if (clean.isEmpty()) {
            return new ArrayList<>();
        }

        // Use code points instead of char indices to handle Arabic properly
        int[] codePoints = clean.codePoints().toArray();

        if (codePoints.length < n) {
            return new ArrayList<>();
        }

        List<String> grams = new ArrayList<>();
        for (int i = 0; i <= codePoints.length - n; i++) {
            StringBuilder gram = new StringBuilder();
            for (int j = 0; j < n; j++) {
                gram.appendCodePoint(codePoints[i + j]);
            }
            grams.add(gram.toString());
        }
        return grams;
    }

    /**
     * Generate token-level n-grams from a list of tokens.
     */
    public static List<String> tokenNGrams(List<String> tokens, int n) {
        if (tokens == null || tokens.isEmpty() || n <= 0) {
            return new ArrayList<>();
        }
        List<String> grams = new ArrayList<>();
        for (int i = 0; i <= tokens.size() - n; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < n; j++) {
                if (j > 0) sb.append(' ');
                sb.append(tokens.get(i + j));
            }
            grams.add(sb.toString());
        }
        return grams;
    }

    /**
     * Convert list to set for efficient Jaccard computation.
     */
    public static Set<String> toSet(List<String> grams) {
        return new HashSet<>(grams);
    }

    /**
     * Calculate Jaccard similarity between two sets.
     * Returns 0.0 if both sets are empty (no meaningful similarity).
     */
    public static double jaccardSimilarity(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) {
            return 0.0;
        }
        if (a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }
        int intersection = 0;
        for (String g : a) {
            if (b.contains(g)) {
                intersection++;
            }
        }
        int union = a.size() + b.size() - intersection;
        return union == 0 ? 0.0 : (double) intersection / union;
    }
}
