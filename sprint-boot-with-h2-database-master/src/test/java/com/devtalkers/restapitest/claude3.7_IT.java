package com.devtalkers.restapitest;

import com.devtalkers.restapitest.controllers.UserController;
import com.devtalkers.restapitest.entity.User;
import com.devtalkers.restapitest.repository.UserRepository;
import com.devtalkers.restapitest.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    public void setup() {
        // Create a test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setGender("Male");
        testUser.setPhone("1234567890");
    }

    @AfterEach
    public void cleanup() {
        // Clean up the test data
        userRepository.deleteAll();
    }

    @Test
    public void testCreateAndRetrieveUser() throws Exception {
        // Create user via API
        String userJson = objectMapper.writeValueAsString(testUser);

        MvcResult createResult = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.gender").value(testUser.getGender()))
                .andExpect(jsonPath("$.phone").value(testUser.getPhone()))
                .andReturn();

        // Extract created user ID
        String createResponseJson = createResult.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(createResponseJson, User.class);
        Integer userId = createdUser.getId();

        assertNotNull(userId, "User ID should not be null");

        // Retrieve user via API using ID
        mockMvc.perform(get("/user/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value(testUser.getName()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.gender").value(testUser.getGender()))
                .andExpect(jsonPath("$.phone").value(testUser.getPhone()));

        // Verify user exists in repository
        assertTrue(userRepository.findById(userId).isPresent(),
                "User should exist in repository");

        // Verify user service returns correct user
        User retrievedUser = userService.getUserById(userId);
        assertEquals(testUser.getName(), retrievedUser.getName());
        assertEquals(testUser.getEmail(), retrievedUser.getEmail());
        assertEquals(testUser.getGender(), retrievedUser.getGender());
        assertEquals(testUser.getPhone(), retrievedUser.getPhone());
    }

    @Test
    public void testGetNonExistentUser() throws Exception {
        // Try to get a user with a non-existent ID
        mockMvc.perform(get("/user/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSaveUserWithInvalidData() throws Exception {
        // Create a user with invalid or missing data
        User invalidUser = new User();
        // Leave required fields empty

        String userJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated()); // Currently the API accepts invalid data
                                                  // In a real application, you might want to add validation
    }

    @Test
    public void testUserServiceAndRepositoryInteraction() {
        // Save user via service
        User savedUser = userService.saveUser(testUser);

        // Verify user was saved in repository
        assertTrue(userRepository.findById(savedUser.getId()).isPresent());

        // Verify data integrity
        User userFromRepo = userRepository.findById(savedUser.getId()).get();
        assertEquals(testUser.getName(), userFromRepo.getName());
        assertEquals(testUser.getEmail(), userFromRepo.getEmail());
        assertEquals(testUser.getGender(), userFromRepo.getGender());
        assertEquals(testUser.getPhone(), userFromRepo.getPhone());
    }
}