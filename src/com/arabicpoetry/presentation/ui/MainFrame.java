package com.arabicpoetry.presentation.ui;

import com.arabicpoetry.bll.service.AuthenticationService;
import com.arabicpoetry.bll.service.ImportService;
import com.arabicpoetry.model.User;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Main application window with menu for accessing all features
 */
public class MainFrame extends JFrame {
    private AuthenticationService authService;
    private ImportService importService;

    public MainFrame() {
        authService = AuthenticationService.getInstance();
        importService = ImportService.getInstance();
        initializeComponents();
    }

    private void initializeComponents() {
        User currentUser = authService.getCurrentUser();
        setTitle("Arabic Poetry Management System - " + (currentUser != null ? currentUser.getFullName() : ""));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        // Create menu bar
        JMenuBar menuBar = new JMenuBar();

        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem importItem = new JMenuItem("Import Poems from File");
        importItem.addActionListener(e -> handleImport());
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(importItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Manage menu
        JMenu manageMenu = new JMenu("Manage");
        JMenuItem booksItem = new JMenuItem("Manage Books");
        booksItem.addActionListener(e -> new BookManagementFrame().setVisible(true));
        JMenuItem poetsItem = new JMenuItem("Manage Poets");
        poetsItem.addActionListener(e -> new PoetManagementFrame().setVisible(true));
        JMenuItem poemsItem = new JMenuItem("Manage Poems");
        poemsItem.addActionListener(e -> new PoemManagementFrame().setVisible(true));
        JMenuItem versesItem = new JMenuItem("Manage Verses");
        versesItem.addActionListener(e -> new VerseManagementFrame().setVisible(true));

        manageMenu.add(booksItem);
        manageMenu.add(poetsItem);
        manageMenu.add(poemsItem);
        manageMenu.add(versesItem);

        // Help menu
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAbout());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(manageMenu);
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // Main panel with welcome message
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel(
                "<html><div style='text-align: center;'>" +
                        "<h1>Welcome to Arabic Poetry Management System</h1>" +
                        "<p>Use the menu above to manage books, poets, poems, and verses.</p>" +
                        "<p>You can also import poems from text files.</p>" +
                        "</div></html>",
                SwingConstants.CENTER
        );
        mainPanel.add(welcomeLabel, BorderLayout.CENTER);

        // Button panel for quick access
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 100, 20, 100));

        JButton booksButton = new JButton("Manage Books");
        booksButton.setFont(new Font("Arial", Font.BOLD, 14));
        booksButton.addActionListener(e -> new BookManagementFrame().setVisible(true));

        JButton poetsButton = new JButton("Manage Poets");
        poetsButton.setFont(new Font("Arial", Font.BOLD, 14));
        poetsButton.addActionListener(e -> new PoetManagementFrame().setVisible(true));

        JButton poemsButton = new JButton("Manage Poems");
        poemsButton.setFont(new Font("Arial", Font.BOLD, 14));
        poemsButton.addActionListener(e -> new PoemManagementFrame().setVisible(true));

        JButton versesButton = new JButton("Manage Verses");
        versesButton.setFont(new Font("Arial", Font.BOLD, 14));
        versesButton.addActionListener(e -> new VerseManagementFrame().setVisible(true));

        buttonPanel.add(booksButton);
        buttonPanel.add(poetsButton);
        buttonPanel.add(poemsButton);
        buttonPanel.add(versesButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void handleImport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setDialogTitle("Select Poem Text File");

        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            // Show progress dialog
            JDialog progressDialog = new JDialog(this, "Importing...", true);
            JLabel progressLabel = new JLabel("Importing poems from file...", SwingConstants.CENTER);
            progressDialog.add(progressLabel);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(this);

            // Import in background thread
            SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
                @Override
                protected String doInBackground() throws Exception {
                    return importService.importFromFile(selectedFile.getAbsolutePath());
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        String message = get();
                        JOptionPane.showMessageDialog(MainFrame.this,
                                message,
                                "Import Successful",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Import failed: " + ex.getMessage(),
                                "Import Error",
                                JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            };

            worker.execute();
            progressDialog.setVisible(true);
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
                "Arabic Poetry Management System\n" +
                        "Version 1.0\n\n" +
                        "A system for managing classical Arabic poetry\n" +
                        "with support for books, poets, poems, and verses.",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
