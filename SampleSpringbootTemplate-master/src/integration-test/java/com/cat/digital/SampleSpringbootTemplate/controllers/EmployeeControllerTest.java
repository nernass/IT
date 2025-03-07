package com.cat.digital.SampleSpringbootTemplate.controllers;

import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import com.cat.digital.SampleSpringbootTemplate.dtos.response.EmployeeResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class EmployeeControllerTest {

    @MockBean
    private RestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void clientHasEmployeeData_getAllEmployees_successReturned() throws Exception {
        DummyEmployeeResponse dummyEmployeeResponse = DummyEmployeeResponse.builder()
                .status("success")
                .data(Arrays.asList(DummyEmployee.builder()
                                            .id("1")
                                            .employeeName("Test user")
                                            .employeeAge("24")
                                            .employeeSalary("110K")
                                            .employeeImage("bcd")
                                            .build()))
                .build();
        when(restTemplate.getForEntity("http://sometestapi/test-all-employees",
                                       DummyEmployeeResponse.class)).thenReturn(
                ResponseEntity.ok(dummyEmployeeResponse));

        List<EmployeeResponseDto> employeesEntity = this.testRestTemplate.getForObject(
                "http://localhost:" + port + "/employees", List.class);

        assertThat(employeesEntity.size(), is(1));
    }
}
