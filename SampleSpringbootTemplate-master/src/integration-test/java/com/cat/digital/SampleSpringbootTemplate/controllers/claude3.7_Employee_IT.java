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

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private List<EmployeeResponseDto> employees;
    private DummyEmployeeResponse dummyResponse;

    @BeforeEach
    public void setUp() {
        // Set up test data
        employees = Arrays.asList(
                EmployeeResponseDto.builder()
                        .id("1")
                        .employeeName("John Doe")
                        .employeeSalary("75000")
                        .employeeAge("30")
                        .employeeImage("")
                        .build(),
                EmployeeResponseDto.builder()
                        .id("2")
                        .employeeName("Jane Smith")
                        .employeeSalary("85000")
                        .employeeAge("35")
                        .employeeImage("")
                        .build());

        // Setup the dummy response that would come from the external service
        List<DummyEmployee> dummyEmployees = Arrays.asList(
                DummyEmployee.builder()
                        .id("1")
                        .employeeName("John Doe")
                        .employeeSalary("75000")
                        .employeeAge("30")
                        .employeeImage("")
                        .build(),
                DummyEmployee.builder()
                        .id("2")
                        .employeeName("Jane Smith")
                        .employeeSalary("85000")
                        .employeeAge("35")
                        .employeeImage("")
                        .build());

        dummyResponse = DummyEmployeeResponse.builder()
                .status("success")
                .data(dummyEmployees)
                .build();
    }

    @Test
    public void getAllEmployees_ShouldReturnEmployeeList() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenReturn(employees);

        // When/Then
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("1")))
                .andExpect(jsonPath("$[0].employeeName", is("John Doe")))
                .andExpect(jsonPath("$[0].employeeSalary", is("75000")))
                .andExpect(jsonPath("$[0].employeeAge", is("30")))
                .andExpect(jsonPath("$[1].id", is("2")))
                .andExpect(jsonPath("$[1].employeeName", is("Jane Smith")))
                .andExpect(jsonPath("$[1].employeeSalary", is("85000")))
                .andExpect(jsonPath("$[1].employeeAge", is("35")));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void getAllEmployees_WhenServiceReturnsEmptyList_ShouldReturnEmptyArray() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void getAllEmployees_WhenServiceThrowsException_ShouldReturn500() throws Exception {
        // Given
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Service failure"));

        // When/Then
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getAllEmployees();
    }
}