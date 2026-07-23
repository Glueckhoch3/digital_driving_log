package de.digidrivelog.controller;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.cost.CostDto;
import de.digidrivelog.dto.cost.CreateCostRequest;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.models.CostType;
import de.digidrivelog.services.CarService;
import de.digidrivelog.services.CostService;
import de.digidrivelog.services.UserService;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CostControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

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

        buyer = userService.createUser(new CreateUserRequest(
                "Bodo", "Buyer", true, LocalDate.of(1988, 8, 8)));
        car = carService.createCar(new CreateCarRequest(
                "Cost Car", "M-CT-1", buyer.getUserId(), null));
    }

    @Test
    void getAllCosts_shouldReturnList() throws Exception {
        createCost("Fuel");

        mockMvc.perform(get("/ddl/api/costs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].description").value("Fuel"));
    }

    @Test
    void createCost_shouldReturn201WithBody() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"description":"Oil","price":19.99,
                                 "quantity":1,"dayOfTransaction":"2025-05-05","costType":"variable","notes":"5W30"}
                                """.formatted(car.getCarId(), buyer.getUserId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costId").isNumber())
                .andExpect(jsonPath("$.description").value("Oil"))
                .andExpect(jsonPath("$.costType").value("VARIABLE"));
    }

    @Test
    void createCostWithNegativePrice_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"description":"Oil","price":-1.00,
                                 "quantity":1,"dayOfTransaction":"2025-05-05","costType":"variable"}
                                """.formatted(car.getCarId(), buyer.getUserId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCostWithInvalidCostType_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"description":"Oil","price":5.00,
                                 "quantity":1,"dayOfTransaction":"2025-05-05","costType":"sometimes"}
                                """.formatted(car.getCarId(), buyer.getUserId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCostWithTooShortDescription_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"description":"O","price":5.00,
                                 "quantity":1,"dayOfTransaction":"2025-05-05","costType":"variable"}
                                """.formatted(car.getCarId(), buyer.getUserId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCostWithTooLongDescription_shouldReturn400() throws Exception {
        String tooLongDescription = "D".repeat(64);

        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"description":"%s","price":5.00,
                                 "quantity":1,"dayOfTransaction":"2025-05-05","costType":"variable"}
                                """.formatted(car.getCarId(), buyer.getUserId(), tooLongDescription)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCostWithUnknownCar_shouldReturn404() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":999999,"buyerId":%d,"description":"Oil","price":5.00,
                                 "quantity":1,"dayOfTransaction":"2025-05-05","costType":"fixed"}
                                """.formatted(buyer.getUserId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCostById_shouldReturnCost() throws Exception {
        CostDto cost = createCost("Tires");

        mockMvc.perform(get("/ddl/api/costs/{id}", cost.getCostId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costId").value(cost.getCostId()))
                .andExpect(jsonPath("$.description").value("Tires"));
    }

    @Test
    void getUnknownCost_shouldReturn404() throws Exception {
        mockMvc.perform(get("/ddl/api/costs/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCost_shouldReturnUpdatedCost() throws Exception {
        CostDto cost = createCost("Before");

        mockMvc.perform(put("/ddl/api/costs/{id}", cost.getCostId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"description":"After","price":80.00,
                                 "quantity":2,"dayOfTransaction":"2025-06-06","costType":"fixed","notes":"changed"}
                                """.formatted(car.getCarId(), buyer.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("After"))
                .andExpect(jsonPath("$.costType").value("FIXED"))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void deleteCost_shouldReturn204() throws Exception {
        CostDto cost = createCost("Del");

        mockMvc.perform(delete("/ddl/api/costs/{id}", cost.getCostId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUnknownCost_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/ddl/api/costs/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void listCostsByVehicleAndUser_shouldReturnFilteredLists() throws Exception {
        createCost("A");
        createCost("B");

        mockMvc.perform(get("/ddl/api/vehicles/{carId}/costs", car.getCarId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/ddl/api/users/{userId}/costs", buyer.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/ddl/api/vehicles/{carId}/costs", 999_999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    private CostDto createCost(String description) {
        return costService.createCost(new CreateCostRequest(
                car.getCarId(),
                buyer.getUserId(),
                description,
                new BigDecimal("42.00"),
                1,
                LocalDate.of(2025, 5, 5),
                CostType.VARIABLE,
                null
        ));
    }
}
