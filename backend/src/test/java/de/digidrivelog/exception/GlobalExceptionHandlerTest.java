package de.digidrivelog.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleUnexpected_shouldReturnGenericMessageWithoutLeakingExceptionDetails() {
        RuntimeException ex = new RuntimeException("connection to db://internal-host failed: secret-token=abc123");

        ProblemDetail problem = handler.handleUnexpected(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail()).isEqualTo("An unexpected error occurred");
        assertThat(problem.getDetail()).doesNotContain("secret-token", "db://internal-host", "RuntimeException");
    }

    @Test
    void handleDataIntegrity_shouldNameThePlateNumberConflict() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement",
                new RuntimeException("duplicate key value violates unique constraint \"idx_car_plate_number\""));

        ProblemDetail problem = handler.handleDataIntegrity(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getDetail()).isEqualTo("Plate number already in use");
    }

    @Test
    void handleDataIntegrity_shouldFallBackToGenericMessageForOtherConstraints() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException(
                "could not execute statement", new RuntimeException("some other constraint violation"));

        ProblemDetail problem = handler.handleDataIntegrity(ex);

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getDetail()).isEqualTo("Request violates a data integrity constraint");
    }
}
