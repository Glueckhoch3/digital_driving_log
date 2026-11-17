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
import de.digidrivelog.repositories.CostCategoryRepository;
import de.digidrivelog.models.CostCategory;
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

    @Autowired
    private CostCategoryRepository costCategoryRepository;

    private CostCategory fuel;
    private CostCategory insurance;

    private CarDto car;
    private UserDto buyer;

    @BeforeEach
    void setUp() {
        costRepository.deleteAll();
        costCategoryRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        fuel = costCategoryRepository.save(new CostCategory(
                null, "Fuel", CostType.VARIABLE, "Liter", true, true, null, null));
        insurance = costCategoryRepository.save(new CostCategory(
                null, "Insurance", CostType.FIXED, "pcs", false, false, null, null));
        buyer = userService.createUser(new CreateUserRequest(
                "Bodo", "Buyer", true, LocalDate.of(1988, 8, 8)));
        car = carService.createCar(new CreateCarRequest(
                "Cost Car", "M-CT-1", buyer.getUserId(), null));
    }

    @Test
    void getAllCosts_shouldReturnList() throws Exception {
        createCost(insurance);

        mockMvc.perform(get("/ddl/api/costs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].costCategoryName").value("Insurance"));
    }

    @Test
    void createCost_shouldReturn201WithBody() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"costCategoryId":%d,"price":19.99,
                                 "quantity":1,"dayOfTransaction":"2025-05-05","odometer":1000,"notes":"5W30"}
                                """.formatted(car.getCarId(), buyer.getUserId(), fuel.getCostCategoryId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costId").isNumber())
                .andExpect(jsonPath("$.costCategoryName").value("Fuel"))
                .andExpect(jsonPath("$.unit").value("Liter"))
                .andExpect(jsonPath("$.odometer").value(1000))
                .andExpect(jsonPath("$.costType").value("VARIABLE"));
    }

    @Test
    void createCostWithNegativePrice_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"costCategoryId":%d,"price":-1.00,
                                 "quantity":1,"dayOfTransaction":"2025-05-05"}
                                """.formatted(car.getCarId(), buyer.getUserId(), insurance.getCostCategoryId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCostWithoutCategory_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"price":5.00,
                                 "quantity":1,"dayOfTransaction":"2025-05-05"}
                                """.formatted(car.getCarId(), buyer.getUserId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCostWithUnknownCategory_shouldReturn404() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"costCategoryId":999999,"price":5.00,
                                 "quantity":1,"dayOfTransaction":"2025-05-05"}
                                """.formatted(car.getCarId(), buyer.getUserId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void createFuelCostWithoutOdometer_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"costCategoryId":%d,"price":5.00,
                                 "quantity":10,"dayOfTransaction":"2025-05-05"}
                                """.formatted(car.getCarId(), buyer.getUserId(), fuel.getCostCategoryId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCostWithNonPositiveOdometer_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"costCategoryId":%d,"price":5.00,
                                 "quantity":10,"dayOfTransaction":"2025-05-05","odometer":0}
                                """.formatted(car.getCarId(), buyer.getUserId(), fuel.getCostCategoryId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCostWithUnknownCar_shouldReturn404() throws Exception {
        mockMvc.perform(post("/ddl/api/costs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":999999,"buyerId":%d,"costCategoryId":%d,"price":5.00,
                                 "quantity":1,"dayOfTransaction":"2025-05-05"}
                                """.formatted(buyer.getUserId(), insurance.getCostCategoryId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCostById_shouldReturnCost() throws Exception {
        CostDto cost = createCost(insurance);

        mockMvc.perform(get("/ddl/api/costs/{id}", cost.getCostId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costId").value(cost.getCostId()))
                .andExpect(jsonPath("$.costCategoryName").value("Insurance"));
    }

    @Test
    void getUnknownCost_shouldReturn404() throws Exception {
        mockMvc.perform(get("/ddl/api/costs/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCost_shouldReturnUpdatedCost() throws Exception {
        CostDto cost = createCost(insurance);

        mockMvc.perform(put("/ddl/api/costs/{id}", cost.getCostId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"buyerId":%d,"costCategoryId":%d,"price":80.00,
                                 "quantity":2,"dayOfTransaction":"2025-06-06","odometer":2000,"notes":"changed"}
                                """.formatted(car.getCarId(), buyer.getUserId(), fuel.getCostCategoryId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costCategoryName").value("Fuel"))
                .andExpect(jsonPath("$.costType").value("VARIABLE"))
                .andExpect(jsonPath("$.odometer").value(2000))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void deleteCost_shouldReturn204() throws Exception {
        CostDto cost = createCost(insurance);

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
        createCost(insurance);
        createCost(insurance);

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

    private CostDto createCost(CostCategory category) {
        return costService.createCost(new CreateCostRequest(
                car.getCarId(),
                buyer.getUserId(),
                category.getCostCategoryId(),
                new BigDecimal("42.00"),
                new BigDecimal("1"),
                LocalDate.of(2025, 5, 5),
                null,
                null
        ));
    }
}
