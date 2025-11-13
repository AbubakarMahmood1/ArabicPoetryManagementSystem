package com.arabicpoetry.model;

import java.sql.Timestamp;

/**
 * Poet entity class representing a poet
 */
public class Poet {
    private int poetId;
    private String name;
    private String biography;
    private String birthYear;
    private String deathYear;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Constructors
    public Poet() {
    }

    public Poet(int poetId, String name, String biography, String birthYear, String deathYear) {
        this.poetId = poetId;
        this.name = name;
        this.biography = biography;
        this.birthYear = birthYear;
        this.deathYear = deathYear;
    }

    // Getters and Setters
    public int getPoetId() {
        return poetId;
    }

    public void setPoetId(int poetId) {
        this.poetId = poetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getBirthYear() {
        return birthYear;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getDeathYear() {
        return deathYear;
    }

    public void setDeathYear(String deathYear) {
        this.deathYear = deathYear;
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
        return name;  // For display in ComboBox
    }
}
