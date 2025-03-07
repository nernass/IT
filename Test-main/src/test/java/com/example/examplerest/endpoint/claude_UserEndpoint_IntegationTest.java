package com.example.examplerest.integration;

import com.example.examplerest.dto.CreateUserDto;
import com.example.examplerest.dto.UserAuthDto;
import com.example.examplerest.model.Role;
import com.example.examplerest.model.User;
import com.example.examplerest.repository.UserRepository;
import com.example.examplerest.service.CurrencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CurrencyService currencyService;

    private User testUser;
    private CreateUserDto createUserDto;
    private UserAuthDto userAuthDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser = User.builder()
                .name("Test User")
                .email("test@test.com")
                .password("password123")
                .age(25)
                .surname("Tester")
                .role(Role.USER)
                .build();

        createUserDto = new CreateUserDto();
        createUserDto.setName("Test User");
        createUserDto.setEmail("test@test.com");
        createUserDto.setPassword("password123");
        createUserDto.setAge(25);
        createUserDto.setSurname("Tester");

        userAuthDto = new UserAuthDto();
        userAuthDto.setEmail("test@test.com");
        userAuthDto.setPassword("password123");

        HashMap<String, String> mockCurrencies = new HashMap<>();
        mockCurrencies.put("USD", "1.0");
        when(currencyService.getCurrencies()).thenReturn(mockCurrencies);
    }

    @Test
    void shouldRegisterNewUser() throws Exception {
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(createUserDto.getEmail()))
                .andExpect(jsonPath("$.name").value(createUserDto.getName()));
    }

    @Test
    void shouldNotRegisterDuplicateUser() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldAuthenticateUser() throws Exception {
        userRepository.save(testUser);

        mockMvc.perform(post("/user/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.email").value(testUser.getEmail()));
    }

    @Test
    void shouldNotAuthenticateWithWrongCredentials() throws Exception {
        userRepository.save(testUser);
        userAuthDto.setPassword("wrongpassword");

        mockMvc.perform(post("/user/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userAuthDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldValidateUserFields() throws Exception {
        createUserDto.setEmail(""); // Invalid email

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isBadRequest());
    }
}