package com.example.examplerest.service;

import com.example.examplerest.model.User;
import com.example.examplerest.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    public void findByEmail() {
        testAddUsers();

        Optional<User> expected = userRepository.findByEmail("user@gmail.com");
        Optional<User> actual = userService.findByEmail("user@gmail.com");
        assertEquals(expected, actual);
    }

    @Test
    public void save() {
        User user = User.builder()
                .id(1)
                .name("user")
                .surname("user")
                .email("suer@gamil.com")
                .password("user")
                .build();
        when(userRepository.save(any())).thenReturn(user);

        userService.save(User.builder()
                .name("user")
                .surname("user")
                .email("suer@gamil.com")
                .password("user")
                .build());

        verify(userRepository, times(1)).save(any());
    }

    private void testAddUsers() {
        userRepository.save(User.builder()
                .name("user")
                .surname("suer")
                .email("user@gmail.com")
                .password("user")
                .build());
        userRepository.save(User.builder()
                .name("user2")
                .surname("suer2")
                .email("user2@gmail.com")
                .password("user2")
                .build());
    }

}