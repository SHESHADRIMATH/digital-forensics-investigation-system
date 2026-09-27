package com.digitalforensics.util;

import com.digitalforensics.model.EvidenceType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** File-handling helpers for evidence storage. */
public final class FileUtil {

    public static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("log", "txt", "csv", "pdf", "docx", "png", "jpg", "jpeg");

    /** Extensions whose contents can be read line by line in Log Analysis. */
    public static final Set<String> TEXT_EXTENSIONS = Set.of("log", "txt", "csv");

    private static final Map<String, EvidenceType> TYPE_BY_EXTENSION = Map.of(
            "log", EvidenceType.LOG,
            "txt", EvidenceType.TEXT,
            "csv", EvidenceType.CSV,
            "pdf", EvidenceType.DOCUMENT,
            "docx", EvidenceType.DOCUMENT,
            "png", EvidenceType.IMAGE,
            "jpg", EvidenceType.IMAGE,
            "jpeg", EvidenceType.IMAGE);

    private FileUtil() {
    }

    public static String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public static EvidenceType guessType(String fileName) {
        return TYPE_BY_EXTENSION.getOrDefault(extension(fileName), EvidenceType.TEXT);
    }

    /**
     * Strips any folder part and unsafe characters from an uploaded name.
     * Stops "path traversal" attacks such as a file named ..\..\web.xml
     */
    public static String sanitize(String fileName) {
        String name = Paths.get(fileName).getFileName().toString();
        return name.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    /** Folder where evidence files are stored; created on first use. */
    public static Path storageDir() {
        Path dir = Paths.get(DBConnection.getSetting("evidence.dir", "evidence-storage")).toAbsolutePath();
        try {
            Files.createDirectories(dir);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Cannot create evidence storage folder " + dir, e);
        }
        return dir;
    }

    public static String humanSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.2f MB", bytes / (1024.0 * 1024));
    }
}
