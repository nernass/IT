package com.github.kaiwinter.user.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

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
        // Create test data
        testUsers = List.of(
            new User(0, "User 0"),
            new User(1, "User 1"),
            new User(2, "User 2"),
            new User(3, "User 3"),
            new User(4, "User 4")
        );
    }
    
    @Test
    void testUserViewGetsUsersFromService() {
        // Arrange
        when(userService.getTopUsers(5)).thenReturn(testUsers);
        
        // Act
        List<User> result = userView.getUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals(0, result.get(0).getId());
        assertEquals("User 0", result.get(0).getName());
        assertEquals(4, result.get(4).getId());
        assertEquals("User 4", result.get(4).getName());
    }
    
    @Test
    void testUserServiceCreatesCorrectNumberOfUsers() {
        // Arrange
        UserService realUserService = new UserService();
        
        // Act
        List<User> result = realUserService.getTopUsers(3);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        for (int i = 0; i < result.size(); i++) {
            User user = result.get(i);
            assertEquals(i, user.getId());
            assertEquals("User " + i, user.getName());
        }
    }
    
    @Test
    void testUserObjectCorrectlyStoresData() {
        // Arrange & Act
        User user = new User(42, "Test User");
        
        // Assert
        assertEquals(42, user.getId());
        assertEquals("Test User", user.getName());
    }
    
    @Test
    void testEmptyUserList() {
        // Arrange
        when(userService.getTopUsers(5)).thenReturn(List.of());
        
        // Act
        List<User> result = userView.getUsers();
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}