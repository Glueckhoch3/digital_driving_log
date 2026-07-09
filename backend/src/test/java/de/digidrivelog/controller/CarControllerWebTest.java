package de.digidrivelog.controller;

import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.drive.CreateDriveRequest;
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
class CarControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarService carService;

    @Autowired
    private UserService userService;

    @Autowired
    private DriveService driveService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DriveRepository driveRepository;

    @Autowired
    private CostRepository costRepository;

    private UserDto owner;

    @BeforeEach
    void setUp() {
        costRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        owner = userService.createUser(new CreateUserRequest(
                "Otto", "Owner", true, LocalDate.of(1985, 3, 3)));
    }

    @Test
    void getAllCars_shouldReturnList() throws Exception {
        createCar("List Car", "M-LC-1");

        mockMvc.perform(get("/ddl/api/vehicles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("List Car"));
    }

    @Test
    void createCar_shouldReturn201WithBody() throws Exception {
        mockMvc.perform(post("/ddl/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"New Car","plateNumber":"M-NC-1","ownerId":%d,"data":"demo"}
                                """.formatted(owner.getUserId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carId").isNumber())
                .andExpect(jsonPath("$.name").value("New Car"))
                .andExpect(jsonPath("$.ownerId").value(owner.getUserId()));
    }

    @Test
    void createCarWithBlankName_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","plateNumber":"M-NC-1","ownerId":%d}
                                """.formatted(owner.getUserId())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCarWithUnknownOwner_shouldReturn404() throws Exception {
        mockMvc.perform(post("/ddl/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Ghost Car","plateNumber":"M-GC-1","ownerId":999999}
                                """))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCarById_shouldReturnCar() throws Exception {
        CarDto car = createCar("Get Car", "M-GT-1");

        mockMvc.perform(get("/ddl/api/vehicles/{id}", car.getCarId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.carId").value(car.getCarId()))
                .andExpect(jsonPath("$.plateNumber").value("M-GT-1"));
    }

    @Test
    void getUnknownCar_shouldReturn404() throws Exception {
        mockMvc.perform(get("/ddl/api/vehicles/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCar_shouldReturnUpdatedCar() throws Exception {
        CarDto car = createCar("Old Car", "M-OC-1");

        mockMvc.perform(put("/ddl/api/vehicles/{id}", car.getCarId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Renamed","plateNumber":"M-OC-2","ownerId":%d,"data":null}
                                """.formatted(owner.getUserId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Renamed"))
                .andExpect(jsonPath("$.plateNumber").value("M-OC-2"));
    }

    @Test
    void deleteCar_shouldReturn204() throws Exception {
        CarDto car = createCar("Del Car", "M-DC-1");

        mockMvc.perform(delete("/ddl/api/vehicles/{id}", car.getCarId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteCarWithDrive_shouldReturn409() throws Exception {
        CarDto car = createCar("Busy Car", "M-BC-1");
        driveService.createDrive(new CreateDriveRequest(
                car.getCarId(), 12, owner.getUserId(), LocalDate.of(2025, 1, 1), null));

        mockMvc.perform(delete("/ddl/api/vehicles/{id}", car.getCarId()))
                .andExpect(status().isConflict());
    }

    private CarDto createCar(String name, String plate) {
        return carService.createCar(new CreateCarRequest(name, plate, owner.getUserId(), null));
    }
}
