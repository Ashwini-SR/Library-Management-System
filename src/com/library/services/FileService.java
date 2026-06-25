package com.library.services;

import com.library.utils.Constants;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Thin, reusable file I/O layer used by {@link LibraryService} and
 * {@link AuthService}. Knows nothing about Book/User/Transaction — it only
 * reads and writes plain text lines — which keeps file-handling concerns
 * separate from business logic (separation of concerns).
 *
 * <p>All methods use try-with-resources so streams are always closed, even
 * if an exception is thrown midway through reading or writing.</p>
 */
public final class FileService {

    private FileService() { /* utility class */ }

    /** Makes sure the data directory exists before any file inside it is touched. */
    public static void ensureDataDirectoryExists() {
        try {
            Files.createDirectories(Paths.get(Constants.DATA_DIR));
        } catch (IOException e) {
            System.err.println("Warning: could not create data directory: " + e.getMessage());
        }
    }

    /**
     * Reads every line of the given file. Returns an empty list (rather than
     * throwing) if the file does not exist yet — callers treat a missing
     * file the same as an empty one, which is exactly what we want the
     * first time the application runs.
     */
    public static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file " + filePath + ": " + e.getMessage());
        }
        return lines;
    }

    /** Overwrites the given file with exactly these lines. */
    public static void writeLines(String filePath, List<String> lines) {
        ensureDataDirectoryExists();
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing file " + filePath + ": " + e.getMessage());
        }
    }
}
