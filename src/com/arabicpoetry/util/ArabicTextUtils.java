package com.arabicpoetry.util;

import java.text.Normalizer;

/**
 * Utility helpers for dealing with Arabic text: stripping diacritics, removing
 * tatweel characters, and normalizing Alef variants to keep lookups
 * consistent.
 */
public final class ArabicTextUtils {
    private ArabicTextUtils() {
    }

    /**
     * Remove common diacritics and other decoration marks so comparisons can be
     * performed on the bare letters.
     */
    public static String removeDiacritics(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFC);
        return normalized.replaceAll("[\\u064B-\\u065F\\u0670\\u06D6-\\u06ED]", "");
    }

    /**
     * Normalize Arabic token by removing diacritics, tatweel, punctuation, and
     * collapsing Alef variations into the bare Alef character. This keeps the
     * indexing keys deterministic.
     */
    public static String normalizeToken(String token) {
        if (token == null) {
            return "";
        }
        String normalized = removeDiacritics(token);
        normalized = normalized.replace("أ", "ا")
                .replace("إ", "ا")
                .replace("آ", "ا")
                .replace("ؤ", "و")
                .replace("ئ", "ي")
                .replace("ة", "ه")
                .replace("ى", "ي")
                .replace("ٱ", "ا")
                .replace("ﻻ", "لا")
                .replaceAll("\\u0640", ""); // Tatweel
        normalized = normalized.replaceAll("[^\\p{IsArabic}\\d]", "");
        return normalized.trim();
    }
}
