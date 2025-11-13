package com.arabicpoetry.dal.dao;

import com.arabicpoetry.model.User;
import java.sql.SQLException;
import java.util.List;

/**
 * Data Access Object interface for User entity
 */
public interface UserDAO {
    User findById(int id) throws SQLException;
    User findByUsername(String username) throws SQLException;
    List<User> findAll() throws SQLException;
    void create(User user) throws SQLException;
    void update(User user) throws SQLException;
    void delete(int id) throws SQLException;
    void updateLastLogin(int userId) throws SQLException;
}
