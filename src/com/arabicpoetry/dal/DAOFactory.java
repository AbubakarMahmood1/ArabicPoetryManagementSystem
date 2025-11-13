package com.arabicpoetry.dal;

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
     */
    public UserDAO getUserDAO() {
        if (userDAO == null) {
            userDAO = new UserDAOImpl();
        }
        return userDAO;
    }

    /**
     * Get BookDAO instance
     */
    public BookDAO getBookDAO() {
        if (bookDAO == null) {
            bookDAO = new BookDAOImpl();
        }
        return bookDAO;
    }

    /**
     * Get PoetDAO instance
     */
    public PoetDAO getPoetDAO() {
        if (poetDAO == null) {
            poetDAO = new PoetDAOImpl();
        }
        return poetDAO;
    }

    /**
     * Get PoemDAO instance
     */
    public PoemDAO getPoemDAO() {
        if (poemDAO == null) {
            poemDAO = new PoemDAOImpl();
        }
        return poemDAO;
    }

    /**
     * Get VerseDAO instance
     */
    public VerseDAO getVerseDAO() {
        if (verseDAO == null) {
            verseDAO = new VerseDAOImpl();
        }
        return verseDAO;
    }
}
