package com.springboot.springbootsecurity.user.controller;

import com.springboot.springbootsecurity.auth.model.Token;
import com.springboot.springbootsecurity.auth.model.dto.request.LoginRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenInvalidateRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenRefreshRequest;
import com.springboot.springbootsecurity.auth.model.dto.response.TokenResponse;
import com.springboot.springbootsecurity.auth.model.mapper.TokenToTokenResponseMapper;
import com.springboot.springbootsecurity.common.model.dto.response.CustomResponse;
import com.springboot.springbootsecurity.user.model.dto.request.UserRegisterRequest;
import com.springboot.springbootsecurity.user.service.UserLoginService;
import com.springboot.springbootsecurity.user.service.UserLogoutService;
import com.springboot.springbootsecurity.user.service.UserRefreshTokenService;
import com.springboot.springbootsecurity.user.service.UserRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class UserAuthControllerTest {

    @Mock
    private UserRegisterService userRegisterService;

    @Mock
    private UserLoginService userLoginService;

    @Mock
    private UserRefreshTokenService userRefreshTokenService;

    @Mock
    private UserLogoutService userLogoutService;

    @Mock
    private TokenToTokenResponseMapper tokenToTokenResponseMapper;

    @InjectMocks
    private UserAuthController userAuthController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userAuthController).build();
    }

    @Test
    void testRegisterUser() throws Exception {
        UserRegisterRequest request = UserRegisterRequest.builder()
                .email("test@example.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("1234567890")
                .build();

        mockMvc.perform(post("/api/v1/authentication/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                        "{\"email\":\"test@example.com\",\"password\":\"password\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"phoneNumber\":\"1234567890\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void testLoginUser() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .accessToken("accessToken")
                .accessTokenExpiresAt(123456789L)
                .refreshToken("refreshToken")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("accessToken")
                .accessTokenExpiresAt(123456789L)
                .refreshToken("refreshToken")
                .build();

        when(userLoginService.login(any(LoginRequest.class))).thenReturn(token);
        when(tokenToTokenResponseMapper.map(any(Token.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/authentication/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@example.com\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("accessToken"));
    }

    @Test
    void testRefreshToken() throws Exception {
        TokenRefreshRequest request = TokenRefreshRequest.builder()
                .refreshToken("refreshToken")
                .build();

        Token token = Token.builder()
                .accessToken("newAccessToken")
                .accessTokenExpiresAt(123456789L)
                .refreshToken("newRefreshToken")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("newAccessToken")
                .accessTokenExpiresAt(123456789L)
                .refreshToken("newRefreshToken")
                .build();

        when(userRefreshTokenService.refreshToken(any(TokenRefreshRequest.class))).thenReturn(token);
        when(tokenToTokenResponseMapper.map(any(Token.class))).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/authentication/user/refresh-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"refreshToken\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("newAccessToken"));
    }

    @Test
    void testLogout() throws Exception {
        TokenInvalidateRequest request = TokenInvalidateRequest.builder()
                .refreshToken("refreshToken")
                .build();

        mockMvc.perform(post("/api/v1/authentication/user/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"refreshToken\":\"refreshToken\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }
}