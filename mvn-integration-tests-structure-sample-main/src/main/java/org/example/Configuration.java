package org.example;

import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.function.Supplier;

@org.springframework.context.annotation.Configuration
public class Configuration {

    @Bean
    public Supplier<Instant> instantSupplier() {
        return Instant::now;
    }
}
