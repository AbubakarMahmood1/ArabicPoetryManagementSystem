package com.arabicpoetry.presentation.ui;

import com.arabicpoetry.bll.service.PoemService;
import com.arabicpoetry.bll.service.VerseService;
import com.arabicpoetry.model.Poem;
import com.arabicpoetry.model.Verse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Verse Management Frame - CRUD operations for verses
 */
public class VerseManagementFrame extends JFrame {
    private VerseService verseService;
    private PoemService poemService;

    private JTable verseTable;
    private DefaultTableModel tableModel;
    private JTextField verseNumberField, searchField;
    private JTextArea textArea;
    private JComboBox<Poem> poemCombo;
    private JButton addButton, updateButton, deleteButton, clearButton, searchButton, refreshButton;

    private Verse selectedVerse = null;

    public VerseManagementFrame() {
        verseService = VerseService.getInstance();
        poemService = PoemService.getInstance();
        initializeComponents();
        loadPoemCombo();
        loadVerses();
    }

    private void initializeComponents() {
        setTitle("Manage Verses");
        setSize(1000, 600);
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
        panel.setBorder(BorderFactory.createTitledBorder("Verse Details"));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Poem:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        poemCombo = new JComboBox<>();
        poemCombo.addActionListener(e -> loadVersesByPoem());
        fieldsPanel.add(poemCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Verse Number:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        verseNumberField = new JTextField(30);
        fieldsPanel.add(verseNumberField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Text:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        textArea = new JTextArea(4, 30);
        textArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane textScrollPane = new JScrollPane(textArea);
        fieldsPanel.add(textScrollPane, gbc);

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
        panel.setBorder(BorderFactory.createTitledBorder("Verses List"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchPanel.add(searchField);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadVerses());
        searchPanel.add(refreshButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Poem", "Verse #", "Text"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        verseTable = new JTable(tableModel);
        verseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        verseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });

        // Set column widths
        verseTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        verseTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        verseTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        verseTable.getColumnModel().getColumn(3).setPreferredWidth(400);

        verseTable.getColumnModel().getColumn(1).setCellRenderer(new BookManagementFrame.RightAlignedTableCellRenderer());
        verseTable.getColumnModel().getColumn(3).setCellRenderer(new BookManagementFrame.RightAlignedTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(verseTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadPoemCombo() {
        try {
            poemCombo.removeAllItems();
            List<Poem> poems = poemService.getAllPoems();
            for (Poem poem : poems) {
                poemCombo.addItem(poem);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading poems: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadVerses() {
        try {
            List<Verse> verses = verseService.getAllVerses();
            updateTable(verses);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading verses: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadVersesByPoem() {
        Poem selectedPoem = (Poem) poemCombo.getSelectedItem();
        if (selectedPoem != null) {
            try {
                List<Verse> verses = verseService.getVersesByPoem(selectedPoem.getPoemId());
                updateTable(verses);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading verses: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateTable(List<Verse> verses) {
        tableModel.setRowCount(0);
        for (Verse verse : verses) {
            String truncatedText = verse.getText();
            if (truncatedText.length() > 100) {
                truncatedText = truncatedText.substring(0, 100) + "...";
            }
            tableModel.addRow(new Object[]{
                    verse.getVerseId(),
                    verse.getPoemTitle() != null ? verse.getPoemTitle() : "N/A",
                    verse.getVerseNumber(),
                    truncatedText
            });
        }
    }

    private void handleTableSelection() {
        int selectedRow = verseTable.getSelectedRow();
        if (selectedRow >= 0) {
            int verseId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                selectedVerse = verseService.getVerseById(verseId);
                if (selectedVerse != null) {
                    // Select poem in combo
                    for (int i = 0; i < poemCombo.getItemCount(); i++) {
                        Poem poem = poemCombo.getItemAt(i);
                        if (poem.getPoemId() == selectedVerse.getPoemId()) {
                            poemCombo.setSelectedIndex(i);
                            break;
                        }
                    }

                    verseNumberField.setText(String.valueOf(selectedVerse.getVerseNumber()));
                    textArea.setText(selectedVerse.getText());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading verse: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAdd() {
        Poem selectedPoem = (Poem) poemCombo.getSelectedItem();
        if (selectedPoem == null) {
            JOptionPane.showMessageDialog(this, "Please select a poem");
            return;
        }

        try {
            Verse verse = new Verse();
            verse.setPoemId(selectedPoem.getPoemId());
            verse.setVerseNumber(Integer.parseInt(verseNumberField.getText().trim()));
            verse.setText(textArea.getText().trim());

            verseService.createVerse(verse);
            JOptionPane.showMessageDialog(this, "Verse added successfully!");
            clearForm();
            loadVersesByPoem();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Verse number must be a valid integer",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding verse: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedVerse == null) {
            JOptionPane.showMessageDialog(this, "Please select a verse to update");
            return;
        }

        Poem selectedPoem = (Poem) poemCombo.getSelectedItem();
        if (selectedPoem == null) {
            JOptionPane.showMessageDialog(this, "Please select a poem");
            return;
        }

        try {
            selectedVerse.setPoemId(selectedPoem.getPoemId());
            selectedVerse.setVerseNumber(Integer.parseInt(verseNumberField.getText().trim()));
            selectedVerse.setText(textArea.getText().trim());

            verseService.updateVerse(selectedVerse);
            JOptionPane.showMessageDialog(this, "Verse updated successfully!");
            clearForm();
            loadVersesByPoem();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Verse number must be a valid integer",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating verse: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedVerse == null) {
            JOptionPane.showMessageDialog(this, "Please select a verse to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this verse?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                verseService.deleteVerse(selectedVerse.getVerseId());
                JOptionPane.showMessageDialog(this, "Verse deleted successfully!");
                clearForm();
                loadVersesByPoem();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting verse: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadVerses();
            return;
        }

        try {
            List<Verse> verses = verseService.searchVerses(keyword);
            updateTable(verses);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error searching verses: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        verseNumberField.setText("");
        textArea.setText("");
        selectedVerse = null;
        verseTable.clearSelection();
    }
}
