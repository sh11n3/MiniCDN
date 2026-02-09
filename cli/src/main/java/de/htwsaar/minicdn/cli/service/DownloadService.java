package de.htwsaar.minicdn.cli.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

public class DownloadService {

    private final HttpClient httpClient;

    public DownloadService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public int downloadFile(
            String routerBaseUrl, String region, String remotePath, Path outputPath, boolean overwrite) {
        DownloadValidator.validateOutputPath(outputPath, overwrite);
        String cleanPath = DownloadValidator.normalizeRemotePath(remotePath);
        String targetUrl = buildDownloadUrl(routerBaseUrl, region, cleanPath);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        int attempts = 0;
        while (attempts < 2) {
            attempts++;
            HttpResponse<InputStream> response;
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            } catch (IOException e) {
                if (attempts < 2) {
                    continue;
                }
                System.err.println("Netzwerkfehler: " + e.getMessage());
                return 1;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Download abgebrochen.");
                return 1;
            }

            int statusCode = response.statusCode();
            if (statusCode >= 200 && statusCode < 300) {
                return writeToFile(response, outputPath, overwrite);
            }

            if (statusCode >= 500 && attempts < 2) {
                continue;
            }

            return handleError(statusCode, response);
        }

        return 1;
    }

    private int writeToFile(HttpResponse<InputStream> response, Path outputPath, boolean overwrite) {
        try (InputStream inputStream = response.body()) {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
            StandardCopyOption[] options = overwrite
                    ? new StandardCopyOption[] {StandardCopyOption.REPLACE_EXISTING}
                    : new StandardCopyOption[0];
            Files.copy(inputStream, outputPath, options);
            System.out.println("Download erfolgreich: " + outputPath.toAbsolutePath());
            return 0;
        } catch (IOException e) {
            System.err.println("Fehler beim Schreiben der Datei: " + e.getMessage());
            return 1;
        }
    }

    private int handleError(int statusCode, HttpResponse<InputStream> response) {
        try (InputStream ignored = response.body()) {
            // drain/close response body
        } catch (IOException ignored) {
            // ignore close errors
        }
        if (statusCode == 400) {
            System.err.println("Anfrage abgelehnt (400): Ungültiger Pfad.");
            return 2;
        }
        if (statusCode == 404) {
            System.err.println("Datei nicht gefunden (404).");
            return 3;
        }
        if (statusCode >= 400 && statusCode < 500) {
            System.err.println("Anfrage abgelehnt (" + statusCode + ").");
            return 2;
        }
        System.err.println("Server- oder Netzwerkproblem (" + statusCode + ").");
        return 4;
    }

    private String buildDownloadUrl(String routerBaseUrl, String region, String cleanPath) {
        String base = routerBaseUrl.endsWith("/")
                ? routerBaseUrl.substring(0, routerBaseUrl.length() - 1)
                : routerBaseUrl;
        return base + "/api/cdn/files/" + cleanPath + "?region=" + region;
    }
}
