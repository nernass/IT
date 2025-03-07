package com.sn.integration;

import com.sn.controller.EmployeeController;
import com.sn.model.Employee;
import com.sn.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class EmployeeControllerIntegrationTest {

    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    public void testGetEmployees() throws Exception {
        Employee employee1 = new Employee(1L, "John", "Doe", "john.doe@example.com");
        Employee employee2 = new Employee(2L, "Jane", "Doe", "jane.doe@example.com");

        when(employeeService.getEmployees()).thenReturn(Arrays.asList(employee1, employee2));

        mockMvc.perform(get("/api/employees/"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{'id':1,'firstName':'John','lastName':'Doe','email':'john.doe@example.com'},{'id':2,'firstName':'Jane','lastName':'Doe','email':'jane.doe@example.com'}]"));
    }

    @Test
    public void testGetEmployeeById() throws Exception {
        Employee employee = new Employee(1L, "John", "Doe", "john.doe@example.com");

        when(employeeService.getEmployeeById(anyLong())).thenReturn(employee);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(
                        content().json("{'id':1,'firstName':'John','lastName':'Doe','email':'john.doe@example.com'}"));
    }

    @Test
    public void testSaveEmployee() throws Exception {
        Employee employee = new Employee(1L, "John", "Doe", "john.doe@example.com");

        when(employeeService.saveEmployee(any(Employee.class))).thenReturn(employee);

        mockMvc.perform(post("/api/employees/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(
                        content().json("{'id':1,'firstName':'John','lastName':'Doe','email':'john.doe@example.com'}"));
    }

    @Test
    public void testTestEmployees() throws Exception {
        mockMvc.perform(get("/api/employees/test"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .json("{'id':1,'firstName':'Subhasish','lastName':'Nag','email':'subhasish.nag1@gmail.com'}"));
    }
}