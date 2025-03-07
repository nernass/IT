package fr.anw.stompUserExample.server;

import fr.anw.stompUserExample.server.config.WsConfig;
import fr.anw.stompUserExample.server.controllers.RegisterController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.security.Principal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IntegrationTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private WebSocketStompClient stompClient;

    @InjectMocks
    private RegisterController registerController;

    @InjectMocks
    private WsConfig wsConfig;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSuccessPath() throws Exception {
        // Mocking dependencies
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        // Mocking WebSocket client
        StompSession session = mock(StompSession.class);
        when(stompClient.connect(anyString(), any(StompSessionHandlerAdapter.class))).thenReturn(session);

        // Test RegisterController
        Message<Object> message = mock(Message.class);
        registerController.register(message, "testPayload", principal);

        // Verify interactions
        verify(messagingTemplate).convertAndSendToUser("testUser", WsConfig.SUBSCRIBE_USER_REPLY,
                "Thanks for your registration!");
        verify(messagingTemplate).convertAndSend(WsConfig.SUBSCRIBE_QUEUE,
                "Someone just registered saying: testPayload");
    }

    @Test
    public void testPartialFailure() throws Exception {
        // Mocking dependencies
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        // Mocking WebSocket client
        StompSession session = mock(StompSession.class);
        when(stompClient.connect(anyString(), any(StompSessionHandlerAdapter.class))).thenReturn(session);

        // Simulate failure in messagingTemplate
        doThrow(new RuntimeException("Simulated failure")).when(messagingTemplate).convertAndSendToUser(anyString(),
                anyString(), anyString());

        // Test RegisterController
        Message<Object> message = mock(Message.class);
        try {
            registerController.register(message, "testPayload", principal);
        } catch (Exception e) {
            // Expected exception
        }

        // Verify interactions
        verify(messagingTemplate).convertAndSendToUser("testUser", WsConfig.SUBSCRIBE_USER_REPLY,
                "Thanks for your registration!");
        verify(messagingTemplate).convertAndSend(WsConfig.SUBSCRIBE_QUEUE,
                "Someone just registered saying: testPayload");
    }

    @Test
    public void testEdgeCase() throws Exception {
        // Mocking dependencies
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testUser");

        // Mocking WebSocket client
        StompSession session = mock(StompSession.class);
        when(stompClient.connect(anyString(), any(StompSessionHandlerAdapter.class))).thenReturn(session);

        // Test RegisterController with empty payload
        Message<Object> message = mock(Message.class);
        registerController.register(message, "", principal);

        // Verify interactions
        verify(messagingTemplate).convertAndSendToUser("testUser", WsConfig.SUBSCRIBE_USER_REPLY,
                "Thanks for your registration!");
        verify(messagingTemplate).convertAndSend(WsConfig.SUBSCRIBE_QUEUE, "Someone just registered saying: ");
    }
}