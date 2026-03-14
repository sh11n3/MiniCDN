package de.htwsaar.minicdn.cli.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit-Tests für reine Hilfslogik in {@link UserFileService}.
 */
class UserFileServiceTest {

    /**
     * Prüft die gleichmäßige Segmentierung inklusive Restverteilung.
     */
    @Test
    void splitIntoSegments_distributesRemainder() {
        List<UserFileService.SegmentPlan> plans = UserFileService.splitIntoSegments(10, 3);

        assertEquals(3, plans.size());
        assertEquals(0, plans.get(0).start());
        assertEquals(3, plans.get(0).end());
        assertEquals(4, plans.get(1).start());
        assertEquals(6, plans.get(1).end());
        assertEquals(7, plans.get(2).start());
        assertEquals(9, plans.get(2).end());
    }

    /**
     * Prüft die Eingabevalidierung für ungültige Dateigrößen.
     */
    @Test
    void splitIntoSegments_rejectsNonPositiveSize() {
        assertThrows(IllegalArgumentException.class, () -> UserFileService.splitIntoSegments(0, 2));
    }
}
