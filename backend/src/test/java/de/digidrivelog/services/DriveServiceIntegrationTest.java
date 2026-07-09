package de.digidrivelog.services;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.drive.CreateDriveRequest;
import de.digidrivelog.dto.drive.DriveDto;
import de.digidrivelog.dto.drive.UpdateDriveRequest;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class DriveServiceIntegrationTest {

    @Autowired
    private DriveService driveService;

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
    private UserDto driver;

    @BeforeEach
    void setUp() {
        costRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        driver = createUser("Anna", "Driver");
        car = carService.createCar(new CreateCarRequest(
                "Shared Car",
                "M-DR-100",
                driver.getUserId(),
                null
        ));
    }

    @Test
    void createDrive_shouldPersistAllFields() {
        DriveDto created = driveService.createDrive(new CreateDriveRequest(
                car.getCarId(),
                42,
                driver.getUserId(),
                LocalDate.of(2025, 3, 14),
                "commute"
        ));

        assertThat(created.getDriveId()).isNotNull();
        assertThat(created.getCarId()).isEqualTo(car.getCarId());
        assertThat(created.getDriverId()).isEqualTo(driver.getUserId());
        assertThat(created.getDistance()).isEqualTo(42);
        assertThat(created.getDriveDate()).isEqualTo(LocalDate.of(2025, 3, 14));
        assertThat(created.getNotes()).isEqualTo("commute");
    }

    @Test
    void createDriveWithUnknownCar_shouldThrowNotFound() {
        assertThatThrownBy(() -> driveService.createDrive(new CreateDriveRequest(
                999_999L,
                10,
                driver.getUserId(),
                LocalDate.of(2025, 1, 1),
                null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createDriveWithUnknownDriver_shouldThrowNotFound() {
        assertThatThrownBy(() -> driveService.createDrive(new CreateDriveRequest(
                car.getCarId(),
                10,
                999_999L,
                LocalDate.of(2025, 1, 1),
                null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getDriveById_shouldReturnDrive() {
        DriveDto created = createDrive(15, "short trip");

        DriveDto fetched = driveService.getDriveById(created.getDriveId());

        assertThat(fetched.getDriveId()).isEqualTo(created.getDriveId());
        assertThat(fetched.getDistance()).isEqualTo(15);
        assertThat(fetched.getNotes()).isEqualTo("short trip");
    }

    @Test
    void getDriveByUnknownId_shouldThrowNotFound() {
        assertThatThrownBy(() -> driveService.getDriveById(999_999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateDrive_shouldChangeAllEditableFields() {
        DriveDto created = createDrive(20, "before");
        UserDto otherDriver = createUser("Ben", "Second");
        CarDto otherCar = carService.createCar(new CreateCarRequest(
                "Other Car",
                "M-DR-200",
                otherDriver.getUserId(),
                null
        ));

        DriveDto updated = driveService.updateDrive(created.getDriveId(), new UpdateDriveRequest(
                otherCar.getCarId(),
                77,
                otherDriver.getUserId(),
                LocalDate.of(2025, 6, 1),
                "after"
        ));

        assertThat(updated.getDriveId()).isEqualTo(created.getDriveId());
        assertThat(updated.getCarId()).isEqualTo(otherCar.getCarId());
        assertThat(updated.getDriverId()).isEqualTo(otherDriver.getUserId());
        assertThat(updated.getDistance()).isEqualTo(77);
        assertThat(updated.getDriveDate()).isEqualTo(LocalDate.of(2025, 6, 1));
        assertThat(updated.getNotes()).isEqualTo("after");
    }

    @Test
    void updateUnknownDrive_shouldThrowNotFound() {
        assertThatThrownBy(() -> driveService.updateDrive(999_999L, new UpdateDriveRequest(
                car.getCarId(),
                10,
                driver.getUserId(),
                LocalDate.of(2025, 1, 1),
                null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteDrive_shouldRemoveIt() {
        DriveDto created = createDrive(30, null);

        driveService.deleteDrive(created.getDriveId());

        assertThat(driveRepository.existsById(created.getDriveId())).isFalse();
    }

    @Test
    void deleteUnknownDrive_shouldThrowNotFound() {
        assertThatThrownBy(() -> driveService.deleteDrive(999_999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void listingByVehicleUserAndBoth_shouldFilterCorrectly() {
        UserDto otherDriver = createUser("Cara", "Third");
        CarDto otherCar = carService.createCar(new CreateCarRequest(
                "Second Car",
                "M-DR-300",
                otherDriver.getUserId(),
                null
        ));

        driveService.createDrive(new CreateDriveRequest(
                car.getCarId(), 10, driver.getUserId(), LocalDate.of(2025, 1, 1), null));
        driveService.createDrive(new CreateDriveRequest(
                car.getCarId(), 20, otherDriver.getUserId(), LocalDate.of(2025, 1, 2), null));
        driveService.createDrive(new CreateDriveRequest(
                otherCar.getCarId(), 30, driver.getUserId(), LocalDate.of(2025, 1, 3), null));

        assertThat(driveService.getAllDrivesByVehicle(car.getCarId())).hasSize(2);
        assertThat(driveService.getAllDrivesByUser(driver.getUserId())).hasSize(2);
        assertThat(driveService.getAllDrivesByVehicleAndUser(car.getCarId(), driver.getUserId()))
                .hasSize(1)
                .allSatisfy(d -> {
                    assertThat(d.getCarId()).isEqualTo(car.getCarId());
                    assertThat(d.getDriverId()).isEqualTo(driver.getUserId());
                });
        assertThat(driveService.getAllDrivesByVehicle(999_999L)).isEmpty();
    }

    private DriveDto createDrive(int distance, String notes) {
        return driveService.createDrive(new CreateDriveRequest(
                car.getCarId(),
                distance,
                driver.getUserId(),
                LocalDate.of(2025, 2, 2),
                notes
        ));
    }

    private UserDto createUser(String firstName, String lastName) {
        return userService.createUser(new CreateUserRequest(
                firstName,
                lastName,
                true,
                LocalDate.of(1990, 5, 12)
        ));
    }
}
