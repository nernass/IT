package com.github.kaiwinter.testcontainers.wildfly.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.kaiwinter.testcontainers.wildfly.core.UserService;
import com.github.kaiwinter.testcontainers.wildfly.db.UserRepository;
import com.github.kaiwinter.testcontainers.wildfly.db.entity.User;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ExtendWith(ArquillianExtension.class)
@Testcontainers
public class UserServiceIntegrationTest {

    @Container
    private static final MariaDBContainer<?> MARIADB = new MariaDBContainer<>("mariadb:10.6")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepository;

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClass(User.class)
                .addClass(UserRepository.class)
                .addClass(UserService.class)
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @BeforeEach
    @Transactional
    public void setup() {
        // Clean up database before each test
        entityManager.createQuery("DELETE FROM User").executeUpdate();

        // Create test users
        User user1 = new User();
        user1.setUsername("user1");
        user1.setLoginCount(5);
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setLoginCount(10);
        userRepository.save(user2);

        User admin = new User();
        admin.setUsername("admin");
        admin.setLoginCount(20);
        userRepository.save(admin);

        User root = new User();
        root.setUsername("root");
        root.setLoginCount(50);
        userRepository.save(root);
    }

    @Test
    @Transactional
    public void testCalculateSumOfLogins() {
        // When
        int totalLogins = userService.calculateSumOfLogins();

        // Then
        assertEquals(85, totalLogins);
    }

    @Test
    @Transactional
    public void testFindByUsername() {
        // When
        User user = userRepository.findByUsername("user1");

        // Then
        assertNotNull(user);
        assertEquals("user1", user.getUsername());
        assertEquals(5, user.getLoginCount());
    }

    @Test
    @Transactional
    public void testFindByUsername_NotFound() {
        // When/Then
        assertThrows(NoResultException.class, () -> {
            userRepository.findByUsername("nonexistent");
        });
    }

    @Test
    @Transactional
    public void testResetLoginCountForUsers() {
        // Given
        assertEquals(5, userRepository.findByUsername("user1").getLoginCount());
        assertEquals(10, userRepository.findByUsername("user2").getLoginCount());
        assertEquals(20, userRepository.findByUsername("admin").getLoginCount());
        assertEquals(50, userRepository.findByUsername("root").getLoginCount());

        // When
        userRepository.resetLoginCountForUsers();

        // Then
        assertEquals(0, userRepository.findByUsername("user1").getLoginCount());
        assertEquals(0, userRepository.findByUsername("user2").getLoginCount());
        assertEquals(20, userRepository.findByUsername("admin").getLoginCount());
        assertEquals(50, userRepository.findByUsername("root").getLoginCount());
    }

    @Test
    @Transactional
    public void testFindAll() {
        // When
        Collection<User> allUsers = userRepository.findAll();

        // Then
        assertEquals(4, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> "user1".equals(u.getUsername())));
        assertTrue(allUsers.stream().anyMatch(u -> "user2".equals(u.getUsername())));
        assertTrue(allUsers.stream().anyMatch(u -> "admin".equals(u.getUsername())));
        assertTrue(allUsers.stream().anyMatch(u -> "root".equals(u.getUsername())));
    }

    @Test
    @Transactional
    public void testSaveAndFind() {
        // Given
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setLoginCount(1);

        // When
        userRepository.save(newUser);
        int id = newUser.getId();
        User foundUser = userRepository.find(id);

        // Then
        assertNotNull(foundUser);
        assertEquals("newuser", foundUser.getUsername());
        assertEquals(1, foundUser.getLoginCount());
    }

    @Test
    @Transactional
    public void testDelete() {
        // Given
        Collection<User> before = userRepository.findAll();
        assertEquals(4, before.size());

        User user = userRepository.findByUsername("user1");

        // When
        userRepository.delete(user);

        // Then
        Collection<User> after = userRepository.findAll();
        assertEquals(3, after.size());
        assertTrue(after.stream().noneMatch(u -> "user1".equals(u.getUsername())));
        assertThrows(NoResultException.class, () -> userRepository.findByUsername("user1"));
    }
}