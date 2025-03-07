package org.example.controller;

import org.example.App;
import org.example.service.TimeService;
import org.junit.jupiter.api.BeforeEach;
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

    private static final String EXPECTED_TIME = "2025-02-19 12:00:00";

    @BeforeEach
    void setUp() {
        when(timeService.getCurrentTimeAsText()).thenReturn(EXPECTED_TIME);
    }

    @Test
    void getCurrentServerTime_ShouldReturnTime() throws Exception {
        mockMvc.perform(get("/time"))
                .andExpect(status().isOk())
                .andExpect(content().string(EXPECTED_TIME));
    }

    @Test
    void getCurrentServerTime_WhenServiceFails_ShouldReturn500() throws Exception {
        when(timeService.getCurrentTimeAsText()).thenThrow(new RuntimeException("Service failure"));

        mockMvc.perform(get("/time"))
                .andExpect(status().isInternalServerError());
    }
}