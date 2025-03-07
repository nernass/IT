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
import org.springframework.messaging.simp.stomp.StompSessionHandler;
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
    private String websocketUrl;
    private List<String> receivedMessages;

    @BeforeEach
    void setup() {
        wsTestUtils = new WsTestUtils();
        websocketUrl = "ws://localhost:" + port + WsConfig.ENDPOINT_CONNECT;
        receivedMessages = new ArrayList<>();
    }

    @Test
    void testSuccessfulWebSocketConnection() throws Exception {
        // Create WebSocket client
        WebSocketStompClient stompClient = wsTestUtils.createWebSocketClient();
        StompSessionHandler sessionHandler = new ClientSessionHandler();

        // Connect to WebSocket server
        StompSession session = stompClient.connect(websocketUrl, sessionHandler)
                .get(5, TimeUnit.SECONDS);
        assertTrue(session.isConnected());

        // Subscribe to private messages
        CompletableFuture<Boolean> messageReceived = new CompletableFuture<>();
        session.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY,
                new ClientFrameHandler(message -> {
                    receivedMessages.add(message);
                    messageReceived.complete(true);
                }));

        // Subscribe to public queue
        CompletableFuture<Boolean> publicMessageReceived = new CompletableFuture<>();
        session.subscribe(WsConfig.SUBSCRIBE_QUEUE,
                new ClientFrameHandler(message -> {
                    receivedMessages.add(message);
                    publicMessageReceived.complete(true);
                }));

        // Send registration message
        String testPayload = "Test Registration";
        session.send(RegisterController.ENDPOINT_REGISTER, testPayload);

        // Wait for messages and verify
        assertTrue(messageReceived.get(5, TimeUnit.SECONDS));
        assertTrue(publicMessageReceived.get(5, TimeUnit.SECONDS));
        assertEquals(2, receivedMessages.size());
        assertTrue(receivedMessages.get(0).contains("Thanks for your registration"));
        assertTrue(receivedMessages.get(1).contains("Someone just registered"));
    }

    @Test
    void testInvalidSubscription() throws Exception {
        WebSocketStompClient stompClient = wsTestUtils.createWebSocketClient();
        StompSessionHandler sessionHandler = new ClientSessionHandler();

        StompSession session = stompClient.connect(websocketUrl, sessionHandler)
                .get(5, TimeUnit.SECONDS);
        assertTrue(session.isConnected());

        // Try to subscribe to invalid destination
        CompletableFuture<Boolean> errorReceived = new CompletableFuture<>();
        try {
            session.subscribe("/invalid/destination",
                    new ClientFrameHandler(message -> {
                        receivedMessages.add(message);
                        errorReceived.complete(true);
                    }));
            fail("Should throw exception for invalid destination");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Invalid"));
        }
    }

    @Test
    void testMultipleClientsInteraction() throws Exception {
        WebSocketStompClient client1 = wsTestUtils.createWebSocketClient();
        WebSocketStompClient client2 = wsTestUtils.createWebSocketClient();

        // Connect both clients
        StompSession session1 = client1.connect(websocketUrl, new ClientSessionHandler())
                .get(5, TimeUnit.SECONDS);
        StompSession session2 = client2.connect(websocketUrl, new ClientSessionHandler())
                .get(5, TimeUnit.SECONDS);

        // Subscribe both clients to public queue
        CompletableFuture<Boolean> client1MessageReceived = new CompletableFuture<>();
        CompletableFuture<Boolean> client2MessageReceived = new CompletableFuture<>();

        session1.subscribe(WsConfig.SUBSCRIBE_QUEUE,
                new ClientFrameHandler(message -> {
                    receivedMessages.add("Client1: " + message);
                    client1MessageReceived.complete(true);
                }));

        session2.subscribe(WsConfig.SUBSCRIBE_QUEUE,
                new ClientFrameHandler(message -> {
                    receivedMessages.add("Client2: " + message);
                    client2MessageReceived.complete(true);
                }));

        // Client 1 sends a registration message
        session1.send(RegisterController.ENDPOINT_REGISTER, "Client 1 Registration");

        // Verify both clients received the public message
        assertTrue(client1MessageReceived.get(5, TimeUnit.SECONDS));
        assertTrue(client2MessageReceived.get(5, TimeUnit.SECONDS));
        assertEquals(2, receivedMessages.size());
        assertTrue(receivedMessages.stream().allMatch(msg -> msg.contains("Someone just registered")));
    }
}