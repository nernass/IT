import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.kaiwinter.user.User;
import com.github.kaiwinter.user.service.UserService;
import com.github.kaiwinter.user.view.UserView;

public class UserIntegrationTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserView userView;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUsers_Success() {
        // Arrange
        List<User> mockUsers = List.of(new User(1, "User 1"), new User(2, "User 2"), new User(3, "User 3"), new User(4, "User 4"), new User(5, "User 5"));
        when(userService.getTopUsers(5)).thenReturn(mockUsers);

        // Act
        List<User> users = userView.getUsers();

        // Assert
        assertEquals(5, users.size());
        assertEquals("User 1", users.get(0).getName());
        assertEquals("User 5", users.get(4).getName());
    }

    @Test
    public void testGetUsers_PartialFailure() {
        // Arrange
        when(userService.getTopUsers(5)).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        try {
            userView.getUsers();
        } catch (RuntimeException e) {
            assertEquals("Service failure", e.getMessage());
        }
    }

    @Test
    public void testGetUsers_EdgeCase() {
        // Arrange
        List<User> mockUsers = List.of();
        when(userService.getTopUsers(0)).thenReturn(mockUsers);

        // Act
        List<User> users = userView.getUsers();

        // Assert
        assertEquals(0, users.size());
    }
}