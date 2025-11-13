package com.arabicpoetry.dal.dao;

import com.arabicpoetry.model.Verse;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Verse entity
 */
public interface VerseDAO {
    Verse findById(int id) throws SQLException;
    List<Verse> findAll() throws SQLException;
    List<Verse> findByPoem(int poemId) throws SQLException;
    void create(Verse verse) throws SQLException;
    void update(Verse verse) throws SQLException;
    void delete(int id) throws SQLException;
    void deleteByPoem(int poemId) throws SQLException;
    List<Verse> search(String keyword) throws SQLException;
}
