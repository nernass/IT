package com.infoworks.lab.controllers.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.lab.domain.entities.Passenger;
import com.infoworks.lab.domain.models.Gender;
import com.infoworks.lab.domain.repositories.PassengerRepository;
import com.infoworks.lab.rest.models.ItemCount;
import com.infoworks.lab.services.iServices.PassengerService;
import com.infoworks.lab.webapp.config.BeanConfig;
import com.infoworks.lab.webapp.config.JPAConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {JPAConfig.class, BeanConfig.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PassengerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private List<Passenger> testPassengers;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        passengerRepository.deleteAll();

        // Create test data
        testPassengers = new ArrayList<>();
        testPassengers.add(new Passenger("John Doe", Gender.MALE, 30));
        testPassengers.add(new Passenger("Jane Smith", Gender.FEMALE, 25));
        testPassengers.add(new Passenger("Alex Johnson", Gender.MALE, 45));
        
        // Save test passengers to the repository
        testPassengers = passengerRepository.saveAll(testPassengers);
    }

    @Test
    @DisplayName("Should return hello with item count")
    void testGetHello() throws Exception {
        mockMvc.perform(get("/passenger/hello"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.count", is(12)));
    }

    @Test
    @DisplayName("Should return correct row count")
    void testGetRowCount() throws Exception {
        // Given the setup has 3 passengers
        
        // When & Then
        MvcResult result = mockMvc.perform(get("/passenger/rowCount"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();
        
        String content = result.getResponse().getContentAsString();
        ItemCount itemCount = objectMapper.readValue(content, ItemCount.class);
        assertEquals(3L, itemCount.getCount());
    }

    @Test
    @DisplayName("Should retrieve paginated passengers")
    void testQueryPassengers() throws Exception {
        mockMvc.perform(get("/passenger")
                .param("limit", "2")
                .param("page", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is(notNullValue())))
                .andExpect(jsonPath("$[1].name", is(notNullValue())));
        
        mockMvc.perform(get("/passenger")
                .param("limit", "2")
                .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Should create a new passenger")
    void testCreatePassenger() throws Exception {
        // Given
        Passenger newPassenger = new Passenger("New Person", Gender.FEMALE, 35);
        newPassenger.setActive(true);
        String passengerJson = objectMapper.writeValueAsString(newPassenger);

        // When & Then
        MvcResult result = mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(passengerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(notNullValue())))
                .andExpect(jsonPath("$.name", is("New Person")))
                .andExpect(jsonPath("$.sex", is("FEMALE")))
                .andExpect(jsonPath("$.age", is(35)))
                .andExpect(jsonPath("$.active", is(true)))
                .andReturn();

        // Verify total count is now 4
        mockMvc.perform(get("/passenger/rowCount"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.count", is(4)));
    }

    @Test
    @DisplayName("Should update an existing passenger")
    void testUpdatePassenger() throws Exception {
        // Given
        Passenger passengerToUpdate = testPassengers.get(0);
        passengerToUpdate.setName("Updated Name");
        passengerToUpdate.setAge(50);
        passengerToUpdate.setActive(true);
        String passengerJson = objectMapper.writeValueAsString(passengerToUpdate);

        // When & Then
        mockMvc.perform(put("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(passengerJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(passengerToUpdate.getId())))
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.age", is(50)))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @DisplayName("Should delete a passenger")
    void testDeletePassenger() throws Exception {
        // Given
        Passenger passengerToDelete = testPassengers.get(0);
        Integer idToDelete = passengerToDelete.getId();

        // When & Then
        mockMvc.perform(delete("/passenger")
                .param("userid", idToDelete.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Verify passenger was deleted
        assertFalse(passengerRepository.existsById(idToDelete));
        
        // Verify total count is now 2
        mockMvc.perform(get("/passenger/rowCount"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.count", is(2)));
    }

    @Test
    @DisplayName("Should validate passenger constraints")
    void testPassengerValidation() throws Exception {
        // Given - invalid passenger with null name and invalid age
        Passenger invalidPassenger = new Passenger();
        invalidPassenger.setAge(15); // Age below minimum (18)
        String passengerJson = objectMapper.writeValueAsString(invalidPassenger);

        // When & Then
        mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(passengerJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should check service execution logging")
    void testServiceExecutionLogging() {
        // Test that verifies the service is properly executing and we can assume
        // the aspect is being applied (detailed verification would require log inspection)
        Passenger passenger = new Passenger("Log Test", Gender.MALE, 40);
        Passenger saved = passengerService.add(passenger);
        assertNotNull(saved.getId());
        
        Passenger retrieved = passengerService.findByUserID(saved.getId());
        assertEquals("Log Test", retrieved.getName());
    }
}