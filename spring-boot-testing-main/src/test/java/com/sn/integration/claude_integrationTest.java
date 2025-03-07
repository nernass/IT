package com.sn.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sn.model.Employee;
import com.sn.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    public void givenEmployeeObject_whenCreateEmployee_thenReturnSavedEmployee() throws Exception {
        // given
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();

        // when
        ResultActions response = mockMvc.perform(post("/api/employees/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employee)));

        // then
        response.andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee.getLastName())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())));
    }

    @Test
    public void givenListOfEmployees_whenGetAllEmployees_thenReturnEmployeesList() throws Exception {
        // given
        List<Employee> employees = new ArrayList<>();
        employees.add(Employee.builder().firstName("John").lastName("Doe").email("john@example.com").build());
        employees.add(Employee.builder().firstName("Jane").lastName("Doe").email("jane@example.com").build());
        employeeRepository.saveAll(employees);

        // when
        ResultActions response = mockMvc.perform(get("/api/employees/"));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", is(employees.size())));
    }

    @Test
    public void givenEmployeeId_whenGetEmployeeById_thenReturnEmployeeObject() throws Exception {
        // given
        Employee employee = Employee.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();
        employeeRepository.save(employee);

        // when
        ResultActions response = mockMvc.perform(get("/api/employees/{id}", employee.getId()));

        // then
        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.firstName", is(employee.getFirstName())))
                .andExpect(jsonPath("$.lastName", is(employee.getLastName())))
                .andExpect(jsonPath("$.email", is(employee.getEmail())));
    }

    @Test
    public void givenInvalidEmployeeId_whenGetEmployeeById_thenReturnNotFound() throws Exception {
        // given
        long employeeId = 1L;

        // when
        ResultActions response = mockMvc.perform(get("/api/employees/{id}", employeeId));

        // then
        response.andExpect(status().isNotFound())
                .andDo(print());
    }
}