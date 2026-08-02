package de.digidrivelog.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

    private final CorsRegistry registry = new CorsRegistry();

    private static CorsConfig configuredWith(String allowedOrigins) {
        CorsConfig config = new CorsConfig();
        ReflectionTestUtils.setField(config, "allowedOrigins", allowedOrigins);
        return config;
    }

    @Test
    void wildcardPortIsRejected() {
        CorsConfig config = configuredWith("http://192.168.178.*:[*]");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> config.addCorsMappings(registry));

        assertTrue(exception.getMessage().contains("port must be fixed"), exception.getMessage());
    }

    @Test
    void wildcardPortIsRejectedEvenWhenOtherEntriesAreValid() {
        CorsConfig config = configuredWith("http://localhost:4200, http://192.168.178.5:[*]");

        assertThrows(IllegalStateException.class, () -> config.addCorsMappings(registry));
    }

    @Test
    void emptyValueIsRejectedInsteadOfFallingBackToAllowAll() {
        for (String blank : new String[] {"", "   ", ",,", " , "}) {
            CorsConfig config = configuredWith(blank);

            IllegalStateException exception = assertThrows(IllegalStateException.class,
                    () -> config.addCorsMappings(registry),
                    "expected '" + blank + "' to be rejected");

            assertTrue(exception.getMessage().contains("ALLOWED_ORIGINS is empty"),
                    exception.getMessage());
        }
    }

    @Test
    void fixedPortsAndHostWildcardsAreAccepted() {
        CorsConfig config = configuredWith(
                "http://localhost:4200, http://192.168.178.*:4200, https://ddl.example.com");

        assertDoesNotThrow(() -> config.addCorsMappings(registry));
    }
}
