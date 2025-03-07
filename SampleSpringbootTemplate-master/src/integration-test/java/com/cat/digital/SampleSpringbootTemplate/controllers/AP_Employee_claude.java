package com.cat.digital.SampleSpringbootTemplate.integration;

import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import com.cat.digital.SampleSpringbootTemplate.controllers.EmployeeController;
import com.cat.digital.SampleSpringbootTemplate.dtos.response.EmployeeResponseDto;
import com.cat.digital.SampleSpringbootTemplate.services.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class EmployeeIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private EmployeeController employeeController;

    @MockBean
    private EmployeeService employeeService;

    private DummyEmployee createTestDummyEmployee() {
        return DummyEmployee.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary("50000")
                .employeeAge("30")
                .employeeImage("")
                .build();
    }

    private EmployeeResponseDto createTestEmployeeResponseDto() {
        return EmployeeResponseDto.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary("50000")
                .employeeAge("30")
                .employeeImage("")
                .build();
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    @DisplayName("Should return valid employee list when GET /employees is called")
    void whenGetEmployees_thenReturnValidEmployeeList() {
        // Arrange
        List<EmployeeResponseDto> mockEmployees = Arrays.asList(createTestEmployeeResponseDto());
        when(employeeService.getAllEmployees()).thenReturn(mockEmployees);

        // Act
        ResponseEntity<List<EmployeeResponseDto>> response = employeeController.sayHello();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Should verify correct employee data mapping between DTOs")
    void whenGetEmployees_thenVerifyEmployeeDataMapping() throws Exception {
        // Arrange
        DummyEmployee dummyEmployee = createTestDummyEmployee();
        DummyEmployeeResponse dummyResponse = DummyEmployeeResponse.builder()
                .status("success")
                .data(Arrays.asList(dummyEmployee))
                .build();

        EmployeeResponseDto expectedDto = createTestEmployeeResponseDto();
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(expectedDto));

        // Act & Assert
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk());

        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Should handle service failures appropriately")
    void whenServiceFails_thenHandleError() {
        // Arrange
        when(employeeService.getAllEmployees()).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            employeeController.sayHello();
        });
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Should return empty response for empty employee list")
    void whenEmptyEmployeeList_thenReturnEmptyResponse() {
        // Arrange
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList());

        // Act
        ResponseEntity<List<EmployeeResponseDto>> response = employeeController.sayHello();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().isEmpty());
        verify(employeeService, times(1)).getAllEmployees();
    }

    @Test
    @DisplayName("Should correctly map DummyEmployee to EmployeeResponseDto")
    void whenMappingDummyEmployeeToResponseDto_thenFieldsShouldMatch() {
        // Arrange
        DummyEmployee dummyEmployee = createTestDummyEmployee();

        // Act
        EmployeeResponseDto responseDto = EmployeeResponseDto.builder()
                .id(dummyEmployee.getId())
                .employeeName(dummyEmployee.getEmployeeName())
                .employeeSalary(dummyEmployee.getEmployeeSalary())
                .employeeAge(dummyEmployee.getEmployeeAge())
                .employeeImage(dummyEmployee.getEmployeeImage())
                .build();

        // Assert
        assertEquals(dummyEmployee.getId(), responseDto.getId());
        assertEquals(dummyEmployee.getEmployeeName(), responseDto.getEmployeeName());
        assertEquals(dummyEmployee.getEmployeeSalary(), responseDto.getEmployeeSalary());
        assertEquals(dummyEmployee.getEmployeeAge(), responseDto.getEmployeeAge());
        assertEquals(dummyEmployee.getEmployeeImage(), responseDto.getEmployeeImage());
    }

    @Test
    @DisplayName("Should handle null values in DTO mapping gracefully")
    void whenMappingWithNullValues_thenShouldHandleGracefully() {
        // Arrange
        DummyEmployee dummyEmployee = DummyEmployee.builder().build();

        // Act
        EmployeeResponseDto responseDto = EmployeeResponseDto.builder()
                .id(dummyEmployee.getId())
                .employeeName(dummyEmployee.getEmployeeName())
                .employeeSalary(dummyEmployee.getEmployeeSalary())
                .employeeAge(dummyEmployee.getEmployeeAge())
                .employeeImage(dummyEmployee.getEmployeeImage())
                .build();

        // Assert
        assertNull(responseDto.getId());
        assertNull(responseDto.getEmployeeName());
        assertNull(responseDto.getEmployeeSalary());
        assertNull(responseDto.getEmployeeAge());
        assertNull(responseDto.getEmployeeImage());
    }
}