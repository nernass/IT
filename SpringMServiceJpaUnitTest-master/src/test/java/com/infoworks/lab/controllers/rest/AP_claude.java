package com.infoworks.lab.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.lab.domain.entities.Passenger;
import com.infoworks.lab.domain.models.Gender;
import com.infoworks.lab.domain.repositories.PassengerRepository;
import com.infoworks.lab.services.ServiceExecutionLogger;
import com.infoworks.lab.services.impl.PassengerServiceImpl;
import com.infoworks.lab.webapp.config.BeanConfig;
import com.infoworks.lab.webapp.config.TestJPAH2Config;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(classes = {
    TestJPAH2Config.class, 
    BeanConfig.class, 
    PassengerServiceImpl.class,
    ServiceExecutionLogger.class
})
public class PassengerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PassengerRepository passengerRepository;

    @Test
    public void testFullIntegrationFlow() throws Exception {
        // 1. Create a new passenger
        Passenger passenger = new Passenger("John Doe", Gender.MALE, 25);
        passenger.setActive(true);

        // Test POST endpoint
        MvcResult postResult = mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isOk())
                .andReturn();

        Passenger savedPassenger = objectMapper.readValue(
            postResult.getResponse().getContentAsString(), 
            Passenger.class
        );
        assertNotNull(savedPassenger.getId());
        assertEquals("John Doe", savedPassenger.getName());

        // 2. Update the passenger
        savedPassenger.setName("John Smith");
        MvcResult putResult = mockMvc.perform(put("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedPassenger)))
                .andExpect(status().isOk())
                .andReturn();

        Passenger updatedPassenger = objectMapper.readValue(
            putResult.getResponse().getContentAsString(), 
            Passenger.class
        );
        assertEquals("John Smith", updatedPassenger.getName());

        // 3. Get passengers list
        MvcResult getResult = mockMvc.perform(get("/passenger")
                .param("limit", "10")
                .param("page", "0"))
                .andExpect(status().isOk())
                .andReturn();

        List<?> passengers = objectMapper.readValue(
            getResult.getResponse().getContentAsString(),
            List.class
        );
        assertFalse(passengers.isEmpty());

        // 4. Verify row count
        MvcResult countResult = mockMvc.perform(get("/passenger/rowCount"))
                .andExpect(status().isOk())
                .andReturn();
        
        String countResponse = countResult.getResponse().getContentAsString();
        assertTrue(countResponse.contains("count"));

        // 5. Delete the passenger
        mockMvc.perform(delete("/passenger")
                .param("userid", savedPassenger.getId().toString()))
                .andExpect(status().isOk());

        // Verify deletion in repository
        assertFalse(passengerRepository.findById(savedPassenger.getId()).isPresent());
    }

    @Test
    public void testEdgeCases() throws Exception {
        // Test invalid age
        Passenger invalidPassenger = new Passenger("Test User", Gender.MALE, 15);
        mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPassenger)))
                .andExpect(status().isBadRequest());

        // Test null name
        Passenger nullNamePassenger = new Passenger(null, Gender.FEMALE, 20);
        mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullNamePassenger)))
                .andExpect(status().isBadRequest());

        // Test invalid gender
        Passenger invalidGenderPassenger = new Passenger("Test User", null, 20);
        mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidGenderPassenger)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testBoundaryConditions() throws Exception {
        // Test pagination with zero size
        mockMvc.perform(get("/passenger")
                .param("limit", "0")
                .param("page", "0"))
                .andExpect(status().isOk());

        // Test pagination with large size
        mockMvc.perform(get("/passenger")
                .param("limit", "1000")
                .param("page", "0"))
                .andExpect(status().isOk());

        // Test delete non-existent passenger
        mockMvc.perform(delete("/passenger")
                .param("userid", "99999"))
                .andExpect(status().isOk())
                .andReturn();
    }
}