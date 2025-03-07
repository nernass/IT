package com.example.examplerest.integration;

import com.example.examplerest.dto.CreateUserDto;
import com.example.examplerest.dto.UserAuthDto;
import com.example.examplerest.dto.UserAuthResponseDto;
import com.example.examplerest.endpoint.UserEndpoint;
import com.example.examplerest.model.User;
import com.example.examplerest.repository.UserRepository;
import com.example.examplerest.service.CurrencyService;
import com.example.examplerest.service.UserService;
import com.example.examplerest.util.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserEndpointIntegrationTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil tokenUtil;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private UserEndpoint userEndpoint;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterSuccess() {
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setEmail("test@example.com");
        createUserDto.setPassword("password");

        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(currencyService.getCurrencies()).thenReturn(new HashMap<>());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.save(any(User.class))).thenReturn(new User());

        ResponseEntity<?> response = userEndpoint.register(createUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).findByEmail(anyString());
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    public void testRegisterUserAlreadyExists() {
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setEmail("test@example.com");

        when(userService.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        ResponseEntity<?> response = userEndpoint.register(createUserDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userService, times(1)).findByEmail(anyString());
        verify(userService, times(0)).save(any(User.class));
    }

    @Test
    public void testAuthSuccess() {
        UserAuthDto userAuthDto = new UserAuthDto();
        userAuthDto.setEmail("test@example.com");
        userAuthDto.setPassword("password");

        User user = new User();
        user.setPassword("encodedPassword");

        when(userService.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenUtil.generateToken(anyString())).thenReturn("token");

        ResponseEntity<?> response = userEndpoint.auth(userAuthDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    public void testAuthFailure() {
        UserAuthDto userAuthDto = new UserAuthDto();
        userAuthDto.setEmail("test@example.com");
        userAuthDto.setPassword("password");

        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());

        ResponseEntity<?> response = userEndpoint.auth(userAuthDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).findByEmail(anyString());
        verify(passwordEncoder, times(0)).matches(anyString(), anyString());
    }
}