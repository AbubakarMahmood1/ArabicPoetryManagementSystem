package com.arabicpoetry.model.linguistics;

import com.arabicpoetry.model.Verse;

/**
 * Represents a single hit returned by the linguistic search. The match may
 * correspond to a token analysis (token/lemma/root/segment modes) or an ad-hoc
 * text/regex snippet.
 */
public class LinguisticMatch {
    private final Verse verse;
    private final TokenAnalysis tokenAnalysis;
    private final LinguisticSearchMode mode;
    private final String matchDetail;

    public LinguisticMatch(Verse verse, TokenAnalysis tokenAnalysis, LinguisticSearchMode mode, String matchDetail) {
        this.verse = verse;
        this.tokenAnalysis = tokenAnalysis;
        this.mode = mode;
        this.matchDetail = matchDetail;
    }

    public Verse getVerse() {
        return verse;
    }

    public TokenAnalysis getTokenAnalysis() {
        return tokenAnalysis;
    }

    public LinguisticSearchMode getMode() {
        return mode;
    }

    public String getMatchDetail() {
        return matchDetail;
    }
}
