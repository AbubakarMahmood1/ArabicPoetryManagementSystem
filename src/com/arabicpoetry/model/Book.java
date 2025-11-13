package com.arabicpoetry.model;

import java.sql.Timestamp;

/**
 * Book entity class representing a poetry book
 */
public class Book {
    private int bookId;
    private String title;
    private String compiler;
    private String era;
    private String description;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public Book() {
    }

    public Book(int bookId, String title, String compiler, String era, String description) {
        this.bookId = bookId;
        this.title = title;
        this.compiler = compiler;
        this.era = era;
        this.description = description;
    }

    // Getters and Setters
    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompiler() {
        return compiler;
    }

    public void setCompiler(String compiler) {
        this.compiler = compiler;
    }

    public String getEra() {
        return era;
    }

    public void setEra(String era) {
        this.era = era;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return title;  // For display in ComboBox
    }
}
