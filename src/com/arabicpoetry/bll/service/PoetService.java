package com.arabicpoetry.bll.service;

import com.arabicpoetry.dal.DAOFactory;
import com.arabicpoetry.dal.dao.PoetDAO;
import com.arabicpoetry.model.Poet;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Poet operations
 * Implements business logic for managing poets
 */
public class PoetService {
    private static PoetService instance;
    private PoetDAO poetDAO;

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
    }

    /**
     * Update existing poet
     */
    public void updatePoet(Poet poet) throws SQLException {
        validatePoet(poet);
        poetDAO.update(poet);
    }

    /**
     * Delete poet
     */
    public void deletePoet(int id) throws SQLException {
        poetDAO.delete(id);
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
}
