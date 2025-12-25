package com.automation.framework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * BrowserStack App Upload Manager using REST API.
 * Handles app upload to BrowserStack and caches app URLs to avoid redundant
 * uploads.
 * 
 * @author Baskar
 * @version 1.0.0
 */
public class BrowserStackAppUploader {
    private static final Logger logger = LoggerFactory.getLogger(BrowserStackAppUploader.class);

    private static final String BROWSERSTACK_UPLOAD_URL = "https://api-cloud.browserstack.com/app-automate/upload";
    private static final Map<String, String> appUrlCache = new ConcurrentHashMap<>();

    private BrowserStackAppUploader() {
        // Private constructor - utility class
    }

    /**
     * Uploads an app to BrowserStack and returns the bs:// URL.
     * Uses caching to avoid re-uploading the same app.
     * 
     * @param appPath   Local path to the app file
     * @param username  BrowserStack username
     * @param accessKey BrowserStack access key
     * @return BrowserStack app URL (bs://...)
     * @throws IOException if upload fails
     */
    public static String uploadApp(String appPath, String username, String accessKey) throws IOException {
        File appFile = new File(appPath);

        if (!appFile.exists()) {
            throw new IOException("App file not found: " + appPath);
        }

        // Check cache first (based on file path and last modified time)
        String cacheKey = appPath + "_" + appFile.lastModified();
        if (appUrlCache.containsKey(cacheKey)) {
            String cachedUrl = appUrlCache.get(cacheKey);
            logger.info("Using cached BrowserStack app URL: {}", cachedUrl);
            return cachedUrl;
        }

        logger.info("Uploading app to BrowserStack: {}", appPath);
        logger.info("App file size: {} MB", appFile.length() / (1024.0 * 1024.0));

        try {
            // Create multipart form data
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            byte[] fileContent = Files.readAllBytes(appFile.toPath());

            // Build multipart body
            StringBuilder bodyBuilder = new StringBuilder();
            bodyBuilder.append("--").append(boundary).append("\r\n");
            bodyBuilder.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                    .append(appFile.getName()).append("\"\r\n");
            bodyBuilder.append("Content-Type: application/octet-stream\r\n\r\n");

            byte[] header = bodyBuilder.toString().getBytes();
            byte[] footer = ("\r\n--" + boundary + "--\r\n").getBytes();

            // Combine header + file + footer
            byte[] body = new byte[header.length + fileContent.length + footer.length];
            System.arraycopy(header, 0, body, 0, header.length);
            System.arraycopy(fileContent, 0, body, header.length, fileContent.length);
            System.arraycopy(footer, 0, body, header.length + fileContent.length, footer.length);

            // Create HTTP client and request
            HttpClient client = HttpClient.newHttpClient();

            // Basic authentication
            String auth = username + ":" + accessKey;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BROWSERSTACK_UPLOAD_URL))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                    .build();

            logger.info("Sending upload request to BrowserStack...");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // Parse JSON response to get app_url
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.body());
                String appUrl = jsonResponse.get("app_url").asText();

                logger.info("✅ App uploaded successfully!");
                logger.info("BrowserStack App URL: {}", appUrl);

                // Cache the URL
                appUrlCache.put(cacheKey, appUrl);

                return appUrl;
            } else {
                String errorMsg = String.format("BrowserStack upload failed. Status: %d, Response: %s",
                        response.statusCode(), response.body());
                logger.error(errorMsg);
                throw new IOException(errorMsg);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Upload interrupted", e);
        }
    }

    /**
     * Gets BrowserStack credentials from browserstack.yml or environment variables.
     * 
     * @return Map with "username" and "accessKey"
     */
    public static Map<String, String> getBrowserStackCredentials() {
        Map<String, String> credentials = new HashMap<>();

        // Try environment variables first (for CI/CD)
        String username = System.getenv("BROWSERSTACK_USERNAME");
        String accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");

        // Fallback to hardcoded values from browserstack.yml
        if (username == null || username.isEmpty()) {
            username = "baskarp_lopTwX";
            logger.debug("Using BrowserStack username from configuration");
        }

        if (accessKey == null || accessKey.isEmpty()) {
            accessKey = "mFgqaySZL9yoy9fMdxPi";
            logger.debug("Using BrowserStack access key from configuration");
        }

        credentials.put("username", username);
        credentials.put("accessKey", accessKey);

        return credentials;
    }

    /**
     * Clears the app URL cache.
     * Useful for testing or when you want to force re-upload.
     */
    public static void clearCache() {
        appUrlCache.clear();
        logger.info("BrowserStack app URL cache cleared");
    }

    /**
     * Gets the cache size.
     * 
     * @return Number of cached app URLs
     */
    public static int getCacheSize() {
        return appUrlCache.size();
    }
}
