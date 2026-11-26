package de.digidrivelog.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.importing.ImportResultDto;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.models.Cost;
import de.digidrivelog.models.CostType;
import de.digidrivelog.models.Drive;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.repositories.UserRepository;
import de.digidrivelog.services.csv.CsvLocale;

@SpringBootTest
@ActiveProfiles("test")
class CsvImportServiceIntegrationTest {

    @Autowired
    private CsvImportService csvImportService;
    @Autowired
    private CsvExportService csvExportService;
    @Autowired
    private CarService carService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private DriveRepository driveRepository;
    @Autowired
    private CostRepository costRepository;
    @Autowired
    private de.digidrivelog.repositories.CostCategoryRepository costCategoryRepository;

    private CarDto car;

    @BeforeEach
    void setUp() {
        costRepository.deleteAll();
        costCategoryRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        UserDto stefan = createUser("Stefan", "Meyer");
        createUser("Anna", "König");
        costCategoryRepository.save(new de.digidrivelog.models.CostCategory(
                null, "Tanken", CostType.VARIABLE, "Liter", true, true, null, null));
        costCategoryRepository.save(new de.digidrivelog.models.CostCategory(
                null, "Kaufpreis", CostType.FIXED, "pcs", false, false, null, null));
        car = carService.createCar(new CreateCarRequest("Import Car", "M-IM-1", stefan.getUserId(), null));
    }

    private Long categoryId(String name) {
        return costCategoryRepository.findByNameIgnoreCase(name).orElseThrow().getCostCategoryId();
    }

    private MultipartFile csv(String content) {
        return new MockMultipartFile("file", "data.csv", "text/csv", content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void importDrives_withHeaderAndMixedDates_persistsAll() {
        String content = """
                km-end;firstname;lastname;date
                146739;Stefan;Meyer;27.12.2020
                146766;Anna;König;28.12.2020
                """;

        ImportResultDto result = csvImportService.importDrives(car.getCarId(), csv(content), CsvLocale.DE);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getImported()).isEqualTo(2);
        List<Drive> drives = driveRepository.findAll();
        assertThat(drives).hasSize(2);
        assertThat(drives).anySatisfy(d -> {
            assertThat(d.getOdometer()).isEqualTo(146739);
            assertThat(d.getDriveDate()).isEqualTo(LocalDate.of(2020, 12, 27));
        });
    }

    @Test
    void importCosts_germanNumbers_parseDecimalsAndTypes() {
        String content = """
                firstname;lastname;date;amount;price;description;odometer
                Stefan;Meyer;29.01.2021;36,89;50,50;tanken;146739
                Anna;König;06.10.2020;1,00;2.700,00;Kaufpreis;
                """;

        ImportResultDto result = csvImportService.importCosts(car.getCarId(), csv(content), CsvLocale.DE);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getImported()).isEqualTo(2);
        List<Cost> costs = costRepository.findAll();
        assertThat(costs).anySatisfy(c -> {
            assertThat(c.getQuantity()).isEqualByComparingTo("36.89");
            assertThat(c.getPrice()).isEqualByComparingTo("50.50");
            assertThat(c.getCostCategory().getCostCategoryId()).isEqualTo(categoryId("Tanken"));
            assertThat(c.getOdometer()).isEqualTo(146739);
        });
        assertThat(costs).anySatisfy(c -> {
            assertThat(c.getPrice()).isEqualByComparingTo("2700.00");
            assertThat(c.getCostCategory().getCostCategoryId()).isEqualTo(categoryId("Kaufpreis"));
            assertThat(c.getOdometer()).isNull();
        });
    }

