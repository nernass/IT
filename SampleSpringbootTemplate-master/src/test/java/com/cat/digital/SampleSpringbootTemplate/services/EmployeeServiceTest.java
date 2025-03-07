package com.cat.digital.SampleSpringbootTemplate.services;

import com.cat.digital.SampleSpringbootTemplate.clients.EmployeeClient;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import com.cat.digital.SampleSpringbootTemplate.dtos.response.EmployeeResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    private EmployeeService employeeService;

    @Mock
    private EmployeeClient employeeClient;

    @BeforeEach
    public void setUp() {
        employeeService = new EmployeeService(employeeClient);
    }

    @Test
    void clientHasEmployeesData_getAllEmployees_employeesListReturned() {
        DummyEmployeeResponse dummyEmployeeResponse = DummyEmployeeResponse.builder()
                .status("success")
                .data(Arrays.asList(DummyEmployee.builder()
                                            .id("1")
                                            .employeeName("Test user")
                                            .employeeAge("24")
                                            .employeeSalary("110K")
                                            .employeeImage("bcd")
                                            .build()))
                .build();

        Mockito.when(employeeClient.getAllEmployees()).thenReturn(dummyEmployeeResponse);
        List<EmployeeResponseDto> allEmployees = employeeService.getAllEmployees();

        assertThat(allEmployees.size(), is(1));
    }
}