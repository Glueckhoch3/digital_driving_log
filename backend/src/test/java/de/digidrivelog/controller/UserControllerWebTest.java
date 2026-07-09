package de.digidrivelog.controller;

import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.services.CarService;
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
class UserControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private CarService carService;

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
    void getAllUsers_shouldReturnList() throws Exception {
        createUser("Lena", "List");

        mockMvc.perform(get("/ddl/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstname").value("Lena"));
    }

    @Test
    void createUser_shouldReturn201WithBody() throws Exception {
        mockMvc.perform(post("/ddl/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstname":"Max","lastname":"Neu","driverLicense":true,"birthday":"1995-08-01"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.firstname").value("Max"))
                .andExpect(jsonPath("$.driverLicense").value(true));
    }

    @Test
    void createUserWithBlankFirstname_shouldReturn400() throws Exception {
        mockMvc.perform(post("/ddl/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstname":"","lastname":"Neu","driverLicense":true,"birthday":"1995-08-01"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        UserDto user = createUser("Gitta", "Get");

        mockMvc.perform(get("/ddl/api/users/{id}", user.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getUserId()))
                .andExpect(jsonPath("$.firstname").value("Gitta"));
    }

    @Test
    void getUnknownUser_shouldReturn404() throws Exception {
        mockMvc.perform(get("/ddl/api/users/{id}", 999_999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() throws Exception {
        UserDto user = createUser("Old", "Name");

        mockMvc.perform(put("/ddl/api/users/{id}", user.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstname":"New","lastname":"Name","driverLicense":false,"birthday":"1990-05-12"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("New"))
                .andExpect(jsonPath("$.driverLicense").value(false));
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        UserDto user = createUser("Del", "Me");

        mockMvc.perform(delete("/ddl/api/users/{id}", user.getUserId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUserWithCar_shouldReturn409() throws Exception {
        UserDto owner = createUser("Owns", "Car");
        carService.createCar(new CreateCarRequest("Blocking Car", "M-XX-1", owner.getUserId(), null));

        mockMvc.perform(delete("/ddl/api/users/{id}", owner.getUserId()))
                .andExpect(status().isConflict());
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
