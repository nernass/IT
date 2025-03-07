package com.cat.digital.SampleSpringbootTemplate.integration;

import com.cat.digital.SampleSpringbootTemplate.SampleSpringbootTemplateApplication;
import com.cat.digital.SampleSpringbootTemplate.config.Config;
import com.cat.digital.SampleSpringbootTemplate.controllers.HelloWorldController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = SampleSpringbootTemplateApplication.class)
@AutoConfigureMockMvc
public class ApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HelloWorldController helloWorldController;

    @Autowired
    private RestTemplate restTemplate;

    @MockBean
    private Config config;

    @BeforeEach
    void setUp() {
        reset(config);
    }

    @Nested
    @DisplayName("Application Context Tests")
    class ApplicationContextTests {
        @Test
        @DisplayName("Context Loads Successfully")
        void contextLoads() {
            assertNotNull(helloWorldController);
            assertNotNull(restTemplate);
        }

        @Test
        @DisplayName("RestTemplate Bean Creation")
        void restTemplateCreation() {
            assertNotNull(restTemplate);
        }
    }

    @Nested
    @DisplayName("HelloWorld Controller Integration Tests")
    class HelloWorldControllerTests {

        @Test
        @DisplayName("GET /sayHello - Success Scenario")
        void testSayHelloSuccess() throws Exception {
            // Arrange
            when(config.getEnvName()).thenReturn("Test Environment");

            // Act & Assert
            mockMvc.perform(get("/sayHello")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello Test Environment!"));

            verify(config, times(1)).getEnvName();
        }

        @Test
        @DisplayName("GET /sayHello - Empty Environment")
        void testSayHelloWithEmptyEnvironment() throws Exception {
            // Arrange
            when(config.getEnvName()).thenReturn("");

            // Act & Assert
            mockMvc.perform(get("/sayHello")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello !"));
        }

        @Test
        @DisplayName("GET /sayHello - Null Environment")
        void testSayHelloWithNullEnvironment() throws Exception {
            // Arrange
            when(config.getEnvName()).thenReturn(null);

            // Act & Assert
            mockMvc.perform(get("/sayHello")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Hello null!"));
        }

        @Test
        @DisplayName("GET /sayHello - Exception Handling")
        void testSayHelloWithException() throws Exception {
            // Arrange
            when(config.getEnvName()).thenThrow(new RuntimeException("Config error"));

            // Act & Assert
            mockMvc.perform(get("/sayHello")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());
        }
    }
}