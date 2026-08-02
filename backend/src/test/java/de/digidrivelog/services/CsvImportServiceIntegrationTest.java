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

    private CarDto car;

    @BeforeEach
    void setUp() {
        costRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        UserDto stefan = createUser("Stefan", "Meyer");
        createUser("Anna", "König");
        car = carService.createCar(new CreateCarRequest("Import Car", "M-IM-1", stefan.getUserId(), null));
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
                firstname;lastname;date;amount;price;description;cost_type
                Stefan;Meyer;29.01.2021;36,89;50,50;Tanken;Var
                Anna;König;06.10.2020;1,00;2.700,00;Kaufpreis;Fix
                """;

        ImportResultDto result = csvImportService.importCosts(car.getCarId(), csv(content), CsvLocale.DE);

        assertThat(result.getErrors()).isEmpty();
        assertThat(result.getImported()).isEqualTo(2);
        List<Cost> costs = costRepository.findAll();
        assertThat(costs).anySatisfy(c -> {
            assertThat(c.getQuantity()).isEqualByComparingTo("36.89");
            assertThat(c.getPrice()).isEqualByComparingTo("50.50");
            assertThat(c.getCostType()).isEqualTo(CostType.VARIABLE);
        });
        assertThat(costs).anySatisfy(c -> {
            assertThat(c.getPrice()).isEqualByComparingTo("2700.00");
            assertThat(c.getCostType()).isEqualTo(CostType.FIXED);
        });
    }

    @Test
    void importCosts_englishNumbers_useDotAsDecimal() {
        String content = "Stefan;Meyer;2021-01-29;36.89;2,700.00;Tanken;Var\n";

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

    private UserDto createUser(String firstName, String lastName) {
        return userService.createUser(new CreateUserRequest(firstName, lastName, true, LocalDate.of(1990, 5, 12)));
    }
}
