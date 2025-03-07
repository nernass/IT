package pl.sda.downloadmanager;

import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sda.downloadmanager.gdrive.DriveService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class DownloadManagerIntegrationTest {

    private DownloadManager downloadManager;
    private Server server;
    private static final int SERVER_PORT = 8080;
    private static final String TEST_FILE_CONTENT = "This is a test file content";
    private static final String TEST_FILE_NAME = "testfile.txt";
    private static final String TEST_URL = "http://localhost:" + SERVER_PORT + "/" + TEST_FILE_NAME;

    @Mock
    private DriveService driveService;

    @Mock
    private DownloadEventListener listener;

    @TempDir
    Path tempDir;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);

        // Start Jetty server with test content
        server = JettyEmbeddedServer.prepareServerWithResponse(TEST_FILE_CONTENT, SERVER_PORT);
        server.start();

        // Create download manager with temp directory and mocked drive service
        downloadManager = new DownloadManager(tempDir, driveService);
        downloadManager.registerListener(listener);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        mocks.close();
    }

    @Test
    void shouldDownloadFileFromServer() throws Exception {
        // Given
        URL fileUrl = new URL(TEST_URL);

        // When
        downloadManager.download(fileUrl);

        // Then
        Path downloadedFile = tempDir.resolve(TEST_FILE_NAME);
        assertTrue(Files.exists(downloadedFile), "Downloaded file should exist");
        String downloadedContent = Files.readString(downloadedFile, StandardCharsets.UTF_8);
        assertEquals(TEST_FILE_CONTENT, downloadedContent, "File content should match");

        // Verify listener was called
        verify(listener).onStart(fileUrl);
        verify(listener).onFinish(fileUrl);
    }

    @Test
    void shouldDownloadMultipleFiles() throws Exception {
        // Given
        Path sourceFile = tempDir.resolve("sources.txt");
        List<String> urls = List.of(TEST_URL, TEST_URL + "2");
        Files.write(sourceFile, urls);

        // When
        downloadManager.downloadAll(sourceFile);

        // Then
        Path downloadedFile = tempDir.resolve(TEST_FILE_NAME);
        assertTrue(Files.exists(downloadedFile), "Downloaded file should exist");
        String downloadedContent = Files.readString(downloadedFile, StandardCharsets.UTF_8);
        assertEquals(TEST_FILE_CONTENT, downloadedContent, "File content should match");

        // Each URL should trigger the listener
        verify(listener, times(2)).onStart(any(URL.class));
        verify(listener, times(2)).onFinish(any(URL.class));
    }

    @Test
    void shouldCopyFileToDriveService() throws IOException, URISyntaxException {
        // Given
        URL fileUrl = new URL(TEST_URL);

        // When
        downloadManager.copyToDrive(fileUrl);

        // Then
        verify(driveService).upload(any(InputStream.class), eq(TEST_FILE_NAME));
    }

    @Test
    void shouldHandleDownloadError() {
        // Given
        URL invalidUrl = null;
        try {
            invalidUrl = new URL("http://non-existent-url.invalid/file.txt");
        } catch (Exception e) {
            fail("Failed to create test URL", e);
        }

        // When/Then
        assertThrows(RuntimeException.class, () -> downloadManager.download(invalidUrl));
    }
}