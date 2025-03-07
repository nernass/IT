package org.example.controller;

import org.example.service.TimeService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TimeControllerTest {

    private static TimeController timeController;

    @BeforeAll
    static void beforeAll() {
        timeController = new TimeController(new TimeService(Instant::now));
    }

    @Test
    void shouldReturnHttpOkWithServerTimeAsText() {
        // when & then
        assertThat(timeController.getCurrentServerTime())
                .matches(response -> response.getStatusCode().value() == 200)
                .matches(response -> !response.getBody().isBlank());
    }
}