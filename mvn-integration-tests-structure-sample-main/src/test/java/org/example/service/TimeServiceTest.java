package org.example.service;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TimeServiceTest {

    @Test
    void shouldPassWhenTimeServiceCurrentTimeAsText() {
        // given
        final var currentTime = Instant.now();
        final var timeService = new TimeService(() -> currentTime);

        // when
        final var actual = timeService.getCurrentTimeAsText();

        // then
        assertThat(actual).isEqualTo(currentTime.toString());
    }
}