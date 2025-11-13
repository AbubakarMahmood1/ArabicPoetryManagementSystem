package com.arabicpoetry.presentation.ui;

import com.arabicpoetry.bll.service.BookService;
import com.arabicpoetry.model.Book;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Book Management Frame - CRUD operations for books
 */
public class BookManagementFrame extends JFrame {
    private BookService bookService;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, compilerField, eraField, searchField;
    private JTextArea descriptionArea;
    private JButton addButton, updateButton, deleteButton, clearButton, searchButton, refreshButton;

    private Book selectedBook = null;

    public BookManagementFrame() {
        bookService = BookService.getInstance();
        initializeComponents();
        loadBooks();
    }

    private void initializeComponents() {
        setTitle("Manage Books");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Book Details"));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Title
        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        titleField = new JTextField(30);
        titleField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        fieldsPanel.add(titleField, gbc);

        // Compiler
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Compiler:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        compilerField = new JTextField(30);
        compilerField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        fieldsPanel.add(compilerField, gbc);

        // Era
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Era:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        eraField = new JTextField(30);
        eraField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        fieldsPanel.add(eraField, gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        descriptionArea = new JTextArea(3, 30);
        descriptionArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScrollPane = new JScrollPane(descriptionArea);
        fieldsPanel.add(descScrollPane, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addButton = new JButton("Add");
        addButton.addActionListener(e -> handleAdd());
        updateButton = new JButton("Update");
        updateButton.addActionListener(e -> handleUpdate());
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> handleDelete());
        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> clearForm());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Books List"));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchPanel.add(searchField);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadBooks());
        searchPanel.add(refreshButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Title", "Compiler", "Era"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });

        // Set RTL for Arabic columns
        bookTable.getColumnModel().getColumn(1).setCellRenderer(new RightAlignedTableCellRenderer());
        bookTable.getColumnModel().getColumn(2).setCellRenderer(new RightAlignedTableCellRenderer());
        bookTable.getColumnModel().getColumn(3).setCellRenderer(new RightAlignedTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(bookTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            updateTable(books);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading books: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Book> books) {
        tableModel.setRowCount(0);
        for (Book book : books) {
            tableModel.addRow(new Object[]{
                    book.getBookId(),
                    book.getTitle(),
                    book.getCompiler(),
                    book.getEra()
            });
        }
    }

    private void handleTableSelection() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow >= 0) {
            int bookId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                selectedBook = bookService.getBookById(bookId);
                if (selectedBook != null) {
                    titleField.setText(selectedBook.getTitle());
                    compilerField.setText(selectedBook.getCompiler());
                    eraField.setText(selectedBook.getEra());
                    descriptionArea.setText(selectedBook.getDescription());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading book: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAdd() {
        try {
            Book book = new Book();
            book.setTitle(titleField.getText().trim());
            book.setCompiler(compilerField.getText().trim());
            book.setEra(eraField.getText().trim());
            book.setDescription(descriptionArea.getText().trim());

            bookService.createBook(book);
            JOptionPane.showMessageDialog(this, "Book added successfully!");
            clearForm();
            loadBooks();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding book: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedBook == null) {
            JOptionPane.showMessageDialog(this, "Please select a book to update");
            return;
        }

        try {
            selectedBook.setTitle(titleField.getText().trim());
            selectedBook.setCompiler(compilerField.getText().trim());
            selectedBook.setEra(eraField.getText().trim());
            selectedBook.setDescription(descriptionArea.getText().trim());

            bookService.updateBook(selectedBook);
            JOptionPane.showMessageDialog(this, "Book updated successfully!");
            clearForm();
            loadBooks();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating book: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedBook == null) {
            JOptionPane.showMessageDialog(this, "Please select a book to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this book?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                bookService.deleteBook(selectedBook.getBookId());
                JOptionPane.showMessageDialog(this, "Book deleted successfully!");
                clearForm();
                loadBooks();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting book: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadBooks();
            return;
        }

        try {
            List<Book> books = bookService.searchBooks(keyword);
            updateTable(books);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error searching books: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        titleField.setText("");
        compilerField.setText("");
        eraField.setText("");
        descriptionArea.setText("");
        selectedBook = null;
        bookTable.clearSelection();
    }

    // Custom cell renderer for RTL text
    static class RightAlignedTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        public RightAlignedTableCellRenderer() {
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
    }
}
