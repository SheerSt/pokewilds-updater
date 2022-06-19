package com.pkmngen.updater;

import com.pkmngen.updater.platform.PlatformUtil;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.zip.ZipInputStream;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;

public class Updater {
    private final VersionControlFetcher versionControlFetcher;
    private final String version;
    private final int EOF = -1;
    private final int BUFFER_SIZE = 4096;

    public Updater(VersionControlFetcher versionControlFetcher, String version) {
        this.versionControlFetcher = versionControlFetcher;
        this.version = version;
    }

    public CompletableFuture<Void> update(String file, Consumer<UpdateStatus> statusConsumer) {
        return versionControlFetcher.fetchBinaries().thenAcceptAsync(content -> {
            try (var inputStream = content.inputStream()) {
                var fileSize = content.contentLength();
                var tempFolder = Path.of("temp");

                Files.createDirectories(tempFolder);

                var downloadDir = Path.of(tempFolder + File.separator + file);
                Files.createFile(downloadDir);

                copyInputStreamToFileNew(inputStream, downloadDir, fileSize, file, statusConsumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void copyInputStreamToFileNew(InputStream source, Path destination, long fileSize, String file, Consumer<UpdateStatus> statusConsumer) throws IOException {
        try (var output = Files.newOutputStream(destination)) {
            try {
                var buffer = new byte[BUFFER_SIZE];
                var count = 0L;
                var n = 0;

                while(EOF != (n = source.read(buffer))) {
                    output.write(buffer, 0, n);
                    count += n;
                    var percentage = (int)(count * 100L / fileSize);
                    statusConsumer.accept(new UpdateStatus("Downloading " + file + " @ " + percentage + "%", percentage));
                }

                unzip("temp/" + destination.getFileName(), "./", statusConsumer);
            } finally {
                statusConsumer.accept(new UpdateStatus("Removing temp files...", 33));
                FileUtils.deleteDirectory(new File("temp"));
                statusConsumer.accept(new UpdateStatus("Moving files to correct directories...", 49));
                move("pokemon-wilds-v" + version, "./", statusConsumer);
                statusConsumer.accept(new UpdateStatus("Updating version file...", 66));
                updateVersionFile();
                statusConsumer.accept(new UpdateStatus("Update completed", 100));
                JOptionPane.showMessageDialog(null, "Game is now ready for use!", "Ready!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void unzip(String zipPath, String dest, Consumer<UpdateStatus> statusConsumer) throws IOException {
        var destDir = Path.of(dest);
        Files.createDirectories(destDir);

        try {
            var zipIn = new ZipInputStream(new FileInputStream(zipPath));
            var entry = zipIn.getNextEntry();

            while(entry != null) {
                var path = Path.of(dest + File.separator + entry.getName());
                var parent = path.getParent();

                Files.createDirectories(parent);

                if (!entry.isDirectory()) {
                    extract(zipIn, path, (int) entry.getSize(), statusConsumer);
                } else {
                    Files.createDirectories(path);
                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }

            zipIn.close();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to extract game data!\n" + e.getMessage(), "Extraction Failed!", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void extract(ZipInputStream zipIn, Path path, int fileSize, Consumer<UpdateStatus> statusConsumer) throws IOException {
        try (var outputStream = new BufferedOutputStream(Files.newOutputStream(path))) {
            var bytesIn = new byte[BUFFER_SIZE];
            var read = 0;
            var count = 0;

            while((read = zipIn.read(bytesIn)) != EOF) {
                outputStream.write(bytesIn, 0, read);
                count += read;
                int percentage = (int) (count * 100L / fileSize);
                statusConsumer.accept(new UpdateStatus("Extracting " + path + " @ " + percentage + "%", percentage));
            }
        }
    }

    private void move(String source, String destination, Consumer<UpdateStatus> statusConsumer) throws IOException {
        var destinationFolder = Path.of(destination);
        var sourceFolder = Path.of(source);

        Files.createDirectories(destinationFolder);

        if (!Files.exists(sourceFolder) || !Files.isDirectory(sourceFolder)) {
            System.out.println(sourceFolder + "  Folder does not exists");
            return;
        }

        try (var streamOfFiles = Files.list(sourceFolder)) {
            var listOfFiles = streamOfFiles.toList();
            var length = listOfFiles.size();
            var currentIndex = 0;

            for (var child : listOfFiles) {
                // We can not overwrite the updater as it is currently in use
                var fileName = child.getFileName().toString();
                if (fileName.toLowerCase().matches("updater.jar")) {
                    continue;
                }

                Files.move(child, Path.of(destinationFolder.toAbsolutePath().toString() + File.separatorChar + fileName));
                var percentage = (currentIndex * 100) / length;
                currentIndex++;
                statusConsumer.accept(new UpdateStatus("Moving " + fileName + " @ " + percentage + "%", percentage));
            }

            Files.delete(sourceFolder);
        }
    }

    public void runGame() throws IOException {
        var executablePath = findPokeWildsExecutable();
        if (executablePath == null) {
            JOptionPane.showMessageDialog(null, "Unable to find game, please launch it manually", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        var runtime = Runtime.getRuntime();
        runtime.exec("java -jar " + executablePath);

        UpdaterApplication.closeApplication();
    }

    private Path findPokeWildsExecutable() {
        var path = Path.of("pokemon-wilds.jar");
        if (Files.exists(path)) {
            return path.toAbsolutePath();
        }

        var secondaryPath = Path.of("pokemon-wilds-v" + version + "-" + PlatformUtil.getPlatform().getName() + File.separator + "Contents" + File.separator + "Resources" + File.separator + "pokemon-wilds.jar");
        if (Files.exists(secondaryPath)) {
            return secondaryPath.toAbsolutePath();
        }

        return null;
    }

    private void updateVersionFile() throws IOException {
        Files.writeString(Path.of("version.txt"), version);
    }
}
