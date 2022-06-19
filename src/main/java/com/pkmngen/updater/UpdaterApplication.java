package com.pkmngen.updater;

import com.pkmngen.updater.gui.UpdaterFrame;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class UpdaterApplication {
    public static void main(String[] args) throws URISyntaxException {
        setLookAndFeel();

        var versionControlFetcher = new VersionControlFetcher();
        var version = versionControlFetcher.fetchVersion();
        var changelog = versionControlFetcher.fetchChangelog();
        var localVersion = getLocalVersion();

        var updater = new Updater(versionControlFetcher, version);
        var updaterFrame = new UpdaterFrame(updater, localVersion, version, changelog);
        updaterFrame.show();
    }

    private static void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static String getLocalVersion() {
        var path = Path.of("version.txt");

        if (Files.exists(path)) {
            try {
                return Files.readString(path);
            } catch (IOException e) {
                return null;
            }
        }

        return null;
    }

    public static void closeApplication() {
        System.exit(0);
    }
}
