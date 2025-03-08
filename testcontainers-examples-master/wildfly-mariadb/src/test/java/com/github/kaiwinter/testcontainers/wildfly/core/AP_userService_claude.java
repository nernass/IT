package com.github.kaiwinter.testcontainers.wildfly.integration;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
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

@ExtendWith(ArquillianExtension.class)
@Testcontainers
public class UserIntegrationTest {

    @Container
    private static final MariaDBContainer<?> MARIA_DB = new MariaDBContainer<>("mariadb:10.5.8")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class)
                .addClass(User.class)
                .addClass(UserRepository.class)
                .addClass(UserService.class)
                .addAsResource("META-INF/persistence.xml");
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserTransaction userTransaction;

    @Inject
    private UserService userService;

    @Inject
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws Exception {
        // Clean up database before each test
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM User").executeUpdate();
        userTransaction.commit();
    }

    @Test
    void testFullIntegration() throws Exception {
        // Test scenario 1: Create users and calculate login sum
        userTransaction.begin();
        
        User user1 = new User();
        user1.setUsername("user1");
        user1.setLoginCount(5);
        userRepository.save(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setLoginCount(3);
        userRepository.save(user2);

        userTransaction.commit();

        // Verify service calculation
        assertEquals(8, userService.calculateSumOfLogins());

        // Test scenario 2: Reset login counts for non-admin users
        userTransaction.begin();
        
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setLoginCount(10);
        userRepository.save(adminUser);

        userRepository.resetLoginCountForUsers();
        userTransaction.commit();

        // Verify reset results
        assertEquals(0, userRepository.findByUsername("user1").getLoginCount());
        assertEquals(0, userRepository.findByUsername("user2").getLoginCount());
        assertEquals(10, userRepository.findByUsername("admin").getLoginCount());

        // Test scenario 3: Verify complete user management workflow
        userTransaction.begin();
        
        // Create and save new user
        User newUser = new User();
        newUser.setUsername("testUser");
        newUser.setLoginCount(7);
        User savedUser = userRepository.save(newUser);

        // Verify save operation
        assertNotNull(savedUser.getId());
        
        // Verify find operations
        User foundUser = userRepository.find(savedUser.getId());
        assertEquals("testUser", foundUser.getUsername());
        assertEquals(7, foundUser.getLoginCount());

        // Verify findAll operation
        assertTrue(userRepository.findAll().size() >= 4);

        // Delete user
        userRepository.delete(foundUser);
        userTransaction.commit();

        // Verify deletion
        userTransaction.begin();
        assertNull(userRepository.find(savedUser.getId()));
        userTransaction.commit();
    }
}