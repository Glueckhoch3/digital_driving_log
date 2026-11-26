package de.digidrivelog.controller;

import de.digidrivelog.services.CsvExportService;
import de.digidrivelog.services.CsvExportService.CsvFile;
import de.digidrivelog.services.csv.CsvLocale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExportControllerWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CsvExportService csvExportService;

    @Test
    void exportDrives_isSentAsCsvAttachment() throws Exception {
        when(csvExportService.exportDrives(7L)).thenReturn(new CsvFile("drives.csv", "a;b".getBytes()));

        mockMvc.perform(get("/ddl/api/vehicles/7/drives/export"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv;charset=UTF-8"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"drives.csv\""))
                .andExpect(content().string("a;b"));
    }

    @Test
    void exportCosts_passesLocale() throws Exception {
        when(csvExportService.exportCosts(7L, CsvLocale.DE)).thenReturn(new CsvFile("costs.csv", "c;d".getBytes()));

        mockMvc.perform(get("/ddl/api/vehicles/7/costs/export").param("locale", "de"))
                .andExpect(status().isOk())
                .andExpect(content().string("c;d"));
    }
}
