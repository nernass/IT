package com.cat.digital.SampleSpringbootTemplate.integration;

import com.cat.digital.SampleSpringbootTemplate.config.Config;
import com.cat.digital.SampleSpringbootTemplate.controllers.HelloWorldController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class HelloWorldIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HelloWorldController helloWorldController;

    @MockBean
    private Config config;

    @Test
    public void testSuccessPathHelloEndpoint() throws Exception {
        // Setup
        when(config.getEnvName()).thenReturn("Integration Test Environment");

        // Execute and verify
        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Hello Integration Test Environment!"));

        // Verify interactions
        verify(config).getEnvName();
    }

    @Test
    public void testEdgeCaseEmptyEnvironment() throws Exception {
        // Setup edge case - empty environment name
        when(config.getEnvName()).thenReturn("");

        // Execute and verify
        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("Hello !"));
    }

    @Test
    public void testEdgeCaseNullEnvironment() throws Exception {
        // Setup edge case - null environment name
        when(config.getEnvName()).thenReturn(null);

        // Execute and verify
        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is5xxServerError()); // Expect error due to null
    }

    @Test
    public void testErrorHandlingInController() throws Exception {
        // Force an exception by making config throw exception
        when(config.getEnvName()).thenThrow(new RuntimeException("Simulated config failure"));

        // Execute and verify proper error handling
        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is5xxServerError());
    }
}