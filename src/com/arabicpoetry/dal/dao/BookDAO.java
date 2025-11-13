package com.arabicpoetry.dal.dao;

import com.arabicpoetry.model.Book;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for Book entity
 */
public interface BookDAO {
    Book findById(int id) throws SQLException;
    List<Book> findAll() throws SQLException;
    void create(Book book) throws SQLException;
    void update(Book book) throws SQLException;
    void delete(int id) throws SQLException;
    List<Book> search(String keyword) throws SQLException;
}
