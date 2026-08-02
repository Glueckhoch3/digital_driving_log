package de.digidrivelog.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties =
        "ALLOWED_ORIGINS=http://allowed.example, http://192.168.178.*:4200")
class CorsConfigWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void preflightFromConfiguredOrigin_isAccepted() throws Exception {
        mockMvc.perform(options("/ddl/api/users")
                        .header(HttpHeaders.ORIGIN, "http://allowed.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://allowed.example"));
    }

    @Test
    void preflightFromOtherOrigin_isRejected() throws Exception {
        mockMvc.perform(options("/ddl/api/users")
                        .header(HttpHeaders.ORIGIN, "http://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void preflightFromOriginMatchingSubnetPattern_isAccepted() throws Exception {
        mockMvc.perform(options("/ddl/api/users")
                        .header(HttpHeaders.ORIGIN, "http://192.168.178.42:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://192.168.178.42:4200"));
    }

    @Test
    void preflightFromOriginOutsideSubnetPattern_isRejected() throws Exception {
        mockMvc.perform(options("/ddl/api/users")
                        .header(HttpHeaders.ORIGIN, "http://192.168.179.42:4200")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }

    @Test
    void simpleRequestFromConfiguredOrigin_carriesCorsHeader() throws Exception {
        mockMvc.perform(get("/ddl/api/users")
                        .header(HttpHeaders.ORIGIN, "http://allowed.example"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://allowed.example"));
    }
}
