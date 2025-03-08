package com.github.kaiwinter.testcontainers.wildfly.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
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
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;

@ExtendWith(ArquillianExtension.class)
@Testcontainers
public class UserServiceIntegrationTest {

    @Container
    private static final MariaDBContainer<?> mariaDBContainer = new MariaDBContainer<>("mariadb:10.5")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.sql"); // You'll need to create this SQL script

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
                .addClasses(
                        User.class,
                        UserRepository.class,
                        UserService.class)
                .addAsResource("META-INF/persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsLibraries(
                        Maven.resolver()
                                .loadPomFromFile("pom.xml")
                                .importRuntimeDependencies()
                                .resolve()
                                .withTransitivity()
                                .asFile());
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
    public void setup() throws Exception {
        // Clean up database before each test
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM User").executeUpdate();
        
        // Set up test data
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setLoginCount(10);
        entityManager.persist(adminUser);
        
        User regularUser = new User();
        regularUser.setUsername("user1");
        regularUser.setLoginCount(5);
        entityManager.persist(regularUser);
        
        User inactiveUser = new User();
        inactiveUser.setUsername("user2");
        inactiveUser.setLoginCount(0);
        entityManager.persist(inactiveUser);
        
        userTransaction.commit();
    }

    @Test
    public void testCalculateSumOfLogins() throws Exception {
        // Test the integration between UserService and UserRepository
        int totalLogins = userService.calculateSumOfLogins();
        
        // The sum should be 15 (10 + 5 + 0)
        assertEquals(15, totalLogins, "Total logins should be 15");
    }

    @Test
    public void testResetLoginCountForUsers() throws Exception {
        userTransaction.begin();
        // Execute the reset method
        userRepository.resetLoginCountForUsers();
        userTransaction.commit();
        
        // Verify admin login count is still 10
        User admin = userRepository.findByUsername("admin");
        assertEquals(10, admin.getLoginCount(), "Admin login count should remain unchanged");
        
        // Verify regular users' login counts are reset
        User user1 = userRepository.findByUsername("user1");
        assertEquals(0, user1.getLoginCount(), "Regular user login count should be reset to 0");
        
        User user2 = userRepository.findByUsername("user2");
        assertEquals(0, user2.getLoginCount(), "Regular user login count should be reset to 0");
        
        // Verify the sum calculation takes the reset into account
        assertEquals(10, userService.calculateSumOfLogins(), "After reset, total logins should be 10");
    }

    @Test
    public void testCRUDOperations() throws Exception {
        userTransaction.begin();
        
        // Create
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setLoginCount(3);
        
        userRepository.save(newUser);
        assertNotNull(newUser.getId(), "User should have an ID after saving");
        
        // Read
        User foundUser = userRepository.findByUsername("testuser");
        assertEquals("testuser", foundUser.getUsername(), "Should find the created user");
        assertEquals(3, foundUser.getLoginCount(), "Login count should match");
        
        // Update
        foundUser.setLoginCount(7);
        entityManager.flush();
        
        User updatedUser = userRepository.find(foundUser.getId());
        assertEquals(7, updatedUser.getLoginCount(), "Login count should be updated");
        
        // Delete
        userRepository.delete(updatedUser);
        userTransaction.commit();
        
        // Verify the user is deleted and the sum is recalculated correctly
        Collection<User> allUsers = userRepository.findAll();
        assertEquals(3, allUsers.size(), "Should have 3 users after deletion");
        
        // Verify UserService calculates the correct sum after all operations
        assertEquals(15, userService.calculateSumOfLogins(), "Total should reflect changes");
    }
    
    @Test
    public void testCompleteWorkflow() throws Exception {
        // Start with a clean database
        userTransaction.begin();
        entityManager.createQuery("DELETE FROM User").executeUpdate();
        userTransaction.commit();
        
        userTransaction.begin();
        // First, add users
        User admin = new User();
        admin.setUsername("admin");
        admin.setLoginCount(20);
        userRepository.save(admin);
        
        User root = new User();
        root.setUsername("root");
        root.setLoginCount(30);
        userRepository.save(root);
        
        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setUsername("user" + i);
            user.setLoginCount(i * 2); // 2, 4, 6, 8, 10
            userRepository.save(user);
        }
        userTransaction.commit();
        
        // Verify all users were saved
        Collection<User> allUsers = userRepository.findAll();
        assertEquals(7, allUsers.size(), "Should have 7 users in total");
        
        // Calculate sum - should be 20 + 30 + (2+4+6+8+10) = 80
        int totalLogins = userService.calculateSumOfLogins();
        assertEquals(80, totalLogins, "Total logins should be 80");
        
        // Reset login counts for non-privileged users
        userTransaction.begin();
        userRepository.resetLoginCountForUsers();
        userTransaction.commit();
        
        // Verify login counts after reset
        User adminAfter = userRepository.findByUsername("admin");
        assertEquals(20, adminAfter.getLoginCount(), "Admin login should still be 20");
        
        User rootAfter = userRepository.findByUsername("root");
        assertEquals(30, rootAfter.getLoginCount(), "Root login should still be 30");
        
        // Check that other users' logins were reset
        for (int i = 1; i <= 5; i++) {
            User user = userRepository.findByUsername("user" + i);
            assertEquals(0, user.getLoginCount(), "User" + i + " login should be reset to 0");
        }
        
        // Final sum check after reset: should be 20 + 30 = 50
        assertEquals(50, userService.calculateSumOfLogins(), "After reset, total logins should be 50");
    }
}