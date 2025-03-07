package com.cat.digital.SampleSpringbootTemplate.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
public class Config {
    @Value("${pm.env.name:}")
    private String envName;

    @Value("${employee.api.base.url}")
    private String employeeApiBaseUrl;

    @Value("${employee.api.all-employees.url}")
    private String allEmployeesRelativeUrl;
}
