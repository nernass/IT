package com.springboot.springbootsecurity.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.springbootsecurity.auth.model.Token;
import com.springboot.springbootsecurity.auth.model.dto.request.LoginRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenInvalidateRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenRefreshRequest;
import com.springboot.springbootsecurity.auth.model.dto.response.TokenResponse;
import com.springboot.springbootsecurity.auth.model.mapper.TokenToTokenResponseMapper;
import com.springboot.springbootsecurity.user.model.User;
import com.springboot.springbootsecurity.user.model.dto.request.UserRegisterRequest;
import com.springboot.springbootsecurity.user.service.UserLoginService;
import com.springboot.springbootsecurity.user.service.UserLogoutService;
import com.springboot.springbootsecurity.user.service.UserRefreshTokenService;
import com.springboot.springbootsecurity.user.service.UserRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserRegisterService userRegisterService;

    @MockBean
    private UserLoginService userLoginService;

    @MockBean
    private UserRefreshTokenService userRefreshTokenService;

    @MockBean
    private UserLogoutService userLogoutService;

    @MockBean
    private TokenToTokenResponseMapper tokenToTokenResponseMapper;

    private UserRegisterRequest userRegisterRequest;
    private LoginRequest loginRequest;
    private Token token;
    private TokenResponse tokenResponse;
    private TokenRefreshRequest tokenRefreshRequest;
    private TokenInvalidateRequest tokenInvalidateRequest;

    @BeforeEach
    void setUp() {
        userRegisterRequest = UserRegisterRequest.builder()
                .email("test@test.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("12345678901")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@test.com")
                .password("password123")
                .build();

        token = Token.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .accessTokenExpiresAt(3600L)
                .build();

        tokenResponse = TokenResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .accessTokenExpiresAt(3600L)
                .build();

        tokenRefreshRequest = new TokenRefreshRequest();
        tokenInvalidateRequest = new TokenInvalidateRequest();
    }

    @Test
    void registerUser_ValidRequest_ReturnsSuccess() throws Exception {
        when(userRegisterService.registerUser(any(UserRegisterRequest.class)))
                .thenReturn(new User());

        mockMvc.perform(post("/api/v1/authentication/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userRegisterRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userRegisterService).registerUser(any(UserRegisterRequest.class));
    }

    @Test
    void loginUser_ValidCredentials_ReturnsTokenResponse() throws Exception {
        when(userLoginService.login(any(LoginRequest.class))).thenReturn(token);
        when(tokenToTokenResponseMapper.map(any(Token.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/authentication/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(userLoginService).login(any(LoginRequest.class));
        verify(tokenToTokenResponseMapper).map(any(Token.class));
    }

    @Test
    void refreshToken_ValidRequest_ReturnsNewTokenResponse() throws Exception {
        when(userRefreshTokenService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(token);
        when(tokenToTokenResponseMapper.map(any(Token.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/authentication/user/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenRefreshRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));

        verify(userRefreshTokenService).refreshToken(any(TokenRefreshRequest.class));
        verify(tokenToTokenResponseMapper).map(any(Token.class));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void logout_ValidRequest_ReturnsSuccess() throws Exception {
        doNothing().when(userLogoutService).logout(any(TokenInvalidateRequest.class));

        mockMvc.perform(post("/api/v1/authentication/user/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenInvalidateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(userLogoutService).logout(any(TokenInvalidateRequest.class));
    }

    @Test
    void logout_WithoutAuthorization_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/authentication/user/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tokenInvalidateRequest)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userLogoutService);
    }
}