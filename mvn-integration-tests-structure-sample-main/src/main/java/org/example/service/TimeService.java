package org.example.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.function.Supplier;

@Service
public class TimeService {

    private final Supplier<Instant> instantSupplier;

    public TimeService(Supplier<Instant> instantSupplier) {
        this.instantSupplier = instantSupplier;
    }

    public String getCurrentTimeAsText() {
        return instantSupplier.get().toString();
    }
}
