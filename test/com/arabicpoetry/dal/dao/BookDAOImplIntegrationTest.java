package com.arabicpoetry.dal.dao;

import com.arabicpoetry.dal.dao.impl.BookDAOImpl;
import com.arabicpoetry.model.Book;
import com.arabicpoetry.testing.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple integration test hitting the test database.
 * Requires config-test.properties to point at the test schema.
 */
class BookDAOImplIntegrationTest {

    private BookDAOImpl dao;
    private Integer createdId;

    @BeforeEach
    void setUp() throws Exception {
        TestSupport.useTestDatabaseConfig();
        dao = new BookDAOImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (createdId != null) {
            dao.delete(createdId);
        }
    }

    @Test
    void createFindDeleteBook() throws Exception {
        Book book = new Book();
        book.setTitle("Test Book " + UUID.randomUUID());
        book.setCompiler("Tester");
        book.setEra("Modern");
        book.setDescription("Integration test book");

        dao.create(book);
        createdId = book.getBookId();

        assertNotNull(createdId, "Generated ID should be set");

        Book fetched = dao.findById(createdId);
        assertNotNull(fetched, "Fetched book should not be null");
        assertEquals(book.getTitle(), fetched.getTitle());
        assertEquals(book.getCompiler(), fetched.getCompiler());

        List<Book> all = dao.findAll();
        assertTrue(all.stream().anyMatch(b -> createdId.equals(b.getBookId())), "New book should be in findAll");

        dao.delete(createdId);
        createdId = null;

        assertNull(dao.findById(book.getBookId()), "Book should be deleted");
    }
}
