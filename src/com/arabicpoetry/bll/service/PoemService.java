package com.arabicpoetry.bll.service;

import com.arabicpoetry.dal.DAOFactory;
import com.arabicpoetry.dal.dao.PoemDAO;
import com.arabicpoetry.model.Poem;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Poem operations
 * Implements business logic for managing poems
 */
public class PoemService {
    private static PoemService instance;
    private PoemDAO poemDAO;

    // Private constructor for Singleton pattern
    private PoemService() {
        this.poemDAO = DAOFactory.getInstance().getPoemDAO();
    }

    /**
     * Get singleton instance
     */
    public static synchronized PoemService getInstance() {
        if (instance == null) {
            instance = new PoemService();
        }
        return instance;
    }

    /**
     * Get all poems
     */
    public List<Poem> getAllPoems() throws SQLException {
        return poemDAO.findAll();
    }

    /**
     * Get poem by ID
     */
    public Poem getPoemById(int id) throws SQLException {
        return poemDAO.findById(id);
    }

    /**
     * Get poems by poet
     */
    public List<Poem> getPoemsByPoet(int poetId) throws SQLException {
        return poemDAO.findByPoet(poetId);
    }

    /**
     * Get poems by book
     */
    public List<Poem> getPoemsByBook(int bookId) throws SQLException {
        return poemDAO.findByBook(bookId);
    }

    /**
     * Create new poem
     */
    public void createPoem(Poem poem) throws SQLException {
        validatePoem(poem);
        poemDAO.create(poem);
    }

    /**
     * Update existing poem
     */
    public void updatePoem(Poem poem) throws SQLException {
        validatePoem(poem);
        poemDAO.update(poem);
    }

    /**
     * Delete poem
     */
    public void deletePoem(int id) throws SQLException {
        poemDAO.delete(id);
    }

    /**
     * Search poems by keyword
     */
    public List<Poem> searchPoems(String keyword) throws SQLException {
        return poemDAO.search(keyword);
    }

    /**
     * Validate poem data
     */
    private void validatePoem(Poem poem) {
        if (poem.getTitle() == null || poem.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Poem title cannot be empty");
        }
    }
}
