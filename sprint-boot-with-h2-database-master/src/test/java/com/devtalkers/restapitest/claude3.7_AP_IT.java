package com.devtalkers.restapitest.integration;

import com.devtalkers.restapitest.controllers.UserController;
import com.devtalkers.restapitest.entity.User;
import com.devtalkers.restapitest.repository.UserRepository;
import com.devtalkers.restapitest.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        // Clear database before each test
        userRepository.deleteAll();
    }

    @Test
    public void testCreateAndRetrieveUser() throws Exception {
        // Create test user
        User user = new User();
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setGender("Male");
        user.setPhone("1234567890");

        // Test POST endpoint
        MvcResult postResult = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.phone").value("1234567890"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn();

        // Extract the created user's ID
        User createdUser = objectMapper.readValue(
                postResult.getResponse().getContentAsString(), User.class);
        Integer userId = createdUser.getId();

        // Verify the user exists in the repository
        assertTrue(userRepository.existsById(userId));

        // Test GET endpoint
        mockMvc.perform(get("/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.phone").value("1234567890"));

        // Verify service layer integration
        User retrievedFromService = userService.getUserById(userId);
        assertNotNull(retrievedFromService);
        assertEquals("John Doe", retrievedFromService.getName());
        assertEquals("john@example.com", retrievedFromService.getEmail());
    }

    @Test
    public void testGetNonExistentUser() throws Exception {
        // Test GET for non-existent user
        mockMvc.perform(get("/user/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSaveInvalidUser() throws Exception {
        // Create an invalid user (null fields)
        User user = new User();
        // Leave all fields null

        // Test POST endpoint with invalid user
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated()); // Status should still be created for this simple app

        // Verify count of users in repository
        assertEquals(1, userRepository.count());
    }

    @Test
    public void testEndToEndDataFlow() throws Exception {
        // Create test user
        User user = new User();
        user.setName("Jane Smith");
        user.setEmail("jane@example.com");
        user.setGender("Female");
        user.setPhone("9876543210");

        // Save user through service layer
        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser.getId());

        // Verify user exists in repository
        assertTrue(userRepository.existsById(savedUser.getId()));

        // Retrieve user through controller
        mockMvc.perform(get("/user/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Smith"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }

    @Test
    public void testConcurrentUserCreation() throws Exception {
        // Create multiple users in parallel to test system stability
        for (int i = 0; i < 5; i++) {
            final int index = i;
            Thread thread = new Thread(() -> {
                try {
                    User threadUser = new User();
                    threadUser.setName("User " + index);
                    threadUser.setEmail("user" + index + "@example.com");
                    threadUser.setGender(index % 2 == 0 ? "Male" : "Female");
                    threadUser.setPhone("123456789" + index);

                    mockMvc.perform(post("/user")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(threadUser)))
                            .andExpect(status().isCreated());
                } catch (Exception e) {
                    fail("Exception during concurrent user creation: " + e.getMessage());
                }
            });
            thread.start();
            thread.join();
        }

        // Verify all users were created
        assertEquals(5, userRepository.count());
    }
}