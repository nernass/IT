package com.infoworks.lab.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.lab.controllers.rest.PassengerController;
import com.infoworks.lab.domain.entities.Passenger;
import com.infoworks.lab.domain.repositories.PassengerRepository;
import com.infoworks.lab.services.impl.PassengerServiceImpl;
import com.infoworks.lab.webapp.config.BeanConfig;
import com.infoworks.lab.webapp.config.JPAConfig;
import com.infoworks.lab.services.ServiceExecutionLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import({BeanConfig.class, JPAConfig.class, ServiceExecutionLogger.class})
public class PassengerIntegrationTest {

    @Autowired
    private PassengerController passengerController;

    @MockBean
    private PassengerRepository passengerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private PassengerServiceImpl passengerService;

    @BeforeEach
    public void setup() {
        passengerService = new PassengerServiceImpl(passengerRepository);
        passengerController = new PassengerController(passengerService, objectMapper);
    }

    @Test
    public void testAddPassenger() {
        Passenger passenger = new Passenger("John Doe", Gender.MALE, 30);
        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);

        Passenger result = passengerController.insert(passenger);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testUpdatePassenger() {
        Passenger passenger = new Passenger("John Doe", Gender.MALE, 30);
        when(passengerRepository.save(any(Passenger.class))).thenReturn(passenger);

        Passenger result = passengerController.update(passenger);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testDeletePassenger() {
        when(passengerRepository.existsById(any(Integer.class))).thenReturn(true);
        Mockito.doNothing().when(passengerRepository).deleteById(any(Integer.class));

        Boolean result = passengerController.delete(1);
        assertTrue(result);
    }

    @Test
    public void testFindPassengerById() {
        Passenger passenger = new Passenger("John Doe", Gender.MALE, 30);
        when(passengerRepository.findById(any(Integer.class))).thenReturn(Optional.of(passenger));

        Passenger result = passengerService.findByUserID(1);
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
    }

    @Test
    public void testGetRowCount() throws Exception {
        when(passengerRepository.count()).thenReturn(10L);

        String response = passengerController.getRowCount().getBody();
        ItemCount count = objectMapper.readValue(response, ItemCount.class);
        assertEquals(10L, count.getCount());
    }
}