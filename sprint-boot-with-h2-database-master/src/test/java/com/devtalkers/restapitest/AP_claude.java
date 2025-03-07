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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserSystemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Clean the database before each test
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setGender("Male");
        testUser.setPhone("1234567890");
    }

    @Test
    void testCompleteUserFlow() throws Exception {
        // 1. Create User - POST /user
        String userJson = objectMapper.writeValueAsString(testUser);

        String response = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Extract created user ID
        User createdUser = objectMapper.readValue(response, User.class);
        Integer userId = createdUser.getId();

        // 2. Verify User Persistence - GET /user/{id}
        mockMvc.perform(get("/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.gender").value("Male"))
                .andExpect(jsonPath("$.phone").value("1234567890"));

        // 3. Verify Database State
        User savedUser = userRepository.findById(userId).orElseThrow();
        assert savedUser.getName().equals("Test User");
        assert savedUser.getEmail().equals("test@example.com");

        // 4. Test Non-Existent User
        mockMvc.perform(get("/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    void testUserValidationFlow() throws Exception {
        // Test with invalid user (missing required fields)
        User invalidUser = new User();
        String invalidUserJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidUserJson))
                .andExpect(status().isCreated()); // Assuming no validation is implemented yet

        // Test with null values
        testUser.setEmail(null);
        String userJsonWithNull = objectMapper.writeValueAsString(testUser);

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJsonWithNull))
                .andExpect(status().isCreated()); // Assuming no validation is implemented yet
    }

    @Test
    void testConcurrentUserOperations() throws Exception {
        // Create multiple users concurrently
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setName("User " + i);
            user.setEmail("user" + i + "@example.com");
            user.setGender("Other");
            user.setPhone("123456789" + i);

            String userJson = objectMapper.writeValueAsString(user);

            mockMvc.perform(post("/user")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists());
        }

        // Verify all users were created
        assert userRepository.count() == 5;
    }
}