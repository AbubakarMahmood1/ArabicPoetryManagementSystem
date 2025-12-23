package com.arabicpoetry.model.linguistics;

import com.arabicpoetry.model.Verse;

/**
 * Represents a verse with its similarity score to a query verse.
 */
public class VerseSimilarity implements Comparable<VerseSimilarity> {
    private final Verse verse;
    private final double similarityScore;

    public VerseSimilarity(Verse verse, double similarityScore) {
        this.verse = verse;
        this.similarityScore = similarityScore;
    }

    public Verse getVerse() {
        return verse;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    @Override
    public int compareTo(VerseSimilarity other) {
        return Double.compare(other.similarityScore, this.similarityScore);
    }
}
