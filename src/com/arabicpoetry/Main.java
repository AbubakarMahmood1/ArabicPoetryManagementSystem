package com.arabicpoetry;

import com.arabicpoetry.presentation.fx.ArabicPoetryApp;
import javafx.application.Application;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Main entry point for Arabic Poetry Management System
 */
public class Main {
    public static void main(String[] args) {
        Path logDir = initializeLogging();
        try {
            Application.launch(ArabicPoetryApp.class, args);
        } catch (Throwable throwable) {
            writeCrashLog(logDir, throwable);
            throw throwable;
        }
    }

    private static Path initializeLogging() {
        String configured = System.getProperty("arabicpoetry.log.dir");
        Path logDir;
        if (configured != null && !configured.trim().isEmpty()) {
            logDir = Path.of(configured.trim());
        } else {
            String localAppData = System.getenv("LOCALAPPDATA");
            Path baseDir = (localAppData != null && !localAppData.trim().isEmpty())
                ? Path.of(localAppData.trim())
                : Path.of(System.getProperty("user.home"));
            logDir = baseDir.resolve("ArabicPoetry").resolve("logs");
            System.setProperty("arabicpoetry.log.dir", logDir.toString());
        }

        try {
            Files.createDirectories(logDir);
        } catch (Exception ignored) {
            // Best effort: if we can't create a folder, the app can still try to run.
        }

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> writeCrashLog(logDir, throwable));
        return logDir;
    }

    private static void writeCrashLog(Path logDir, Throwable throwable) {
        try {
            Path crashLog = logDir.resolve("crash.log");
            StringWriter stringWriter = new StringWriter();
            throwable.printStackTrace(new PrintWriter(stringWriter));
            String message = "=== Uncaught exception ===" + System.lineSeparator()
                + "Thread: " + Thread.currentThread().getName() + System.lineSeparator()
                + stringWriter + System.lineSeparator();
            Files.writeString(crashLog, message, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ignored) {
            // Avoid recursive failures while trying to report a crash.
        }
    }
}
