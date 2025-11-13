package com.arabicpoetry.dal.dao;

import com.arabicpoetry.model.Poem;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Poem entity
 */
public interface PoemDAO {
    Poem findById(int id) throws SQLException;
    List<Poem> findAll() throws SQLException;
    List<Poem> findByPoet(int poetId) throws SQLException;
    List<Poem> findByBook(int bookId) throws SQLException;
    void create(Poem poem) throws SQLException;
    void update(Poem poem) throws SQLException;
    void delete(int id) throws SQLException;
    List<Poem> search(String keyword) throws SQLException;
}
