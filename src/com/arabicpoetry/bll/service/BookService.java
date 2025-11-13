package com.arabicpoetry.bll.service;

import com.arabicpoetry.dal.DAOFactory;
import com.arabicpoetry.dal.dao.BookDAO;
import com.arabicpoetry.model.Book;

import java.sql.SQLException;
import java.util.List;

/**
 * Service class for Book operations
 * Implements business logic for managing books
 */
public class BookService {
    private static BookService instance;
    private BookDAO bookDAO;

    // Private constructor for Singleton pattern
    private BookService() {
        this.bookDAO = DAOFactory.getInstance().getBookDAO();
    }

    /**
     * Get singleton instance
     */
    public static synchronized BookService getInstance() {
        if (instance == null) {
            instance = new BookService();
        }
        return instance;
    }

    /**
     * Get all books
     */
    public List<Book> getAllBooks() throws SQLException {
        return bookDAO.findAll();
    }

    /**
     * Get book by ID
     */
    public Book getBookById(int id) throws SQLException {
        return bookDAO.findById(id);
    }

    /**
     * Create new book
     */
    public void createBook(Book book) throws SQLException {
        validateBook(book);
        bookDAO.create(book);
    }

    /**
     * Update existing book
     */
    public void updateBook(Book book) throws SQLException {
        validateBook(book);
        bookDAO.update(book);
    }

    /**
     * Delete book
     */
    public void deleteBook(int id) throws SQLException {
        bookDAO.delete(id);
    }

    /**
     * Search books by keyword
     */
    public List<Book> searchBooks(String keyword) throws SQLException {
        return bookDAO.search(keyword);
    }

    /**
     * Validate book data
     */
    private void validateBook(Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }
    }
}
