package com.devtalkers.restapitest.integration;

import com.devtalkers.restapitest.entity.User;
import com.devtalkers.restapitest.repository.UserRepository;
import com.devtalkers.restapitest.service.UserService;
import com.devtalkers.restapitest.controllers.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class UserIntegrationTest {

    @MockBean
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1);
        user.setName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setGender("Male");
        user.setPhone("1234567890");
    }

    @Test
    public void testGetUserById_Success() {
        when(userService.getUserById(1)).thenReturn(user);
        ResponseEntity<User> response = userController.getUserById(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    public void testSaveUser_Success() {
        when(userService.saveUser(any(User.class))).thenReturn(user);
        ResponseEntity<User> response = userController.saveUser(user);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(user, response.getBody());
    }

    @Test
    public void testGetUserById_NotFound() {
        when(userService.getUserById(1)).thenReturn(null);
        ResponseEntity<User> response = userController.getUserById(1);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testSaveUser_InvalidInput() {
        user.setEmail(null); // Invalid email
        when(userService.saveUser(any(User.class))).thenThrow(new IllegalArgumentException("Invalid email"));
        try {
            userController.saveUser(user);
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid email", e.getMessage());
        }
    }
}