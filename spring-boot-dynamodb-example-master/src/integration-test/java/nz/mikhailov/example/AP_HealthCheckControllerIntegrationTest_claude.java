package nz.mikhailov.example.healthcheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HealthCheckControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHealthCheckEndpoint_Success() throws Exception {
        // Test successful health check response
        MvcResult result = mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertEquals("up", response);
    }

    @Test
    public void testHealthCheckEndpoint_WrongPath() throws Exception {
        // Test invalid endpoint path
        mockMvc.perform(get("/health/invalid"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testHealthCheckEndpoint_WrongMethod() throws Exception {
        // Test wrong HTTP method
        mockMvc.perform(post("/health"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void testApplicationContext_Loads() {
        // Verify Spring context loads successfully
        assertNotNull(mockMvc);
    }
}