package com.maprando.web.controller;

import com.maprando.web.dto.SeedRequest;
import com.maprando.web.dto.SeedResponse;
import com.maprando.web.service.SeedGenerationService;
import com.maprando.web.service.FilesystemSeedStorageService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * REST API controller for seed generation and retrieval.
 * Provides endpoints for creating new seeds, retrieving seed details,
 * and listing recent seeds.
 */
@RestController
@RequestMapping("/api/seeds")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"})
public class SeedApiController {

    private static final Logger logger = LoggerFactory.getLogger(SeedApiController.class);

    private final SeedGenerationService seedGenerationService;
    private final FilesystemSeedStorageService storageService;

    /**
     * Creates a new SeedApiController.
     *
     * @param seedGenerationService seed generation service
     * @param storageService        filesystem storage service
     */
    public SeedApiController(
            SeedGenerationService seedGenerationService,
            FilesystemSeedStorageService storageService
    ) {
        this.seedGenerationService = seedGenerationService;
        this.storageService = storageService;
    }

    /**
     * Generates a new randomization seed.
     *
     * @param request seed generation request
     * @return generated seed response
     */
    @PostMapping("/generate")
    public ResponseEntity<SeedResponse> generateSeed(@Valid @RequestBody SeedRequest request) {
        logger.info("Received seed generation request: algorithm={}", request.algorithm());

        try {
            SeedResponse response = seedGenerationService.generateSeed(request);
            if (response.successful()) {
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
            }
        } catch (Exception e) {
            logger.error("Failed to generate seed", e);
            SeedResponse errorResponse = SeedResponse.failure(
                    "unknown",
                    request.getEffectiveSeed(),
                    request.algorithm(),
                    List.of("Internal server error: " + e.getMessage())
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Retrieves details for a specific seed.
     *
     * @param seedId unique seed identifier
     * @return seed details
     */
    @GetMapping("/{seedId}")
    public ResponseEntity<SeedResponse> getSeed(@PathVariable String seedId) {
        logger.info("Retrieving seed: {}", seedId);

        try {
            if (!storageService.seedExists(seedId)) {
                return ResponseEntity.notFound().build();
            }

            SeedResponse response = storageService.getSeedMetadata(seedId);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Failed to retrieve seed: {}", seedId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Lists recent seeds.
     *
     * @return list of recent seed summaries
     */
    @GetMapping("/recent")
    public ResponseEntity<List<SeedResponse>> getRecentSeeds(
            @RequestParam(defaultValue = "10") int limit
    ) {
        logger.info("Retrieving recent seeds, limit={}", limit);

        try {
            // For MVP, return empty list
            // Full implementation would scan storage directory and return recent seeds
            return ResponseEntity.ok(List.of());
        } catch (Exception e) {
            logger.error("Failed to retrieve recent seeds", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Checks if a spoiler log exists for a seed.
     *
     * @param seedId unique seed identifier
     * @return true if spoiler exists, false otherwise
     */
    @GetMapping("/{seedId}/spoiler")
    public ResponseEntity<Boolean> hasSpoiler(@PathVariable String seedId) {
        logger.info("Checking spoiler availability for seed: {}", seedId);

        try {
            if (!storageService.seedExists(seedId)) {
                return ResponseEntity.notFound().build();
            }

            boolean hasSpoiler = storageService.spoilerExists(seedId);
            return ResponseEntity.ok(hasSpoiler);
        } catch (Exception e) {
            logger.error("Failed to check spoiler for seed: {}", seedId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}