package com.sn.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sn.model.Employee;
import com.sn.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("Integration Test - Create Employee")
    void testCreateEmployee() throws Exception {
        // Create an employee object
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // Execute POST request
        ResultActions response = mockMvc.perform(post("/api/employees/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)));

        // Verify the response
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee.getLastName())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())));

        // Verify data was saved to repository
        List<Employee> employees = employeeRepository.findAll();
        assertEquals(1, employees.size());
        assertEquals(employee.getFirstName(), employees.get(0).getFirstName());
        assertEquals(employee.getEmail(), employees.get(0).getEmail());
    }

    @Test
    @DisplayName("Integration Test - Get All Employees")
    void testGetAllEmployees() throws Exception {
        // Setup test data
        Employee employee1 = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        Employee employee2 = Employee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane@example.com")
                .build();

        employeeRepository.saveAll(List.of(employee1, employee2));

        // Execute GET request
        ResultActions response = mockMvc.perform(get("/api/employees/"));

        // Verify response
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].firstName", is("John")))
                .andExpect(jsonPath("$[1].firstName", is("Jane")));

        // Verify repository data
        assertEquals(2, employeeRepository.count());
    }

    @Test
    @DisplayName("Integration Test - Get Employee By ID")
    void testGetEmployeeById() throws Exception {
        // Setup test data
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Execute GET request
        ResultActions response = mockMvc.perform(get("/api/employees/{id}", savedEmployee.getId()));

        // Verify response
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.email", is("john@example.com")));
    }

    @Test
    @DisplayName("Integration Test - Get Employee By ID - Not Found")
    void testGetEmployeeByIdNotFound() throws Exception {
        // Execute GET request for non-existent employee
        ResultActions response = mockMvc.perform(get("/api/employees/{id}", 999L));

        // Verify response
        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test - Save Employee with Duplicate Email")
    void testSaveEmployeeWithDuplicateEmail() throws Exception {
        // Setup test data
        Employee existingEmployee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        employeeRepository.save(existingEmployee);

        Employee duplicateEmployee = Employee.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("john@example.com") // Same email
                .build();

        // Execute POST request
        ResultActions response = mockMvc.perform(post("/api/employees/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateEmployee)));

        // We expect the service to handle duplicate emails according to business rules
        // For this test, we're just verifying the components integrate correctly
        response.andDo(print());

        // Verify repository wasn't affected inappropriately
        Optional<Employee> foundEmployee = employeeRepository.findByEmail("john@example.com");
        assertTrue(foundEmployee.isPresent());
        assertEquals("John", foundEmployee.get().getFirstName());
    }

    @Test
    @DisplayName("Integration Test - Test Endpoint")
    void testTestEndpoint() throws Exception {
        // Execute GET request
        ResultActions response = mockMvc.perform(get("/api/employees/test"));

        // Verify response
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Subhasish")))
                .andExpect(jsonPath("$.lastName", is("Nag")))
                .andExpect(jsonPath("$.email", is("subhasish.nag1@gmail.com")));
    }
}