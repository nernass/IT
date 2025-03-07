package com.example.examplerest.integration;

import com.example.examplerest.dto.CreateUserDto;
import com.example.examplerest.dto.UserAuthDto;
import com.example.examplerest.model.Role;
import com.example.examplerest.model.User;
import com.example.examplerest.repository.UserRepository;
import com.example.examplerest.service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private CurrencyService currencyService;

    @Test
    public void testUserRegistrationAndAuthentication() throws Exception {
        // Setup test data
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName("Test User");
        createUserDto.setEmail("test@test.com");
        createUserDto.setPassword("password123");
        createUserDto.setAge(25);
        createUserDto.setSurname("Tester");

        // Mock currency service
        HashMap<String, String> mockCurrencies = new HashMap<>();
        mockCurrencies.put("USD", "1.0");
        when(currencyService.getCurrencies()).thenReturn(mockCurrencies);

        // Test registration
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@test.com"));

        // Verify user was saved in repository
        Optional<User> savedUser = userRepository.findByEmail("test@test.com");
        assert savedUser.isPresent();
        assert savedUser.get().getRole() == Role.USER;

        // Test authentication
        UserAuthDto authDto = new UserAuthDto();
        authDto.setEmail("test@test.com");
        authDto.setPassword("password123");

        mockMvc.perform(post("/user/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value("test@test.com"));
    }

    @Test
    public void testDuplicateUserRegistration() throws Exception {
        // Setup existing user
        User existingUser = User.builder()
                .name("Existing User")
                .email("existing@test.com")
                .password("password123")
                .role(Role.USER)
                .build();
        userRepository.save(existingUser);

        // Try to register with same email
        CreateUserDto duplicateUserDto = new CreateUserDto();
        duplicateUserDto.setName("Duplicate User");
        duplicateUserDto.setEmail("existing@test.com");
        duplicateUserDto.setPassword("password456");

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUserDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testAuthenticationWithInvalidCredentials() throws Exception {
        // Setup user
        User user = User.builder()
                .name("Auth Test User")
                .email("auth@test.com")
                .password("$2a$10$correctPasswordHash")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Test with wrong password
        UserAuthDto wrongAuthDto = new UserAuthDto();
        wrongAuthDto.setEmail("auth@test.com");
        wrongAuthDto.setPassword("wrongpassword");

        mockMvc.perform(post("/user/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongAuthDto)))
                .andExpect(status().isNoContent());
    }
}