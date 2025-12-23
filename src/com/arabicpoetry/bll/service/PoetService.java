package com.arabicpoetry.bll.service;

import com.arabicpoetry.dal.DAOFactory;
import com.arabicpoetry.dal.dao.PoetDAO;
import com.arabicpoetry.model.Poet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Poet operations
 * Implements business logic for managing poets
 */
public class PoetService {
    private static PoetService instance;
    private PoetDAO poetDAO;
    private static final Logger LOGGER = LogManager.getLogger(PoetService.class);

    // Private constructor for Singleton pattern
    private PoetService() throws SQLException {
        this.poetDAO = DAOFactory.getInstance().getPoetDAO();
    }

    /**
     * Get singleton instance
     * @throws SQLException 
     */
    public static synchronized PoetService getInstance() throws SQLException {
        if (instance == null) {
            instance = new PoetService();
        }
        return instance;
    }

    // For tests
    public static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * Get all poets
     */
    public List<Poet> getAllPoets() throws SQLException {
        return poetDAO.findAll();
    }

    /**
     * Get poet by ID
     */
    public Poet getPoetById(int id) throws SQLException {
        return poetDAO.findById(id);
    }

    /**
     * Get poet by name
     */
    public Poet getPoetByName(String name) throws SQLException {
        return poetDAO.findByName(name);
    }

    /**
     * Create new poet
     */
    public void createPoet(Poet poet) throws SQLException {
        validatePoet(poet);
        poetDAO.create(poet);
        LOGGER.info("Created poet '{}'", poet.getName());
    }

    /**
     * Update existing poet
     */
    public void updatePoet(Poet poet) throws SQLException {
        validatePoet(poet);
        poetDAO.update(poet);
        LOGGER.info("Updated poet '{}'", poet.getName());
    }

    /**
     * Delete poet
     */
    public void deletePoet(int id) throws SQLException {
        poetDAO.delete(id);
        LOGGER.info("Deleted poet id={}", id);
    }

    /**
     * Search poets by keyword
     */
    public List<Poet> searchPoets(String keyword) throws SQLException {
        return poetDAO.search(keyword);
    }

    /**
     * Validate poet data
     */
    private void validatePoet(Poet poet) {
        if (poet.getName() == null || poet.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Poet name cannot be empty");
        }
    }

    // Package-private for tests
    void setPoetDAO(PoetDAO poetDAO) {
        this.poetDAO = poetDAO;
    }
}
