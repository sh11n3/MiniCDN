package de.htwsaar.minicdn.common.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.UUID;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Unit-Tests für {@link TraceIdFilter}.
 *
 * <p>Diese Tests sichern die zentralen Tracing-Anforderungen ab:
 * Header-Übernahme, Header-Erzeugung und Cleanup des MDC-Kontexts.
 */
class TraceIdFilterTest {

    /**
     * Wenn ein Client bereits eine Trace-ID mitsendet, muss diese unverändert
     * in Response-Header und MDC weiterverwendet werden.
     */
    @Test
    void shouldReuseIncomingTraceId() throws ServletException, IOException {
        TraceIdFilter filter = new TraceIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(TraceIdFilter.TRACE_ID_HEADER, "test-trace-123");

        FilterChain chain = (req, resp) -> assertEquals("test-trace-123", MDC.get(TraceIdFilter.TRACE_ID_KEY));

        filter.doFilter(request, response, chain);

        assertEquals("test-trace-123", response.getHeader(TraceIdFilter.TRACE_ID_HEADER));
        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());
    }

    /**
     * Wenn keine Trace-ID geliefert wird, erzeugt der Filter eine UUID,
     * schreibt sie in den Response-Header und räumt den MDC danach auf.
     */
    @Test
    void shouldCreateTraceIdWhenHeaderIsMissing() throws ServletException, IOException {
        TraceIdFilter filter = new TraceIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        FilterChain chain = (req, resp) -> {
            String traceIdFromMdc = MDC.get(TraceIdFilter.TRACE_ID_KEY);
            assertNotNull(traceIdFromMdc);
            assertFalse(traceIdFromMdc.isBlank());
            assertDoesNotThrowUuidParse(traceIdFromMdc);
        };

        filter.doFilter(request, response, chain);

        String traceIdFromHeader = response.getHeader(TraceIdFilter.TRACE_ID_HEADER);
        assertNotNull(traceIdFromHeader);
        assertDoesNotThrowUuidParse(traceIdFromHeader);
        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());
    }

    /**
     * Kleine Hilfsmethode für klaren Test-Intent ohne zusätzliche Assert-Libraries.
     */
    private static void assertDoesNotThrowUuidParse(String traceId) {
        UUID.fromString(traceId);
    }
}
