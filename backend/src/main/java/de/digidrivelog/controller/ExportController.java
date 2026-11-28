package de.digidrivelog.controller;

import java.nio.charset.StandardCharsets;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.digidrivelog.services.CsvExportService;
import de.digidrivelog.services.CsvExportService.CsvFile;
import de.digidrivelog.services.csv.CsvLocale;
import lombok.RequiredArgsConstructor;

/**
 * CSV download endpoints, the counterpart of {@link ImportController}: same
 * car scope, same column layout, same {@code locale} parameter.
 */
@RestController
@RequestMapping("/ddl/api")
@RequiredArgsConstructor
public class ExportController {

    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final CsvExportService csvExportService;

    @GetMapping("/vehicles/{carId}/drives/export")
    public ResponseEntity<byte[]> exportDrives(@PathVariable Long carId) {
        return download(csvExportService.exportDrives(carId));
    }

    @GetMapping("/vehicles/{carId}/costs/export")
    public ResponseEntity<byte[]> exportCosts(
            @PathVariable Long carId,
            @RequestParam(value = "locale", defaultValue = "en") String locale) {
        return download(csvExportService.exportCosts(carId, CsvLocale.from(locale)));
    }

    private ResponseEntity<byte[]> download(CsvFile file) {
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(file.filename()).build().toString())
                .body(file.content());
    }
}
