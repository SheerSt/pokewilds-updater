package com.pkmngen.updater;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipInputStream;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *  Author: ItsLuke
 *
 * */

public class Updater {
    private final UpdaterApplication application;

    private final int EOF = -1;
    private final int BUFFER_SIZE = 4096;
    private String versionFile = "version.txt";

    public Updater(UpdaterApplication application) {
        this.application = application;
    }

    public void update(String file) {
        var connectionURL = "https://www.github.com/SheerSt/pokemon-wilds/releases/latest/download/" + file;

        try {
            var url = new URL(connectionURL);
            try (var in = url.openStream()) {
                var connection = url.openConnection();
                connection.connect();
                int fileSize = connection.getContentLength();

                var tempFolder = new File("temp");
                if(!tempFolder.exists())
                    tempFolder.mkdir();

                var downloadDir = new File("temp/" + file);
                if(!downloadDir.exists()) {
                    downloadDir.createNewFile();
                }

                copyInputStreamToFileNew(in, downloadDir, fileSize, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void copyInputStreamToFileNew(InputStream source, File destination, int fileSize, String file) throws IOException {
        try (var output = FileUtils.openOutputStream(destination)) {
            try {
                var buffer = new byte[BUFFER_SIZE];
                var count = 0L;
                var n = 0;

                while(EOF != (n = source.read(buffer))) {
                    output.write(buffer, 0, n);
                    count += n;
                    var percentage = (int)(count * 100L / fileSize);
                    application.message.setText("Downloading " + file + " @ " + percentage + "%");
                    application.progress.setValue(percentage);
                }

                unzip("temp/" + destination.getName(), "./");
            } finally {
                IOUtils.closeQuietly(output);

                application.progress.setValue(33);
                application.message.setText("Removing temp files...");
                FileUtils.deleteDirectory(new File("temp"));
                application.message.setText("Moving files to correct directories...");
                move("pokemon-wilds-v" + application.versionControl.getLatestVersion(), "./");
                application.progress.setValue(66);
                application.message.setText("Updating version file...");
                updateVersionFile();
                application.progress.setValue(100);

                application.message.setText("Update completed");
                JOptionPane.showMessageDialog(null, "Game is now ready for use! Click to close updater", "Ready!", 1);
                runGame();
            }
        }
    }

    public void unzip(String zipPath, String dest) {
        var destDir = new File(dest);
        if(!destDir.exists()) destDir.mkdir();

        try {
            var zipIn = new ZipInputStream(new FileInputStream(zipPath));
            var entry = zipIn.getNextEntry();

            while(entry != null) {
                var path = dest + File.separator + entry.getName();
                var parent = (new File(path)).getParentFile();

                if(!parent.exists()) {
                    parent.mkdirs();
                }

                if(!entry.isDirectory()) {
                    extract(zipIn, path, (int) entry.getSize());
                } else {
                    File dir = new File(path);
                    dir.mkdir();
                }

                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }

            zipIn.close();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to extract game data!\n" + e.getMessage(), "Extraction Failed!", 0);
            e.printStackTrace();
        }
    }

    void extract(ZipInputStream zipIn, String path, int fileSize) throws IOException {
        try (var outputStream = new BufferedOutputStream(new FileOutputStream(path))) {
            var bytesIn = new byte[BUFFER_SIZE];
            var read = 0;
            var count = 0;

            while((read = zipIn.read(bytesIn)) != EOF) {
                outputStream.write(bytesIn, 0, read);
                count += read;
                int percentage = (int) (count * 100L / fileSize);
                application.message.setText("Extracting " + path + " @ " + percentage + "%");
                application.progress.setValue(percentage);
            }
        }
    }

    private void move(String source, String destination) {
        var destinationFolder = new File(destination);
        var sourceFolder = new File(source);

        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
            System.out.println(sourceFolder + "  Folder does not exists");
            return;
        }

        var listOfFiles = sourceFolder.listFiles();
        if (listOfFiles == null) {
            return;
        }

        var length = listOfFiles.length;
        var currentIndex = 0;

        for (var child : listOfFiles) {
            // We can not overwrite the updater as it is currently in use
            if(!child.getName().toLowerCase().matches("updater.jar")) {
                child.renameTo(new File(destinationFolder + "\\" + child.getName()));
                var percentage = (currentIndex * 100) / length;
                currentIndex++;
                application.message.setText("Moving " + child.getName() + " @ " + percentage + "%");
                application.progress.setValue(percentage);
            }
        }

        sourceFolder.delete();
    }

    private void runGame() throws IOException {
        var filePath = "./pokemon-wilds.jar"; //where your jar is located.
        var runtime = Runtime.getRuntime();
        runtime.exec(" java -jar " + filePath);

        application.closeApplication();
    }

    private void updateVersionFile() throws IOException {
        var toWrite = application.versionControl.getLatestVersion();

        try(var in = new FileOutputStream(versionFile)) {
            try(var writer = new BufferedWriter(new OutputStreamWriter(in))) {
                writer.write(toWrite);
            }
        }
    }
}
