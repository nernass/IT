package com.cat.digital.SampleSpringbootTemplate.controllers;

import com.cat.digital.SampleSpringbootTemplate.dtos.response.EmployeeResponseDto;
import com.cat.digital.SampleSpringbootTemplate.services.EmployeeService;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerIntegrationTest {

    @MockBean
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    public void testGetAllEmployees_Success() throws Exception {
        EmployeeResponseDto employee1 = new EmployeeResponseDto("1", "John Doe", "50000", "30", "image1.jpg");
        EmployeeResponseDto employee2 = new EmployeeResponseDto("2", "Jane Doe", "60000", "25", "image2.jpg");
        List<EmployeeResponseDto> employees = Arrays.asList(employee1, employee2);

        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$[0].employeeSalary").value("50000"))
                .andExpect(jsonPath("$[0].employeeAge").value("30"))
                .andExpect(jsonPath("$[0].employeeImage").value("image1.jpg"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].employeeName").value("Jane Doe"))
                .andExpect(jsonPath("$[1].employeeSalary").value("60000"))
                .andExpect(jsonPath("$[1].employeeAge").value("25"))
                .andExpect(jsonPath("$[1].employeeImage").value("image2.jpg"));

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testGetAllEmployees_Failure() throws Exception {
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Service unavailable"));

        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testGetAllEmployees_EmptyList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verify(employeeService, times(1)).getAllEmployees();
    }
}