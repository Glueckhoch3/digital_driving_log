package de.digidrivelog.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.cost.CreateCostRequest;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.models.CostCategory;
import de.digidrivelog.models.CostType;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostCategoryRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.repositories.UserRepository;
import de.digidrivelog.services.CarService;
import de.digidrivelog.services.CostService;
import de.digidrivelog.services.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CostCategoryControllerWebTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CostCategoryRepository costCategoryRepository;
    @Autowired private CostRepository costRepository;
    @Autowired private DriveRepository driveRepository;
    @Autowired private CarRepository carRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CostService costService;
    @Autowired private CarService carService;
    @Autowired private UserService userService;

    @BeforeEach
    void setUp() {
        costRepository.deleteAll();
        costCategoryRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();
    }

    private static final String BODY = """
            {"name":"%s","costType":"VARIABLE","unit":"Liter","requiresOdometer":true,"fuel":true}
            """;

    @Test
    void create_shouldReturn201WithBody() throws Exception {
        mockMvc.perform(post("/ddl/api/cost-categories").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.formatted("Fuel")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costCategoryId").isNumber())
                .andExpect(jsonPath("$.name").value("Fuel"))
                .andExpect(jsonPath("$.unit").value("Liter"))
                .andExpect(jsonPath("$.requiresOdometer").value(true))
                .andExpect(jsonPath("$.fuel").value(true));
    }

    @Test
    void create_withDuplicateName_shouldReturn409() throws Exception {
        costCategoryRepository.save(category("Fuel"));

        mockMvc.perform(post("/ddl/api/cost-categories").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY.formatted("fuel")))
                .andExpect(status().isConflict());
    }

    @Test
    void create_withInvalidBody_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/cost-categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"costType\":\"VARIABLE\",\"unit\":\"\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/ddl/api/cost-categories").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fuel\",\"costType\":\"sometimes\",\"unit\":\"l\",\"requiresOdometer\":false,\"fuel\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void list_shouldReturnSortedByName() throws Exception {
        costCategoryRepository.save(category("Insurance"));
        costCategoryRepository.save(category("Fuel"));

        mockMvc.perform(get("/ddl/api/cost-categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Fuel"));
    }

    @Test
    void update_shouldReplaceFields_andRejectDuplicateName() throws Exception {
        CostCategory fuel = costCategoryRepository.save(category("Fuel"));
        costCategoryRepository.save(category("Insurance"));

        mockMvc.perform(put("/ddl/api/cost-categories/{id}", fuel.getCostCategoryId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Fuel\",\"costType\":\"FIXED\",\"unit\":\"l\",\"requiresOdometer\":false,\"fuel\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.costType").value("FIXED"))
                .andExpect(jsonPath("$.requiresOdometer").value(false));

        mockMvc.perform(put("/ddl/api/cost-categories/{id}", fuel.getCostCategoryId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"insurance\",\"costType\":\"FIXED\",\"unit\":\"l\",\"requiresOdometer\":false,\"fuel\":false}"))
                .andExpect(status().isConflict());
    }

    @Test
    void getAndDelete_unknown_shouldReturn404() throws Exception {
        mockMvc.perform(get("/ddl/api/cost-categories/{id}", 999_999L)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/ddl/api/cost-categories/{id}", 999_999L)).andExpect(status().isNotFound());
    }

    @Test
    void delete_unused_shouldReturn204() throws Exception {
        CostCategory c = costCategoryRepository.save(category("Fuel"));

        mockMvc.perform(delete("/ddl/api/cost-categories/{id}", c.getCostCategoryId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_usedByCost_shouldReturn409() throws Exception {
        CostCategory c = costCategoryRepository.save(category("Insurance"));
        UserDto user = userService.createUser(new CreateUserRequest("Cat", "Owner", true, LocalDate.of(1990, 1, 1)));
        CarDto car = carService.createCar(new CreateCarRequest("Car", "M-CC-1", user.getUserId(), null));
        costService.createCost(new CreateCostRequest(car.getCarId(), user.getUserId(), c.getCostCategoryId(),
                new BigDecimal("10.00"), BigDecimal.ONE, LocalDate.of(2025, 1, 1), null, null));

        mockMvc.perform(delete("/ddl/api/cost-categories/{id}", c.getCostCategoryId()))
                .andExpect(status().isConflict());
    }

    private CostCategory category(String name) {
        return new CostCategory(null, name, CostType.VARIABLE, "Liter", false, false, null, null);
    }
}
