package com.arabicpoetry.model.linguistics;

import java.util.Collections;
import java.util.List;

import com.arabicpoetry.model.Verse;

/**
 * Container that holds a verse and the token analyses derived from it.
 */
public class VerseAnalysis {
    private final Verse verse;
    private final List<TokenAnalysis> tokens;

    public VerseAnalysis(Verse verse, List<TokenAnalysis> tokens) {
        this.verse = verse;
        this.tokens = tokens == null ? Collections.emptyList() : Collections.unmodifiableList(tokens);
    }

    public Verse getVerse() {
        return verse;
    }

    public List<TokenAnalysis> getTokens() {
        return tokens;
    }
}
