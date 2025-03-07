package fr.anw.stompUserExample.server;

import fr.anw.stompUserExample.server.config.WsConfig;
import fr.anw.stompUserExample.server.controllers.RegisterController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RegisterController registerController;

    private WebSocketStompClient stompClient;
    private StompSession session;
    private WsTestUtils wsTestUtils;
    private String username;
    private List<String> receivedMessages;
    private List<String> broadcastMessages;

    @BeforeEach
    public void setup() throws Exception {
        wsTestUtils = new WsTestUtils();
        stompClient = wsTestUtils.createWebSocketClient();
        username = "user-" + UUID.randomUUID().toString().substring(0, 8);
        receivedMessages = new ArrayList<>();
        broadcastMessages = new ArrayList<>();

        // Connect to WebSocket server
        StompHeaders headers = new StompHeaders();
        headers.add("user-name", username);

        ClientSessionHandler sessionHandler = new ClientSessionHandler();
        String wsUrl = "ws://localhost:" + port + WsConfig.ENDPOINT_CONNECT;
        session = stompClient.connect(wsUrl, headers, sessionHandler).get(5, TimeUnit.SECONDS);

        // Subscribe to private messages
        session.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + "/" + username + WsConfig.SUBSCRIBE_USER_REPLY,
                new ClientFrameHandler(message -> receivedMessages.add(message)));

        // Subscribe to broadcast messages
        session.subscribe(WsConfig.SUBSCRIBE_QUEUE,
                new ClientFrameHandler(message -> broadcastMessages.add(message)));

        // Allow time for subscriptions to complete
        Thread.sleep(500);
    }

    @AfterEach
    public void teardown() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        if (stompClient != null) {
            stompClient.stop();
        }
    }

    @Test
    public void testRegisterEndpoint() throws ExecutionException, InterruptedException, TimeoutException {
        // Send a registration message
        String payload = "Hello from " + username;
        session.send(RegisterController.ENDPOINT_REGISTER, payload);

        // Wait for response
        CompletableFuture<Boolean> messageReceived = new CompletableFuture<>();
        CompletableFuture<Boolean> broadcastReceived = new CompletableFuture<>();

        // Wait for messages with timeout
        long timeout = System.currentTimeMillis() + 5000;
        while (System.currentTimeMillis() < timeout) {
            if (!messageReceived.isDone() && !receivedMessages.isEmpty()) {
                messageReceived.complete(true);
            }
            if (!broadcastReceived.isDone() && !broadcastMessages.isEmpty()) {
                broadcastReceived.complete(true);
            }

            if (messageReceived.isDone() && broadcastReceived.isDone()) {
                break;
            }

            Thread.sleep(100);
        }

        // Verify private message was received
        assertTrue(messageReceived.getNow(false), "Should have received private message");
        assertEquals("Thanks for your registration!", receivedMessages.get(0));

        // Verify broadcast message was received
        assertTrue(broadcastReceived.getNow(false), "Should have received broadcast message");
        assertEquals("Someone just registered saying: " + payload, broadcastMessages.get(0));
    }

    @Test
    public void testErrorHandling() throws InterruptedException {
        // Test malformed message handling
        try {
            // Send invalid destination to trigger an exception
            session.send("/invalid-endpoint", "This should cause an error");

            // Wait a bit for error handler to potentially be called
            Thread.sleep(1000);

            // The session should remain active despite the error
            assertTrue(session.isConnected(), "Session should remain connected after error");
        } catch (Exception e) {
            fail("Error handling should not throw exception: " + e.getMessage());
        }
    }
}