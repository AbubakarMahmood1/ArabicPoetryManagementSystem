package com.arabicpoetry.dal.dao;

import com.arabicpoetry.dal.dao.impl.BookDAOImpl;
import com.arabicpoetry.dal.dao.impl.PoemDAOImpl;
import com.arabicpoetry.dal.dao.impl.PoetDAOImpl;
import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Poet;
import com.arabicpoetry.testing.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PoemDAOImplIntegrationTest {

    private PoemDAOImpl poemDao;
    private BookDAOImpl bookDao;
    private PoetDAOImpl poetDao;

    private Integer bookId;
    private Integer poetId;
    private Integer poemId;

    @BeforeEach
    void setUp() throws Exception {
        TestSupport.useTestDatabaseConfig();
        bookDao = new BookDAOImpl();
        poetDao = new PoetDAOImpl();
        poemDao = new PoemDAOImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (poemId != null) {
            poemDao.delete(poemId);
        }
        if (bookId != null) {
            bookDao.delete(bookId);
        }
        if (poetId != null) {
            poetDao.delete(poetId);
        }
    }

    @Test
    void createFindAndDeletePoem() throws SQLException {
        Book book = new Book();
        book.setTitle("Book-" + UUID.randomUUID());
        book.setCompiler("Tester");
        book.setEra("Era");
        bookDao.create(book);
        bookId = book.getBookId();

        Poet poet = new Poet();
        poet.setName("Poet-" + UUID.randomUUID());
        poet.setBiography("Bio");
        poetDao.create(poet);
        poetId = poet.getPoetId();

        Poem poem = new Poem();
        poem.setTitle("Poem-" + UUID.randomUUID());
        poem.setBookId(bookId);
        poem.setPoetId(poetId);
        poemDao.create(poem);
        poemId = poem.getPoemId();
        assertNotNull(poemId);

        Poem fetched = poemDao.findById(poemId);
        assertNotNull(fetched);
        assertEquals(poem.getTitle(), fetched.getTitle());
        assertEquals(bookId, fetched.getBookId());
        assertEquals(poetId, fetched.getPoetId());

        poemDao.delete(poemId);
        poemId = null;
        assertNull(poemDao.findById(poem.getPoemId()));
    }
}
