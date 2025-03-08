package com.github.kaiwinter.testcontainers.wildfly.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.kaiwinter.testcontainers.wildfly.db.UserRepository;
import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;

@ExtendWith(MockitoExtension.class)
public class UserServiceIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User rootUser;
    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setUp() {
        rootUser = new User();
        rootUser.setUsername("root");
        rootUser.setLoginCount(5);

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setLoginCount(3);

        normalUser = new User();
        normalUser.setUsername("user");
        normalUser.setLoginCount(1);
    }

    @Test
    void testCalculateSumOfLogins() {
        Collection<User> users = Arrays.asList(rootUser, adminUser, normalUser);
        when(userRepository.findAll()).thenReturn(users);

        int sumOfLogins = userService.calculateSumOfLogins();

        assertEquals(9, sumOfLogins);
    }

    @Test
    void testFindUserById() {
        when(userRepository.find(1)).thenReturn(rootUser);

        User user = userRepository.find(1);

        assertEquals("root", user.getUsername());
        assertEquals(5, user.getLoginCount());
    }

    @Test
    void testFindUserByUsername() {
        when(userRepository.findByUsername("admin")).thenReturn(adminUser);

        User user = userRepository.findByUsername("admin");

        assertEquals("admin", user.getUsername());
        assertEquals(3, user.getLoginCount());
    }

    @Test
    void testSaveUser() {
        when(userRepository.save(normalUser)).thenReturn(normalUser);

        User savedUser = userRepository.save(normalUser);

        assertEquals("user", savedUser.getUsername());
        assertEquals(1, savedUser.getLoginCount());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).delete(normalUser);

        userRepository.delete(normalUser);

        verify(userRepository, times(1)).delete(normalUser);
    }

    @Test
    void testResetLoginCountForUsers() {
        doNothing().when(userRepository).resetLoginCountForUsers();

        userRepository.resetLoginCountForUsers();

        verify(userRepository, times(1)).resetLoginCountForUsers();
    }
}