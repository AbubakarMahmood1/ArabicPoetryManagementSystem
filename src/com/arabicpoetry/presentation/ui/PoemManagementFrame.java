package com.arabicpoetry.presentation.ui;

import com.arabicpoetry.bll.service.BookService;
import com.arabicpoetry.bll.service.PoetService;
import com.arabicpoetry.bll.service.PoemService;
import com.arabicpoetry.model.Book;
import com.arabicpoetry.model.Poet;
import com.arabicpoetry.model.Poem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Poem Management Frame - CRUD operations for poems
 */
public class PoemManagementFrame extends JFrame {
    private PoemService poemService;
    private PoetService poetService;
    private BookService bookService;

    private JTable poemTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, searchField;
    private JComboBox<Poet> poetCombo;
    private JComboBox<Book> bookCombo;
    private JButton addButton, updateButton, deleteButton, clearButton, searchButton, refreshButton;

    private Poem selectedPoem = null;

    public PoemManagementFrame() {
        poemService = PoemService.getInstance();
        poetService = PoetService.getInstance();
        bookService = BookService.getInstance();
        initializeComponents();
        loadComboBoxes();
        loadPoems();
    }

    private void initializeComponents() {
        setTitle("Manage Poems");
        setSize(900, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.NORTH);

        JPanel tablePanel = createTablePanel();
        mainPanel.add(tablePanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Poem Details"));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        titleField = new JTextField(30);
        titleField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        fieldsPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Poet:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        poetCombo = new JComboBox<>();
        fieldsPanel.add(poetCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Book:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        bookCombo = new JComboBox<>();
        fieldsPanel.add(bookCombo, gbc);

        panel.add(fieldsPanel, BorderLayout.CENTER);

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
        panel.setBorder(BorderFactory.createTitledBorder("Poems List"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchPanel.add(searchField);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPoems());
        searchPanel.add(refreshButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Title", "Poet", "Book"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        poemTable = new JTable(tableModel);
        poemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        poemTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });

        poemTable.getColumnModel().getColumn(1).setCellRenderer(new BookManagementFrame.RightAlignedTableCellRenderer());
        poemTable.getColumnModel().getColumn(2).setCellRenderer(new BookManagementFrame.RightAlignedTableCellRenderer());
        poemTable.getColumnModel().getColumn(3).setCellRenderer(new BookManagementFrame.RightAlignedTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(poemTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadComboBoxes() {
        try {
            // Load poets
            poetCombo.removeAllItems();
            poetCombo.addItem(null); // Add null option
            List<Poet> poets = poetService.getAllPoets();
            for (Poet poet : poets) {
                poetCombo.addItem(poet);
            }

            // Load books
            bookCombo.removeAllItems();
            bookCombo.addItem(null); // Add null option
            List<Book> books = bookService.getAllBooks();
            for (Book book : books) {
                bookCombo.addItem(book);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading poets/books: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPoems() {
        try {
            List<Poem> poems = poemService.getAllPoems();
            updateTable(poems);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading poems: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Poem> poems) {
        tableModel.setRowCount(0);
        for (Poem poem : poems) {
            tableModel.addRow(new Object[]{
                    poem.getPoemId(),
                    poem.getTitle(),
                    poem.getPoetName() != null ? poem.getPoetName() : "N/A",
                    poem.getBookTitle() != null ? poem.getBookTitle() : "N/A"
            });
        }
    }

    private void handleTableSelection() {
        int selectedRow = poemTable.getSelectedRow();
        if (selectedRow >= 0) {
            int poemId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                selectedPoem = poemService.getPoemById(poemId);
                if (selectedPoem != null) {
                    titleField.setText(selectedPoem.getTitle());

                    // Select poet in combo
                    if (selectedPoem.getPoetId() != null) {
                        for (int i = 0; i < poetCombo.getItemCount(); i++) {
                            Poet poet = poetCombo.getItemAt(i);
                            if (poet != null && poet.getPoetId() == selectedPoem.getPoetId()) {
                                poetCombo.setSelectedIndex(i);
                                break;
                            }
                        }
                    } else {
                        poetCombo.setSelectedIndex(0);
                    }

                    // Select book in combo
                    if (selectedPoem.getBookId() != null) {
                        for (int i = 0; i < bookCombo.getItemCount(); i++) {
                            Book book = bookCombo.getItemAt(i);
                            if (book != null && book.getBookId() == selectedPoem.getBookId()) {
                                bookCombo.setSelectedIndex(i);
                                break;
                            }
                        }
                    } else {
                        bookCombo.setSelectedIndex(0);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading poem: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAdd() {
        try {
            Poem poem = new Poem();
            poem.setTitle(titleField.getText().trim());

            Poet selectedPoet = (Poet) poetCombo.getSelectedItem();
            poem.setPoetId(selectedPoet != null ? selectedPoet.getPoetId() : null);

            Book selectedBook = (Book) bookCombo.getSelectedItem();
            poem.setBookId(selectedBook != null ? selectedBook.getBookId() : null);

            poemService.createPoem(poem);
            JOptionPane.showMessageDialog(this, "Poem added successfully!");
            clearForm();
            loadPoems();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding poem: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedPoem == null) {
            JOptionPane.showMessageDialog(this, "Please select a poem to update");
            return;
        }

        try {
            selectedPoem.setTitle(titleField.getText().trim());

            Poet selectedPoet = (Poet) poetCombo.getSelectedItem();
            selectedPoem.setPoetId(selectedPoet != null ? selectedPoet.getPoetId() : null);

            Book selectedBook = (Book) bookCombo.getSelectedItem();
            selectedPoem.setBookId(selectedBook != null ? selectedBook.getBookId() : null);

            poemService.updatePoem(selectedPoem);
            JOptionPane.showMessageDialog(this, "Poem updated successfully!");
            clearForm();
            loadPoems();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating poem: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedPoem == null) {
            JOptionPane.showMessageDialog(this, "Please select a poem to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this poem? This will also delete all verses.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                poemService.deletePoem(selectedPoem.getPoemId());
                JOptionPane.showMessageDialog(this, "Poem deleted successfully!");
                clearForm();
                loadPoems();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting poem: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadPoems();
            return;
        }

        try {
            List<Poem> poems = poemService.searchPoems(keyword);
            updateTable(poems);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error searching poems: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        titleField.setText("");
        poetCombo.setSelectedIndex(0);
        bookCombo.setSelectedIndex(0);
        selectedPoem = null;
        poemTable.clearSelection();
    }
}
