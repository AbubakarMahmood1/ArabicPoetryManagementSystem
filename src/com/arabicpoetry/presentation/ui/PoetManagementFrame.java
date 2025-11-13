package com.arabicpoetry.presentation.ui;

import com.arabicpoetry.bll.service.PoetService;
import com.arabicpoetry.model.Poet;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Poet Management Frame - CRUD operations for poets
 */
public class PoetManagementFrame extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7248656872127107203L;
	private PoetService poetService;
    private JTable poetTable;
    private DefaultTableModel tableModel;
    private JTextField nameField, birthYearField, deathYearField, searchField;
    private JTextArea biographyArea;
    private JButton addButton, updateButton, deleteButton, clearButton, searchButton, refreshButton;

    private Poet selectedPoet = null;

    public PoetManagementFrame() throws SQLException {
        poetService = PoetService.getInstance();
        initializeComponents();
        loadPoets();
    }

    private void initializeComponents() {
        setTitle("Manage Poets");
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
        panel.setBorder(BorderFactory.createTitledBorder("Poet Details"));

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        fieldsPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField(30);
        nameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        fieldsPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Birth Year:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        birthYearField = new JTextField(30);
        birthYearField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        fieldsPanel.add(birthYearField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Death Year:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        deathYearField = new JTextField(30);
        deathYearField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        fieldsPanel.add(deathYearField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        fieldsPanel.add(new JLabel("Biography:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        biographyArea = new JTextArea(3, 30);
        biographyArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        biographyArea.setLineWrap(true);
        biographyArea.setWrapStyleWord(true);
        JScrollPane bioScrollPane = new JScrollPane(biographyArea);
        fieldsPanel.add(bioScrollPane, gbc);

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
        panel.setBorder(BorderFactory.createTitledBorder("Poets List"));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        searchPanel.add(searchField);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> handleSearch());
        searchPanel.add(searchButton);
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPoets());
        searchPanel.add(refreshButton);

        panel.add(searchPanel, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Birth Year", "Death Year"};
        tableModel = new DefaultTableModel(columns, 0) {
            /**
			 * 
			 */
			private static final long serialVersionUID = -4468267769229484825L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        poetTable = new JTable(tableModel);
        poetTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        poetTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleTableSelection();
            }
        });

        poetTable.getColumnModel().getColumn(1).setCellRenderer(new BookManagementFrame.RightAlignedTableCellRenderer());
        poetTable.getColumnModel().getColumn(2).setCellRenderer(new BookManagementFrame.RightAlignedTableCellRenderer());
        poetTable.getColumnModel().getColumn(3).setCellRenderer(new BookManagementFrame.RightAlignedTableCellRenderer());

        JScrollPane scrollPane = new JScrollPane(poetTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadPoets() {
        try {
            List<Poet> poets = poetService.getAllPoets();
            updateTable(poets);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading poets: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTable(List<Poet> poets) {
        tableModel.setRowCount(0);
        for (Poet poet : poets) {
            tableModel.addRow(new Object[]{
                    poet.getPoetId(),
                    poet.getName(),
                    poet.getBirthYear(),
                    poet.getDeathYear()
            });
        }
    }

    private void handleTableSelection() {
        int selectedRow = poetTable.getSelectedRow();
        if (selectedRow >= 0) {
            int poetId = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                selectedPoet = poetService.getPoetById(poetId);
                if (selectedPoet != null) {
                    nameField.setText(selectedPoet.getName());
                    birthYearField.setText(selectedPoet.getBirthYear());
                    deathYearField.setText(selectedPoet.getDeathYear());
                    biographyArea.setText(selectedPoet.getBiography());
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error loading poet: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleAdd() {
        try {
            Poet poet = new Poet();
            poet.setName(nameField.getText().trim());
            poet.setBirthYear(birthYearField.getText().trim());
            poet.setDeathYear(deathYearField.getText().trim());
            poet.setBiography(biographyArea.getText().trim());

            poetService.createPoet(poet);
            JOptionPane.showMessageDialog(this, "Poet added successfully!");
            clearForm();
            loadPoets();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding poet: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleUpdate() {
        if (selectedPoet == null) {
            JOptionPane.showMessageDialog(this, "Please select a poet to update");
            return;
        }

        try {
            selectedPoet.setName(nameField.getText().trim());
            selectedPoet.setBirthYear(birthYearField.getText().trim());
            selectedPoet.setDeathYear(deathYearField.getText().trim());
            selectedPoet.setBiography(biographyArea.getText().trim());

            poetService.updatePoet(selectedPoet);
            JOptionPane.showMessageDialog(this, "Poet updated successfully!");
            clearForm();
            loadPoets();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error updating poet: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleDelete() {
        if (selectedPoet == null) {
            JOptionPane.showMessageDialog(this, "Please select a poet to delete");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this poet?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                poetService.deletePoet(selectedPoet.getPoetId());
                JOptionPane.showMessageDialog(this, "Poet deleted successfully!");
                clearForm();
                loadPoets();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Error deleting poet: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadPoets();
            return;
        }

        try {
            List<Poet> poets = poetService.searchPoets(keyword);
            updateTable(poets);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error searching poets: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearForm() {
        nameField.setText("");
        birthYearField.setText("");
        deathYearField.setText("");
        biographyArea.setText("");
        selectedPoet = null;
        poetTable.clearSelection();
    }
}
