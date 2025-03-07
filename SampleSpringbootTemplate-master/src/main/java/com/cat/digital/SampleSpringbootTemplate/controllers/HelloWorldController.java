package com.cat.digital.SampleSpringbootTemplate.controllers;

import com.cat.digital.SampleSpringbootTemplate.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
public class HelloWorldController {
    private Config config;

    @Autowired
    public HelloWorldController(Config config) {
        this.config = config;
    }

    @RequestMapping(value = "/sayHello", method = GET)
    public ResponseEntity<String> sayHello() {
        String message = new StringBuilder("Hello")
                .append(" ")
                .append(config.getEnvName())
                .append("!")
                .toString();
        return ResponseEntity.ok(message);
    }
}
