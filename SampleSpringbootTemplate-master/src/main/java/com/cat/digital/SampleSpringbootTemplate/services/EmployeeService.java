package com.cat.digital.SampleSpringbootTemplate.services;

import com.cat.digital.SampleSpringbootTemplate.clients.EmployeeClient;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.DummyEmployee;
import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import com.cat.digital.SampleSpringbootTemplate.dtos.response.EmployeeResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmployeeService.class);

    private EmployeeClient employeeClient;

    @Autowired
    public EmployeeService(EmployeeClient employeeClient) {
        this.employeeClient = employeeClient;
    }

    public List<EmployeeResponseDto> getAllEmployees(){
        LOGGER.debug("getAllEmployees <START>");
        DummyEmployeeResponse allEmployees = employeeClient.getAllEmployees();
        LOGGER.debug("getAllEmployees <END>");
        return allEmployees.getData().stream()
                .map(this::mapEmployeeResponse)
                .collect(Collectors.toList());
    }

    private EmployeeResponseDto mapEmployeeResponse(DummyEmployee dummyEmployee){
        return EmployeeResponseDto.builder()
                .id(dummyEmployee.getId())
                .employeeName(dummyEmployee.getEmployeeName())
                .employeeAge(dummyEmployee.getEmployeeAge())
                .employeeSalary(dummyEmployee.getEmployeeSalary())
                .employeeImage(dummyEmployee.getEmployeeImage())
                .build();
    }
}
