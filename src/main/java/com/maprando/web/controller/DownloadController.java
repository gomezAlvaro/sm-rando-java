package com.maprando.web.controller;

import com.maprando.web.service.FilesystemSeedStorageService;
import com.maprando.web.service.RomGenerationService;
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
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Controller for file downloads, including spoiler logs and ROM files.
 * Provides endpoints for downloading seed-related files.
 */
@RestController
@RequestMapping("/seed")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"})
public class DownloadController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    private final FilesystemSeedStorageService storageService;
    private final RomGenerationService romGenerationService;

    /**
     * Creates a new DownloadController.
     *
     * @param storageService        filesystem storage service
     * @param romGenerationService  ROM generation service
     */
    public DownloadController(
            FilesystemSeedStorageService storageService,
            RomGenerationService romGenerationService
    ) {
        this.storageService = storageService;
        this.romGenerationService = romGenerationService;
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

    /**
     * Downloads a patched ROM file for a specific seed.
     * The ROM is generated on-demand if not cached.
     *
     * @param seedId unique seed identifier
     * @return ROM file as binary download (.smc format)
     */
    @GetMapping("/{seedId}/rom")
    public ResponseEntity<Resource> downloadRom(@PathVariable String seedId) {
        logger.info("Downloading ROM for seed: {}", seedId);

        try {
            // Check if ROM generation is available
            if (!romGenerationService.isRomGenerationAvailable()) {
                logger.warn("ROM generation is not available");
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new ByteArrayResource("ROM generation is not configured".getBytes()));
            }

            // Check if seed exists
            if (!storageService.seedExists(seedId)) {
                return ResponseEntity.notFound().build();
            }

            // Generate or retrieve ROM
            byte[] romBytes = romGenerationService.generateRomBytes(seedId);

            // Create resource with ROM data
            ByteArrayResource resource = new ByteArrayResource(romBytes);

            // Set headers for file download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", seedId + ".smc");

            // Add cache control
            headers.setCacheControl("public, max-age=3600");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(romBytes.length)
                    .body(resource);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid seed ID for ROM download: {}", seedId);
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            logger.error("ROM generator not initialized: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ByteArrayResource("ROM generation is not available".getBytes()));
        } catch (IOException e) {
            logger.error("Failed to generate ROM for seed: {}", seedId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Checks if a ROM is available for a seed.
     *
     * @param seedId unique seed identifier
     * @return HTTP 200 if ROM is available, 404 if not, 503 if ROM generation disabled
     */
    @GetMapping("/{seedId}/rom/status")
    public ResponseEntity<Void> checkRomStatus(@PathVariable String seedId) {
        logger.debug("Checking ROM status for seed: {}", seedId);

        // Check if ROM generation is available
        if (!romGenerationService.isRomGenerationAvailable()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        // Check if seed exists
        if (!storageService.seedExists(seedId)) {
            return ResponseEntity.notFound().build();
        }

        // ROM can be generated
        return ResponseEntity.ok().build();
    }
}