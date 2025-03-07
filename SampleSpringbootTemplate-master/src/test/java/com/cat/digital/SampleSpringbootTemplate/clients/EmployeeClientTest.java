package com.cat.digital.SampleSpringbootTemplate.clients;

import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import com.cat.digital.SampleSpringbootTemplate.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeClientTest {
    private static final String BASE_URL = "baseUrl";
    private static final String TEST_EMPLOYEES_URL = "/test-Employees";
    @Mock
    private RestTemplate restTemplate;

    @Mock
    private Config config;

    private EmployeeClient employeeClient;
    @BeforeEach
    public void setUp(){
        when(config.getEmployeeApiBaseUrl()).thenReturn(BASE_URL);
        when(config.getAllEmployeesRelativeUrl()).thenReturn(TEST_EMPLOYEES_URL);
        employeeClient = new EmployeeClient(restTemplate, config);
    }

    @Test
    void clientHasData_getAllEmployees_dataReturned() {
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
        when(restTemplate.getForEntity("baseUrl/test-Employees", DummyEmployeeResponse.class)).thenReturn(
                ResponseEntity.ok(dummyEmployeeResponse));


        DummyEmployeeResponse allEmployees = employeeClient.getAllEmployees();
        assertThat(allEmployees.getData().size(), is(1));
        assertThat(allEmployees.getStatus(), is("success"));
    }
}