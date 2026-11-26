package de.digidrivelog.services;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.digidrivelog.models.Car;
import de.digidrivelog.models.Cost;
import de.digidrivelog.models.Drive;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.services.csv.CsvLocale;
import lombok.RequiredArgsConstructor;

/**
 * CSV export of all drives or costs of a single car. The files use exactly the
 * column layout {@link CsvImportService} reads, so an export can be imported
 * again: {@code ;} separated, a header row, {@code dd.MM.yyyy} dates and UTF-8
 * with a byte order mark (so spreadsheets detect the encoding). Notes are not
 * part of the format.
 */
@Service
@RequiredArgsConstructor
public class CsvExportService {

    private static final String BOM = "\uFEFF";
    private static final String DRIVE_HEADER = "km-end;firstname;lastname;date";
    private static final String COST_HEADER = "firstname;lastname;date;amount;price;category;odometer";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final CarRepository carRepository;
    private final DriveRepository driveRepository;
    private final CostRepository costRepository;

    /** A finished export: suggested download name and file content. */
    public record CsvFile(String filename, byte[] content) {}

    @Transactional(readOnly = true)
    public CsvFile exportDrives(Long carId) {
        Car car = requireCar(carId);
        List<String> lines = new ArrayList<>();
        lines.add(DRIVE_HEADER);
        for (Drive drive : driveRepository.findByCarCarIdOrderByOdometerAscDriveDateAsc(carId)) {
            lines.add(String.join(";",
                    String.valueOf(drive.getOdometer()),
                    text(drive.getDriver().getFirstname()),
                    text(drive.getDriver().getLastname()),
                    date(drive.getDriveDate())));
        }
        return toFile("drives", car, lines);
    }

    @Transactional(readOnly = true)
    public CsvFile exportCosts(Long carId, CsvLocale locale) {
        Car car = requireCar(carId);
        List<String> lines = new ArrayList<>();
        lines.add(COST_HEADER);
        for (Cost cost : costRepository.findByCarCarIdOrderByDayOfTransactionAscCostIdAsc(carId)) {
            lines.add(String.join(";",
                    text(cost.getBuyer().getFirstname()),
                    text(cost.getBuyer().getLastname()),
                    date(cost.getDayOfTransaction()),
                    decimal(cost.getQuantity(), locale),
                    decimal(cost.getPrice(), locale),
                    text(cost.getCostCategory().getName()),
                    cost.getOdometer() == null ? "" : String.valueOf(cost.getOdometer())));
        }
        return toFile("costs", car, lines);
    }

    private Car requireCar(Long carId) {
        return carRepository.findById(carId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found"));
    }

    private CsvFile toFile(String kind, Car car, List<String> lines) {
        String plate = car.getPlateNumber().replaceAll("[^A-Za-z0-9]+", "-");
        String filename = kind + "_" + plate + "_" + LocalDate.now() + ".csv";
        String content = BOM + String.join("\r\n", lines) + "\r\n";
        return new CsvFile(filename, content.getBytes(StandardCharsets.UTF_8));
    }

    /** The import knows no quoting, so separators and line breaks cannot be kept inside a value. */
    private String text(String value) {
        return value.replaceAll("[;\\t\\r\\n]+", " ");
    }

    private String date(LocalDate value) {
        return DATE_FORMAT.format(value);
    }

    /** No thousands grouping; only the decimal separator depends on the locale. */
    private String decimal(BigDecimal value, CsvLocale locale) {
        String plain = value.toPlainString();
        return locale == CsvLocale.DE ? plain.replace('.', ',') : plain;
    }
}
