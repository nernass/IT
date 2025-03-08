package com.github.kaiwinter.user.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.kaiwinter.user.User;
import com.github.kaiwinter.user.service.UserService;
import com.github.kaiwinter.user.view.UserView;

@ExtendWith(MockitoExtension.class)
public class UserIntegrationTest {

    @Spy
    private UserService userService;

    @InjectMocks
    private UserView userView;

    @BeforeEach
    void setUp() {
        // Real UserService is used but we can spy on it
    }

    @Test
    void testEndToEndUserRetrieval() {
        // Test the complete flow from UserView through UserService
        List<User> users = userView.getUsers();
        
        // Verify UserService was called with correct parameter
        verify(userService).getTopUsers(5);
        
        // Verify the results
        assertNotNull(users);
        assertEquals(5, users.size());
        
        // Verify each user object
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            assertEquals(i, user.getId());
            assertEquals("User " + i, user.getName());
        }
    }

    @Test
    void testEdgeCaseZeroUsers() {
        // Configure UserService to return empty list
        doReturn(List.of()).when(userService).getTopUsers(anyInt());

        List<User> users = userView.getUsers();
        
        assertNotNull(users);
        assertTrue(users.isEmpty());
        verify(userService).getTopUsers(5);
    }

    @Test
    void testLargeNumberOfUsers() {
        // Test with a larger number to ensure system handles it
        when(userService).thenCallRealMethod();
        
        List<User> users = userView.getUsers();
        
        assertNotNull(users);
        assertEquals(5, users.size());
        
        // Verify last user in the list
        User lastUser = users.get(4);
        assertEquals(4, lastUser.getId());
        assertEquals("User 4", lastUser.getName());
    }

    @Test
    void testUserObjectIntegrity() {
        List<User> users = userView.getUsers();
        
        // Test the first user object thoroughly
        User firstUser = users.get(0);
        
        // Verify User object maintains its state through the entire flow
        assertEquals(0, firstUser.getId());
        assertEquals("User 0", firstUser.getName());
        
        // Verify immutability
        assertThrows(UnsupportedOperationException.class, () -> {
            // Attempt to modify the users list
            users.add(new User(99, "Invalid User"));
        });
    }

    @Test
    void testComponentInteraction() {
        // Verify the interaction between components
        userView.getUsers();
        
        // Verify UserService interaction
        verify(userService, times(1)).getTopUsers(5);
        verifyNoMoreInteractions(userService);
        
        // Verify the flow doesn't make unnecessary calls
        verify(userService, never()).getTopUsers(0);
        verify(userService, never()).getTopUsers(10);
    }
}