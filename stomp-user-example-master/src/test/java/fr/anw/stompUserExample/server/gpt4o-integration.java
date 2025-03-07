package fr.anw.stompUserExample.server;

import fr.anw.stompUserExample.server.config.WsConfig;
import fr.anw.stompUserExample.server.controllers.RegisterController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class IntegrationTest {

    @Autowired
    private WsTestUtils wsTestUtils;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private WebSocketStompClient stompClient;
    private StompSession stompSession;

    @BeforeEach
    public void setup() throws ExecutionException, InterruptedException {
        stompClient = wsTestUtils.createWebSocketClient();
        stompSession = stompClient.connect(WsConfig.ENDPOINT_CONNECT, new ClientSessionHandler()).get();
    }

    @Test
    public void testRegisterController() throws Exception {
        String payload = "testPayload";
        String username = "testUser";

        RegisterController registerController = new RegisterController(messagingTemplate);
        registerController.register(null, payload, () -> username);

        verify(messagingTemplate).convertAndSendToUser(username, WsConfig.SUBSCRIBE_USER_REPLY,
                "Thanks for your registration!");
        verify(messagingTemplate).convertAndSend(WsConfig.SUBSCRIBE_QUEUE,
                "Someone just registered saying: " + payload);
    }

    @Test
    public void testClientFrameHandler() {
        ClientFrameHandler clientFrameHandler = new ClientFrameHandler(System.out::println);
        clientFrameHandler.handleFrame(null, "testPayload");

        // Add assertions or verifications as needed
    }

    @Test
    public void testClientSessionHandler() {
        ClientSessionHandler clientSessionHandler = new ClientSessionHandler();
        StompHeaders headers = new StompHeaders();
        headers.add("user-name", "testUser");

        clientSessionHandler.afterConnected(stompSession, headers);
        // Add assertions or verifications as needed
    }
}