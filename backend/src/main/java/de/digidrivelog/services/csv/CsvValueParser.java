package de.digidrivelog.services.csv;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import de.digidrivelog.models.CostType;

/**
 * Stateless parsing of the raw string fields found in the import CSVs into the
 * domain types. Every method throws {@link CsvParseException} with a
 * user-facing message when the input cannot be read.
 */
public final class CsvValueParser {

    private CsvValueParser() {}

    /** Accepted date formats, tried in order. Covers the mixed {@code dd.MM.yy} / {@code dd.MM.yyyy} in the sample files plus ISO. */
    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE);

    /** Parse a whole-number field such as the drive odometer. */
    public static int parseInteger(String raw, String fieldLabel) {
        String value = raw == null ? "" : raw.trim();
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new CsvParseException(fieldLabel + " '" + raw + "' is not a whole number");
        }
    }

    /** Parse a decimal field ({@code amount}, {@code price}) using the file's number format. */
    public static BigDecimal parseDecimal(String raw, CsvLocale locale, String fieldLabel) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) {
            throw new CsvParseException(fieldLabel + " is missing");
        }
        // Strip the thousands separator, then normalise the decimal separator to '.'.
        String normalised = locale == CsvLocale.DE
                ? value.replace(".", "").replace(',', '.')
                : value.replace(",", "");
        try {
            return new BigDecimal(normalised);
        } catch (NumberFormatException e) {
            String expected = locale == CsvLocale.DE ? "German" : "English";
            throw new CsvParseException("Could not read " + fieldLabel + " '" + raw + "' as a " + expected + " number");
        }
    }

    /** Parse a date field accepting the formats in {@link #DATE_FORMATS}. */
    public static LocalDate parseDate(String raw, String fieldLabel) {
        String value = raw == null ? "" : raw.trim();
        for (DateTimeFormatter format : DATE_FORMATS) {
            try {
                return LocalDate.parse(value, format);
            } catch (DateTimeParseException ignored) {
                // try the next format
            }
        }
        throw new CsvParseException(fieldLabel + " '" + raw + "' is not a valid date (expected e.g. 27.12.2020)");
    }

    /** Map the {@code cost_type} column: {@code Fix[ed]} -> FIXED, {@code Var[iable]} -> VARIABLE (case/space-insensitive). */
    public static CostType parseCostType(String raw) {
        String value = raw == null ? "" : raw.trim().toUpperCase();
        if (value.startsWith("FIX")) {
            return CostType.FIXED;
        }
        if (value.startsWith("VAR")) {
            return CostType.VARIABLE;
        }
        throw new CsvParseException("cost_type '" + raw + "' must be Fix or Var");
    }
}
