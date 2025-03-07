package fr.anw.stompUserExample.server;

import fr.anw.stompUserExample.server.config.WsConfig;
import fr.anw.stompUserExample.server.controllers.RegisterController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RegisterController registerController;

    private WsTestUtils wsTestUtils;
    private WebSocketStompClient stompClient;
    private String websocketUrl;

    @BeforeEach
    void setup() {
        wsTestUtils = new WsTestUtils();
        stompClient = wsTestUtils.createWebSocketClient();
        websocketUrl = "ws://localhost:" + port + WsConfig.ENDPOINT_CONNECT;
    }

    @Test
    void testWebSocketConnection() throws Exception {
        // Create session handler and frame handlers
        ClientSessionHandler sessionHandler = new ClientSessionHandler();
        List<String> receivedMessages = new ArrayList<>();
        ClientFrameHandler frameHandler = new ClientFrameHandler(receivedMessages::add);

        // Connect to websocket
        StompSession session = stompClient.connect(websocketUrl, sessionHandler)
                .get(5, TimeUnit.SECONDS);
        assertTrue(session.isConnected());

        // Subscribe to topics
        session.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY, frameHandler);
        session.subscribe(WsConfig.SUBSCRIBE_QUEUE, frameHandler);

        // Send registration message
        String testPayload = "Test registration message";
        session.send(RegisterController.ENDPOINT_REGISTER, testPayload);

        // Wait for messages
        CompletableFuture<Void> messagesFuture = new CompletableFuture<>();
        Thread.sleep(1000); // Wait for message processing

        // Verify received messages
        assertTrue(receivedMessages.size() >= 2);
        assertTrue(receivedMessages.stream().anyMatch(msg -> msg.contains("Thanks for your registration!")));
        assertTrue(receivedMessages.stream()
                .anyMatch(msg -> msg.contains("Someone just registered saying: " + testPayload)));

        // Cleanup
        session.disconnect();
        assertFalse(session.isConnected());
    }

    @Test
    void testWebSocketErrorHandling() throws Exception {
        ClientSessionHandler sessionHandler = new ClientSessionHandler() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                throw new RuntimeException("Simulated error in frame handling");
            }
        };

        StompSession session = stompClient.connect(websocketUrl, sessionHandler)
                .get(5, TimeUnit.SECONDS);
        assertTrue(session.isConnected());

        // Send invalid message to trigger error
        try {
            session.send("/invalid/endpoint", "Invalid message");
            Thread.sleep(500); // Wait for error processing
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Message handling failed"));
        }

        session.disconnect();
    }

    @Test
    void testMultipleClientsInteraction() throws Exception {
        List<String> client1Messages = new ArrayList<>();
        List<String> client2Messages = new ArrayList<>();

        // Create two client sessions
        StompSession session1 = stompClient.connect(websocketUrl, new ClientSessionHandler())
                .get(5, TimeUnit.SECONDS);
        StompSession session2 = stompClient.connect(websocketUrl, new ClientSessionHandler())
                .get(5, TimeUnit.SECONDS);

        // Subscribe both clients
        session1.subscribe(WsConfig.SUBSCRIBE_QUEUE, new ClientFrameHandler(client1Messages::add));
        session2.subscribe(WsConfig.SUBSCRIBE_QUEUE, new ClientFrameHandler(client2Messages::add));

        // Send message from client 1
        session1.send(RegisterController.ENDPOINT_REGISTER, "Hello from client 1");
        Thread.sleep(500);

        // Verify both clients received the broadcast message
        assertTrue(client1Messages.size() >= 1);
        assertTrue(client2Messages.size() >= 1);
        assertEquals(client1Messages, client2Messages);

        // Cleanup
        session1.disconnect();
        session2.disconnect();
    }
}