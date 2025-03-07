package com.cat.digital.SampleSpringbootTemplate.clients;

import com.cat.digital.SampleSpringbootTemplate.clients.dto.response.DummyEmployeeResponse;
import com.cat.digital.SampleSpringbootTemplate.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Component
public class EmployeeClient {

    private RestTemplate restTemplate;
    private Config config;

    @Autowired
    public EmployeeClient(RestTemplate restTemplate, Config config) {
        this.restTemplate = restTemplate;
        this.config = config;
    }

    public DummyEmployeeResponse getAllEmployees() {
    String url = new StringBuilder(config.getEmployeeApiBaseUrl())
            .append(config.getAllEmployeesRelativeUrl())
            .toString();
        ResponseEntity<DummyEmployeeResponse> allEmployeesEntity = restTemplate.getForEntity(url,
                                                                                    DummyEmployeeResponse.class);
        return allEmployeesEntity.getBody();
    }
}
