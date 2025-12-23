package com.arabicpoetry.bll.service;

import com.arabicpoetry.dal.dao.BookDAO;
import com.arabicpoetry.model.Book;
import com.arabicpoetry.testing.TestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookServiceTest {

    private BookService service;
    private BookDAO bookDAO;

    @BeforeEach
    void setUp() throws SQLException {
        TestSupport.resetSingletons();
        service = BookService.getInstance();
        bookDAO = Mockito.mock(BookDAO.class);
        service.setBookDAO(bookDAO);
    }

    @Test
    void createBookDelegatesToDao() throws SQLException {
        Book book = new Book();
        book.setTitle("Sample");

        service.createBook(book);

        verify(bookDAO).create(any(Book.class));
    }

    @Test
    void createBookRejectsEmptyTitle() {
        Book book = new Book();
        book.setTitle("  ");

        assertThrows(IllegalArgumentException.class, () -> service.createBook(book));
    }

    @Test
    void updateBookDelegatesToDao() throws SQLException {
        Book book = new Book();
        book.setTitle("Update");

        service.updateBook(book);

        verify(bookDAO).update(any(Book.class));
    }

    @Test
    void searchBooksDelegates() throws SQLException {
        when(bookDAO.search("key")).thenReturn(java.util.List.of());
        assertEquals(0, service.searchBooks("key").size());
        verify(bookDAO).search("key");
    }
}
