package nz.mikhailov.example.healthcheck;

import nz.mikhailov.example.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class HealthCheckControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testHealthEndpointReturnsUp() throws Exception {
        // Send a GET request to the health endpoint
        MvcResult result = mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify the response is "up"
        String content = result.getResponse().getContentAsString();
        assertEquals("up", content);
    }

    @Test
    public void testHealthEndpointWithAcceptHeader() throws Exception {
        // Send a GET request to the health endpoint with Accept header
        MvcResult result = mockMvc.perform(get("/health")
                .header("Accept", "text/plain"))
                .andExpect(status().isOk())
                .andReturn();

        // Verify the response is "up"
        String content = result.getResponse().getContentAsString();
        assertEquals("up", content);
    }

    @Test
    public void testApplicationContextLoadsHealthController() throws Exception {
        // This test verifies that the Spring context loads successfully
        // with our HealthCheckController available
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }
}