package com.arabicpoetry.model;

import java.sql.Timestamp;

/**
 * Verse entity class representing a verse in a poem
 */
public class Verse {
    private int verseId;
    private int poemId;
    private int verseNumber;
    private String text;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // For display purposes (not stored in DB)
    private String poemTitle;

    // Constructors
    public Verse() {
    }

    public Verse(int verseId, int poemId, int verseNumber, String text) {
        this.verseId = verseId;
        this.poemId = poemId;
        this.verseNumber = verseNumber;
        this.text = text;
    }

    // Getters and Setters
    public int getVerseId() {
        return verseId;
    }

    public void setVerseId(int verseId) {
        this.verseId = verseId;
    }

    public int getPoemId() {
        return poemId;
    }

    public void setPoemId(int poemId) {
        this.poemId = poemId;
    }

    public int getVerseNumber() {
        return verseNumber;
    }

    public void setVerseNumber(int verseNumber) {
        this.verseNumber = verseNumber;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPoemTitle() {
        return poemTitle;
    }

    public void setPoemTitle(String poemTitle) {
        this.poemTitle = poemTitle;
    }

    @Override
    public String toString() {
        return verseNumber + ": " + text.substring(0, Math.min(50, text.length())) + "...";
    }
}
