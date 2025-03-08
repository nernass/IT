package com.github.kaiwinter.user.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.kaiwinter.user.User;
import com.github.kaiwinter.user.service.UserService;
import com.github.kaiwinter.user.view.UserView;

@ExtendWith(MockitoExtension.class)
public class UserIntegrationTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserView userView;

    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        testUsers = List.of(
            new User(0, "User 0"),
            new User(1, "User 1"),
            new User(2, "User 2"),
            new User(3, "User 3"),
            new User(4, "User 4")
        );
    }

    @Test
    void testUserViewIntegration() {
        // Given
        when(userService.getTopUsers(5)).thenReturn(testUsers);

        // When
        List<User> result = userView.getUsers();

        // Then
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(userService).getTopUsers(5);
        
        // Verify user data
        for (int i = 0; i < result.size(); i++) {
            User user = result.get(i);
            assertEquals(i, user.getId());
            assertEquals("User " + i, user.getName());
        }
    }

    @Test
    void testUserServiceDirectly() {
        // Given
        UserService realUserService = new UserService();

        // When
        List<User> users = realUserService.getTopUsers(3);

        // Then
        assertNotNull(users);
        assertEquals(3, users.size());
        for (int i = 0; i < users.size(); i++) {
            assertEquals(i, users.get(i).getId());
            assertEquals("User " + i, users.get(i).getName());
        }
    }

    @Test
    void testUserCreation() {
        // Given
        int id = 1;
        String name = "Test User";

        // When
        User user = new User(id, name);

        // Then
        assertEquals(id, user.getId());
        assertEquals(name, user.getName());
    }

    @Test
    void testEmptyUserList() {
        // Given
        when(userService.getTopUsers(0)).thenReturn(List.of());

        // When
        List<User> users = userService.getTopUsers(0);

        // Then
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }
}