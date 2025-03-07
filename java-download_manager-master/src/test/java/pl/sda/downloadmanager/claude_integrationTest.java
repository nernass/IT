package pl.sda.downloadmanager;

import com.google.api.services.drive.Drive;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sda.downloadmanager.gdrive.DriveService;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DownloadManagerIntegrationTest {

    private static final int PORT = 8089;
    private static final String TEST_CONTENT = "Test file content";

    @TempDir
    Path tempDir;

    @Mock
    private Drive driveMock;

    private Server server;
    private DownloadManager downloadManager;
    private DriveService driveService;
    private Path downloadPath;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Setup embedded server
        server = JettyEmbeddedServer.prepareServerWithResponse(TEST_CONTENT, PORT);
        server.start();

        // Setup download manager
        downloadPath = tempDir.resolve("downloads");
        Files.createDirectory(downloadPath);
        driveService = new DriveService(driveMock);
        downloadManager = new DownloadManager(downloadPath, driveService);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void shouldDownloadFileAndUploadToDrive() throws Exception {
        // given
        URL testFileUrl = new URL("http://localhost:" + PORT + "/test.txt");

        // when
        downloadManager.download(testFileUrl);
        downloadManager.copyToDrive(testFileUrl);

        // then
        Path downloadedFile = downloadPath.resolve("test.txt");
        assertTrue(Files.exists(downloadedFile));
        assertEquals(TEST_CONTENT, Files.readString(downloadedFile));
        verify(driveMock.files(), times(1)).create(any(), any());
    }

    @Test
    void shouldDownloadMultipleFiles() throws Exception {
        // given
        Path urlsFile = tempDir.resolve("urls.txt");
        List<String> urls = List.of(
                "http://localhost:" + PORT + "/test1.txt",
                "http://localhost:" + PORT + "/test2.txt");
        Files.write(urlsFile, urls);

        // when
        downloadManager.downloadAll(urlsFile);

        // then
        assertTrue(Files.exists(downloadPath.resolve("test1.txt")));
        assertTrue(Files.exists(downloadPath.resolve("test2.txt")));
    }

    @Test
    void shouldNotifyListenersOnDownload() throws Exception {
        // given
        URL testFileUrl = new URL("http://localhost:" + PORT + "/test.txt");
        DownloadEventListener listener = mock(DownloadEventListener.class);
        downloadManager.registerListener(listener);

        // when
        downloadManager.download(testFileUrl);

        // then
        verify(listener).onStart(testFileUrl);
        verify(listener).onFinish(testFileUrl);
    }

    @Test
    void shouldHandleDownloadError() {
        // given
        URL invalidUrl = null;
        try {
            invalidUrl = new URL("http://localhost:" + PORT + "/nonexistent.txt");
        } catch (Exception e) {
            fail("URL creation failed");
        }

        // when & then
        assertThrows(RuntimeException.class, () -> downloadManager.download(invalidUrl));
    }
}