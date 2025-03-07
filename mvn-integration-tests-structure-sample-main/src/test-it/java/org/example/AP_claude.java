package org.example.integration;

import org.example.App;
import org.example.service.TimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = App.class)
@AutoConfigureMockMvc
public class TimeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeService timeService;

    @Test
    public void testGetCurrentServerTime_Success() throws Exception {
        // Arrange
        String expectedTime = "2025-02-25 12:00:00";
        when(timeService.getCurrentTimeAsText()).thenReturn(expectedTime);

        // Act & Assert
        mockMvc.perform(get("/time"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedTime));
    }

    @Test
    public void testGetCurrentServerTime_ServiceFailure() throws Exception {
        // Arrange
        when(timeService.getCurrentTimeAsText()).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        mockMvc.perform(get("/time"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetCurrentServerTime_EmptyResponse() throws Exception {
        // Arrange
        when(timeService.getCurrentTimeAsText()).thenReturn("");

        // Act & Assert
        mockMvc.perform(get("/time"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}