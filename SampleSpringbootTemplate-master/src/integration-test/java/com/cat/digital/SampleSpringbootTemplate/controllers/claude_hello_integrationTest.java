package com.cat.digital.SampleSpringbootTemplate.integration;

import com.cat.digital.SampleSpringbootTemplate.SampleSpringbootTemplateApplication;
import com.cat.digital.SampleSpringbootTemplate.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = SampleSpringbootTemplateApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class HelloWorldIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private Config config;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }

    @Test
    void sayHello_ShouldReturnExpectedMessageWithEnvironment() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/sayHello",
                String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isEqualTo("Hello " + config.getEnvName() + "!");
    }

    @Test
    void sayHello_WithInvalidPath_ShouldReturn404() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/invalidPath",
                String.class);

        // Then
        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}