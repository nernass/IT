package com.springboot.springbootsecurity.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springbootsecurity.auth.model.Token;
import com.springboot.springbootsecurity.auth.model.dto.request.LoginRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenInvalidateRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenRefreshRequest;
import com.springboot.springbootsecurity.auth.model.dto.response.TokenResponse;
import com.springboot.springbootsecurity.auth.model.mapper.TokenToTokenResponseMapper;
import com.springboot.springbootsecurity.common.model.dto.response.CustomResponse;
import com.springboot.springbootsecurity.user.model.User;
import com.springboot.springbootsecurity.user.model.dto.request.UserRegisterRequest;
import com.springboot.springbootsecurity.user.service.UserLoginService;
import com.springboot.springbootsecurity.user.service.UserLogoutService;
import com.springboot.springbootsecurity.user.service.UserRefreshTokenService;
import com.springboot.springbootsecurity.user.service.UserRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class UserAuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserRegisterService userRegisterService;

    @Mock
    private UserLoginService userLoginService;

    @Mock
    private UserRefreshTokenService userRefreshTokenService;

    @Mock
    private UserLogoutService userLogoutService;

    @Spy
    private TokenToTokenResponseMapper tokenToTokenResponseMapper = TokenToTokenResponseMapper.initialize();

    @InjectMocks
    private UserAuthController userAuthController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(userAuthController).build();
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        // Given
        UserRegisterRequest registerRequest = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("12345678901")
                .build();

        User user = User.builder().email("test@example.com").build(); // Assuming User has a builder

        // When
        when(userRegisterService.registerUser(any(UserRegisterRequest.class))).thenReturn(user);

        // Then
        mockMvc.perform(post("/api/v1/authentication/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(userRegisterService, times(1)).registerUser(any(UserRegisterRequest.class));
    }

    @Test
    public void testLoginUser_Success() throws Exception {
        // Given
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        Token token = Token.builder()
                .accessToken("access-token")
                .accessTokenExpiresAt(1709132400000L)
                .refreshToken("refresh-token")
                .build();

        // When
        when(userLoginService.login(any(LoginRequest.class))).thenReturn(token);

        // Then
        mockMvc.perform(post("/api/v1/authentication/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", is("access-token")))
                .andExpect(jsonPath("$.data.accessTokenExpiresAt", is(1709132400000L)))
                .andExpect(jsonPath("$.data.refreshToken", is("refresh-token")));

        verify(userLoginService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    public void testRefreshToken_Success() throws Exception {
        // Given
        TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
        // Set properties for refreshRequest

        Token token = Token.builder()
                .accessToken("new-access-token")
                .accessTokenExpiresAt(1709132400000L)
                .refreshToken("new-refresh-token")
                .build();

        // When
        when(userRefreshTokenService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(token);

        // Then
        mockMvc.perform(post("/api/v1/authentication/user/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(refreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", is("new-access-token")))
                .andExpect(jsonPath("$.data.accessTokenExpiresAt", is(1709132400000L)))
                .andExpect(jsonPath("$.data.refreshToken", is("new-refresh-token")));

        verify(userRefreshTokenService, times(1)).refreshToken(any(TokenRefreshRequest.class));
    }

    @Test
    public void testLogout_Success() throws Exception {
        // Given
        TokenInvalidateRequest invalidateRequest = new TokenInvalidateRequest();
        // Set properties for invalidateRequest

        doNothing().when(userLogoutService).logout(any(TokenInvalidateRequest.class));

        // Then
        mockMvc.perform(post("/api/v1/authentication/user/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(userLogoutService, times(1)).logout(any(TokenInvalidateRequest.class));
    }

    @Test
    public void testRegisterUser_ValidationError() throws Exception {
        // Given
        UserRegisterRequest invalidRequest = UserRegisterRequest.builder()
                .email("invalid") // Invalid email
                .password("short") // Too short password
                .firstName("") // Blank first name
                .lastName("") // Blank last name
                .phoneNumber("123") // Too short phone number
                .build();

        // Then
        mockMvc.perform(post("/api/v1/authentication/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(userRegisterService, never()).registerUser(any(UserRegisterRequest.class));
    }

    @Test
    public void testMapper_TokenToTokenResponse() {
        // Given
        Token token = Token.builder()
                .accessToken("test-token")
                .accessTokenExpiresAt(1709132400000L)
                .refreshToken("test-refresh-token")
                .build();

        // When
        TokenResponse tokenResponse = tokenToTokenResponseMapper.map(token);

        // Then
        assert tokenResponse.getAccessToken().equals(token.getAccessToken());
        assert tokenResponse.getAccessTokenExpiresAt().equals(token.getAccessTokenExpiresAt());
        assert tokenResponse.getRefreshToken().equals(token.getRefreshToken());
    }
}