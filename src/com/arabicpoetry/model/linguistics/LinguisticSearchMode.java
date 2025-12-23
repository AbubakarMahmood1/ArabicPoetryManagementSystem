package com.arabicpoetry.model.linguistics;

/**
 * Enumerates the supported search/browse modes for the linguistic explorer UI.
 */
public enum LinguisticSearchMode {
    TOKEN("Tokens"),
    LEMMA("Lemmas"),
    ROOT("Roots"),
    SEGMENT("Segments"),
    STRING("Exact Text"),
    REGEX("Regular Expression");

    private final String displayLabel;

    LinguisticSearchMode(String displayLabel) {
        this.displayLabel = displayLabel;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    @Override
    public String toString() {
        return displayLabel;
    }
}
