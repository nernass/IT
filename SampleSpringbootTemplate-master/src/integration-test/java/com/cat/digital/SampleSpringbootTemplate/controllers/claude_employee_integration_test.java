package com.cat.digital.SampleSpringbootTemplate.controllers;

import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import com.cat.digital.SampleSpringbootTemplate.dtos.response.EmployeeResponseDto;
import com.cat.digital.SampleSpringbootTemplate.services.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private DummyEmployee dummyEmployee;
    private EmployeeResponseDto employeeResponseDto;
    private DummyEmployeeResponse dummyEmployeeResponse;

    @BeforeEach
    void setUp() {
        dummyEmployee = DummyEmployee.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary("50000")
                .employeeAge("30")
                .employeeImage("image.jpg")
                .build();

        employeeResponseDto = EmployeeResponseDto.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary("50000")
                .employeeAge("30")
                .employeeImage("image.jpg")
                .build();

        dummyEmployeeResponse = DummyEmployeeResponse.builder()
                .status("success")
                .data(Arrays.asList(dummyEmployee))
                .build();
    }

    @Test
    void shouldReturnEmployeesList() throws Exception {
        List<EmployeeResponseDto> employeeList = Arrays.asList(employeeResponseDto);
        when(employeeService.getAllEmployees()).thenReturn(employeeList);

        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$[0].employeeSalary").value("50000"))
                .andExpect(jsonPath("$[0].employeeAge").value("30"))
                .andExpect(jsonPath("$[0].employeeImage").value("image.jpg"));
    }

    @Test
    void shouldReturnEmptyListWhenNoEmployees() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}