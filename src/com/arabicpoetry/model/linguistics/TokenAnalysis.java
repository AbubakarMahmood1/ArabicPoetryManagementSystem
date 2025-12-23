package com.arabicpoetry.model.linguistics;

import java.util.Collections;
import java.util.List;

import com.arabicpoetry.model.Verse;

/**
 * Represents the linguistic information extracted from a single token inside a
 * verse, including lemma/root candidates and segmentation details.
 */
public class TokenAnalysis {
    private final Verse verse;
    private final String token;
    private final String normalizedToken;
    private final int position;
    private final List<String> lemmas;
    private final List<String> roots;
    private final List<String> segments;
    private final String partOfSpeech;
    private final String stem;
    private final String proclitic;
    private final String enclitic;

    public TokenAnalysis(Verse verse,
                         String token,
                         String normalizedToken,
                         int position,
                         List<String> lemmas,
                         List<String> roots,
                         List<String> segments,
                         String partOfSpeech,
                         String stem,
                         String proclitic,
                         String enclitic) {
        this.verse = verse;
        this.token = token;
        this.normalizedToken = normalizedToken;
        this.position = position;
        this.lemmas = lemmas == null ? Collections.emptyList() : Collections.unmodifiableList(lemmas);
        this.roots = roots == null ? Collections.emptyList() : Collections.unmodifiableList(roots);
        this.segments = segments == null ? Collections.emptyList() : Collections.unmodifiableList(segments);
        this.partOfSpeech = partOfSpeech;
        this.stem = stem;
        this.proclitic = proclitic;
        this.enclitic = enclitic;
    }

    public Verse getVerse() {
        return verse;
    }

    public String getToken() {
        return token;
    }

    public String getNormalizedToken() {
        return normalizedToken;
    }

    public int getPosition() {
        return position;
    }

    public List<String> getLemmas() {
        return lemmas;
    }

    public List<String> getRoots() {
        return roots;
    }

    public List<String> getSegments() {
        return segments;
    }

    public String getPartOfSpeech() {
        return partOfSpeech;
    }

    public String getStem() {
        return stem;
    }

    public String getProclitic() {
        return proclitic;
    }

    public String getEnclitic() {
        return enclitic;
    }

    public String getLemmaSummary() {
        return lemmas.isEmpty() ? "" : String.join("، ", lemmas);
    }

    public String getRootSummary() {
        return roots.isEmpty() ? "" : String.join("، ", roots);
    }

    public String getSegmentSummary() {
        return segments.isEmpty() ? "" : String.join(" + ", segments);
    }
}
