package de.htwsaar.minicdn.cli.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class DownloadValidator {

    private static final List<String> FORBIDDEN_REMOTE_SEGMENTS = List.of("..", ".");

    private DownloadValidator() {}

    public static String normalizeRemotePath(String rawPath) {
        if (rawPath == null || rawPath.isBlank()) {
            throw new IllegalArgumentException("Remote-Pfad darf nicht leer sein.");
        }

        String cleanPath = rawPath.trim();
        if (cleanPath.startsWith("/")) {
            cleanPath = cleanPath.substring(1);
        }

        cleanPath = cleanPath.replaceFirst("^(origin/)?(data/)?", "");

        if (cleanPath.isBlank()) {
            throw new IllegalArgumentException("Remote-Pfad ist nach der Normalisierung leer.");
        }

        if (cleanPath.contains("\\")) {
            throw new IllegalArgumentException("Remote-Pfad darf keine Backslashes enthalten.");
        }

        String[] segments = cleanPath.split("/");
        boolean hasTraversal = Arrays.stream(segments)
                .anyMatch(segment -> FORBIDDEN_REMOTE_SEGMENTS.contains(segment));
        if (hasTraversal) {
            throw new IllegalArgumentException("Remote-Pfad enthält ungültige Segmente (.. oder .).");
        }

        return cleanPath;
    }

    public static void validateOutputPath(Path outputPath, boolean overwrite) {
        if (outputPath == null) {
            throw new IllegalArgumentException("Output-Pfad darf nicht leer sein.");
        }

        String pathString = outputPath.toString();
        if (pathString.isBlank()) {
            throw new IllegalArgumentException("Output-Pfad darf nicht leer sein.");
        }

        if (pathString.contains(".." + System.getProperty("file.separator"))) {
            throw new IllegalArgumentException("Output-Pfad darf keine Traversal-Segmente enthalten.");
        }

        if (Files.exists(outputPath) && !overwrite) {
            throw new IllegalArgumentException(
                    "Output-Datei existiert bereits. Verwende --overwrite, um sie zu ersetzen.");
        }

        if (Files.exists(outputPath) && Files.isDirectory(outputPath)) {
            throw new IllegalArgumentException("Output-Pfad darf kein Verzeichnis sein.");
        }
    }

    public static String validateRegion(String region) {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region darf nicht leer sein.");
        }
        return region.trim();
    }
}
