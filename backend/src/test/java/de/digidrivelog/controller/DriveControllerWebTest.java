package de.digidrivelog.controller;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.drive.CreateDriveRequest;
import de.digidrivelog.dto.drive.DriveDto;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.services.CarService;
import de.digidrivelog.services.DriveService;
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
class DriveControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

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

        driver = userService.createUser(new CreateUserRequest(
                "Dora", "Driver", true, LocalDate.of(1992, 2, 2)));
        car = carService.createCar(new CreateCarRequest(
                "Drive Car", "M-DV-1", driver.getUserId(), null));
    }

    @Test
    void createDrive_shouldReturn201WithBody() throws Exception {
        mockMvc.perform(post("/ddl/api/drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"odometer":25,"driverId":%d,"driveDate":"2025-03-01","notes":"trip"}
                                """.formatted(car.getCarId(), driver.getUserId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driveId").isNumber())
                .andExpect(jsonPath("$.odometer").value(25))
                .andExpect(jsonPath("$.notes").value("trip"));
    }

    @Test
    void createDriveWithNegativeDistance_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"odometer":-5,"driverId":%d,"driveDate":"2025-03-01"}
                                """.formatted(car.getCarId(), driver.getUserId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDriveWithMissingDriveDate_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"odometer":5,"driverId":%d}
                                """.formatted(car.getCarId(), driver.getUserId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createDriveWithUnknownCar_shouldReturn404() throws Exception {
        mockMvc.perform(post("/ddl/api/drives")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":999999,"odometer":5,"driverId":%d,"driveDate":"2025-03-01"}
                                """.formatted(driver.getUserId())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getDriveById_shouldReturnDrive() throws Exception {
        DriveDto drive = createDrive(33);

        mockMvc.perform(get("/ddl/api/drives/{id}", drive.getDriveId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driveId").value(drive.getDriveId()))
                .andExpect(jsonPath("$.odometer").value(33));
    }

    @Test
    void getUnknownDrive_shouldReturn404() throws Exception {
        mockMvc.perform(get("/ddl/api/drives/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateDrive_shouldReturnUpdatedDrive() throws Exception {
        DriveDto drive = createDrive(10);

        mockMvc.perform(put("/ddl/api/drives/{id}", drive.getDriveId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"carId":%d,"odometer":99,"driverId":%d,"driveDate":"2025-04-04","notes":"longer"}
                                """.formatted(car.getCarId(), driver.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.odometer").value(99))
                .andExpect(jsonPath("$.notes").value("longer"));
    }

    @Test
    void deleteDrive_shouldReturn204() throws Exception {
        DriveDto drive = createDrive(10);

        mockMvc.perform(delete("/ddl/api/drives/{id}", drive.getDriveId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUnknownDrive_shouldReturn404() throws Exception {
        mockMvc.perform(delete("/ddl/api/drives/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void listDrivesByVehicleUserAndBoth_shouldReturnFilteredLists() throws Exception {
        createDrive(11);
        createDrive(22);

        mockMvc.perform(get("/ddl/api/vehicles/{carId}/drives", car.getCarId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/ddl/api/users/{userId}/drives", driver.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/ddl/api/vehicles/{carId}/users/{userId}/drives",
                        car.getCarId(), driver.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        mockMvc.perform(get("/ddl/api/vehicles/{carId}/drives", 999_999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    private DriveDto createDrive(int distance) {
        return driveService.createDrive(new CreateDriveRequest(
                car.getCarId(), distance, driver.getUserId(), LocalDate.of(2025, 2, 2), null));
    }
}
