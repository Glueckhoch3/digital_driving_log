package de.digidrivelog.services.csv;

/**
 * Thrown for a single malformed CSV value. The service catches it per row and
 * turns it into a {@link de.digidrivelog.dto.importing.RowErrorDto}, so the
 * message is user-facing and should name the offending value.
 */
public class CsvParseException extends RuntimeException {
    public CsvParseException(String message) {
        super(message);
    }
}
