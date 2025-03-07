package pl.sda.downloadmanager;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Create;
import com.google.api.services.drive.model.File;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import pl.sda.downloadmanager.gdrive.DriveService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IntegrationTests {

    private static final int SERVER_PORT = 9876;
    private static final String TEST_FILE_CONTENT = "Test file content";
    private static final String TEST_FILE_NAME = "testfile.txt";

    private Server server;
    private Drive driveMock;
    private DriveService driveService;
    private DownloadManager downloadManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() throws Exception {
        // Start embedded server
        server = JettyEmbeddedServer.prepareServerWithResponse(TEST_FILE_CONTENT, SERVER_PORT);
        server.start();

        // Setup Drive mock
        driveMock = mock(Drive.class);
        Files filesMock = mock(Files.class);
        Create createMock = mock(Create.class);

        when(driveMock.files()).thenReturn(filesMock);
        when(filesMock.create(any(File.class), any(AbstractInputStreamContent.class))).thenReturn(createMock);
        when(createMock.execute()).thenReturn(new File());

        // Initialize services
        driveService = new DriveService(driveMock);
        downloadManager = new DownloadManager(tempDir, driveService);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null && server.isStarted()) {
            server.stop();
        }
    }

    @Test
    void testDownloadAndVerifyContent() throws Exception {
        // Given
        URL fileUrl = new URL("http://localhost:" + SERVER_PORT + "/" + TEST_FILE_NAME);

        // Track download events
        AtomicBoolean downloadStarted = new AtomicBoolean(false);
        AtomicBoolean downloadFinished = new AtomicBoolean(false);

        downloadManager.registerListener(new DownloadEventListener() {
            @Override
            public void onStart(URL url) {
                assertEquals(fileUrl, url);
                downloadStarted.set(true);
            }

            @Override
            public void onFinish(URL url) {
                assertEquals(fileUrl, url);
                downloadFinished.set(true);
            }
        });

        // When
        downloadManager.download(fileUrl);

        // Then
        Path downloadedFile = tempDir.resolve(TEST_FILE_NAME);
        assertTrue(Files.exists(downloadedFile), "Downloaded file should exist");
        String actualContent = Files.readString(downloadedFile);
        assertEquals(TEST_FILE_CONTENT, actualContent, "File content should match");

        // Verify events were fired
        assertTrue(downloadStarted.get(), "Download start event should have been fired");
        assertTrue(downloadFinished.get(), "Download finish event should have been fired");
    }

    @Test
    void testCopyToDriveIntegration() throws Exception {
        // Given
        URL fileUrl = new URL("http://localhost:" + SERVER_PORT + "/" + TEST_FILE_NAME);

        // Prepare to capture arguments
        ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class);
        ArgumentCaptor<AbstractInputStreamContent> contentCaptor = ArgumentCaptor
                .forClass(AbstractInputStreamContent.class);

        Files filesMock = driveMock.files();

        // When
        downloadManager.copyToDrive(fileUrl);

        // Then
        verify(filesMock).create(fileCaptor.capture(), contentCaptor.capture());
        assertEquals(TEST_FILE_NAME, fileCaptor.getValue().getName(),
                "Correct filename should be used for Drive upload");

        // Verify content was properly uploaded
        byte[] capturedContent = readInputStreamContent(contentCaptor.getValue());
        String uploadedContent = new String(capturedContent, StandardCharsets.UTF_8);
        assertEquals(TEST_FILE_CONTENT, uploadedContent,
                "Content uploaded to Drive should match the downloaded content");
    }

    @Test
    void testDownloadAllIntegration() throws Exception {
        // Given
        Path urlListFile = tempDir.resolve("urls.txt");
        String url1 = "http://localhost:" + SERVER_PORT + "/file1.txt";
        String url2 = "http://localhost:" + SERVER_PORT + "/file2.txt";

        Files.writeString(urlListFile, url1 + System.lineSeparator() + url2);

        // Track download events
        AtomicBoolean file1Started = new AtomicBoolean(false);
        AtomicBoolean file1Finished = new AtomicBoolean(false);
        AtomicBoolean file2Started = new AtomicBoolean(false);
        AtomicBoolean file2Finished = new AtomicBoolean(false);

        downloadManager.registerListener(new DownloadEventListener() {
            @Override
            public void onStart(URL url) {
                if (url.toString().contains("file1.txt")) {
                    file1Started.set(true);
                } else if (url.toString().contains("file2.txt")) {
                    file2Started.set(true);
                }
            }

            @Override
            public void onFinish(URL url) {
                if (url.toString().contains("file1.txt")) {
                    file1Finished.set(true);
                } else if (url.toString().contains("file2.txt")) {
                    file2Finished.set(true);
                }
            }
        });

        // When
        downloadManager.downloadAll(urlListFile);

        // Then
        Path downloadedFile1 = tempDir.resolve("file1.txt");
        Path downloadedFile2 = tempDir.resolve("file2.txt");

        assertTrue(Files.exists(downloadedFile1), "First file should be downloaded");
        assertTrue(Files.exists(downloadedFile2), "Second file should be downloaded");

        assertEquals(TEST_FILE_CONTENT, Files.readString(downloadedFile1));
        assertEquals(TEST_FILE_CONTENT, Files.readString(downloadedFile2));

        assertTrue(file1Started.get() && file1Finished.get(),
                "Events for first file should have been fired");
        assertTrue(file2Started.get() && file2Finished.get(),
                "Events for second file should have been fired");
    }

    @Test
    void testErrorHandlingWhenServerUnavailable() throws MalformedURLException {
        // Given
        URL fileUrl = new URL("http://nonexistentserver:12345/somefile.txt");

        // When & Then
        Exception exception = assertThrows(RuntimeException.class, () -> {
            downloadManager.download(fileUrl);
        });

        assertTrue(exception.getMessage().contains("Failed to download file"),
                "Exception should contain appropriate error message");
    }

    private byte[] readInputStreamContent(AbstractInputStreamContent content) throws IOException {
        try (InputStream is = content.getInputStream()) {
            return is.readAllBytes();
        }
    }
}