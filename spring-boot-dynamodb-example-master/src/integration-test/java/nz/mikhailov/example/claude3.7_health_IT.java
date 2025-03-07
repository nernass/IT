package nz.mikhailov.example.healthcheck;

import nz.mikhailov.example.Application;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class HealthCheckControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HealthCheckController healthCheckController;

    @Test
    public void contextLoads() {
        assertThat(healthCheckController).isNotNull();
    }

    @Test
    public void healthCheckEndpointReturnsUpStatus() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        String content = result.getResponse().getContentAsString();
        assertThat(content).isEqualTo("up");
    }

    @Test
    public void healthCheckEndpointIsAvailableWhenApplicationStarts() throws Exception {
        // Given application is running (handled by @SpringBootTest)
        
        // When & Then
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }
}