package com.arabicpoetry.dal.dao.impl;

import com.arabicpoetry.dal.dao.VerseDAO;
import com.arabicpoetry.model.Verse;
import com.arabicpoetry.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of VerseDAO interface
 */
public class VerseDAOImpl implements VerseDAO {
    private Connection connection;

    public VerseDAOImpl() throws SQLException {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Verse findById(int id) throws SQLException {
        String sql = "SELECT v.*, p.title as poem_title " +
                     "FROM verses v " +
                     "LEFT JOIN poems p ON v.poem_id = p.poem_id " +
                     "WHERE v.verse_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToVerse(rs);
            }
        }
        return null;
    }

    @Override
    public List<Verse> findAll() throws SQLException {
        List<Verse> verses = new ArrayList<>();
        String sql = "SELECT v.*, p.title as poem_title " +
                     "FROM verses v " +
                     "LEFT JOIN poems p ON v.poem_id = p.poem_id " +
                     "ORDER BY v.poem_id, v.verse_number";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                verses.add(mapResultSetToVerse(rs));
            }
        }
        return verses;
    }

    @Override
    public List<Verse> findByPoem(int poemId) throws SQLException {
        List<Verse> verses = new ArrayList<>();
        String sql = "SELECT v.*, p.title as poem_title " +
                     "FROM verses v " +
                     "LEFT JOIN poems p ON v.poem_id = p.poem_id " +
                     "WHERE v.poem_id = ? ORDER BY v.verse_number";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, poemId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                verses.add(mapResultSetToVerse(rs));
            }
        }
        return verses;
    }

    @Override
    public void create(Verse verse) throws SQLException {
        String sql = "INSERT INTO verses (poem_id, verse_number, text) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, verse.getPoemId());
            stmt.setInt(2, verse.getVerseNumber());
            stmt.setString(3, verse.getText());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                verse.setVerseId(rs.getInt(1));
            }
        }
    }

    @Override
    public void update(Verse verse) throws SQLException {
        String sql = "UPDATE verses SET poem_id = ?, verse_number = ?, text = ? WHERE verse_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, verse.getPoemId());
            stmt.setInt(2, verse.getVerseNumber());
            stmt.setString(3, verse.getText());
            stmt.setInt(4, verse.getVerseId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM verses WHERE verse_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public void deleteByPoem(int poemId) throws SQLException {
        String sql = "DELETE FROM verses WHERE poem_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, poemId);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Verse> search(String keyword) throws SQLException {
        List<Verse> verses = new ArrayList<>();
        String sql = "SELECT v.*, p.title as poem_title " +
                     "FROM verses v " +
                     "LEFT JOIN poems p ON v.poem_id = p.poem_id " +
                     "WHERE v.text LIKE ? ORDER BY v.poem_id, v.verse_number";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                verses.add(mapResultSetToVerse(rs));
            }
        }
        return verses;
    }

    private Verse mapResultSetToVerse(ResultSet rs) throws SQLException {
        Verse verse = new Verse();
        verse.setVerseId(rs.getInt("verse_id"));
        verse.setPoemId(rs.getInt("poem_id"));
        verse.setVerseNumber(rs.getInt("verse_number"));
        verse.setText(rs.getString("text"));
        verse.setCreatedAt(rs.getTimestamp("created_at"));
        verse.setUpdatedAt(rs.getTimestamp("updated_at"));
        verse.setPoemTitle(rs.getString("poem_title"));
        return verse;
    }
}
