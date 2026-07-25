package de.digidrivelog.dto.importing;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A single row that failed CSV validation. {@code line} is 1-based and counts
 * every physical line in the uploaded file (including a header row, if present),
 * so it matches what the user sees in a spreadsheet editor.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RowErrorDto {
    private int line;
    private String message;
}
