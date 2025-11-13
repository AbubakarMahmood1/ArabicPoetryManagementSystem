package com.arabicpoetry.dal.dao.impl;

import com.arabicpoetry.dal.dao.PoetDAO;
import com.arabicpoetry.model.Poet;
import com.arabicpoetry.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PoetDAO interface
 */
public class PoetDAOImpl implements PoetDAO {
    private Connection connection;

    public PoetDAOImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Poet findById(int id) throws SQLException {
        String sql = "SELECT * FROM poets WHERE poet_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPoet(rs);
            }
        }
        return null;
    }

    @Override
    public Poet findByName(String name) throws SQLException {
        String sql = "SELECT * FROM poets WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPoet(rs);
            }
        }
        return null;
    }

    @Override
    public List<Poet> findAll() throws SQLException {
        List<Poet> poets = new ArrayList<>();
        String sql = "SELECT * FROM poets ORDER BY name";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                poets.add(mapResultSetToPoet(rs));
            }
        }
        return poets;
    }

    @Override
    public void create(Poet poet) throws SQLException {
        String sql = "INSERT INTO poets (name, biography, birth_year, death_year) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, poet.getName());
            stmt.setString(2, poet.getBiography());
            stmt.setString(3, poet.getBirthYear());
            stmt.setString(4, poet.getDeathYear());
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                poet.setPoetId(rs.getInt(1));
            }
        }
    }

    @Override
    public void update(Poet poet) throws SQLException {
        String sql = "UPDATE poets SET name = ?, biography = ?, birth_year = ?, death_year = ? WHERE poet_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, poet.getName());
            stmt.setString(2, poet.getBiography());
            stmt.setString(3, poet.getBirthYear());
            stmt.setString(4, poet.getDeathYear());
            stmt.setInt(5, poet.getPoetId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM poets WHERE poet_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Poet> search(String keyword) throws SQLException {
        List<Poet> poets = new ArrayList<>();
        String sql = "SELECT * FROM poets WHERE name LIKE ? OR biography LIKE ? ORDER BY name";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            String searchPattern = "%" + keyword + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                poets.add(mapResultSetToPoet(rs));
            }
        }
        return poets;
    }

    private Poet mapResultSetToPoet(ResultSet rs) throws SQLException {
        Poet poet = new Poet();
        poet.setPoetId(rs.getInt("poet_id"));
        poet.setName(rs.getString("name"));
        poet.setBiography(rs.getString("biography"));
        poet.setBirthYear(rs.getString("birth_year"));
        poet.setDeathYear(rs.getString("death_year"));
        poet.setCreatedAt(rs.getTimestamp("created_at"));
        poet.setUpdatedAt(rs.getTimestamp("updated_at"));
        return poet;
    }
}
