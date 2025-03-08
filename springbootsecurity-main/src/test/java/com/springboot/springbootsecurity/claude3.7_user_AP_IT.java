package com.springboot.springbootsecurity.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springbootsecurity.auth.model.Token;
import com.springboot.springbootsecurity.auth.model.dto.request.LoginRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenInvalidateRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenRefreshRequest;
import com.springboot.springbootsecurity.auth.model.dto.response.TokenResponse;
import com.springboot.springbootsecurity.auth.model.mapper.TokenToTokenResponseMapper;
import com.springboot.springbootsecurity.user.controller.UserAuthController;
import com.springboot.springbootsecurity.user.model.User;
import com.springboot.springbootsecurity.user.model.dto.request.UserRegisterRequest;
import com.springboot.springbootsecurity.user.service.UserLoginService;
import com.springboot.springbootsecurity.user.service.UserLogoutService;
import com.springboot.springbootsecurity.user.service.UserRefreshTokenService;
import com.springboot.springbootsecurity.user.service.UserRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthenticationFlowIntegrationTest {

    private MockMvc mockMvc;

    @Mock
    private UserRegisterService userRegisterService;

    @Mock
    private UserLoginService userLoginService;

    @Mock
    private UserRefreshTokenService userRefreshTokenService;

    @Mock
    private UserLogoutService userLogoutService;

    @InjectMocks
    private UserAuthController userAuthController;

    @Captor
    private ArgumentCaptor<UserRegisterRequest> userRegisterRequestCaptor;

    @Captor
    private ArgumentCaptor<LoginRequest> loginRequestCaptor;

    @Captor
    private ArgumentCaptor<TokenRefreshRequest> tokenRefreshRequestCaptor;

    @Captor
    private ArgumentCaptor<TokenInvalidateRequest> tokenInvalidateRequestCaptor;

    @Captor
    private ArgumentCaptor<Token> tokenCaptor;

    private TokenToTokenResponseMapper tokenToTokenResponseMapper;
    private ObjectMapper objectMapper;
    private UserRegisterRequest userRegisterRequest;
    private LoginRequest loginRequest;
    private TokenRefreshRequest tokenRefreshRequest;
    private TokenInvalidateRequest tokenInvalidateRequest;
    private Token token;
    private TokenResponse tokenResponse;
    private User user;

    @BeforeEach
    void setUp() {
        // Initialize ObjectMapper
        objectMapper = new ObjectMapper();

        // Initialize real mapper instance
        tokenToTokenResponseMapper = TokenToTokenResponseMapper.initialize();

        // Configure controller with real mapper for testing
        userAuthController = new UserAuthController(
                userRegisterService,
                userLoginService,
                userRefreshTokenService,
                userLogoutService);

        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(userAuthController).build();

        // Setup test data
        userRegisterRequest = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("12345678901")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        tokenRefreshRequest = new TokenRefreshRequest();
        tokenRefreshRequest.setRefreshToken("refresh-token-123");

        tokenInvalidateRequest = new TokenInvalidateRequest();
        tokenInvalidateRequest.setRefreshToken("refresh-token-123");

        token = Token.builder()
                .accessToken("access-token-123")
                .accessTokenExpiresAt(1738245228000L) // Some future timestamp
                .refreshToken("refresh-token-123")
                .build();

        tokenResponse = TokenResponse.builder()
                .accessToken("access-token-123")
                .accessTokenExpiresAt(1738245228000L)
                .refreshToken("refresh-token-123")
                .build();

        user = new User(); // Assuming User class has necessary fields
    }

    @Test
    public void testUserRegistration() throws Exception {
        // Given
        doNothing().when(userRegisterService).registerUser(any(UserRegisterRequest.class));

        // When
        ResultActions response = mockMvc.perform(post("/api/v1/authentication/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegisterRequest)));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(userRegisterService).registerUser(userRegisterRequestCaptor.capture());
        UserRegisterRequest capturedRequest = userRegisterRequestCaptor.getValue();
        assertEquals(userRegisterRequest.getEmail(), capturedRequest.getEmail());
        assertEquals(userRegisterRequest.getPassword(), capturedRequest.getPassword());
        assertEquals(userRegisterRequest.getFirstName(), capturedRequest.getFirstName());
    }

    @Test
    public void testUserLogin() throws Exception {
        // Given
        given(userLoginService.login(any(LoginRequest.class))).willReturn(token);

        // When
        ResultActions response = mockMvc.perform(post("/api/v1/authentication/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", is(token.getAccessToken())))
                .andExpect(jsonPath("$.data.refreshToken", is(token.getRefreshToken())));

        verify(userLoginService).login(loginRequestCaptor.capture());
        LoginRequest capturedRequest = loginRequestCaptor.getValue();
        assertEquals(loginRequest.getEmail(), capturedRequest.getEmail());
        assertEquals(loginRequest.getPassword(), capturedRequest.getPassword());
    }

    @Test
    public void testTokenRefresh() throws Exception {
        // Given
        given(userRefreshTokenService.refreshToken(any(TokenRefreshRequest.class))).willReturn(token);

        // When
        ResultActions response = mockMvc.perform(post("/api/v1/authentication/user/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRefreshRequest)));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", is(token.getAccessToken())))
                .andExpect(jsonPath("$.data.refreshToken", is(token.getRefreshToken())));

        verify(userRefreshTokenService).refreshToken(tokenRefreshRequestCaptor.capture());
        TokenRefreshRequest capturedRequest = tokenRefreshRequestCaptor.getValue();
        assertEquals(tokenRefreshRequest.getRefreshToken(), capturedRequest.getRefreshToken());
    }

    @Test
    public void testUserLogout() throws Exception {
        // Given
        doNothing().when(userLogoutService).logout(any(TokenInvalidateRequest.class));

        // When
        ResultActions response = mockMvc.perform(post("/api/v1/authentication/user/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenInvalidateRequest)));

        // Then
        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(userLogoutService).logout(tokenInvalidateRequestCaptor.capture());
        TokenInvalidateRequest capturedRequest = tokenInvalidateRequestCaptor.getValue();
        assertEquals(tokenInvalidateRequest.getRefreshToken(), capturedRequest.getRefreshToken());
    }

    @Test
    public void testInvalidUserRegistration() throws Exception {
        // Given
        UserRegisterRequest invalidRequest = UserRegisterRequest.builder()
                .email("invalid") // Invalid email format
                .password("123") // Too short password
                .firstName("") // Empty first name
                .lastName("") // Empty last name
                .phoneNumber("123") // Too short phone number
                .build();

        // When
        ResultActions response = mockMvc.perform(post("/api/v1/authentication/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)));

        // Then
        response.andExpect(status().isBadRequest());
        verify(userRegisterService, never()).registerUser(any());
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        // Given
        given(userLoginService.login(any(LoginRequest.class)))
                .willThrow(new RuntimeException("Invalid credentials"));

        // When
        ResultActions response = mockMvc.perform(post("/api/v1/authentication/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then
        response.andExpect(status().isInternalServerError());
    }

    @Test
    public void testRefreshTokenWithInvalidToken() throws Exception {
        // Given
        given(userRefreshTokenService.refreshToken(any(TokenRefreshRequest.class)))
                .willThrow(new RuntimeException("Invalid refresh token"));

        // When
        ResultActions response = mockMvc.perform(post("/api/v1/authentication/user/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRefreshRequest)));

        // Then
        response.andExpect(status().isInternalServerError());
    }

    @Test
    public void testTokenToTokenResponseMapper() {
        // Given
        Token sourceToken = Token.builder()
                .accessToken("test-access-token")
                .accessTokenExpiresAt(1738245228000L)
                .refreshToken("test-refresh-token")
                .build();

        // When
        TokenResponse mappedResponse = tokenToTokenResponseMapper.map(sourceToken);

        // Then
        assertNotNull(mappedResponse);
        assertEquals(sourceToken.getAccessToken(), mappedResponse.getAccessToken());
        assertEquals(sourceToken.getAccessTokenExpiresAt(), mappedResponse.getAccessTokenExpiresAt());
        assertEquals(sourceToken.getRefreshToken(), mappedResponse.getRefreshToken());
    }

    @Test
    public void testCompleteAuthenticationFlow() throws Exception {
        // Setup test data and mocks
        given(userRegisterService.registerUser(any(UserRegisterRequest.class))).willReturn(user);
        given(userLoginService.login(any(LoginRequest.class))).willReturn(token);
        given(userRefreshTokenService.refreshToken(any(TokenRefreshRequest.class))).willReturn(token);
        doNothing().when(userLogoutService).logout(any(TokenInvalidateRequest.class));

        // 1. Register user
        mockMvc.perform(post("/api/v1/authentication/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(userRegisterService).registerUser(any(UserRegisterRequest.class));

        // 2. Login with registered credentials
        ResultActions loginResponse = mockMvc.perform(post("/api/v1/authentication/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", is(token.getAccessToken())))
                .andExpect(jsonPath("$.data.refreshToken", is(token.getRefreshToken())));

        verify(userLoginService).login(any(LoginRequest.class));

        // 3. Refresh token
        mockMvc.perform(post("/api/v1/authentication/user/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.accessToken", is(token.getAccessToken())));

        verify(userRefreshTokenService).refreshToken(any(TokenRefreshRequest.class));

        // 4. Logout
        mockMvc.perform(post("/api/v1/authentication/user/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenInvalidateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        verify(userLogoutService).logout(any(TokenInvalidateRequest.class));
    }

    @Test
    public void testTokenUtilityMethods() {
        // Test isBearerToken method
        boolean validToken = Token.isBearerToken("Bearer xyz123");
        boolean invalidToken1 = Token.isBearerToken(null);
        boolean invalidToken2 = Token.isBearerToken("");
        boolean invalidToken3 = Token.isBearerToken("Basic xyz123");

        assertEquals(true, validToken);
        assertEquals(false, invalidToken1);
        assertEquals(false, invalidToken2);
        assertEquals(false, invalidToken3);

        // Test getJwt method
        String extractedToken = Token.getJwt("Bearer xyz123");
        assertEquals("xyz123", extractedToken);
    }
}