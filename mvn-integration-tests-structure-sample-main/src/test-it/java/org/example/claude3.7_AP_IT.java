package org.example.integration;

import org.example.App;
import org.example.service.TimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    @DisplayName("Should return expected time when service works correctly")
    public void testSuccessPathGetCurrentServerTime() throws Exception {
        // Arrange
        String expectedTime = "2025-03-01 12:00:00";
        when(timeService.getCurrentTimeAsText()).thenReturn(expectedTime);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/time")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedTime));

        // Verify service interaction
        verify(timeService, times(1)).getCurrentTimeAsText();
    }

    @Test
    @DisplayName("Should return 500 error when time service fails")
    public void testFailurePathGetCurrentServerTime() throws Exception {
        // Arrange - simulate service failure
        when(timeService.getCurrentTimeAsText()).thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/time")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Should handle empty time response")
    public void testEdgeCaseEmptyTimeResponse() throws Exception {
        // Arrange - simulate empty response
        when(timeService.getCurrentTimeAsText()).thenReturn("");

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/time")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    @DisplayName("Should return null time response correctly")
    public void testEdgeCaseNullTimeResponse() throws Exception {
        // Arrange - simulate null response
        when(timeService.getCurrentTimeAsText()).thenReturn(null);

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/time")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }
}