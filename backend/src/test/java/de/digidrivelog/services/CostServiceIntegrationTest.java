package de.digidrivelog.services;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.cost.CostDto;
import de.digidrivelog.dto.cost.CreateCostRequest;
import de.digidrivelog.dto.cost.UpdateCostRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CostServiceIntegrationTest {

    @Autowired
    private CostService costService;

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
    private UserDto buyer;

    @BeforeEach
    void setUp() {
        costRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        buyer = createUser("Olaf", "Buyer");
        car = carService.createCar(new CreateCarRequest(
                "Cost Car",
                "M-CO-100",
                buyer.getUserId(),
                null
        ));
    }

    @Test
    void createCost_shouldPersistAllFields() {
        CostDto created = costService.createCost(new CreateCostRequest(
                car.getCarId(),
                buyer.getUserId(),
                "Fuel",
                new BigDecimal("54.90"),
                40,
                LocalDate.of(2025, 4, 2),
                "variable",
                "full tank"
        ));

        assertThat(created.getCostId()).isNotNull();
        assertThat(created.getCarId()).isEqualTo(car.getCarId());
        assertThat(created.getBuyerId()).isEqualTo(buyer.getUserId());
        assertThat(created.getTransactionObject()).isEqualTo("Fuel");
        assertThat(created.getPrice()).isEqualByComparingTo("54.90");
        assertThat(created.getAmount()).isEqualTo(40);
        assertThat(created.getDayOfTransaction()).isEqualTo(LocalDate.of(2025, 4, 2));
        assertThat(created.getCostType()).isEqualTo("VARIABLE");
        assertThat(created.getNotes()).isEqualTo("full tank");
    }

    @Test
    void createCostWithInvalidCostType_shouldThrowBadRequest() {
        assertThatThrownBy(() -> costService.createCost(new CreateCostRequest(
                car.getCarId(),
                buyer.getUserId(),
                "Fuel",
                new BigDecimal("10.00"),
                1,
                LocalDate.of(2025, 4, 2),
                "sometimes",
                null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void createCostWithUnknownCar_shouldThrowNotFound() {
        assertThatThrownBy(() -> costService.createCost(costRequestFor(999_999L, buyer.getUserId())))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void createCostWithUnknownBuyer_shouldThrowNotFound() {
        assertThatThrownBy(() -> costService.createCost(costRequestFor(car.getCarId(), 999_999L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getCostById_shouldReturnCost() {
        CostDto created = costService.createCost(costRequestFor(car.getCarId(), buyer.getUserId()));

        CostDto fetched = costService.getCostById(created.getCostId());

        assertThat(fetched.getCostId()).isEqualTo(created.getCostId());
        assertThat(fetched.getTransactionObject()).isEqualTo("Tires");
    }

    @Test
    void getCostByUnknownId_shouldThrowNotFound() {
        assertThatThrownBy(() -> costService.getCostById(999_999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void updateCost_shouldChangeAllEditableFields() {
        CostDto created = costService.createCost(costRequestFor(car.getCarId(), buyer.getUserId()));
        UserDto otherBuyer = createUser("Pia", "Second");
        CarDto otherCar = carService.createCar(new CreateCarRequest(
                "Other Car",
                "M-CO-200",
                otherBuyer.getUserId(),
                null
        ));

        CostDto updated = costService.updateCost(created.getCostId(), new UpdateCostRequest(
                otherCar.getCarId(),
                otherBuyer.getUserId(),
                "Insurance",
                new BigDecimal("300.00"),
                1,
                LocalDate.of(2025, 7, 1),
                "fixed",
                "yearly"
        ));

        assertThat(updated.getCostId()).isEqualTo(created.getCostId());
        assertThat(updated.getCarId()).isEqualTo(otherCar.getCarId());
        assertThat(updated.getBuyerId()).isEqualTo(otherBuyer.getUserId());
        assertThat(updated.getTransactionObject()).isEqualTo("Insurance");
        assertThat(updated.getPrice()).isEqualByComparingTo("300.00");
        assertThat(updated.getAmount()).isEqualTo(1);
        assertThat(updated.getDayOfTransaction()).isEqualTo(LocalDate.of(2025, 7, 1));
        assertThat(updated.getCostType()).isEqualTo("FIXED");
        assertThat(updated.getNotes()).isEqualTo("yearly");
    }

    @Test
    void updateUnknownCost_shouldThrowNotFound() {
        assertThatThrownBy(() -> costService.updateCost(999_999L, new UpdateCostRequest(
                car.getCarId(),
                buyer.getUserId(),
                "Fuel",
                new BigDecimal("10.00"),
                1,
                LocalDate.of(2025, 4, 2),
                "variable",
                null
        )))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void deleteCost_shouldRemoveIt() {
        CostDto created = costService.createCost(costRequestFor(car.getCarId(), buyer.getUserId()));

        costService.deleteCost(created.getCostId());

        assertThat(costRepository.existsById(created.getCostId())).isFalse();
    }

    @Test
    void deleteUnknownCost_shouldThrowNotFound() {
        assertThatThrownBy(() -> costService.deleteCost(999_999L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void listingByVehicleAndUser_shouldFilterCorrectly() {
        UserDto otherBuyer = createUser("Rita", "Third");
        CarDto otherCar = carService.createCar(new CreateCarRequest(
                "Second Car",
                "M-CO-300",
                otherBuyer.getUserId(),
                null
        ));

        costService.createCost(costRequestFor(car.getCarId(), buyer.getUserId()));
        costService.createCost(costRequestFor(car.getCarId(), otherBuyer.getUserId()));
        costService.createCost(costRequestFor(otherCar.getCarId(), buyer.getUserId()));

        assertThat(costService.getAllCosts()).hasSize(3);
        assertThat(costService.getAllCostsByVehicle(car.getCarId())).hasSize(2);
        assertThat(costService.getAllCostsByUser(buyer.getUserId())).hasSize(2);
        assertThat(costService.getAllCostsByVehicle(999_999L)).isEmpty();
    }

    private CreateCostRequest costRequestFor(Long carId, Long buyerId) {
        return new CreateCostRequest(
                carId,
                buyerId,
                "Tires",
                new BigDecimal("120.00"),
                4,
                LocalDate.of(2025, 5, 20),
                "VARIABLE",
                null
        );
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
