package com.pkmngen.updater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 *  Author: ItsLuke
 *  
 * */

public class VersionControl {
    private static final String VERSION_ENDPOINT = "https://github.com/SheerSt/pokemon-wilds/releases/latest/download/version.txt";
    private static final String CHANGELOG_ENDPOINT = "https://github.com/SheerSt/pokemon-wilds/releases/latest/download/changelog.txt";

    private String latestVersion = "";

    //Extract our changelog for display
    public String getChangelog() {
        this.latestVersion = getVersion();
        var changelogBuilder = new StringBuilder("Changelog (" + this.latestVersion + "):\n");
        try {
            var url = new URL(CHANGELOG_ENDPOINT);

            try (var inputStream = url.openStream()) {
                var connection = url.openConnection();
                connection.connect();

                try (var reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    var line = "";

                    while((line = reader.readLine()) != null) {
                        changelogBuilder.append(line).append("\n");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return changelogBuilder.toString();
    }

    // Different from check version, does not store the grabbed version
    // Does not alert the user if theres a new version
    public String getVersion() {
        var versionBuilder = new StringBuilder();

        try {
            var url = new URL(VERSION_ENDPOINT);
            var in = url.openStream();

            var connection = url.openConnection();
            connection.connect();

            var reader = new BufferedReader(new InputStreamReader(in));
            var line = "";

            while((line = reader.readLine()) != null) {
                versionBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return versionBuilder.toString();
    }

    public String getLatestVersion() {
        return this.latestVersion;
    }
}
