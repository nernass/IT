package com.infoworks.lab.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infoworks.lab.controllers.rest.PassengerController;
import com.infoworks.lab.domain.entities.Passenger;
import com.infoworks.lab.services.iServices.PassengerService;
import com.infoworks.lab.webapp.config.TestJPAH2Config;
import com.infoworks.lab.webapp.config.BeanConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Import({TestJPAH2Config.class, BeanConfig.class})
public class PassengerIntegrationTest {

    @Autowired
    private PassengerController passengerController;

    @MockBean
    private PassengerService passengerService;

    @Autowired
    private ObjectMapper objectMapper;

    private Passenger passenger;

    @BeforeEach
    public void setup() {
        passenger = new Passenger("John Doe", Gender.MALE, 30);
    }

    @Test
    public void testAddPassengerSuccess() throws Exception {
        when(passengerService.add(any(Passenger.class))).thenReturn(passenger);

        Passenger result = passengerController.insert(passenger);

        assertEquals(passenger.getName(), result.getName());
        assertEquals(passenger.getAge(), result.getAge());
    }

    @Test
    public void testGetAllPassengers() throws Exception {
        List<Passenger> passengers = Arrays.asList(passenger);
        when(passengerService.findAll(anyInt(), anyInt())).thenReturn(passengers);

        List<Passenger> result = passengerController.query(0, 10);

        assertEquals(1, result.size());
        assertEquals(passenger.getName(), result.get(0).getName());
    }

    @Test
    public void testUpdatePassenger() throws Exception {
        when(passengerService.update(any(Passenger.class))).thenReturn(passenger);

        Passenger result = passengerController.update(passenger);

        assertEquals(passenger.getName(), result.getName());
        assertEquals(passenger.getAge(), result.getAge());
    }

    @Test
    public void testDeletePassenger() throws Exception {
        when(passengerService.remove(anyInt())).thenReturn(true);

        Boolean result = passengerController.delete(passenger.getId());

        assertEquals(true, result);
    }

    @Test
    public void testGetRowCount() throws Exception {
        when(passengerService.totalCount()).thenReturn(1L);

        String result = passengerController.getRowCount();

        ItemCount count = objectMapper.readValue(result, ItemCount.class);
        assertEquals(1L, count.getCount());
    }
}