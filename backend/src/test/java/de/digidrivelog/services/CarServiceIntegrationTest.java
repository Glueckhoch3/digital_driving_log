package de.digidrivelog.services;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.car.UpdateCarRequest;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.models.Drive;
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
class CarServiceIntegrationTest {

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

    @BeforeEach
    void cleanDatabase() {
        costRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createCarWithExistingOwner_shouldSucceed() {
        UserDto owner = createUser("Lena", "Owner");

        CarDto created = carService.createCar(new CreateCarRequest(
                "City Car",
                "M-AB-1001",
                owner.getUserId(),
                "demo"
        ));

        assertThat(created.getCarId()).isNotNull();
        assertThat(created.getName()).isEqualTo("City Car");
        assertThat(created.getPlateNumber()).isEqualTo("M-AB-1001");
        assertThat(created.getOwnerId()).isEqualTo(owner.getUserId());
        assertThat(created.getData()).isEqualTo("demo");
    }

    @Test
    void createCarWithUnknownOwner_shouldThrowNotFound() {
        assertThatThrownBy(() -> carService.createCar(new CreateCarRequest(
                "No Owner",
                "XX-YY-1",
                123_456L,
                null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateCar_shouldChangeAllEditableFields() {
        UserDto owner = createUser("Mara", "Driver");
        UserDto newOwner = createUser("Nina", "Other");

        CarDto created = carService.createCar(new CreateCarRequest(
                "Old Name",
                "AA-BB-10",
                owner.getUserId(),
                "old"
        ));

        CarDto updated = carService.updateCar(created.getCarId(), new UpdateCarRequest(
                "New Name",
                "AA-BB-11",
                newOwner.getUserId(),
                "new"
        ));

        assertThat(updated.getCarId()).isEqualTo(created.getCarId());
        assertThat(updated.getName()).isEqualTo("New Name");
        assertThat(updated.getPlateNumber()).isEqualTo("AA-BB-11");
        assertThat(updated.getOwnerId()).isEqualTo(newOwner.getUserId());
        assertThat(updated.getData()).isEqualTo("new");
    }

    @Test
    void deleteCarWithExistingDrive_shouldThrowConflict() {
        UserDto owner = createUser("Mika", "Owner");
        UserDto driver = createUser("Tim", "Driver");
        CarDto created = carService.createCar(new CreateCarRequest(
                "Trip Car",
                "TR-IP-1",
                owner.getUserId(),
                null
        ));

        Drive drive = new Drive();
        drive.setCar(carRepository.findById(created.getCarId()).orElseThrow());
        drive.setDriver(userRepository.findById(driver.getUserId()).orElseThrow());
        drive.setDriveDate(LocalDate.of(2025, 1, 7));
        drive.setOdometer(42);
        drive.setNotes("integration test");
        driveRepository.save(drive);

        assertThatThrownBy(() -> carService.deleteCar(created.getCarId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
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
