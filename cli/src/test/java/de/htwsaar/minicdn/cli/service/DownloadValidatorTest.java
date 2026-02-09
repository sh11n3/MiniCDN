package de.htwsaar.minicdn.cli.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DownloadValidatorTest {

    @Test
    void normalizeRemotePath_stripsPrefixesAndSlashes() {
        String result = DownloadValidator.normalizeRemotePath("/origin/data/docs/file.txt");
        assertEquals("docs/file.txt", result);
    }

    @Test
    void normalizeRemotePath_rejectsTraversal() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> DownloadValidator.normalizeRemotePath("../a"));
        assertEquals("Remote-Pfad enthält ungültige Segmente (.. oder .).", ex.getMessage());
    }

    @Test
    void validateOutputPath_acceptsNewPath() {
        assertDoesNotThrow(() -> DownloadValidator.validateOutputPath(Path.of("build/output.txt"), true));
    }

    @Test
    void validateRegion_requiresValue() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class, () -> DownloadValidator.validateRegion(" "));
        assertEquals("Region darf nicht leer sein.", ex.getMessage());
    }
}
