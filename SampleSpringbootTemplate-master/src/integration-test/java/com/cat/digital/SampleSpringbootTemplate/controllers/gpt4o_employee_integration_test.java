package com.cat.digital.SampleSpringbootTemplate.controllers;

import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import com.cat.digital.SampleSpringbootTemplate.dtos.response.EmployeeResponseDto;
import com.cat.digital.SampleSpringbootTemplate.services.EmployeeService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerIntegrationTest {

    @MockBean
    private EmployeeService employeeService;

    @InjectMocks
    private EmployeeController employeeController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    public void testGetAllEmployees() throws Exception {
        DummyEmployee dummyEmployee = DummyEmployee.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary("50000")
                .employeeAge("30")
                .employeeImage("image.jpg")
                .build();

        DummyEmployeeResponse dummyEmployeeResponse = DummyEmployeeResponse.builder()
                .status("success")
                .data(Arrays.asList(dummyEmployee))
                .build();

        EmployeeResponseDto employeeResponseDto = EmployeeResponseDto.builder()
                .id("1")
                .employeeName("John Doe")
                .employeeSalary("50000")
                .employeeAge("30")
                .employeeImage("image.jpg")
                .build();

        List<EmployeeResponseDto> employeeResponseDtoList = Arrays.asList(employeeResponseDto);

        when(employeeService.getAllEmployees()).thenReturn(employeeResponseDtoList);

        mockMvc.perform(get("/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "[{'id':'1','employeeName':'John Doe','employeeSalary':'50000','employeeAge':'30','employeeImage':'image.jpg'}]"));
    }
}