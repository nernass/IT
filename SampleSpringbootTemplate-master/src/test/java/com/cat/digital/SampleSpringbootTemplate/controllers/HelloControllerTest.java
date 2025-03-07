package com.cat.digital.SampleSpringbootTemplate.controllers;

import com.cat.digital.SampleSpringbootTemplate.config.Config;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HelloWorldController.class)
public class HelloControllerTest extends BaseControllerTest {

    @MockBean
    private Config config;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void callSayHello_successReceived() throws Exception {
        Mockito.when(config.getEnvName()).thenReturn("test");
        mockMvc.perform(get("/sayHello"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello test!")));
    }
}
