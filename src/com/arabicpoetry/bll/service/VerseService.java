package com.arabicpoetry.bll.service;

import com.arabicpoetry.dal.DAOFactory;
import com.arabicpoetry.dal.dao.VerseDAO;
import com.arabicpoetry.model.Verse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Verse operations
 * Implements business logic for managing verses
 */
public class VerseService {
    private static VerseService instance;
    private VerseDAO verseDAO;
    private static final Logger LOGGER = LogManager.getLogger(VerseService.class);

    // Private constructor for Singleton pattern
    private VerseService() throws SQLException {
        this.verseDAO = DAOFactory.getInstance().getVerseDAO();
    }

    /**
     * Get singleton instance
     * @throws SQLException 
     */
    public static synchronized VerseService getInstance() throws SQLException {
        if (instance == null) {
            instance = new VerseService();
        }
        return instance;
    }

    // For tests
    public static synchronized void resetInstance() {
        instance = null;
    }

    /**
     * Get all verses
     */
    public List<Verse> getAllVerses() throws SQLException {
        return verseDAO.findAll();
    }

    /**
     * Get verse by ID
     */
    public Verse getVerseById(int id) throws SQLException {
        return verseDAO.findById(id);
    }

    /**
     * Get verses by poem
     */
    public List<Verse> getVersesByPoem(int poemId) throws SQLException {
        return verseDAO.findByPoem(poemId);
    }

    /**
     * Create new verse
     */
    public void createVerse(Verse verse) throws SQLException {
        validateVerse(verse);
        verseDAO.create(verse);
        LOGGER.info("Created verse {}", verse.getVerseNumber());
    }

    /**
     * Update existing verse
     */
    public void updateVerse(Verse verse) throws SQLException {
        validateVerse(verse);
        verseDAO.update(verse);
        LOGGER.info("Updated verse {}", verse.getVerseNumber());
    }

    /**
     * Delete verse
     */
    public void deleteVerse(int id) throws SQLException {
        verseDAO.delete(id);
        LOGGER.info("Deleted verse id={}", id);
    }

    /**
     * Delete all verses from a poem
     */
    public void deleteVersesByPoem(int poemId) throws SQLException {
        verseDAO.deleteByPoem(poemId);
        LOGGER.info("Deleted verses for poem id={}", poemId);
    }

    /**
     * Search verses by keyword
     */
    public List<Verse> searchVerses(String keyword) throws SQLException {
        return verseDAO.search(keyword);
    }

    /**
     * Validate verse data
     */
    private void validateVerse(Verse verse) {
        if (verse.getText() == null || verse.getText().trim().isEmpty()) {
            throw new IllegalArgumentException("Verse text cannot be empty");
        }
        if (verse.getVerseNumber() <= 0) {
            throw new IllegalArgumentException("Verse number must be positive");
        }
    }

    // Package-private for tests
    void setVerseDAO(VerseDAO verseDAO) {
        this.verseDAO = verseDAO;
    }
}
