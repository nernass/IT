import com.google.api.services.drive.Drive;
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

public class DownloadManagerIntegrationTest {

    private DownloadManager downloadManager;
    private DriveService driveService;
    private Server server;
    private Path downloadDirectory;

    @BeforeEach
    public void setUp() throws Exception {
        downloadDirectory = Files.createTempDirectory("downloads");
        Drive mockDrive = mock(Drive.class, RETURNS_DEEP_STUBS);
        driveService = new DriveService(mockDrive);
        downloadManager = new DownloadManager(downloadDirectory, driveService);

        String fileContent = "test content";
        server = JettyEmbeddedServer.prepareServerWithResponse(fileContent, 8080);
        server.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        server.stop();
        Files.walk(downloadDirectory)
                .map(Path::toFile)
                .forEach(file -> {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                });
    }

    @Test
    public void testDownloadAndUploadToDrive() throws Exception {
        URL fileUrl = new URL("http://localhost:8080/testfile.txt");
        downloadManager.download(fileUrl);

        Path downloadedFile = downloadDirectory.resolve("testfile.txt");
        assertTrue(Files.exists(downloadedFile));

        downloadManager.copyToDrive(fileUrl);
        verify(driveService, times(1)).upload(any(), eq("testfile.txt"));
    }

    @Test
    public void testDownloadAll() throws Exception {
        Path urlsFile = Files.createTempFile("urls", ".txt");
        Files.write(urlsFile, "http://localhost:8080/testfile1.txt\nhttp://localhost:8080/testfile2.txt".getBytes());

        downloadManager.downloadAll(urlsFile);

        assertTrue(Files.exists(downloadDirectory.resolve("testfile1.txt")));
        assertTrue(Files.exists(downloadDirectory.resolve("testfile2.txt")));
    }

    @Test
    public void testErrorHandling() {
        URL invalidUrl = Mockito.mock(URL.class);
        when(invalidUrl.toString()).thenReturn("http://invalid-url");

        Exception exception = null;
        try {
            downloadManager.download(invalidUrl);
        } catch (RuntimeException e) {
            exception = e;
        }

        assertTrue(exception instanceof RuntimeException);
    }
}