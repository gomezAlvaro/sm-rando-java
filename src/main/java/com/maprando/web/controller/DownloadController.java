package com.maprando.web.controller;

import com.maprando.web.service.FilesystemSeedStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Controller for file downloads, primarily spoiler logs.
 * Provides endpoints for downloading seed-related files.
 */
@RestController
@RequestMapping("/seed")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"})
public class DownloadController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    private final FilesystemSeedStorageService storageService;

    /**
     * Creates a new DownloadController.
     *
     * @param storageService filesystem storage service
     */
    public DownloadController(FilesystemSeedStorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Downloads a spoiler log for a specific seed.
     *
     * @param seedId unique seed identifier
     * @return spoiler log file as text download
     */
    @GetMapping("/{seedId}/spoiler")
    public ResponseEntity<Resource> downloadSpoiler(@PathVariable String seedId) {
        logger.info("Downloading spoiler for seed: {}", seedId);

        try {
            // Check if seed exists
            if (!storageService.seedExists(seedId)) {
                return ResponseEntity.notFound().build();
            }

            // Check if spoiler exists
            if (!storageService.spoilerExists(seedId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            // Get spoiler content
            String spoilerContent = storageService.getSpoilerLog(seedId);

            // Create resource with spoiler content
            ByteArrayResource resource = new ByteArrayResource(
                    spoilerContent.getBytes(StandardCharsets.UTF_8)
            );

            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_PLAIN);
            headers.setContentDispositionFormData("attachment", seedId + "-spoiler.txt");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);

        } catch (IOException e) {
            logger.error("Failed to download spoiler for seed: {}", seedId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}