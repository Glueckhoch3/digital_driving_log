package de.digidrivelog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import de.digidrivelog.dto.importing.ImportResultDto;
import de.digidrivelog.services.CsvImportService;
import de.digidrivelog.services.csv.CsvLocale;
import lombok.RequiredArgsConstructor;

/**
 * CSV bulk-import endpoints, scoped to a car (the CSV files carry no car of their
 * own). Each returns {@code 200 OK} with an {@link ImportResultDto}: on success
 * {@code imported} is the row count and {@code errors} is empty; when rows fail
 * validation the body lists the per-line errors and nothing was persisted.
 * Preconditions that aren't about row content — unknown car, unreadable/empty
 * file — surface as the usual 4xx ProblemDetail responses.
 */
@RestController
@RequestMapping("/ddl/api")
@RequiredArgsConstructor
public class ImportController {

    private final CsvImportService csvImportService;

    @PostMapping(value = "/vehicles/{carId}/drives/import", consumes = "multipart/form-data")
    public ResponseEntity<ImportResultDto> importDrives(
            @PathVariable Long carId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "locale", defaultValue = "en") String locale) {
        return ResponseEntity.ok(csvImportService.importDrives(carId, file, CsvLocale.from(locale)));
    }

    @PostMapping(value = "/vehicles/{carId}/costs/import", consumes = "multipart/form-data")
    public ResponseEntity<ImportResultDto> importCosts(
            @PathVariable Long carId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "locale", defaultValue = "en") String locale) {
        return ResponseEntity.ok(csvImportService.importCosts(carId, file, CsvLocale.from(locale)));
    }
}
