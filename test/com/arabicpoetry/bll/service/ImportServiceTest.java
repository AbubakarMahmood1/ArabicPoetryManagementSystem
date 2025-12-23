package com.arabicpoetry.bll.service;

import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Poet;
import com.arabicpoetry.model.Verse;
import com.arabicpoetry.testing.TestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class ImportServiceTest {

    private ImportService service;
    private BookService bookService;
    private PoetService poetService;
    private PoemService poemService;
    private VerseService verseService;
    private Path tempFile;

    @BeforeEach
    void setUp() throws Exception {
        TestSupport.resetSingletons();
        service = ImportService.getInstance();
        bookService = Mockito.mock(BookService.class);
        poetService = Mockito.mock(PoetService.class);
        poemService = Mockito.mock(PoemService.class);
        verseService = Mockito.mock(VerseService.class);
        service.setBookService(bookService);
        service.setPoetService(poetService);
        service.setPoemService(poemService);
        service.setVerseService(verseService);

        when(bookService.searchBooks(any(String.class))).thenReturn(List.of());
        Mockito.doAnswer(invocation -> {
            Book b = invocation.getArgument(0);
            b.setBookId(100);
            return null;
        }).when(bookService).createBook(any(Book.class));

        when(poetService.getPoetByName(any(String.class))).thenReturn(null);
        Mockito.doAnswer(invocation -> {
            Poet p = invocation.getArgument(0);
            p.setPoetId(200);
            return null;
        }).when(poetService).createPoet(any(Poet.class));

        Mockito.doAnswer(invocation -> {
            Poem p = invocation.getArgument(0);
            p.setPoemId(300);
            return null;
        }).when(poemService).createPoem(any(Poem.class));
    }

    @AfterEach
    void cleanup() throws Exception {
        if (tempFile != null) {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void importsBookPoemVerseAndSkipsFootnotes() throws Exception {
        String content = ""
                + "?????? : Test Book\n"
                + "[Test Poem]\n"
                + "(first) (second)\n"
                + "_________\n"
                + "(footnote should be skipped)\n"
                + "==========\n";
        tempFile = Files.createTempFile("import-test", ".txt");
        Files.writeString(tempFile, content);

        service.importFromFile(tempFile.toString());

        Mockito.verify(bookService, times(1)).createBook(any(Book.class));
        Mockito.verify(poetService, times(1)).createPoet(any(Poet.class));
        Mockito.verify(poemService, times(1)).createPoem(any(Poem.class));
        ArgumentCaptor<Verse> verseCaptor = ArgumentCaptor.forClass(Verse.class);
        Mockito.verify(verseService, times(1)).createVerse(verseCaptor.capture());
        Verse captured = verseCaptor.getValue();
        assertTrue(captured.getText().contains("first"));
        assertTrue(captured.getText().contains("second"));
    }
}
