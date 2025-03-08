// Required imports
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collections;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.kaiwinter.testcontainers.wildfly.core.UserService;
import com.github.kaiwinter.testcontainers.wildfly.db.UserRepository;
import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;

@ExtendWith(MockitoExtension.class)
public class IntegrationTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<User> typedQuery;

    @InjectMocks
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    public void setUp() {
        user1 = new User();
        user1.setId(1);
        user1.setUsername("user1");
        user1.setLoginCount(5);

        user2 = new User();
        user2.setId(2);
        user2.setUsername("user2");
        user2.setLoginCount(3);
    }

    @Test
    public void testCalculateSumOfLogins_Success() {
        when(entityManager.createQuery("SELECT u FROM User u", User.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Arrays.asList(user1, user2));

        int sumOfLogins = userService.calculateSumOfLogins();

        assertEquals(8, sumOfLogins);
    }

    @Test
    public void testCalculateSumOfLogins_PartialFailure() {
        when(entityManager.createQuery("SELECT u FROM User u", User.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenThrow(new RuntimeException("Database error"));

        assertThrows(RuntimeException.class, () -> userService.calculateSumOfLogins());
    }

    @Test
    public void testCalculateSumOfLogins_EdgeCase() {
        when(entityManager.createQuery("SELECT u FROM User u", User.class)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(Collections.emptyList());

        int sumOfLogins = userService.calculateSumOfLogins();

        assertEquals(0, sumOfLogins);
    }
}