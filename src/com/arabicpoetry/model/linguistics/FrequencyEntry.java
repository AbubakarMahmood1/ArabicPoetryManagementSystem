package com.arabicpoetry.model.linguistics;

/**
 * Represents a term (token, lemma, or root) with its frequency count.
 */
public class FrequencyEntry implements Comparable<FrequencyEntry> {
    private final String term;
    private final int count;

    public FrequencyEntry(String term, int count) {
        this.term = term;
        this.count = count;
    }

    public String getTerm() {
        return term;
    }

    public int getCount() {
        return count;
    }

    @Override
    public int compareTo(FrequencyEntry other) {
        int countCompare = Integer.compare(other.count, this.count);
        if (countCompare != 0) {
            return countCompare;
        }
        return this.term.compareTo(other.term);
    }
}
