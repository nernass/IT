package com.github.kaiwinter.user.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.kaiwinter.user.User;
import com.github.kaiwinter.user.service.UserService;
import com.github.kaiwinter.user.view.UserView;

@ExtendWith(MockitoExtension.class)
public class UserIntegrationTest {

    @Mock
    private UserService mockUserService;
    
    @InjectMocks
    private UserView userView;
    
    @Spy
    private UserService spyUserService = new UserService();
    
    private List<User> testUsers;
    
    @BeforeEach
    void setUp() {
        testUsers = Arrays.asList(
            new User(1, "Test User 1"),
            new User(2, "Test User 2"),
            new User(3, "Test User 3")
        );
    }
    
    @Test
    void testCompleteIntegration_Success() {
        // Test with real service implementation (spy)
        UserView realView = new UserView();
        Mockito.setField(realView, "userService", spyUserService);
        
        // Execute flow through components
        List<User> result = realView.getUsers();
        
        // Verify interactions and results
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(spyUserService).getTopUsers(5);
        
        // Verify data structure is correct
        for (int i = 0; i < result.size(); i++) {
            assertEquals(i, result.get(i).getId());
            assertEquals("User " + i, result.get(i).getName());
        }
    }
    
    @Test
    void testWithMockService_CustomUserList() {
        // Set up mock to return our test users
        when(mockUserService.getTopUsers(anyInt())).thenReturn(testUsers);
        
        // Execute the view's method that uses the service
        List<User> result = userView.getUsers();
        
        // Verify service was called with correct parameter
        verify(mockUserService).getTopUsers(5);
        
        // Verify view returns what the service provided
        assertEquals(testUsers, result);
        assertEquals(3, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals("Test User 1", result.get(0).getName());
    }
    
    @Test
    void testWithEmptyUserList() {
        // Set up mock to return empty list (edge case)
        when(mockUserService.getTopUsers(anyInt())).thenReturn(Collections.emptyList());
        
        // Execute and verify
        List<User> result = userView.getUsers();
        
        verify(mockUserService).getTopUsers(5);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testUserCreation() {
        // Test User object construction and getters
        User user = new User(42, "Integration Test User");
        
        assertEquals(42, user.getId());
        assertEquals("Integration Test User", user.getName());
    }
    
    @Test
    void testUserServiceDirectly_VariousUserCounts() {
        // Test the service with different count parameters
        UserService service = new UserService();
        
        List<User> oneUser = service.getTopUsers(1);
        assertEquals(1, oneUser.size());
        
        List<User> tenUsers = service.getTopUsers(10);
        assertEquals(10, tenUsers.size());
        
        List<User> zeroUsers = service.getTopUsers(0);
        assertEquals(0, zeroUsers.size());
    }
}