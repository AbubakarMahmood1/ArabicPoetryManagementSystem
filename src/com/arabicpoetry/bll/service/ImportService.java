package com.arabicpoetry.bll.service;

import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.Poet;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Verse;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service class for importing poems from text file
 * Parses Poem.txt according to the format specified in README
 */
public class ImportService {
    private static ImportService instance;

    private BookService bookService;
    private PoetService poetService;
    private PoemService poemService;
    private VerseService verseService;

    // Regex patterns for parsing
    private static final Pattern BOOK_TITLE_PATTERN = Pattern.compile("الكتاب\\s*:\\s*(.+)");
    private static final Pattern POEM_TITLE_PATTERN = Pattern.compile("\\[(.+)\\]");
    private static final Pattern VERSE_PATTERN = Pattern.compile("\\(([^)]+)\\)");
    private static final String FOOTNOTE_DELIMITER = "_________";
    private static final String PAGE_DELIMITER = "==========";

    // Private constructor for Singleton pattern
    private ImportService() {
        this.bookService = BookService.getInstance();
        this.poetService = PoetService.getInstance();
        this.poemService = PoemService.getInstance();
        this.verseService = VerseService.getInstance();
    }

    /**
     * Get singleton instance
     */
    public static synchronized ImportService getInstance() {
        if (instance == null) {
            instance = new ImportService();
        }
        return instance;
    }

    /**
     * Import poems from file
     * @param filePath Path to the poem text file
     * @return Import summary message
     */
    public String importFromFile(String filePath) throws Exception {
        int booksImported = 0;
        int poetsImported = 0;
        int poemsImported = 0;
        int versesImported = 0;

        Book currentBook = null;
        Poem currentPoem = null;
        Poet currentPoet = null;
        int verseNumber = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            boolean inFootnotes = false;
            List<String> versePartsBuffer = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                // Check for footnote delimiter
                if (line.contains(FOOTNOTE_DELIMITER)) {
                    inFootnotes = true;
                    continue;
                }

                // Check for page delimiter
                if (line.contains(PAGE_DELIMITER)) {
                    inFootnotes = false;
                    continue;
                }

                // Skip footnotes
                if (inFootnotes) {
                    continue;
                }

                // Check for book title
                Matcher bookMatcher = BOOK_TITLE_PATTERN.matcher(line);
                if (bookMatcher.find()) {
                    String bookTitle = bookMatcher.group(1).trim();
                    currentBook = getOrCreateBook(bookTitle);
                    booksImported++;
                    continue;
                }

                // Check for poem title
                Matcher poemMatcher = POEM_TITLE_PATTERN.matcher(line);
                if (poemMatcher.find()) {
                    String poemTitle = poemMatcher.group(1).trim();

                    // Extract poet name from poem title if available
                    currentPoet = extractAndCreatePoet(poemTitle);
                    if (currentPoet != null) {
                        poetsImported++;
                    }

                    // Create poem
                    currentPoem = new Poem();
                    currentPoem.setTitle(poemTitle);
                    currentPoem.setBookId(currentBook != null ? currentBook.getBookId() : null);
                    currentPoem.setPoetId(currentPoet != null ? currentPoet.getPoetId() : null);
                    poemService.createPoem(currentPoem);
                    poemsImported++;
                    verseNumber = 0;
                    continue;
                }

                // Check for verses
                Matcher verseMatcher = VERSE_PATTERN.matcher(line);
                while (verseMatcher.find()) {
                    String versePart = verseMatcher.group(1).trim();
                    versePartsBuffer.add(versePart);
                }

                // If we have collected verse parts, combine them
                if (!versePartsBuffer.isEmpty() && currentPoem != null) {
                    String fullVerse = String.join(" ... ", versePartsBuffer);
                    verseNumber++;

                    Verse verse = new Verse();
                    verse.setPoemId(currentPoem.getPoemId());
                    verse.setVerseNumber(verseNumber);
                    verse.setText(fullVerse);
                    verseService.createVerse(verse);
                    versesImported++;

                    versePartsBuffer.clear();
                }
            }
        }

        return String.format("Import completed successfully!\n" +
                "Books: %d\n" +
                "Poets: %d\n" +
                "Poems: %d\n" +
                "Verses: %d",
                booksImported, poetsImported, poemsImported, versesImported);
    }

    /**
     * Get existing book or create new one
     */
    private Book getOrCreateBook(String title) throws SQLException {
        List<Book> books = bookService.searchBooks(title);

        if (!books.isEmpty()) {
            return books.get(0);
        }

        Book book = new Book();
        book.setTitle(title);
        book.setCompiler("غير محدد");
        book.setEra("العصر الإسلامي");
        bookService.createBook(book);
        return book;
    }

    /**
     * Extract poet name from poem title and create poet if not exists
     * Example: [قال قُرَيْطُ بنُ أُنَيف أحد بني العنبر]
     */
    private Poet extractAndCreatePoet(String poemTitle) throws SQLException {
        // Try to extract poet name (simplified extraction)
        // Look for patterns like "قال [name]" or "[name]"
        String poetName = null;

        if (poemTitle.contains("قال")) {
            int qalIndex = poemTitle.indexOf("قال");
            String afterQal = poemTitle.substring(qalIndex + 3).trim();

            // Extract name until common words
            String[] parts = afterQal.split("\\s+");
            if (parts.length >= 3) {
                poetName = parts[0] + " " + parts[1] + " " + parts[2];
            } else if (parts.length > 0) {
                poetName = parts[0];
            }
        }

        if (poetName == null || poetName.isEmpty()) {
            return null;
        }

        // Check if poet exists
        Poet existingPoet = poetService.getPoetByName(poetName);
        if (existingPoet != null) {
            return existingPoet;
        }

        // Create new poet
        Poet poet = new Poet();
        poet.setName(poetName);
        poet.setBiography("مستورد من ملف النص");
        poet.setBirthYear("غير معروف");
        poet.setDeathYear("غير معروف");
        poetService.createPoet(poet);
        return poet;
    }
}
