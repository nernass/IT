package com.springboot.springbootsecurity.integration;

import com.springboot.springbootsecurity.auth.model.Token;
import com.springboot.springbootsecurity.auth.model.dto.request.LoginRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenInvalidateRequest;
import com.springboot.springbootsecurity.auth.model.dto.request.TokenRefreshRequest;
import com.springboot.springbootsecurity.auth.model.dto.response.TokenResponse;
import com.springboot.springbootsecurity.auth.model.mapper.TokenToTokenResponseMapper;
import com.springboot.springbootsecurity.common.model.dto.response.CustomResponse;
import com.springboot.springbootsecurity.user.controller.UserAuthController;
import com.springboot.springbootsecurity.user.model.User;
import com.springboot.springbootsecurity.user.model.dto.request.UserRegisterRequest;
import com.springboot.springbootsecurity.user.service.UserLoginService;
import com.springboot.springbootsecurity.user.service.UserLogoutService;
import com.springboot.springbootsecurity.user.service.UserRefreshTokenService;
import com.springboot.springbootsecurity.user.service.UserRegisterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAuthenticationIntegrationTest {

    @Mock
    private UserRegisterService userRegisterService;
    @Mock
    private UserLoginService userLoginService;
    @Mock
    private UserRefreshTokenService userRefreshTokenService;
    @Mock
    private UserLogoutService userLogoutService;

    private UserAuthController userAuthController;
    private TokenToTokenResponseMapper tokenMapper;

    @BeforeEach
    void setUp() {
        tokenMapper = TokenToTokenResponseMapper.initialize();
        userAuthController = new UserAuthController(
                userRegisterService,
                userLoginService,
                userRefreshTokenService,
                userLogoutService);
    }

    @Nested
    @DisplayName("User Authentication Flow Tests")
    class UserAuthFlowTests {

        @Test
        @DisplayName("Should successfully register a new user")
        void registerUser_ValidRequest_SuccessfulRegistration() {
            // Arrange
            UserRegisterRequest request = UserRegisterRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .firstName("John")
                    .lastName("Doe")
                    .phoneNumber("12345678901")
                    .build();

            User mockUser = User.builder().email(request.getEmail()).build();
            when(userRegisterService.registerUser(request)).thenReturn(mockUser);

            // Act
            CustomResponse<Void> response = userAuthController.registerUser(request);

            // Assert
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            verify(userRegisterService, times(1)).registerUser(request);
        }

        @Test
        @DisplayName("Should successfully login user and return token")
        void loginUser_ValidCredentials_ReturnsToken() {
            // Arrange
            LoginRequest loginRequest = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            Token mockToken = Token.builder()
                    .accessToken("mock-access-token")
                    .refreshToken("mock-refresh-token")
                    .accessTokenExpiresAt(System.currentTimeMillis() + 3600000)
                    .build();

            when(userLoginService.login(loginRequest)).thenReturn(mockToken);

            // Act
            CustomResponse<TokenResponse> response = userAuthController.loginUser(loginRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            assertNotNull(response.getData());
            assertEquals(mockToken.getAccessToken(), response.getData().getAccessToken());
            verify(userLoginService, times(1)).login(loginRequest);
        }

        @Test
        @DisplayName("Should successfully refresh token")
        void refreshToken_ValidRequest_ReturnsNewToken() {
            // Arrange
            TokenRefreshRequest refreshRequest = new TokenRefreshRequest();
            Token mockNewToken = Token.builder()
                    .accessToken("new-access-token")
                    .refreshToken("new-refresh-token")
                    .accessTokenExpiresAt(System.currentTimeMillis() + 3600000)
                    .build();

            when(userRefreshTokenService.refreshToken(any())).thenReturn(mockNewToken);

            // Act
            CustomResponse<TokenResponse> response = userAuthController.refreshToken(refreshRequest);

            // Assert
            assertNotNull(response);
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            verify(userRefreshTokenService, times(1)).refreshToken(refreshRequest);
        }

        @Test
        @DisplayName("Should successfully logout user")
        void logout_ValidRequest_SuccessfulLogout() {
            // Arrange
            TokenInvalidateRequest logoutRequest = new TokenInvalidateRequest();
            doNothing().when(userLogoutService).logout(logoutRequest);

            // Act
            CustomResponse<Void> response = userAuthController.logout(logoutRequest);

            // Assert
            assertEquals(HttpStatus.OK.value(), response.getStatus());
            verify(userLogoutService, times(1)).logout(logoutRequest);
        }
    }

    @Nested
    @DisplayName("Token Mapping Tests")
    class TokenMappingTests {

        @Test
        @DisplayName("Should correctly map Token to TokenResponse")
        void map_ValidToken_ReturnsCorrectTokenResponse() {
            // Arrange
            Token token = Token.builder()
                    .accessToken("test-access-token")
                    .refreshToken("test-refresh-token")
                    .accessTokenExpiresAt(1234567890L)
                    .build();

            // Act
            TokenResponse response = tokenMapper.map(token);

            // Assert
            assertNotNull(response);
            assertEquals(token.getAccessToken(), response.getAccessToken());
            assertEquals(token.getRefreshToken(), response.getRefreshToken());
            assertEquals(token.getAccessTokenExpiresAt(), response.getAccessTokenExpiresAt());
        }

        @Test
        @DisplayName("Should handle null token mapping")
        void map_NullToken_ReturnsNull() {
            // Act
            TokenResponse response = tokenMapper.map(null);

            // Assert
            assertNull(response);
        }

        @Test
        @DisplayName("Should handle token with null fields")
        void map_TokenWithNullFields_ReturnsTokenResponseWithNullFields() {
            // Arrange
            Token token = Token.builder().build();

            // Act
            TokenResponse response = tokenMapper.map(token);

            // Assert
            assertNotNull(response);
            assertNull(response.getAccessToken());
            assertNull(response.getRefreshToken());
            assertNull(response.getAccessTokenExpiresAt());
        }
    }
}