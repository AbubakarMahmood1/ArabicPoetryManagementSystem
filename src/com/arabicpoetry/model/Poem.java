package com.arabicpoetry.model;

import java.sql.Timestamp;

/**
 * Poem entity class representing a poem
 */
public class Poem {
    private int poemId;
    private String title;
    private Integer poetId;  // Nullable foreign key
    private Integer bookId;  // Nullable foreign key
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // For display purposes (not stored in DB)
    private String poetName;
    private String bookTitle;

    // Constructors
    public Poem() {
    }

    public Poem(int poemId, String title, Integer poetId, Integer bookId) {
        this.poemId = poemId;
        this.title = title;
        this.poetId = poetId;
        this.bookId = bookId;
    }

    // Getters and Setters
    public int getPoemId() {
        return poemId;
    }

    public void setPoemId(int poemId) {
        this.poemId = poemId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPoetId() {
        return poetId;
    }

    public void setPoetId(Integer poetId) {
        this.poetId = poetId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
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

    public String getPoetName() {
        return poetName;
    }

    public void setPoetName(String poetName) {
        this.poetName = poetName;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    @Override
    public String toString() {
        return title;  // For display in ComboBox
    }
}
