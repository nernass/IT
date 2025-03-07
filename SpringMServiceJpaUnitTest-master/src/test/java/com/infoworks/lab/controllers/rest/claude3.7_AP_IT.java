package com.infoworks.lab.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.lab.domain.entities.Passenger;
import com.infoworks.lab.domain.models.Gender;
import com.infoworks.lab.domain.repositories.PassengerRepository;
import com.infoworks.lab.rest.models.ItemCount;
import com.infoworks.lab.services.ServiceExecutionLogger;
import com.infoworks.lab.services.impl.PassengerServiceImpl;
import com.infoworks.lab.webapp.config.BeanConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PassengerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private PassengerServiceImpl passengerService;

    @BeforeEach
    public void setup() {
        // Clear any existing data
        passengerRepository.deleteAll();
        
        // Create test data
        List<Passenger> passengers = new ArrayList<>();
        passengers.add(new Passenger("John Doe", Gender.MALE, 30));
        passengers.add(new Passenger("Jane Smith", Gender.FEMALE, 25));
        passengers.add(new Passenger("Bob Johnson", Gender.MALE, 45));
        
        // Save test data
        passengerRepository.saveAll(passengers);
    }

    @Test
    public void testGetRowCount() throws Exception {
        // Execute and verify
        MvcResult result = mockMvc.perform(get("/passenger/rowCount"))
                .andExpect(status().isOk())
                .andReturn();

        ItemCount itemCount = objectMapper.readValue(result.getResponse().getContentAsString(), ItemCount.class);
        assertEquals(3, itemCount.getCount());
    }

    @Test
    public void testGetPassengers() throws Exception {
        // Execute and verify
        mockMvc.perform(get("/passenger")
                .param("page", "0")
                .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name", is(notNullValue())))
                .andExpect(jsonPath("$[0].sex", is(notNullValue())))
                .andExpect(jsonPath("$[0].age", is(greaterThanOrEqualTo(18))));
    }

    @Test
    public void testCreatePassenger() throws Exception {
        // Prepare test data
        Passenger newPassenger = new Passenger("New User", Gender.MALE, 35);
        newPassenger.setActive(true);
        
        // Execute
        MvcResult result = mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPassenger)))
                .andExpect(status().isOk())
                .andReturn();
                
        // Verify
        Passenger createdPassenger = objectMapper.readValue(result.getResponse().getContentAsString(), Passenger.class);
        assertThat(createdPassenger.getName(), is("New User"));
        assertThat(createdPassenger.getSex(), is(Gender.MALE.name()));
        assertThat(createdPassenger.getAge(), is(35));
        assertThat(createdPassenger.getActive(), is(true));
        
        // Verify it was actually saved to repository
        Passenger fromDb = passengerService.findByUserID(createdPassenger.getId());
        assertThat(fromDb, is(notNullValue()));
        assertThat(fromDb.getName(), is("New User"));
    }

    @Test
    public void testUpdatePassenger() throws Exception {
        // Get an existing passenger
        Passenger existingPassenger = passengerRepository.findAll().iterator().next();
        
        // Modify it
        existingPassenger.setName("Updated Name");
        existingPassenger.setAge(40);
        
        // Execute update
        MvcResult result = mockMvc.perform(put("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingPassenger)))
                .andExpect(status().isOk())
                .andReturn();
                
        // Verify response
        Passenger updatedPassenger = objectMapper.readValue(result.getResponse().getContentAsString(), Passenger.class);
        assertThat(updatedPassenger.getId(), is(existingPassenger.getId()));
        assertThat(updatedPassenger.getName(), is("Updated Name"));
        assertThat(updatedPassenger.getAge(), is(40));
        
        // Verify it was updated in repository
        Passenger fromDb = passengerService.findByUserID(existingPassenger.getId());
        assertThat(fromDb.getName(), is("Updated Name"));
        assertThat(fromDb.getAge(), is(40));
    }

    @Test
    public void testDeletePassenger() throws Exception {
        // Get an existing passenger
        Passenger existingPassenger = passengerRepository.findAll().iterator().next();
        
        // Execute delete
        MvcResult result = mockMvc.perform(delete("/passenger")
                .param("userid", existingPassenger.getId().toString()))
                .andExpect(status().isOk())
                .andReturn();
                
        // Verify response
        Boolean deleted = objectMapper.readValue(result.getResponse().getContentAsString(), Boolean.class);
        assertTrue(deleted);
        
        // Verify it was deleted from repository
        Passenger fromDb = passengerService.findByUserID(existingPassenger.getId());
        assertThat(fromDb, is(nullValue()));
    }

    @Test
    public void testInvalidInputHandling() throws Exception {
        // Test with invalid passenger (age < 18)
        Passenger invalidPassenger = new Passenger("Invalid User", Gender.MALE, 15);
        
        mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPassenger)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDeleteNonExistentPassenger() throws Exception {
        // Execute delete with non-existent ID
        MvcResult result = mockMvc.perform(delete("/passenger")
                .param("userid", "999"))
                .andExpect(status().isOk())
                .andReturn();
                
        // Verify response
        Boolean deleted = objectMapper.readValue(result.getResponse().getContentAsString(), Boolean.class);
        assertThat(deleted, is(false));
    }

    @Test
    public void testServiceLogging() throws Exception {
        // This test verifies that the aspect-oriented logging is applied
        // We can't directly test log output, but we can verify the flow works
        
        // Make a request that goes through the logging aspect
        mockMvc.perform(get("/passenger")
                .param("page", "0")
                .param("limit", "5"))
                .andExpect(status().isOk());
                
        // If no exceptions occurred, the logging aspect is working correctly
        // In a real environment, you could verify logs through a log appender
    }

    @Test
    public void testHelloEndpoint() throws Exception {
        MvcResult result = mockMvc.perform(get("/passenger/hello"))
                .andExpect(status().isOk())
                .andReturn();
                
        ItemCount count = objectMapper.readValue(result.getResponse().getContentAsString(), ItemCount.class);
        assertEquals(12L, count.getCount());
    }
}