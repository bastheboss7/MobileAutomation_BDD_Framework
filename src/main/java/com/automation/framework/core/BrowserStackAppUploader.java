package com.automation.framework.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
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
    private static final String CACHE_FILE = "target/bs_app_url_cache.properties";
    private static final String LOCK_FILE = "target/bs_upload.lock";
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
        if (appPath == null || appPath.isEmpty()) {
            throw new IOException("App path is empty. Provide a local file path or bs:// URL");
        }

        // If already a BrowserStack URL, return as-is (Standard Mode)
        if (appPath.startsWith("bs://")) {
            logger.info("Using existing BrowserStack app URL: {}", appPath);
            return appPath;
        }

        File appFile = new File(appPath);

        if (!appFile.exists()) {
            throw new IOException("App file not found: " + appPath);
        }

        // Check in-memory cache first
        String cacheKey = appPath + "_" + appFile.lastModified();
        if (appUrlCache.containsKey(cacheKey)) {
            String cachedUrl = appUrlCache.get(cacheKey);
            logger.info("Using in-memory cached BrowserStack app URL: {}", cachedUrl);
            return cachedUrl;
        }

        // Cross-process and intra-process synchronization: Ensure only one
        // process/thread uploads
        File targetDir = new File("target");
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        try (RandomAccessFile raf = new RandomAccessFile(new File(LOCK_FILE), "rw");
                FileChannel channel = raf.getChannel()) {

            // Acquire lock using a loop to handle both other processes (blocking/null
            // return)
            // and other threads in the same JVM (OverlappingFileLockException)
            FileLock lock = null;
            int lockRetries = 60; // Wait up to 60 seconds for the lock
            while (lock == null && lockRetries > 0) {
                try {
                    lock = channel.tryLock();
                    if (lock == null) {
                        Thread.sleep(1000);
                        lockRetries--;
                    }
                } catch (OverlappingFileLockException e) {
                    Thread.sleep(1000);
                    lockRetries--;
                }
            }

            if (lock == null) {
                throw new IOException("Could not acquire upload lock after 60 seconds");
            }

            try {
                // Re-check file-based cache after acquiring lock
                Properties props = new Properties();
                File cacheFile = new File(CACHE_FILE);
                if (cacheFile.exists()) {
                    try (FileInputStream fis = new FileInputStream(cacheFile)) {
                        props.load(fis);
                        if (props.containsKey(cacheKey)) {
                            String appUrl = props.getProperty(cacheKey);
                            logger.info("Using file-based cached BrowserStack app URL: {}", appUrl);
                            appUrlCache.put(cacheKey, appUrl);
                            return appUrl;
                        }
                    }
                }

                logger.info("Uploading app to BrowserStack (Lock acquired): {}", appPath);

                // Perform upload with retry mechanism for transient errors (504, 429)
                String appUrl = null;
                int uploadRetries = 3;
                for (int i = 0; i < uploadRetries; i++) {
                    try {
                        appUrl = performUpload(appFile, username, accessKey);
                        break;
                    } catch (IOException e) {
                        if (i == uploadRetries - 1)
                            throw e;
                        logger.warn("Upload attempt {} failed (Status: {}), retrying in 5 seconds...", i + 1,
                                e.getMessage());
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }

                // Update file-based cache
                props.setProperty(cacheKey, appUrl);
                try (FileOutputStream fos = new FileOutputStream(CACHE_FILE)) {
                    props.store(fos, "BrowserStack App URL Cache");
                }

                // Add a small delay to ensure URL is ready across all BrowserStack regions
                logger.info("Waiting 3 seconds for app URL propagation...");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }

                appUrlCache.put(cacheKey, appUrl);
                return appUrl;
            } finally {
                if (lock != null)
                    lock.release();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Wait interrupted", e);
        }
    }

    private static String performUpload(File appFile, String username, String accessKey) throws IOException {
        try {
            // Create multipart form data
            String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();
            byte[] fileContent = java.nio.file.Files.readAllBytes(appFile.toPath());

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
                logger.info("BrowserStack Upload Response: {}", response.body());
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonResponse = mapper.readTree(response.body());
                String appUrl = jsonResponse.get("app_url").asText();

                logger.info("✅ App uploaded successfully!");
                logger.info("BrowserStack App URL: {}", appUrl);

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

        // Try system properties first (set via mvn -D or SDK)
        String username = System.getProperty("browserstack.user");
        if (username == null)
            username = System.getProperty("browserstack.username");

        String accessKey = System.getProperty("browserstack.key");
        if (accessKey == null)
            accessKey = System.getProperty("browserstack.accessKey");

        // Try environment variables
        if (username == null || username.isEmpty()) {
            username = System.getenv("BROWSERSTACK_USERNAME");
            if (username == null)
                username = System.getenv("BROWSERSTACK_USER");
        }
        if (accessKey == null || accessKey.isEmpty()) {
            accessKey = System.getenv("BROWSERSTACK_ACCESS_KEY");
            if (accessKey == null)
                accessKey = System.getenv("BROWSERSTACK_KEY");
        }

        // Fallback to configuration values
        if (username == null || username.isEmpty()) {
            username = "aishwaryalakshmi_flVCjb";
            logger.debug("Using hardcoded BrowserStack username");
        }

        if (accessKey == null || accessKey.isEmpty()) {
            accessKey = "RsRLTRVEKShpDovu5sgb";
            logger.debug("Using hardcoded BrowserStack access key");
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
