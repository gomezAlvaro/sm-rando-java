package com.maprando.web.service;

import com.maprando.data.DataLoader;
import com.maprando.data.model.DifficultyData;
import com.maprando.data.model.LocationData;
import com.maprando.demo.PrintableSpoiler;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.ItemPoolFactory;
import com.maprando.randomize.Location;
import com.maprando.randomize.RandomizationResult;
import com.maprando.randomize.BasicRandomizer;
import com.maprando.randomize.advanced.ForesightRandomizer;
import com.maprando.randomize.advanced.QualityMetricsCalculator;
import com.maprando.randomize.advanced.SeedQualityMetrics;
import com.maprando.web.dto.QualityMetricsDto;
import com.maprando.web.dto.SeedRequest;
import com.maprando.web.dto.SeedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Service for generating randomized seeds using the existing randomization API.
 * This service acts as a facade over BasicRandomizer and ForesightRandomizer,
 * handling the conversion between DTOs and domain objects.
 */
@Service
public class SeedGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(SeedGenerationService.class);

    private final DataLoader dataLoader;
    private final QualityMetricsCalculator qualityMetricsCalculator;
    private final FilesystemSeedStorageService storageService;
    private final ItemPoolFactory itemPoolFactory;

    /**
     * Creates a new SeedGenerationService.
     *
     * @param dataLoader              loaded game data
     * @param qualityMetricsCalculator quality metrics calculator
     * @param storageService          filesystem storage service
     */
    public SeedGenerationService(
            DataLoader dataLoader,
            QualityMetricsCalculator qualityMetricsCalculator,
            FilesystemSeedStorageService storageService
    ) {
        this.dataLoader = dataLoader;
        this.qualityMetricsCalculator = qualityMetricsCalculator;
        this.storageService = storageService;
        this.itemPoolFactory = new ItemPoolFactory(dataLoader);
    }

    /**
     * Generates a seed based on the provided request.
     *
     * @param request seed generation request
     * @return seed response with generation results
     */
    public SeedResponse generateSeed(SeedRequest request) {
        logger.info("Generating seed with algorithm: {}, seed: {}", request.algorithm(), request.getEffectiveSeed());

        String seedId = storageService.generateSeedId();
        String effectiveSeed = request.getEffectiveSeed();

        try {
            // Create randomizer based on algorithm choice
            if ("foresight".equalsIgnoreCase(request.algorithm())) {
                return generateWithForesightRandomizer(seedId, effectiveSeed, request);
            } else {
                return generateWithBasicRandomizer(seedId, effectiveSeed, request);
            }
        } catch (Exception e) {
            logger.error("Failed to generate seed", e);
            return SeedResponse.failure(
                    seedId,
                    effectiveSeed,
                    request.algorithm(),
                    List.of("Generation failed: " + e.getMessage())
            );
        }
    }

    /**
     * Generates a seed using the ForesightRandomizer (advanced algorithm).
     */
    private SeedResponse generateWithForesightRandomizer(
            String seedId,
            String seed,
            SeedRequest request
    ) throws IOException {
        ForesightRandomizer randomizer = new ForesightRandomizer(seed, dataLoader);

        // Get difficulty preset
        String difficultyId = request.getEffectiveDifficulty();
        DifficultyData difficulty = dataLoader.getDifficultyPreset(difficultyId);

        // Configure randomizer with difficulty-adjusted item pool
        ItemPool itemPool = itemPoolFactory.createPool(difficultyId);
        randomizer.setItemPool(itemPool);

        // Apply difficulty settings to randomizer
        if (difficulty != null) {
            randomizer.setDifficultyTechLevel(difficulty.getSettings().getTechAssumptions());
            randomizer.setStartingItems(difficulty.getStartingItems());
        }

        // Add locations from data loader
        List<Location> locations = createLocationsFromData();
        for (Location location : locations) {
            randomizer.addLocation(location);
        }

        // Generate seed
        RandomizationResult result = randomizer.randomize();

        // Save complete randomization result (needed for ROM generation)
        storageService.saveRandomizationResult(seedId, result);

        // Calculate quality metrics
        QualityMetricsDto qualityMetrics = calculateQualityMetrics(result, randomizer);

        // Generate warnings
        List<String> warnings = new ArrayList<>();
        if (!result.isSuccessful()) {
            warnings.add("Seed generation completed with some items unplaced");
        }

        // Add difficulty info to warnings
        if (difficulty != null) {
            warnings.add(String.format("Difficulty: %s (%s)", difficulty.getName(), difficulty.getDescription()));
        }

        // Create response
        SeedResponse response = SeedResponse.success(
                seedId,
                seed,
                "foresight",
                qualityMetrics,
                warnings
        );

        // Save seed metadata
        storageService.saveSeedMetadata(seedId, response);

        // Save spoiler log if enabled
        if (request.isSpoilerEnabled()) {
            String spoilerLog = generateSpoilerLog(result);
            storageService.saveSpoilerLog(seedId, spoilerLog);
        }

        logger.info("Successfully generated seed with ID: {} (difficulty: {})", seedId, difficultyId);
        return response;
    }

    /**
     * Generates a seed using the BasicRandomizer (simple algorithm).
     */
    private SeedResponse generateWithBasicRandomizer(
            String seedId,
            String seed,
            SeedRequest request
    ) throws IOException {
        BasicRandomizer randomizer = new BasicRandomizer(seed);

        // Get difficulty preset
        String difficultyId = request.getEffectiveDifficulty();
        DifficultyData difficulty = dataLoader.getDifficultyPreset(difficultyId);

        // Configure randomizer with difficulty-adjusted item pool
        ItemPool itemPool = itemPoolFactory.createPool(difficultyId);
        randomizer.setItemPool(itemPool);

        // Add locations from data loader
        List<Location> locations = createLocationsFromData();
        for (Location location : locations) {
            randomizer.addLocation(location);
        }

        // Generate seed
        RandomizationResult result = randomizer.randomize();

        // Save complete randomization result (needed for ROM generation)
        storageService.saveRandomizationResult(seedId, result);

        // Basic quality metrics (limited for basic randomizer)
        QualityMetricsDto qualityMetrics = QualityMetricsDto.fromBasicMetrics(
                "Basic",
                5.0,
                100.0,
                difficultyId
        );

        // Generate warnings
        List<String> warnings = new ArrayList<>();
        if (!result.isSuccessful()) {
            warnings.add("Seed generation completed with some items unplaced");
        }
        warnings.add("Basic randomizer does not perform quality validation");

        // Add difficulty info
        if (difficulty != null) {
            warnings.add(String.format("Difficulty: %s (%s)", difficulty.getName(), difficulty.getDescription()));
        }

        // Create response
        SeedResponse response = SeedResponse.success(
                seedId,
                seed,
                "basic",
                qualityMetrics,
                warnings
        );

        // Save seed metadata
        storageService.saveSeedMetadata(seedId, response);

        // Save spoiler log if enabled
        if (request.isSpoilerEnabled()) {
            String spoilerLog = generateSpoilerLog(result);
            storageService.saveSpoilerLog(seedId, spoilerLog);
        }

        logger.info("Successfully generated seed with ID: {} (difficulty: {})", seedId, difficultyId);
        return response;
    }

    /**
     * Calculates quality metrics for a generated seed.
     */
    private QualityMetricsDto calculateQualityMetrics(
            RandomizationResult result,
            ForesightRandomizer randomizer
    ) {
        try {
            // Get quality metrics from randomizer
            SeedQualityMetrics metrics = randomizer.getQualityMetrics();

            if (metrics == null) {
                logger.warn("Quality metrics are null, returning default metrics");
                return createDefaultQualityMetrics();
            }

            // Map quality rating based on path quality score
            String rating = mapQualityRating(metrics.getPathQualityScore());

            // Convert to DTO
            return new QualityMetricsDto(
                    rating,
                    metrics.getPathQualityScore(),
                    metrics.getReachablePercentage(),
                    metrics.getDifficultyRating(),
                    randomizer.getBacktrackCount(),
                    extractWarnings(metrics)
            );
        } catch (Exception e) {
            logger.warn("Failed to calculate quality metrics", e);
            return QualityMetricsDto.fromBasicMetrics(
                    "Unknown",
                    0.0,
                    0.0,
                    "Unknown"
            );
        }
    }

    /**
     * Creates default quality metrics when calculation fails.
     *
     * @return default quality metrics DTO
     */
    private QualityMetricsDto createDefaultQualityMetrics() {
        return new QualityMetricsDto(
                "Fair",
                5.0,
                50.0,
                "Moderate",
                0,
                List.of("Quality metrics calculation failed")
        );
    }

    /**
     * Maps numerical quality score to rating string.
     */
    private String mapQualityRating(double score) {
        if (score >= 9.0) return "Excellent";
        if (score >= 7.0) return "Good";
        if (score >= 5.0) return "Fair";
        return "Poor";
    }

    /**
     * Extracts warnings from quality metrics.
     */
    private List<String> extractWarnings(SeedQualityMetrics metrics) {
        List<String> warnings = new ArrayList<>();
        // Add any quality warnings from metrics
        if (metrics.getReachablePercentage() < 100.0) {
            warnings.add(String.format("Only %.1f%% of locations are reachable", metrics.getReachablePercentage()));
        }
        return warnings;
    }

    /**
     * Generates a printable spoiler log from randomization result.
     */
    private String generateSpoilerLog(RandomizationResult result) {
        PrintableSpoiler spoiler = new PrintableSpoiler(result);
        return spoiler.generateSpoilerLog();
    }

    /**
     * Creates location list from loaded data.
     */
    private List<Location> createLocationsFromData() {
        List<Location> locations = new ArrayList<>();
        LocationData locationData = dataLoader.getLocationData();

        if (locationData != null && locationData.getLocations() != null) {
            for (LocationData.LocationDefinition locDef : locationData.getLocations()) {
                Location location = Location.builder()
                    .id(locDef.getId())
                    .name(locDef.getName())
                    .region(locDef.getRegion())
                    .requirements(locDef.getRequirements() != null ?
                        new HashSet<>(locDef.getRequirements()) : new HashSet<>())
                    .build();
                locations.add(location);
            }
        }

        logger.info("Loaded {} locations from data", locations.size());
        return locations;
    }
}