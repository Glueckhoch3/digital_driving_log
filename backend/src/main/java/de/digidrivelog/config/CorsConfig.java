package de.digidrivelog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Central CORS configuration for the REST API. The allowed origin comes from
 * the ALLOWED_ORIGIN environment variable (see .env.example / docker-compose.yml)
 * instead of a wildcard on each controller.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${ALLOWED_ORIGIN:http://localhost:4200}")
    private String allowedOrigin;

    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/ddl/api/**")
                .allowedOrigins(allowedOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }
}
