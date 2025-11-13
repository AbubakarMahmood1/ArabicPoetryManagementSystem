package com.arabicpoetry.dal.dao.impl;

import com.arabicpoetry.dal.dao.PoemDAO;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of PoemDAO interface
 */
public class PoemDAOImpl implements PoemDAO {
    private Connection connection;

    public PoemDAOImpl() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public Poem findById(int id) throws SQLException {
        String sql = "SELECT p.*, po.name as poet_name, b.title as book_title " +
                     "FROM poems p " +
                     "LEFT JOIN poets po ON p.poet_id = po.poet_id " +
                     "LEFT JOIN books b ON p.book_id = b.book_id " +
                     "WHERE p.poem_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPoem(rs);
            }
        }
        return null;
    }

    @Override
    public List<Poem> findAll() throws SQLException {
        List<Poem> poems = new ArrayList<>();
        String sql = "SELECT p.*, po.name as poet_name, b.title as book_title " +
                     "FROM poems p " +
                     "LEFT JOIN poets po ON p.poet_id = po.poet_id " +
                     "LEFT JOIN books b ON p.book_id = b.book_id " +
                     "ORDER BY p.title";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                poems.add(mapResultSetToPoem(rs));
            }
        }
        return poems;
    }

    @Override
    public List<Poem> findByPoet(int poetId) throws SQLException {
        List<Poem> poems = new ArrayList<>();
        String sql = "SELECT p.*, po.name as poet_name, b.title as book_title " +
                     "FROM poems p " +
                     "LEFT JOIN poets po ON p.poet_id = po.poet_id " +
                     "LEFT JOIN books b ON p.book_id = b.book_id " +
                     "WHERE p.poet_id = ? ORDER BY p.title";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, poetId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                poems.add(mapResultSetToPoem(rs));
            }
        }
        return poems;
    }

    @Override
    public List<Poem> findByBook(int bookId) throws SQLException {
        List<Poem> poems = new ArrayList<>();
        String sql = "SELECT p.*, po.name as poet_name, b.title as book_title " +
                     "FROM poems p " +
                     "LEFT JOIN poets po ON p.poet_id = po.poet_id " +
                     "LEFT JOIN books b ON p.book_id = b.book_id " +
                     "WHERE p.book_id = ? ORDER BY p.title";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                poems.add(mapResultSetToPoem(rs));
            }
        }
        return poems;
    }

    @Override
    public void create(Poem poem) throws SQLException {
        String sql = "INSERT INTO poems (title, poet_id, book_id) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, poem.getTitle());
            if (poem.getPoetId() != null) {
                stmt.setInt(2, poem.getPoetId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            if (poem.getBookId() != null) {
                stmt.setInt(3, poem.getBookId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                poem.setPoemId(rs.getInt(1));
            }
        }
    }

    @Override
    public void update(Poem poem) throws SQLException {
        String sql = "UPDATE poems SET title = ?, poet_id = ?, book_id = ? WHERE poem_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, poem.getTitle());
            if (poem.getPoetId() != null) {
                stmt.setInt(2, poem.getPoetId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            if (poem.getBookId() != null) {
                stmt.setInt(3, poem.getBookId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setInt(4, poem.getPoemId());
            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM poems WHERE poem_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public List<Poem> search(String keyword) throws SQLException {
        List<Poem> poems = new ArrayList<>();
        String sql = "SELECT p.*, po.name as poet_name, b.title as book_title " +
                     "FROM poems p " +
                     "LEFT JOIN poets po ON p.poet_id = po.poet_id " +
                     "LEFT JOIN books b ON p.book_id = b.book_id " +
                     "WHERE p.title LIKE ? ORDER BY p.title";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                poems.add(mapResultSetToPoem(rs));
            }
        }
        return poems;
    }

    private Poem mapResultSetToPoem(ResultSet rs) throws SQLException {
        Poem poem = new Poem();
        poem.setPoemId(rs.getInt("poem_id"));
        poem.setTitle(rs.getString("title"));

        int poetId = rs.getInt("poet_id");
        poem.setPoetId(rs.wasNull() ? null : poetId);

        int bookId = rs.getInt("book_id");
        poem.setBookId(rs.wasNull() ? null : bookId);

        poem.setCreatedAt(rs.getTimestamp("created_at"));
        poem.setUpdatedAt(rs.getTimestamp("updated_at"));
        poem.setPoetName(rs.getString("poet_name"));
        poem.setBookTitle(rs.getString("book_title"));
        return poem;
    }
}
