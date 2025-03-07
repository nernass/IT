package com.example.examplerest.integration;

import com.example.examplerest.dto.*;
import com.example.examplerest.endpoint.UserEndpoint;
import com.example.examplerest.mapper.UserMapper;
import com.example.examplerest.model.Role;
import com.example.examplerest.model.User;
import com.example.examplerest.repository.CustomUserRepo;
import com.example.examplerest.repository.UserRepository;
import com.example.examplerest.service.CurrencyService;
import com.example.examplerest.service.UserService;
import com.example.examplerest.util.JwtTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserEndpoint userEndpoint;
    
    @MockBean
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    @MockBean
    private CustomUserRepo customUserRepo;
    
    @MockBean
    private UserMapper userMapper;
    
    @MockBean
    private PasswordEncoder passwordEncoder;
    
    @MockBean
    private JwtTokenUtil jwtTokenUtil;
    
    @MockBean
    private CurrencyService currencyService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private User testUser;
    private CreateUserDto createUserDto;
    private UserResponseDto userResponseDto;
    private UserAuthDto authDto;
    
    @BeforeEach
    public void setup() {
        testUser = User.builder()
                .id(1)
                .name("John")
                .surname("Doe")
                .email("john.doe@example.com")
                .password("hashedPassword")
                .age(30)
                .role(Role.USER)
                .build();
                
        createUserDto = new CreateUserDto();
        createUserDto.setName("John");
        createUserDto.setSurname("Doe");
        createUserDto.setEmail("john.doe@example.com");
        createUserDto.setPassword("password123");
        createUserDto.setAge(30);
        
        userResponseDto = new UserResponseDto();
        userResponseDto.setId(1);
        userResponseDto.setName("John");
        userResponseDto.setSurname("Doe");
        userResponseDto.setEmail("john.doe@example.com");
        
        authDto = new UserAuthDto();
        authDto.setEmail("john.doe@example.com");
        authDto.setPassword("password123");

        // Configure common mocks
        HashMap<String, String> mockCurrencies = new HashMap<>();
        mockCurrencies.put("USD", "1.0");
        when(currencyService.getCurrencies()).thenReturn(mockCurrencies);
    }
    
    // DIRECT COMPONENT INTERACTION TESTS
    
    @Test
    public void testRegisterUser_Success() {
        // Setup mocks to simulate the flow: Controller -> Service -> Repository
        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userMapper.map(any(CreateUserDto.class))).thenReturn(testUser);
        when(userService.save(any(User.class))).thenReturn(testUser);
        when(userMapper.map(any(User.class))).thenReturn(userResponseDto);
        
        // Execute
        ResponseEntity<?> response = userEndpoint.register(createUserDto);
        
        // Verify interactions
        verify(userService).findByEmail("john.doe@example.com");
        verify(userMapper).map(createUserDto);
        verify(passwordEncoder).encode("password123");
        verify(userService).save(testUser);
        verify(userMapper).map(testUser);
        verify(currencyService).getCurrencies();
        
        // Assert response
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponseDto, response.getBody());
    }
    
    @Test
    public void testRegisterUser_UserAlreadyExists() {
        // Setup mocks for the existing user scenario
        when(userService.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        // Execute
        ResponseEntity<?> response = userEndpoint.register(createUserDto);
        
        // Verify
        verify(userService).findByEmail("john.doe@example.com");
        verifyNoInteractions(passwordEncoder);
        verifyNoMoreInteractions(userService);
        
        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    public void testAuthenticateUser_Success() {
        // Setup for successful authentication
        when(userService.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenUtil.generateToken(anyString())).thenReturn("jwt-token-example");
        when(userMapper.map(any(User.class))).thenReturn(userResponseDto);
        
        // Execute
        ResponseEntity<?> response = userEndpoint.auth(authDto);
        
        // Verify
        verify(userService).findByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtTokenUtil).generateToken("john.doe@example.com");
        verify(userMapper).map(testUser);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
    
    @Test
    public void testAuthenticateUser_WrongCredentials() {
        // Setup for wrong credentials
        when(userService.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        
        // Execute
        ResponseEntity<?> response = userEndpoint.auth(authDto);
        
        // Verify
        verify(userService).findByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verifyNoInteractions(jwtTokenUtil);
        
        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    public void testGetUsers() {
        // Setup
        UserFilterDto filterDto = new UserFilterDto();
        List<User> userList = Arrays.asList(testUser);
        List<UserResponseDto> userResponseDtoList = Arrays.asList(userResponseDto);
        
        when(customUserRepo.users(any(UserFilterDto.class))).thenReturn(userList);
        when(userMapper.map(anyList())).thenReturn(userResponseDtoList);
        
        // Execute
        ResponseEntity<List<UserResponseDto>> response = userEndpoint.getUsers(filterDto);
        
        // Verify
        verify(customUserRepo).users(filterDto);
        verify(userMapper).map(userList);
        
        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userResponseDtoList, response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(userResponseDto, response.getBody().get(0));
    }
    
    // END-TO-END MVC INTEGRATION TESTS
    
    @Test
    public void testRegisterUser_EndToEnd_Success() throws Exception {
        // Setup test data and behavior
        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userService.save(any(User.class))).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        
        // Perform the HTTP request and validate response
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    public void testRegisterUser_EndToEnd_UserExists() throws Exception {
        // Setup service behavior for existing user
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));

        // Perform the HTTP request and validate conflict response
        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testAuthenticateUser_EndToEnd_Success() throws Exception {
        // Setup service behavior for successful auth
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(true);
        when(jwtTokenUtil.generateToken("john.doe@example.com")).thenReturn("test-jwt-token");
        when(userMapper.map(any(User.class))).thenReturn(userResponseDto);

        // Perform the HTTP request and validate successful auth response
        mockMvc.perform(post("/user/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-jwt-token"))
                .andExpect(jsonPath("$.user").exists());
    }

    @Test
    public void testAuthenticateUser_EndToEnd_BadCredentials() throws Exception {
        // Setup service behavior for failed authentication
        when(userService.findByEmail("john.doe@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password123", "hashedPassword")).thenReturn(false);

        // Perform the HTTP request and validate no content response
        mockMvc.perform(post("/user/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authDto)))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetUsers_EndToEnd() throws Exception {
        // Setup test data
        UserFilterDto filterDto = new UserFilterDto();
        
        List<User> users = new ArrayList<>();
        users.add(testUser);
        users.add(User.builder()
                .id(2)
                .name("Jane")
                .surname("Doe")
                .email("jane@example.com")
                .age(28)
                .role(Role.USER)
                .build());

        List<UserResponseDto> responseDtos = new ArrayList<>();
        responseDtos.add(userResponseDto);
        
        UserResponseDto secondUserDto = new UserResponseDto();
        secondUserDto.setId(2);
        secondUserDto.setName("Jane");
        secondUserDto.setEmail("jane@example.com");
        secondUserDto.setSurname("Doe");
        responseDtos.add(secondUserDto);

        // Mock repository and mapper behavior
        when(customUserRepo.users(any(UserFilterDto.class))).thenReturn(users);
        when(userMapper.map(anyList())).thenReturn(responseDtos);

        // Perform the HTTP request and validate response
        mockMvc.perform(get("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("John"))
                .andExpect(jsonPath("$[1].name").value("Jane"));
    }
    
    // COMPONENT INTERACTIONS WITH SIMULATED FAILURES
    
    @Test
    public void testRegisterUser_ServiceFailure() {
        // Setup mocks to simulate service failure
        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userMapper.map(any(CreateUserDto.class))).thenReturn(testUser);
        when(userService.save(any(User.class))).thenThrow(new RuntimeException("Database error"));
        
        // Execute and verify exception is propagated
        assertThrows(RuntimeException.class, () -> {
            userEndpoint.register(createUserDto);
        });
        
        // Verify interactions still occurred
        verify(userService).findByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userMapper).map(createUserDto);
        verify(userService).save(testUser);
    }
    
    @Test
    public void testAuthenticateUser_UserNotFound() {
        // Simulate user not found in database
        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
        
        // Execute
        ResponseEntity<?> response = userEndpoint.auth(authDto);
        
        // Verify
        verify(userService).findByEmail("john.doe@example.com");
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtTokenUtil);
        
        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }
    
    @Test
    public void testGetUsers_RepositoryFailure() {
        // Setup
        UserFilterDto filterDto = new UserFilterDto();
        when(customUserRepo.users(any(UserFilterDto.class))).thenThrow(new RuntimeException("Repository error"));
        
        // Execute and verify exception is propagated
        assertThrows(RuntimeException.class, () -> {
            userEndpoint.getUsers(filterDto);
        });
        
        // Verify interaction occurred
        verify(customUserRepo).users(filterDto);
    }
    
    // EDGE CASE TESTS
    
    @Test
    public void testRegisterUser_NullEmail() {
        // Setup
        CreateUserDto invalidDto = new CreateUserDto();
        invalidDto.setName("John");
        invalidDto.setSurname("Doe");
        invalidDto.setEmail(null); // Invalid email
        invalidDto.setPassword("password123");
        
        // Execute and expect validation failure
        assertThrows(Exception.class, () -> {
            userEndpoint.register(invalidDto);
        });
    }
    
    @Test
    public void testAuthenticateUser_EmptyPassword() {
        // Setup
        UserAuthDto invalidAuthDto = new UserAuthDto();
        invalidAuthDto.setEmail("john.doe@example.com");
        invalidAuthDto.setPassword(""); // Empty password
        
        // Execute and expect validation failure
        assertThrows(Exception.class, () -> {
            userEndpoint.auth(invalidAuthDto);
        });
    }
}