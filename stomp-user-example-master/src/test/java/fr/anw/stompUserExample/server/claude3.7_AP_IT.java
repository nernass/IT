package fr.anw.stompUserExample.server;

import fr.anw.stompUserExample.server.config.WsConfig;
import fr.anw.stompUserExample.server.controllers.RegisterController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    private String wsUrl;
    private WsTestUtils wsTestUtils;

    @BeforeEach
    public void setup() {
        this.wsUrl = "ws://localhost:" + port + WsConfig.ENDPOINT_CONNECT;
        this.wsTestUtils = new WsTestUtils();
    }

    @Test
    public void testClientRegistrationAndMessageFlow() throws Exception {
        // Test data
        final String registrationMessage = "Hello from integration test";
        final String expectedPrivateReply = "Thanks for your registration!";
        final String expectedPublicMessage = "Someone just registered saying: " + registrationMessage;

        // Set up completable futures to wait for responses
        CompletableFuture<String> privateMessageFuture = new CompletableFuture<>();
        CompletableFuture<String> publicMessageFuture = new CompletableFuture<>();

        // Create STOMP client
        var stompClient = wsTestUtils.createWebSocketClient();

        // Connect to WebSocket server with custom session handler
        ClientSessionHandler sessionHandler = new ClientSessionHandler();
        StompSession session = stompClient.connect(wsUrl, sessionHandler).get(5, TimeUnit.SECONDS);

        // Subscribe to private user channel
        session.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY,
                new ClientFrameHandler(message -> privateMessageFuture.complete(message)));

        // Subscribe to public queue channel
        session.subscribe(WsConfig.SUBSCRIBE_QUEUE,
                new ClientFrameHandler(message -> publicMessageFuture.complete(message)));

        // Send registration message
        session.send(RegisterController.ENDPOINT_REGISTER, registrationMessage);

        // Wait for and verify responses
        String privateResponse = privateMessageFuture.get(5, TimeUnit.SECONDS);
        String publicResponse = publicMessageFuture.get(5, TimeUnit.SECONDS);

        assertEquals(expectedPrivateReply, privateResponse);
        assertEquals(expectedPublicMessage, publicResponse);

        // Clean up
        session.disconnect();
    }

    @Test
    public void testMultipleClientsReceivePublicMessages() throws Exception {
        // Test data
        final String registrationMessage = "Hello from multiple clients test";
        final String expectedPublicMessage = "Someone just registered saying: " + registrationMessage;

        // Prepare multiple clients (3 in this case)
        int clientCount = 3;
        List<StompSession> sessions = new ArrayList<>();
        List<CompletableFuture<String>> publicMessageFutures = new ArrayList<>();

        // Connect all clients
        for (int i = 0; i < clientCount; i++) {
            var stompClient = wsTestUtils.createWebSocketClient();
            CompletableFuture<String> future = new CompletableFuture<>();
            publicMessageFutures.add(future);

            StompSession session = stompClient.connect(wsUrl, new ClientSessionHandler()).get(5, TimeUnit.SECONDS);
            sessions.add(session);

            // Subscribe each client to the public queue
            session.subscribe(WsConfig.SUBSCRIBE_QUEUE, new ClientFrameHandler(message -> future.complete(message)));
        }

        // One client sends a registration message
        sessions.get(0).send(RegisterController.ENDPOINT_REGISTER, registrationMessage);

        // Verify all clients received the public message
        for (int i = 0; i < clientCount; i++) {
            String publicResponse = publicMessageFutures.get(i).get(5, TimeUnit.SECONDS);
            assertEquals(expectedPublicMessage, publicResponse);
        }

        // Clean up
        for (StompSession session : sessions) {
            session.disconnect();
        }
    }

    @Test
    public void testPrivateMessageIsOnlySentToSender() throws Exception {
        // Test data
        final String registrationMessage = "Private message test";
        final String expectedPrivateReply = "Thanks for your registration!";

        // Set up sender client
        CompletableFuture<String> senderPrivateMessageFuture = new CompletableFuture<>();
        var senderClient = wsTestUtils.createWebSocketClient();
        StompSession senderSession = senderClient.connect(wsUrl, new ClientSessionHandler()).get(5, TimeUnit.SECONDS);

        // Subscribe sender to private channel
        senderSession.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY,
                new ClientFrameHandler(message -> senderPrivateMessageFuture.complete(message)));

        // Set up receiver client that should not receive the private message
        CompletableFuture<String> receiverPrivateMessageFuture = new CompletableFuture<>();
        var receiverClient = wsTestUtils.createWebSocketClient();
        StompSession receiverSession = receiverClient.connect(wsUrl, new ClientSessionHandler()).get(5,
                TimeUnit.SECONDS);

        // Subscribe receiver to private channel
        receiverSession.subscribe(WsConfig.SUBSCRIBE_USER_PREFIX + WsConfig.SUBSCRIBE_USER_REPLY,
                new ClientFrameHandler(message -> receiverPrivateMessageFuture.complete(message)));

        // Send registration message from sender
        senderSession.send(RegisterController.ENDPOINT_REGISTER, registrationMessage);

        // Verify sender received private message
        String senderResponse = senderPrivateMessageFuture.get(5, TimeUnit.SECONDS);
        assertEquals(expectedPrivateReply, senderResponse);

        // Verify receiver did not get the private message (should timeout)
        assertThrows(TimeoutException.class, () -> {
            receiverPrivateMessageFuture.get(2, TimeUnit.SECONDS);
        });

        // Clean up
        senderSession.disconnect();
        receiverSession.disconnect();
    }
}