package com.arabicpoetry.dal.dao;

import com.arabicpoetry.dal.dao.impl.BookDAOImpl;
import com.arabicpoetry.dal.dao.impl.PoemDAOImpl;
import com.arabicpoetry.dal.dao.impl.PoetDAOImpl;
import com.arabicpoetry.dal.dao.impl.VerseDAOImpl;
import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Poet;
import com.arabicpoetry.model.Verse;
import com.arabicpoetry.testing.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VerseDAOImplIntegrationTest {

    private VerseDAOImpl verseDao;
    private PoemDAOImpl poemDao;
    private BookDAOImpl bookDao;
    private PoetDAOImpl poetDao;

    private Integer verseId;
    private Integer poemId;
    private Integer bookId;
    private Integer poetId;

    @BeforeEach
    void setUp() throws Exception {
        TestSupport.useTestDatabaseConfig();
        bookDao = new BookDAOImpl();
        poetDao = new PoetDAOImpl();
        poemDao = new PoemDAOImpl();
        verseDao = new VerseDAOImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (verseId != null) {
            verseDao.delete(verseId);
        }
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
    void createFindAndDeleteVerse() throws SQLException {
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

        Verse verse = new Verse();
        verse.setPoemId(poemId);
        verse.setVerseNumber(1);
        verse.setText("Test verse text");
        verseDao.create(verse);
        verseId = verse.getVerseId();
        assertNotNull(verseId);

        Verse fetched = verseDao.findById(verseId);
        assertNotNull(fetched);
        assertEquals("Test verse text", fetched.getText());
        assertEquals(poemId.intValue(), fetched.getPoemId());

        verseDao.delete(verseId);
        verseId = null;
        assertNull(verseDao.findById(verse.getVerseId()));
    }
}
