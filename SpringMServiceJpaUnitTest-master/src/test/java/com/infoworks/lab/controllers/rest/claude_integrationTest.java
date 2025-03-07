package com.infoworks.lab.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.lab.domain.entities.Passenger;
import com.infoworks.lab.domain.models.Gender;
import com.infoworks.lab.domain.repositories.PassengerRepository;
import com.infoworks.lab.rest.models.ItemCount;
import com.infoworks.lab.services.ServiceExecutionLogger;
import com.infoworks.lab.services.impl.PassengerServiceImpl;
import com.infoworks.lab.webapp.config.BeanConfig;
import com.infoworks.lab.webapp.config.JPAConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {JPAConfig.class, BeanConfig.class, PassengerServiceImpl.class, ServiceExecutionLogger.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PassengerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PassengerRepository passengerRepository;

    @BeforeEach
    void setUp() {
        passengerRepository.deleteAll();
    }

    @Test
    void testCreatePassenger() throws Exception {
        Passenger passenger = new Passenger("John Doe", Gender.MALE, 25);

        MvcResult result = mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isOk())
                .andReturn();

        Passenger created = objectMapper.readValue(result.getResponse().getContentAsString(), Passenger.class);
        assertNotNull(created.getId());
        assertEquals(passenger.getName(), created.getName());
    }

    @Test
    void testGetPassengers() throws Exception {
        // Create test passengers
        Passenger p1 = new Passenger("John Doe", Gender.MALE, 25);
        Passenger p2 = new Passenger("Jane Doe", Gender.FEMALE, 23);
        passengerRepository.saveAll(List.of(p1, p2));

        MvcResult result = mockMvc.perform(get("/passenger")
                .param("limit", "10")
                .param("page", "0"))
                .andExpect(status().isOk())
                .andReturn();

        List<?> passengers = objectMapper.readValue(result.getResponse().getContentAsString(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Passenger.class));
        assertEquals(2, passengers.size());
    }

    @Test
    void testUpdatePassenger() throws Exception {
        Passenger passenger = new Passenger("John Doe", Gender.MALE, 25);
        passenger = passengerRepository.save(passenger);

        passenger.setName("John Updated");
        MvcResult result = mockMvc.perform(put("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isOk())
                .andReturn();

        Passenger updated = objectMapper.readValue(result.getResponse().getContentAsString(), Passenger.class);
        assertEquals("John Updated", updated.getName());
    }

    @Test
    void testDeletePassenger() throws Exception {
        Passenger passenger = new Passenger("John Doe", Gender.MALE, 25);
        passenger = passengerRepository.save(passenger);

        mockMvc.perform(delete("/passenger")
                .param("userid", passenger.getId().toString()))
                .andExpect(status().isOk());

        assertFalse(passengerRepository.existsById(passenger.getId()));
    }

    @Test
    void testGetRowCount() throws Exception {
        Passenger passenger = new Passenger("John Doe", Gender.MALE, 25);
        passengerRepository.save(passenger);

        MvcResult result = mockMvc.perform(get("/passenger/rowCount"))
                .andExpect(status().isOk())
                .andReturn();

        ItemCount count = objectMapper.readValue(result.getResponse().getContentAsString(), ItemCount.class);
        assertEquals(1L, count.getCount());
    }

    @Test
    void testInvalidPassengerCreation() throws Exception {
        Passenger passenger = new Passenger("", Gender.MALE, 15); // Invalid age and empty name

        mockMvc.perform(post("/passenger")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passenger)))
                .andExpect(status().isBadRequest());
    }
}