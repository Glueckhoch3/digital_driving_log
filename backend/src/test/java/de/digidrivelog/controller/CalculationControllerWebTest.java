package de.digidrivelog.controller;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.drive.CreateDriveRequest;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.repositories.CostTotalCarYearRepository;
import de.digidrivelog.repositories.DriveAccountYearRepository;
import de.digidrivelog.repositories.DriveLogMonthTotalRepository;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.repositories.UserCostFactorYearRepository;
import de.digidrivelog.repositories.UserRepository;
import de.digidrivelog.services.CarService;
import de.digidrivelog.services.CalculationService;
import de.digidrivelog.services.DriveService;
import de.digidrivelog.services.UserService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Covers the issue #32 participant and availability endpoints. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CalculationControllerWebTest {

    private static final int YEAR = 2025;

    @Autowired private MockMvc mockMvc;
    @Autowired private CalculationService calculationService;
    @Autowired private CarService carService;
    @Autowired private UserService userService;
    @Autowired private DriveService driveService;
    @Autowired private CarRepository carRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DriveRepository driveRepository;
    @Autowired private CostRepository costRepository;
    @Autowired private DriveLogMonthTotalRepository monthTotalRepository;
    @Autowired private DriveAccountYearRepository accountYearRepository;
    @Autowired private CostTotalCarYearRepository costTotalRepository;
    @Autowired private UserCostFactorYearRepository factorRepository;

    private CarDto car;
    private UserDto driver;
    private UserDto extra;

    @BeforeEach
    void setUp() {
        factorRepository.deleteAll();
        accountYearRepository.deleteAll();
        costTotalRepository.deleteAll();
        monthTotalRepository.deleteAll();
        costRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        driver = userService.createUser(new CreateUserRequest("Dana", "Driver", true, LocalDate.of(1990, 1, 1)));
        extra = userService.createUser(new CreateUserRequest("Eddie", "Extra", true, LocalDate.of(1991, 1, 1)));
        car = carService.createCar(new CreateCarRequest("Participant Car", "P-AR-1", driver.getUserId(), null));

        driveService.createDrive(new CreateDriveRequest(car.getCarId(), 1000, driver.getUserId(),
                LocalDate.of(YEAR, 1, 5), null));
        driveService.createDrive(new CreateDriveRequest(car.getCarId(), 1100, driver.getUserId(),
                LocalDate.of(YEAR, 1, 20), null));
        calculationService.aggregateMonth(car.getCarId(), YEAR, 1);
    }

    @Test
    void getParticipants_defaultSet_listsAllUsersWithOnlyDriverParticipating() throws Exception {
        mockMvc.perform(get("/ddl/api/calculations/participants")
                        .param("carId", car.getCarId().toString())
                        .param("year", String.valueOf(YEAR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stored").value(false))
                .andExpect(jsonPath("$.rows[?(@.userId == %d)].participating".formatted(driver.getUserId())).value(true))
                .andExpect(jsonPath("$.rows[?(@.userId == %d)].participating".formatted(extra.getUserId())).value(false));
    }

    @Test
    void putThenGetParticipants_roundTrips() throws Exception {
        mockMvc.perform(put("/ddl/api/calculations/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"year":%d,"userIds":[%d,%d]}
                                """.formatted(car.getCarId(), YEAR, driver.getUserId(), extra.getUserId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/ddl/api/calculations/participants")
                        .param("carId", car.getCarId().toString())
                        .param("year", String.valueOf(YEAR)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stored").value(true))
                .andExpect(jsonPath("$.rows[?(@.userId == %d)].participating".formatted(extra.getUserId())).value(true))
                .andExpect(jsonPath("$.rows[?(@.userId == %d)].manuallyAdded".formatted(extra.getUserId())).value(true))
                // regression: the driver has no factor row yet (the year hasn't been run), but
                // must still show as participating — only the manual addition writes a row.
                .andExpect(jsonPath("$.rows[?(@.userId == %d)].participating".formatted(driver.getUserId())).value(true));
    }

    @Test
    void putParticipants_droppingADriver_returns400() throws Exception {
        mockMvc.perform(put("/ddl/api/calculations/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"year":%d,"userIds":[%d]}
                                """.formatted(car.getCarId(), YEAR, extra.getUserId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void putParticipants_unknownUser_returns404() throws Exception {
        mockMvc.perform(put("/ddl/api/calculations/participants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"year":%d,"userIds":[%d,999999]}
                                """.formatted(car.getCarId(), YEAR, driver.getUserId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteParticipants_returnsToDriversOnly() throws Exception {
        calculationService.saveParticipants(
                new de.digidrivelog.dto.calculation.ParticipantUpdateRequest(
                        car.getCarId(), YEAR, java.util.List.of(driver.getUserId(), extra.getUserId())));

        mockMvc.perform(delete("/ddl/api/calculations/participants")
                        .param("carId", car.getCarId().toString())
                        .param("year", String.valueOf(YEAR)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/ddl/api/calculations/participants")
                        .param("carId", car.getCarId().toString())
                        .param("year", String.valueOf(YEAR)))
                .andExpect(jsonPath("$.stored").value(false));
    }

    @Test
    void availability_reportsAggregatedMonthAndDriveMonths() throws Exception {
        mockMvc.perform(get("/ddl/api/calculations/availability")
                        .param("carId", car.getCarId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.years[?(@.year == %d)].aggregatedMonths".formatted(YEAR)).isNotEmpty())
                .andExpect(jsonPath("$.years[?(@.year == %d)].yearCalculated".formatted(YEAR)).value(false));
    }
}
