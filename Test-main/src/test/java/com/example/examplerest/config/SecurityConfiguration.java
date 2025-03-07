package com.example.examplerest.config;

import com.example.examplerest.model.Role;
import com.example.examplerest.model.User;
import com.example.examplerest.security.CurrentUser;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;

@TestConfiguration
public class SecurityConfiguration {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        User user = new User(1,"poxos","poxosyan", "poxos@gmail.com", "poxos", Role.USER);
        CurrentUser basicUser = new CurrentUser(user);

        return new InMemoryUserDetailsManager(Arrays.asList(
                basicUser
        ));
    }
}