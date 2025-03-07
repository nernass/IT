package org.example.integration;

import org.example.App;
import org.example.service.TimeService;
import org.example.controller.TimeController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = App.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TimeControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @MockBean
    private TimeService timeService;

    @BeforeEach
    public void setUp() {
        when(timeService.getCurrentTimeAsText()).thenReturn("2025-02-25T10:00:00Z");
    }

    @Test
    public void testGetCurrentServerTime_Success() {
        ResponseEntity<String> response = restTemplate.getForEntity("/time", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("2025-02-25T10:00:00Z", response.getBody());
    }

    @Test
    public void testGetCurrentServerTime_Failure() {
        when(timeService.getCurrentTimeAsText()).thenThrow(new RuntimeException("Service failure"));
        ResponseEntity<String> response = restTemplate.getForEntity("/time", String.class);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetCurrentServerTime_EdgeCase() {
        when(timeService.getCurrentTimeAsText()).thenReturn("");
        ResponseEntity<String> response = restTemplate.getForEntity("/time", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("", response.getBody());
    }
}