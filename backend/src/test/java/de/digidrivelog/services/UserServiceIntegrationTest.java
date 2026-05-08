package de.digidrivelog.services;

import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UpdateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.repositories.CarRepository;
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
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @BeforeEach
    void cleanDatabase() {
        carRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void createAndUpdateUser_shouldPersistExpectedValues() {
        UserDto created = userService.createUser(new CreateUserRequest(
                "Anna",
                "Meyer",
                true,
                LocalDate.of(1998, 3, 14)
        ));

        UserDto updated = userService.updateUser(created.getUserId(), new UpdateUserRequest(
                "Anja",
                "Meyer",
                false,
                LocalDate.of(1999, 6, 21)
        ));

        assertThat(updated.getUserId()).isEqualTo(created.getUserId());
        assertThat(updated.getFirstname()).isEqualTo("Anja");
        assertThat(updated.getLastname()).isEqualTo("Meyer");
        assertThat(updated.getDriverLicense()).isFalse();
        assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(1999, 6, 21));
    }

    @Test
    void updateMissingUser_shouldThrowNotFound() {
        assertThatThrownBy(() -> userService.updateUser(
                999_999L,
                new UpdateUserRequest("X", "Y", true, LocalDate.of(1990, 1, 1))
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteUserWithExistingCar_shouldThrowConflict() {
        UserDto owner = userService.createUser(new CreateUserRequest(
                "Jon",
                "Owner",
                true,
                LocalDate.of(1985, 2, 10)
        ));

        carService.createCar(new CreateCarRequest(
                "Family Car",
                "AB-CD-123",
                owner.getUserId(),
                "test"
        ));

        assertThatThrownBy(() -> userService.deleteUser(owner.getUserId()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }
}
