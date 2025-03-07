package com.sn.integration;

import com.sn.controller.EmployeeController;
import com.sn.model.Employee;
import com.sn.repository.EmployeeRepository;
import com.sn.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
    }

    @Test
    public void testGetEmployees() throws Exception {
        when(employeeService.getEmployees()).thenReturn(Arrays.asList(employee));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    public void testGetEmployeeById() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    public void testSaveEmployee() throws Exception {
        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    public void testGetEmployeeByEmail() throws Exception {
        when(employeeRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(employee));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }
}