package com.example.examplerest.endpoint;

import com.example.examplerest.dto.CreateUserDto;
import com.example.examplerest.dto.UserAuthDto;
import com.example.examplerest.dto.UserAuthResponseDto;
import com.example.examplerest.dto.UserResponseDto;
import com.example.examplerest.mapper.UserMapper;
import com.example.examplerest.model.Role;
import com.example.examplerest.model.User;
import com.example.examplerest.repository.CustomUserRepo;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserEndpointIntegrationTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenUtil tokenUtil;

    @Mock
    private CurrencyService currencyService;

    @Mock
    private CustomUserRepo customUserRepo;

    @InjectMocks
    private UserEndpoint userEndpoint;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser() {
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setEmail("test@example.com");
        createUserDto.setPassword("password");

        when(userService.findByEmail(anyString())).thenReturn(Optional.empty());
        when(currencyService.getCurrencies()).thenReturn(new HashMap<>());
        when(userMapper.map(any(CreateUserDto.class))).thenReturn(new User());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.save(any(User.class))).thenReturn(new User());
        when(userMapper.map(any(User.class))).thenReturn(new UserResponseDto());

        ResponseEntity<?> response = userEndpoint.register(createUserDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).findByEmail(anyString());
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    public void testAuthUser() {
        UserAuthDto userAuthDto = new UserAuthDto();
        userAuthDto.setEmail("test@example.com");
        userAuthDto.setPassword("password");

        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userService.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(tokenUtil.generateToken(anyString())).thenReturn("token");
        when(userMapper.map(any(User.class))).thenReturn(new UserResponseDto());

        ResponseEntity<?> response = userEndpoint.auth(userAuthDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).findByEmail(anyString());
    }

    @Test
    public void testGetUsers() {
        when(customUserRepo.users(any())).thenReturn(List.of(new User()));
        when(userMapper.map(anyList())).thenReturn(List.of(new UserResponseDto()));

        ResponseEntity<List<UserResponseDto>> response = userEndpoint.getUsers(new UserFilterDto());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(customUserRepo, times(1)).users(any());
    }
}