package com.arabicpoetry.dal;

import java.sql.SQLException;

import com.arabicpoetry.dal.dao.*;
import com.arabicpoetry.dal.dao.impl.*;

/**
 * Abstract Factory pattern for creating DAO instances
 * Provides a centralized way to get DAO objects
 */
public class DAOFactory {
    private static DAOFactory instance;

    // DAO instances (Singleton pattern for each DAO)
    private UserDAO userDAO;
    private BookDAO bookDAO;
    private PoetDAO poetDAO;
    private PoemDAO poemDAO;
    private VerseDAO verseDAO;

    // Private constructor for Singleton pattern
    private DAOFactory() {
        // Initialize DAOs lazily
    }

    /**
     * Get singleton instance of DAOFactory
     */
    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }

    /**
     * Get UserDAO instance
     * @throws SQLException 
     */
    public UserDAO getUserDAO() throws SQLException {
        if (userDAO == null) {
            userDAO = new UserDAOImpl();
        }
        return userDAO;
    }

    /**
     * Get BookDAO instance
     * @throws SQLException 
     */
    public BookDAO getBookDAO() throws SQLException {
        if (bookDAO == null) {
            bookDAO = new BookDAOImpl();
        }
        return bookDAO;
    }

    /**
     * Get PoetDAO instance
     * @throws SQLException 
     */
    public PoetDAO getPoetDAO() throws SQLException {
        if (poetDAO == null) {
            poetDAO = new PoetDAOImpl();
        }
        return poetDAO;
    }

    /**
     * Get PoemDAO instance
     * @throws SQLException 
     */
    public PoemDAO getPoemDAO() throws SQLException {
        if (poemDAO == null) {
            poemDAO = new PoemDAOImpl();
        }
        return poemDAO;
    }

    /**
     * Get VerseDAO instance
     * @throws SQLException 
     */
    public VerseDAO getVerseDAO() throws SQLException {
        if (verseDAO == null) {
            verseDAO = new VerseDAOImpl();
        }
        return verseDAO;
    }
}
