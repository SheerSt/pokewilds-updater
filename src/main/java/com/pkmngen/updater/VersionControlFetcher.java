package com.pkmngen.updater;

import com.pkmngen.updater.platform.PlatformUtil;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class VersionControlFetcher {
    private static final String BASE_PATH = "https://github.com/SheerSt/pokemon-wilds/releases/latest/download/";

    private final HttpClient client;
    private final HttpRequest versionRequest;
    private final HttpRequest changelogRequest;

    public VersionControlFetcher() throws URISyntaxException {
        this.client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
        this.versionRequest = HttpRequest.newBuilder(new URI(BASE_PATH + "version.txt")).GET().build();
        this.changelogRequest = HttpRequest.newBuilder(new URI(BASE_PATH + "changelog.txt")).GET().build();
    }

    public String fetchChangelog() {
        try {
            var response = this.client.send(changelogRequest, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String fetchVersion() {
        try {
            var response = this.client.send(versionRequest, HttpResponse.BodyHandlers.ofString());

            return response.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<FetchContent> fetchBinaries() {
        var fileName = PlatformUtil.getPlatform().getDownloadFileName();
        try {
            var request = HttpRequest.newBuilder(new URI(BASE_PATH + fileName)).GET().build();
            return this.client.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApplyAsync(response -> {
                    var contentLength = response.headers().firstValueAsLong("Content-Length").getAsLong();

                    return new FetchContent(response.body(), contentLength);
                });
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public record FetchContent(InputStream inputStream, long contentLength) {}
}