    @Test
    void importCosts_tabSeparatedSpreadsheetExport_isAccepted() {
        // columns: Firstname, Lastname, Datum, Anzahl, Preis, Beschreibung (no odometer), tab-separated
        String content = "\uFEFFFirstname\tLastname\tDatum\tAnzahl\tPreis\tBeschreibung\n"
                + "Stefan\tMeyer\t06.10.2020\t1\t2700\tKaufpreis\n"
                + "Anna\tKönig\t17.10.2020\t1\t160,77\tKaufpreis\n";

        ImportResultDto result = csvImportService.importCosts(car.getCarId(), csv(content), CsvLocale.DE);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getImported()).isEqualTo(2);
        assertThat(costRepository.findAll()).anySatisfy(c -> {
            assertThat(c.getPrice()).isEqualByComparingTo("160.77");
            assertThat(c.getDayOfTransaction()).isEqualTo(LocalDate.of(2020, 10, 17));
            assertThat(c.getOdometer()).isNull();
        });
    }

    @Test
    void importDrives_tabSeparated_isAccepted() {
        String content = "km-end\tfirstname\tlastname\tdate\n146739\tStefan\tMeyer\t27.12.2020\n";

        ImportResultDto result = csvImportService.importDrives(car.getCarId(), csv(content), CsvLocale.DE);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getImported()).isEqualTo(1);
    }

    @Test
    void importCosts_tabSeparatedWithWrongColumnCount_reportsLine() {
        String content = "Stefan\tMeyer\t06.10.2020\t1\n";

        ImportResultDto result = csvImportService.importCosts(car.getCarId(), csv(content), CsvLocale.DE);

        assertThat(result.getImported()).isZero();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().get(0).getLine()).isEqualTo(1);
    }

    @Test
    void importCosts_unknownCategory_isRowError() {
        ImportResultDto result = csvImportService.importCosts(car.getCarId(),
                csv("Stefan;Meyer;29.01.2021;1,00;5,00;Unbekannt;\n"), CsvLocale.DE);

        assertThat(result.getImported()).isZero();
        assertThat(result.getErrors()).singleElement()
                .satisfies(e -> assertThat(e.getMessage()).contains("Unbekannt"));
        assertThat(costRepository.findAll()).isEmpty();
    }

    @Test
    void importCosts_missingRequiredOdometer_isRowError() {
        ImportResultDto result = csvImportService.importCosts(car.getCarId(),
                csv("Stefan;Meyer;29.01.2021;36,89;50,50;Tanken\n"), CsvLocale.DE);

        assertThat(result.getImported()).isZero();
        assertThat(result.getErrors()).hasSize(1);
    }

    @Test
    void importCosts_englishNumbers_useDotAsDecimal() {
        String content = "Stefan;Meyer;2021-01-29;36.89;2,700.00;Tanken;150000\n";

        ImportResultDto result = csvImportService.importCosts(car.getCarId(), csv(content), CsvLocale.EN);

        assertThat(result.getErrors()).isEmpty();
        assertThat(costRepository.findAll()).singleElement().satisfies(c -> {
            assertThat(c.getQuantity()).isEqualByComparingTo("36.89");
            assertThat(c.getPrice()).isEqualByComparingTo("2700.00");
        });
    }

    @Test
    void import_isAllOrNothing_whenAnyRowInvalid() {
        String content = """
                146739;Stefan;Meyer;27.12.2020
                146766;Max;Bauer;28.12.2020
                not-a-number;Anna;König;29.12.2020
                """;

        ImportResultDto result = csvImportService.importDrives(car.getCarId(), csv(content), CsvLocale.DE);

        assertThat(result.getImported()).isZero();
        assertThat(result.getErrors()).hasSize(2);
        assertThat(result.getErrors()).anySatisfy(e -> {
            assertThat(e.getLine()).isEqualTo(2);
            assertThat(e.getMessage()).contains("Max Bauer");
        });
        assertThat(driveRepository.findAll()).isEmpty();
    }

    @Test
    void import_unknownCar_throwsNotFound() {
        assertThatThrownBy(() -> csvImportService.importDrives(999_999L, csv("146739;Stefan;Meyer;27.12.2020"), CsvLocale.DE))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void import_emptyFile_throwsBadRequest() {
        assertThatThrownBy(() -> csvImportService.importCosts(car.getCarId(), csv(""), CsvLocale.DE))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void exportDrives_writesImportFormat_thatImportsAgain() {
        String rows = "km-end;firstname;lastname;date\r\n"
                + "146739;Stefan;Meyer;27.12.2020\r\n"
                + "146766;Anna;König;28.12.2020\r\n";
        csvImportService.importDrives(car.getCarId(), csv(rows), CsvLocale.EN);

        CsvExportService.CsvFile file = csvExportService.exportDrives(car.getCarId());

        String content = new String(file.content(), StandardCharsets.UTF_8);
        assertThat(content).isEqualTo("\uFEFF" + rows);
        assertThat(file.filename()).startsWith("drives_M-IM-1_").endsWith(".csv");

        driveRepository.deleteAll();
        ImportResultDto result = csvImportService.importDrives(car.getCarId(), csv(content), CsvLocale.EN);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getImported()).isEqualTo(2);
    }

    @Test
    void exportCosts_usesLocaleDecimals_andImportsAgain() {
        String rows = "firstname;lastname;date;amount;price;category;odometer\r\n"
                + "Anna;König;06.10.2020;1,00;2700,00;Kaufpreis;\r\n"
                + "Stefan;Meyer;29.01.2021;36,89;50,50;Tanken;146739\r\n";
        csvImportService.importCosts(car.getCarId(), csv(rows), CsvLocale.DE);

        String german = new String(
                csvExportService.exportCosts(car.getCarId(), CsvLocale.DE).content(), StandardCharsets.UTF_8);
        String english = new String(
                csvExportService.exportCosts(car.getCarId(), CsvLocale.EN).content(), StandardCharsets.UTF_8);

        assertThat(german).isEqualTo("\uFEFF" + rows);
        assertThat(english).contains("Stefan;Meyer;29.01.2021;36.89;50.50;Tanken;146739");

        costRepository.deleteAll();
        ImportResultDto result = csvImportService.importCosts(car.getCarId(), csv(english), CsvLocale.EN);
        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getImported()).isEqualTo(2);
    }

    @Test
    void export_unknownCar_throwsNotFound() {
        assertThatThrownBy(() -> csvExportService.exportDrives(999_999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    private UserDto createUser(String firstName, String lastName) {
        return userService.createUser(new CreateUserRequest(firstName, lastName, true, LocalDate.of(1990, 5, 12)));
    }
}
