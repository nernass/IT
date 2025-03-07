package pl.sda.downloadmanager;

import com.google.api.services.drive.Drive;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.sda.downloadmanager.gdrive.DriveService;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IntegrationTest {
    private static final int SERVER_PORT = 8080;
    private static final String TEST_CONTENT = "Test file content";
    private static Server server;
    private DownloadManager downloadManager;
    private Path downloadDirectory;
    private List<String> downloadedFiles;

    @Mock
    private Drive driveMock;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create temporary download directory
        downloadDirectory = Files.createTempDirectory("download-test");
        downloadedFiles = new ArrayList<>();

        // Setup DriveService with mock
        DriveService driveService = new DriveService(driveMock);

        // Initialize DownloadManager
        downloadManager = new DownloadManager(downloadDirectory, driveService);

        // Add listener to track downloads
        downloadManager.registerListener(new DownloadEventListener() {
            @Override
            public void onStart(URL url) {
                System.out.println("Started downloading: " + url);
            }

            @Override
            public void onFinish(URL url) {
                downloadedFiles.add(url.toString());
                System.out.println("Finished downloading: " + url);
            }
        });

        // Start embedded server
        server = JettyEmbeddedServer.prepareServerWithResponse(TEST_CONTENT, SERVER_PORT);
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Stop server and cleanup
        if (server != null) {
            server.stop();
        }
        // Delete temporary directory
        Files.walk(downloadDirectory)
                .sorted((p1, p2) -> -p1.compareTo(p2))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete: " + path);
                    }
                });
    }

    @Test
    void testIntegratedDownloadAndDriveUpload() throws Exception {
        // Test URL pointing to our embedded server
        URL testUrl = new URL("http://localhost:" + SERVER_PORT + "/test.txt");

        // Download file
        downloadManager.download(testUrl);

        // Verify file was downloaded
        Path downloadedFile = downloadDirectory.resolve("test.txt");
        assertTrue(Files.exists(downloadedFile), "Downloaded file should exist");
        assertEquals(TEST_CONTENT, Files.readString(downloadedFile), "File content should match");

        // Verify download was tracked
        assertEquals(1, downloadedFiles.size(), "Should have tracked one download");
        assertTrue(downloadedFiles.contains(testUrl.toString()), "Should have tracked correct URL");

        // Test Drive upload
        downloadManager.copyToDrive(testUrl);

        // Verify Drive upload was attempted
        verify(driveMock.files(), times(1)).create(any(), any());
    }

    @Test
    void testBatchDownload() throws Exception {
        // Create test URLs file
        Path urlsFile = downloadDirectory.resolve("urls.txt");
        List<String> urls = List.of(
                "http://localhost:" + SERVER_PORT + "/test1.txt",
                "http://localhost:" + SERVER_PORT + "/test2.txt",
                "http://localhost:" + SERVER_PORT + "/test3.txt");
        Files.write(urlsFile, urls);

        // Download all files
        downloadManager.downloadAll(urlsFile);

        // Verify all files were downloaded
        assertEquals(3, downloadedFiles.size(), "Should have downloaded 3 files");
        urls.forEach(url -> assertTrue(downloadedFiles.contains(url), "Should have downloaded " + url));

        // Verify file contents
        for (String url : urls) {
            String filename = url.substring(url.lastIndexOf('/') + 1);
            Path downloadedFile = downloadDirectory.resolve(filename);
            assertTrue(Files.exists(downloadedFile), "File should exist: " + filename);
            assertEquals(TEST_CONTENT, Files.readString(downloadedFile),
                    "Content should match for " + filename);
        }
    }

    @Test
    void testErrorHandling() {
        // Test with invalid URL
        URL invalidUrl;
        try {
            invalidUrl = new URL("http://localhost:" + (SERVER_PORT + 1) + "/nonexistent.txt");
            Exception exception = assertThrows(RuntimeException.class, () -> {
                downloadManager.download(invalidUrl);
            });
            assertTrue(exception.getMessage().contains("Failed to download file"),
                    "Should throw appropriate error message");

            // Verify no files were tracked as downloaded
            assertEquals(0, downloadedFiles.size(), "Should not have tracked any downloads");

        } catch (Exception e) {
            fail("Should not throw exception while creating URL: " + e.getMessage());
        }
    }
}