package org.example.controller;

import org.example.service.TimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TimeController {

    private final TimeService timeService;

    public TimeController(TimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping("/time")
    public ResponseEntity<String> getCurrentServerTime() {
        return ResponseEntity.ok(timeService.getCurrentTimeAsText());
    }
}
