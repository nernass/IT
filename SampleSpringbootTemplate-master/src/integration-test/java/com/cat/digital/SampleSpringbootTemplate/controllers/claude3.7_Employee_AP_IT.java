package com.cat.digital.SampleSpringbootTemplate.integration;

import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import com.cat.digital.SampleSpringbootTemplate.controllers.EmployeeController;
import com.cat.digital.SampleSpringbootTemplate.dtos.response.EmployeeResponseDto;
import com.cat.digital.SampleSpringbootTemplate.services.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private EmployeeController employeeController;

    private List<EmployeeResponseDto> mockEmployeeResponseList;
    private List<DummyEmployee> mockDummyEmployeeList;
    private DummyEmployeeResponse mockDummyEmployeeResponse;

    @BeforeEach
    void setUp() {
        // Setup test data - DummyEmployee objects
        mockDummyEmployeeList = Arrays.asList(
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

        // Create mock DummyEmployeeResponse that would come from external API
        mockDummyEmployeeResponse = DummyEmployeeResponse.builder()
                .status("success")
                .data(mockDummyEmployeeList)
                .build();

        // Create the EmployeeResponseDto objects that would be returned after
        // transformation
        mockEmployeeResponseList = mockDummyEmployeeList.stream()
                .map(emp -> EmployeeResponseDto.builder()
                        .id(emp.getId())
                        .employeeName(emp.getEmployeeName())
                        .employeeSalary(emp.getEmployeeSalary())
                        .employeeAge(emp.getEmployeeAge())
                        .employeeImage(emp.getEmployeeImage())
                        .build())
                .collect(Collectors.toList());
    }

    @Test
    public void testSuccessfulEmployeeDataFlow() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(mockEmployeeResponseList);

        // Act & Assert - Using MockMvc to test the HTTP endpoint
        mockMvc.perform(MockMvcRequestBuilders.get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].employeeName").value("John Doe"))
                .andExpect(jsonPath("$[0].employeeSalary").value("75000"))
                .andExpect(jsonPath("$[0].employeeAge").value("30"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].employeeName").value("Jane Smith"));

        // Verify service was called
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testControllerServiceIntegration() {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(mockEmployeeResponseList);

        // Act - Direct call to controller without HTTP
        ResponseEntity<List<EmployeeResponseDto>> response = employeeController.sayHello();

        // Assert - Verify the response structure and content
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(2, response.getBody().size());
        assertEquals("1", response.getBody().get(0).getId());
        assertEquals("John Doe", response.getBody().get(0).getEmployeeName());
        assertEquals("75000", response.getBody().get(0).getEmployeeSalary());
        assertEquals("30", response.getBody().get(0).getEmployeeAge());
        assertEquals("2", response.getBody().get(1).getId());
        assertEquals("Jane Smith", response.getBody().get(1).getEmployeeName());
    }

    @Test
    public void testEmptyEmployeeList() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        // Verify service was called
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testServiceFailure() throws Exception {
        // Arrange
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert - Testing error handling
        mockMvc.perform(MockMvcRequestBuilders.get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        // Verify service was called
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testEmployeeDataTransformation() throws Exception {
        // Arrange - Test the transformation from DummyEmployee to EmployeeResponseDto
        when(employeeService.getAllEmployees()).thenReturn(mockEmployeeResponseList);

        // Act & Assert - Verify the data is correctly transformed
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(mockDummyEmployeeList.get(0).getId()))
                .andExpect(jsonPath("$[0].employeeName").value(mockDummyEmployeeList.get(0).getEmployeeName()))
                .andExpect(jsonPath("$[0].employeeSalary").value(mockDummyEmployeeList.get(0).getEmployeeSalary()))
                .andExpect(jsonPath("$[0].employeeAge").value(mockDummyEmployeeList.get(0).getEmployeeAge()))
                .andExpect(jsonPath("$[1].id").value(mockDummyEmployeeList.get(1).getId()))
                .andExpect(jsonPath("$[1].employeeName").value(mockDummyEmployeeList.get(1).getEmployeeName()));

        // Verify service was called
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    public void testFieldMappingFromDummyToResponse() {
        // Arrange
        // This test verifies that the field mapping between DummyEmployee and
        // EmployeeResponseDto is correct
        DummyEmployee dummyEmployee = mockDummyEmployeeList.get(0);
        EmployeeResponseDto responseDto = mockEmployeeResponseList.get(0);

        // Assert - Checking field mapping integrity
        assertEquals(dummyEmployee.getId(), responseDto.getId());
        assertEquals(dummyEmployee.getEmployeeName(), responseDto.getEmployeeName());
        assertEquals(dummyEmployee.getEmployeeSalary(), responseDto.getEmployeeSalary());
        assertEquals(dummyEmployee.getEmployeeAge(), responseDto.getEmployeeAge());
        assertEquals(dummyEmployee.getEmployeeImage(), responseDto.getEmployeeImage());
    }

    @Test
    public void testEmployeeDataWithNullValues() throws Exception {
        // Arrange - Create employee with null values to test null handling
        List<EmployeeResponseDto> employeesWithNulls = Arrays.asList(
                EmployeeResponseDto.builder()
                        .id("3")
                        .employeeName(null) // null name
                        .employeeSalary("65000")
                        .employeeAge("28")
                        .employeeImage(null) // null image
                        .build());

        when(employeeService.getAllEmployees()).thenReturn(employeesWithNulls);

        // Act & Assert - Test handling of null values
        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value("3"))
                .andExpect(jsonPath("$[0].employeeName").doesNotExist())
                .andExpect(jsonPath("$[0].employeeSalary").value("65000"))
                .andExpect(jsonPath("$[0].employeeAge").value("28"))
                .andExpect(jsonPath("$[0].employeeImage").doesNotExist());

        // Verify service was called
        verify(employeeService, times(1)).getAllEmployees();
    }
}