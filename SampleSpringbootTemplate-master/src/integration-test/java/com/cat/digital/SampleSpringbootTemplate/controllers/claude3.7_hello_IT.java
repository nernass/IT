package com.cat.digital.SampleSpringbootTemplate.controllers;

import com.cat.digital.SampleSpringbootTemplate.SampleSpringbootTemplateApplication;
import com.cat.digital.SampleSpringbootTemplate.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SampleSpringbootTemplateApplication.class)
@AutoConfigureMockMvc
public class HelloWorldControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private Config config;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();
    }

    @Test
    public void testSayHello_Success() throws Exception {
        // Given
        when(config.getEnvName()).thenReturn("TestEnvironment");

        // When/Then
        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello TestEnvironment!"));
    }

    @Test
    public void testSayHello_EmptyEnvironment() throws Exception {
        // Given
        when(config.getEnvName()).thenReturn("");

        // When/Then
        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello !"));
    }

    @Test
    public void testSayHello_NullEnvironment() throws Exception {
        // Given
        when(config.getEnvName()).thenReturn(null);

        // When/Then
        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }
}