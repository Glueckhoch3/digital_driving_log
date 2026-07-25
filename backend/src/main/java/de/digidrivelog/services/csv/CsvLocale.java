package de.digidrivelog.services.csv;

/**
 * Number format of an uploaded CSV. Determines how {@code amount} and
 * {@code price} are read:
 * <ul>
 *   <li>{@link #DE}: {@code .} groups thousands, {@code ,} is the decimal separator ({@code 2.700,00}).</li>
 *   <li>{@link #EN}: {@code ,} groups thousands, {@code .} is the decimal separator ({@code 2,700.00}).</li>
 * </ul>
 */
public enum CsvLocale {
    DE,
    EN;

    /** Case-insensitive lookup used by the controller; defaults to {@link #EN} when blank or unrecognised. */
    public static CsvLocale from(String value) {
        if (value == null || value.isBlank()) {
            return EN;
        }
        try {
            return CsvLocale.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return EN;
        }
    }
}
