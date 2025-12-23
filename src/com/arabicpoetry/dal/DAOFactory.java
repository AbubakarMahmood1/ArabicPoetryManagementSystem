package com.arabicpoetry.dal;

import java.sql.SQLException;

import com.arabicpoetry.dal.dao.BookDAO;
import com.arabicpoetry.dal.dao.PoemDAO;
import com.arabicpoetry.dal.dao.PoetDAO;
import com.arabicpoetry.dal.dao.UserDAO;
import com.arabicpoetry.dal.dao.VerseDAO;
import com.arabicpoetry.dal.dao.impl.BookDAOImpl;
import com.arabicpoetry.dal.dao.impl.PoemDAOImpl;
import com.arabicpoetry.dal.dao.impl.PoetDAOImpl;
import com.arabicpoetry.dal.dao.impl.UserDAOImpl;
import com.arabicpoetry.dal.dao.impl.VerseDAOImpl;
import com.arabicpoetry.util.ConnectionProvider;
import com.arabicpoetry.util.DatabaseConnection;

/**
 * Abstract Factory pattern for creating DAO instances
 * Provides a centralized way to get DAO objects
 */
public class DAOFactory {
    private static DAOFactory instance;
    private ConnectionProvider connectionProvider;

    // DAO instances (Singleton pattern for each DAO)
    private UserDAO userDAO;
    private BookDAO bookDAO;
    private PoetDAO poetDAO;
    private PoemDAO poemDAO;
    private VerseDAO verseDAO;

    // Private constructor for Singleton pattern
    private DAOFactory() {
        this.connectionProvider = defaultProvider();
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
            userDAO = new UserDAOImpl(connectionProvider.getConnection());
        }
        return userDAO;
    }

    /**
     * Get BookDAO instance
     * @throws SQLException 
     */
    public BookDAO getBookDAO() throws SQLException {
        if (bookDAO == null) {
            bookDAO = new BookDAOImpl(connectionProvider.getConnection());
        }
        return bookDAO;
    }

    /**
     * Get PoetDAO instance
     * @throws SQLException 
     */
    public PoetDAO getPoetDAO() throws SQLException {
        if (poetDAO == null) {
            poetDAO = new PoetDAOImpl(connectionProvider.getConnection());
        }
        return poetDAO;
    }

    /**
     * Get PoemDAO instance
     * @throws SQLException 
     */
    public PoemDAO getPoemDAO() throws SQLException {
        if (poemDAO == null) {
            poemDAO = new PoemDAOImpl(connectionProvider.getConnection());
        }
        return poemDAO;
    }

    /**
     * Get VerseDAO instance
     * @throws SQLException 
     */
    public VerseDAO getVerseDAO() throws SQLException {
        if (verseDAO == null) {
            verseDAO = new VerseDAOImpl(connectionProvider.getConnection());
        }
        return verseDAO;
    }

    // Package-private setters for tests to inject mocks/fakes without breaking API
    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setBookDAO(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    public void setPoetDAO(PoetDAO poetDAO) {
        this.poetDAO = poetDAO;
    }

    public void setPoemDAO(PoemDAO poemDAO) {
        this.poemDAO = poemDAO;
    }

    public void setVerseDAO(VerseDAO verseDAO) {
        this.verseDAO = verseDAO;
    }

    /**
     * Replace the connection provider (e.g., inject a test DataSource) and drop cached DAOs.
     */
    public synchronized void setConnectionProvider(ConnectionProvider provider) {
        this.connectionProvider = provider != null ? provider : defaultProvider();
        clearCachedDaos();
    }

    private ConnectionProvider defaultProvider() {
        return () -> DatabaseConnection.getInstance().getConnection();
    }

    private void clearCachedDaos() {
        userDAO = null;
        bookDAO = null;
        poetDAO = null;
        poemDAO = null;
        verseDAO = null;
    }

    // Reset hook for tests
    public void reset() {
        clearCachedDaos();
        connectionProvider = defaultProvider();
        instance = null;
    }
}
