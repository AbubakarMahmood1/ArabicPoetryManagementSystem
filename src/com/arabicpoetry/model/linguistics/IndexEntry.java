package com.arabicpoetry.model.linguistics;

/**
 * Represents a single occurrence of a term in the index,
 * linking back to its source verse by ID (memory efficient).
 */
public class IndexEntry {
    private final String term;
    private final int verseId;
    private final int poemId;
    private final int position;

    public IndexEntry(String term, int verseId, int poemId, int position) {
        this.term = term;
        this.verseId = verseId;
        this.poemId = poemId;
        this.position = position;
    }

    public String getTerm() {
        return term;
    }

    public int getVerseId() {
        return verseId;
    }

    public int getPoemId() {
        return poemId;
    }

    public int getPosition() {
        return position;
    }
}
