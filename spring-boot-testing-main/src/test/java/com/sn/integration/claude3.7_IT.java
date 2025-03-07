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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class EmployeeControllerIntegrationTest {

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
    @DisplayName("Integration Test for Save Employee operation")
    public void givenEmployeeObject_whenCreateEmployee_thenReturnSavedEmployee() throws Exception {
        // given - precondition or setup
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // when - action or the behavior that we are testing
        ResultActions response = mockMvc.perform(post("/api/employees/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee.getLastName())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())));
    }

    @Test
    @DisplayName("Integration Test for Get All Employees operation")
    public void givenListOfEmployees_whenGetAllEmployees_thenReturnEmployeesList() throws Exception {
        // given - precondition or setup
        List<Employee> employees = new ArrayList<>();
        employees.add(Employee.builder().firstName("John").lastName("Doe").email("john@example.com").build());
        employees.add(Employee.builder().firstName("Jane").lastName("Doe").email("jane@example.com").build());
        employeeRepository.saveAll(employees);

        // when - action or the behavior that we are testing
        ResultActions response = mockMvc.perform(get("/api/employees/"));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(employees.size())));
    }

    @Test
    @DisplayName("Integration Test for Get Employee By ID operation")
    public void givenEmployeeId_whenGetEmployeeById_thenReturnEmployeeObject() throws Exception {
        // given - precondition or setup
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
        employeeRepository.save(employee);

        // when - action or the behavior that we are testing
        ResultActions response = mockMvc.perform(get("/api/employees/{id}", employee.getId()));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee.getLastName())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())));
    }

    @Test
    @DisplayName("Integration Test for Get Employee By ID - Not Found scenario")
    public void givenInvalidEmployeeId_whenGetEmployeeById_thenReturnNotFound() throws Exception {
        // given - precondition or setup
        long employeeId = 1L;

        // when - action or the behavior that we are testing
        ResultActions response = mockMvc.perform(get("/api/employees/{id}", employeeId));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration Test for Test Employee endpoint")
    public void whenTestEmployees_thenReturnDefaultEmployee() throws Exception {
        // when - action or the behavior that we are testing
        ResultActions response = mockMvc.perform(get("/api/employees/test"));

        // then - verify the output
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.firstName", is("Subhasish")))
                .andExpect(jsonPath("$.lastName", is("Nag")))
                .andExpect(jsonPath("$.email", is("subhasish.nag1@gmail.com")));
    }
}