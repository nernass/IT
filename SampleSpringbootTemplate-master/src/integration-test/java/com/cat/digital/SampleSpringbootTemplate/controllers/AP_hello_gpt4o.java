package com.cat.digital.SampleSpringbootTemplate.integration;

import com.cat.digital.SampleSpringbootTemplate.SampleSpringbootTemplateApplication;
import com.cat.digital.SampleSpringbootTemplate.config.Config;
import com.cat.digital.SampleSpringbootTemplate.controllers.HelloWorldController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SampleSpringbootTemplateApplication.class)
public class HelloWorldControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private Config config;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testSayHelloSuccess() throws Exception {
        Mockito.when(config.getEnvName()).thenReturn("TestEnv");

        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello TestEnv!"));
    }

    @Test
    public void testSayHelloConfigFailure() throws Exception {
        Mockito.when(config.getEnvName()).thenThrow(new RuntimeException("Config error"));

        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void testSayHelloEdgeCaseEmptyEnvName() throws Exception {
        Mockito.when(config.getEnvName()).thenReturn("");

        mockMvc.perform(get("/sayHello")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello !"));
    }
}