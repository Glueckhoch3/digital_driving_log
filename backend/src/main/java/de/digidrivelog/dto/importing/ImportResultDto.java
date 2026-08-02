package de.digidrivelog.dto.importing;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Outcome of a CSV import. Imports are all-or-nothing: when {@code errors} is
 * non-empty nothing was persisted and {@code imported} is 0; otherwise
 * {@code imported} is the number of rows written and {@code errors} is empty.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDto {
    private int imported;
    private List<RowErrorDto> errors;
}
