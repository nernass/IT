import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import pl.sda.downloadmanager.gdrive.DriveService;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class IntegrationTest {

    private Server server;
    private DownloadManager downloadManager;
    private DriveService driveService;
    private Path downloadDirectory;

    @BeforeEach
    public void setUp() throws Exception {
        // Prepare Jetty server with a response
        server = JettyEmbeddedServer.prepareServerWithResponse("Test content", 8080);
        server.start();

        // Mock DriveService
        driveService = mock(DriveService.class);

        // Prepare DownloadManager with mocked DriveService
        downloadDirectory = Files.createTempDirectory("download");
        downloadManager = new DownloadManager(downloadDirectory, driveService);
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.stop();
        Files.deleteIfExists(downloadDirectory);
    }

    @Test
    public void testDownloadAndUploadSuccess() throws Exception {
        URL fileUrl = new URL("http://localhost:8080/testfile.txt");

        // Execute download
        downloadManager.download(fileUrl);

        // Verify file is downloaded
        Path downloadedFile = downloadDirectory.resolve("testfile.txt");
        assertTrue(Files.exists(downloadedFile));

        // Execute upload
        downloadManager.copyToDrive(fileUrl);

        // Verify upload is called
        verify(driveService, times(1)).upload(any(), eq("testfile.txt"));
    }

    @Test
    public void testDownloadFailure() {
        URL invalidUrl = new URL("http://localhost:8080/invalidfile.txt");

        // Expect RuntimeException due to invalid URL
        try {
            downloadManager.download(invalidUrl);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Failed to download file"));
        }
    }

    @Test
    public void testUploadFailure() throws Exception {
        URL fileUrl = new URL("http://localhost:8080/testfile.txt");

        // Mock DriveService to throw IOException
        doThrow(new IOException("Upload failed")).when(driveService).upload(any(), any());

        // Execute download
        downloadManager.download(fileUrl);

        // Verify file is downloaded
        Path downloadedFile = downloadDirectory.resolve("testfile.txt");
        assertTrue(Files.exists(downloadedFile));

        // Expect IOException during upload
        try {
            downloadManager.copyToDrive(fileUrl);
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Upload failed"));
        }
    }
}