package com.arabicpoetry.dal.dao;

import com.arabicpoetry.model.Poet;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Poet entity
 */
public interface PoetDAO {
    Poet findById(int id) throws SQLException;
    Poet findByName(String name) throws SQLException;
    List<Poet> findAll() throws SQLException;
    void create(Poet poet) throws SQLException;
    void update(Poet poet) throws SQLException;
    void delete(int id) throws SQLException;
    List<Poet> search(String keyword) throws SQLException;
}
