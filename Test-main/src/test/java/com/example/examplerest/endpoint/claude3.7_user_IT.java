package com.example.examplerest.endpoint;

import com.example.examplerest.dto.CreateUserDto;
import com.example.examplerest.dto.UserAuthDto;
import com.example.examplerest.dto.UserFilterDto;
import com.example.examplerest.model.Role;
import com.example.examplerest.model.User;
import com.example.examplerest.repository.CustomUserRepo;
import com.example.examplerest.repository.UserRepository;
import com.example.examplerest.service.CurrencyService;
import com.example.examplerest.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class UserEndpointIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private CurrencyService currencyService;

    @MockBean
    private CustomUserRepo customUserRepo;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();

        // Mock currency service to return dummy data
        HashMap<String, String> currencies = new HashMap<>();
        currencies.put("USD", "1.0");
        when(currencyService.getCurrencies()).thenReturn(currencies);

        // Mock custom user repo to return an empty list
        when(customUserRepo.users(any(UserFilterDto.class))).thenReturn(Collections.emptyList());
    }

    @Test
    public void testRegisterUser() throws Exception {
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName("Test User");
        createUserDto.setSurname("Surname");
        createUserDto.setAge(30);
        createUserDto.setEmail("test@example.com");
        createUserDto.setPassword("password123");

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        // Verify user is saved in repository
        Optional<User> savedUser = userRepository.findByEmail("test@example.com");
        assert savedUser.isPresent();
        assert savedUser.get().getRole() == Role.USER;
    }

    @Test
    public void testRegisterDuplicateUser() throws Exception {
        // Create a user first
        User user = User.builder()
                .name("Existing User")
                .surname("Existing")
                .age(25)
                .email("existing@example.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Try to register with the same email
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setName("Another User");
        createUserDto.setSurname("Surname");
        createUserDto.setAge(30);
        createUserDto.setEmail("existing@example.com");
        createUserDto.setPassword("password123");

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    public void testUserAuthentication() throws Exception {
        // Create a user first
        User user = User.builder()
                .name("Auth User")
                .surname("Auth")
                .age(28)
                .email("auth@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Authenticate
        UserAuthDto authDto = new UserAuthDto();
        authDto.setEmail("auth@example.com");
        authDto.setPassword("password123");

        mockMvc.perform(post("/user/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authDto)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email", is("auth@example.com")))
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void testUserAuthenticationFailure() throws Exception {
        // Create a user first
        User user = User.builder()
                .name("Auth User")
                .surname("Auth")
                .age(28)
                .email("auth@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        // Try with wrong password
        UserAuthDto authDto = new UserAuthDto();
        authDto.setEmail("auth@example.com");
        authDto.setPassword("wrongpassword");

        mockMvc.perform(post("/user/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authDto)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetUsers() throws Exception {
        UserFilterDto filterDto = new UserFilterDto();
        filterDto.setName("Test");

        mockMvc.perform(get("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDto)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void testInvalidUserRegistration() throws Exception {
        // Email is required, so this should fail validation
        CreateUserDto invalidDto = new CreateUserDto();
        invalidDto.setName("Invalid User");
        invalidDto.setSurname("Invalid");
        invalidDto.setAge(30);
        invalidDto.setPassword("password123");
        // email is missing

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}