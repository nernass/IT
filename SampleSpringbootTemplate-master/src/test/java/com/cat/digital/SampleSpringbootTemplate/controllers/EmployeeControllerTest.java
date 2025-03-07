package com.cat.digital.SampleSpringbootTemplate.controllers;

import com.cat.digital.SampleSpringbootTemplate.dtos.response.EmployeeResponseDto;
import com.cat.digital.SampleSpringbootTemplate.services.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest extends BaseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void noEmployeesExist_callAllEmployees_emptyListReturn() throws Exception {
        List<EmployeeResponseDto> allEmployees = new ArrayList<>();
        when(employeeService.getAllEmployees()).thenReturn(allEmployees);
        mockMvc.perform(get("/employees"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[]")));
    }

    @Test
    void employeesExist_callAllEmployees_employeesReturnWithSuccess() throws Exception {
        List<EmployeeResponseDto> allEmployees = Arrays.asList(EmployeeResponseDto.builder()
                                                                       .id("1")
                                                                       .employeeName("Test user")
                                                                       .employeeAge("24")
                                                                       .employeeSalary("110K")
                                                                       .employeeImage("bcd")
                                                                       .build());
        when(employeeService.getAllEmployees()).thenReturn(allEmployees);
        String expectedResponse = "[{\"id\":\"1\",\"employeeName\":\"Test user\",\"employeeSalary\":\"110K\",\"employeeAge\":\"24\",\"employeeImage\":\"bcd\"}]";
        mockMvc.perform(get("/employees"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(expectedResponse)));
    }
}
