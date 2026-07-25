package de.digidrivelog.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Central CORS configuration for the REST API. The allowed origins come from
 * the ALLOWED_ORIGINS environment variable (see .env.example / docker-compose.yml)
 * instead of a wildcard on each controller.
 *
 * <p>ALLOWED_ORIGINS is a comma-separated list. Each entry may be an exact origin
 * ({@code http://localhost:4200}) or a host pattern with {@code *} placeholders
 * ({@code http://192.168.178.*:4200}). The port must always be fixed - a wildcard
 * port ({@code :[*]}) is rejected at startup.
 * The legacy single-value ALLOWED_ORIGIN variable is still honoured as a fallback.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String WILDCARD_PORT = ":[*]";

    @Value("${ALLOWED_ORIGINS:${ALLOWED_ORIGIN:http://localhost:4200}}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        String[] origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);

        // An empty list would leave Spring's permissive defaults (allowedOrigins = "*")
        // in place, so refuse to start instead of silently allowing every origin.
        if (origins.length == 0) {
            throw new IllegalStateException(
                    "ALLOWED_ORIGINS is empty. Configure at least one origin, "
                            + "e.g. ALLOWED_ORIGINS=http://localhost:4200");
        }

        for (String origin : origins) {
            if (origin.contains(WILDCARD_PORT)) {
                throw new IllegalStateException(
                        "Invalid ALLOWED_ORIGINS entry '" + origin + "': the port must be fixed, "
                                + "a wildcard port ':[*]' is not allowed.");
            }
        }

        registry.addMapping("/ddl/api/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
