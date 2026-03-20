package com.maprando.web.service;

import com.maprando.data.DataLoader;
import com.maprando.patch.Rom;
import com.maprando.patch.RomGenerator;
import com.maprando.randomize.RandomizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for generating ROM files from randomization results.
 * Handles ROM generation, caching, and file management.
 *
 * ROMs are generated on-demand and cached for performance.
 * Base ROM path is configured in application.properties.
 */
@Service
public class RomGenerationService {

    private static final Logger logger = LoggerFactory.getLogger(RomGenerationService.class);

    private final DataLoader dataLoader;
    private final FilesystemSeedStorageService storageService;

    @Value("${randomizer.base-rom-path:./data/vanilla.smc}")
    private String baseRomPathConfig;

    @Value("${randomizer.rom-output-dir:./roms}")
    private String romOutputDirConfig;

    @Value("${randomizer.cache-generated-roms:true}")
    private boolean cacheRoms;

    private Path baseRomPath;
    private Path romOutputDir;
    private RomGenerator romGenerator;

    // ROM cache: seedId -> ROM file path
    private final ConcurrentHashMap<String, Path> romCache = new ConcurrentHashMap<>();

    /**
     * Creates a new RomGenerationService.
     *
     * @param dataLoader     data loader for ROM mappings
     * @param storageService filesystem storage service for seed metadata
     */
    public RomGenerationService(
            DataLoader dataLoader,
            FilesystemSeedStorageService storageService
    ) {
        this.dataLoader = dataLoader;
        this.storageService = storageService;
    }

    /**
     * Initializes the ROM generator after Spring configuration is loaded.
     *
     * @throws IOException if base ROM cannot be loaded or output directory cannot be created
     */
    @PostConstruct
    public void init() throws IOException {
        this.baseRomPath = Paths.get(baseRomPathConfig);
        this.romOutputDir = Paths.get(romOutputDirConfig);

        // Validate base ROM exists
        if (!Files.exists(baseRomPath)) {
            logger.warn("Base ROM not found at: {}. ROM generation will be disabled.", baseRomPath);
            logger.warn("To enable ROM generation, place a vanilla Super Metroid ROM at: {}", baseRomPath);
            this.romGenerator = null;
            return;
        }

        // Create ROM output directory
        if (!Files.exists(romOutputDir)) {
            Files.createDirectories(romOutputDir);
            logger.info("Created ROM output directory: {}", romOutputDir);
        }

        // Initialize ROM generator
        try {
            this.romGenerator = new RomGenerator(baseRomPath, dataLoader);
            logger.info("ROM generator initialized successfully");
            logger.info("Base ROM: {}", baseRomPath);
            logger.info("Output directory: {}", romOutputDir);
        } catch (IOException e) {
            logger.error("Failed to initialize ROM generator", e);
            this.romGenerator = null;
        }
    }

    /**
     * Generates a ROM file for a seed and returns the path.
     * Uses cache if ROM was previously generated.
     *
     * @param seedId seed identifier
     * @return path to generated ROM file
     * @throws IOException if ROM cannot be generated
     * @throws IllegalStateException if ROM generator is not initialized
     */
    public Path generateRom(String seedId) throws IOException {
        if (romGenerator == null) {
            throw new IllegalStateException("ROM generator is not initialized. Check base ROM configuration.");
        }

        // Check cache first
        if (cacheRoms && romCache.containsKey(seedId)) {
            Path cachedPath = romCache.get(seedId);
            if (Files.exists(cachedPath)) {
                logger.debug("Using cached ROM for seed: {}", seedId);
                return cachedPath;
            } else {
                // Remove from cache if file no longer exists
                romCache.remove(seedId);
            }
        }

        // Load seed metadata
        if (!storageService.seedExists(seedId)) {
            throw new IllegalArgumentException("Seed not found: " + seedId);
        }

        // Reconstruct randomization result from stored metadata
        RandomizationResult result = reconstructRandomizationResult(seedId);

        // Generate ROM
        Path outputPath = romOutputDir.resolve(seedId + ".smc");
        romGenerator.generateAndSave(result, outputPath);

        // Cache the ROM path
        if (cacheRoms) {
            romCache.put(seedId, outputPath);
        }

        logger.info("Generated ROM for seed {}: {}", seedId, outputPath);
        return outputPath;
    }

    /**
     * Gets the ROM file path for a seed, generating it if necessary.
     *
     * @param seedId seed identifier
     * @return path to ROM file
     * @throws IOException if ROM cannot be generated
     */
    public Path getRomPath(String seedId) throws IOException {
        return generateRom(seedId);
    }

    /**
     * Generates a ROM and returns it as a byte array.
     * Useful for direct download without file storage.
     *
     * @param seedId seed identifier
     * @return ROM file contents as byte array
     * @throws IOException if ROM cannot be generated
     */
    public byte[] generateRomBytes(String seedId) throws IOException {
        if (romGenerator == null) {
            throw new IllegalStateException("ROM generator is not initialized. Check base ROM configuration.");
        }

        // Load seed metadata
        if (!storageService.seedExists(seedId)) {
            throw new IllegalArgumentException("Seed not found: " + seedId);
        }

        // Reconstruct randomization result from stored metadata
        RandomizationResult result = reconstructRandomizationResult(seedId);

        // Generate ROM in memory
        Rom rom = romGenerator.generate(result);

        return rom.data;
    }

    /**
     * Checks if ROM generation is available (base ROM is configured).
     *
     * @return true if ROM generation is available, false otherwise
     */
    public boolean isRomGenerationAvailable() {
        return romGenerator != null;
    }

    /**
     * Checks if a ROM has been generated for a seed.
     *
     * @param seedId seed identifier
     * @return true if ROM file exists, false otherwise
     */
    public boolean romExists(String seedId) {
        if (!cacheRoms) {
            return false;
        }

        Path cachedPath = romCache.get(seedId);
        return cachedPath != null && Files.exists(cachedPath);
    }

    /**
     * Clears the ROM cache.
     */
    public void clearCache() {
        romCache.clear();
        logger.info("ROM cache cleared");
    }

    /**
     * Reconstructs a RandomizationResult from stored seed metadata.
     * NOTE: This is a simplified reconstruction for proof-of-concept.
     * Production should store the full RandomizationResult.
     *
     * @param seedId seed identifier
     * @return reconstructed randomization result
     * @throws IOException if metadata cannot be loaded
     */
    private RandomizationResult reconstructRandomizationResult(String seedId) throws IOException {
        // Load seed metadata
        var metadata = storageService.getSeedMetadata(seedId);

        // For proof-of-concept, create a simple result with basic metadata
        // In production, the full RandomizationResult should be stored
        RandomizationResult.Builder builder = RandomizationResult.builder()
            .seed(metadata.seed())
            .timestamp(metadata.timestamp())
            .algorithmUsed(metadata.algorithmUsed());

        // TODO: Load actual placements from storage
        // For now, we'll need to store the full placements or reconstruct them
        // This is a limitation of the current implementation

        return builder.build();
    }

    /**
     * Gets the base ROM path.
     *
     * @return base ROM path
     */
    public Path getBaseRomPath() {
        return baseRomPath;
    }

    /**
     * Gets the ROM output directory.
     *
     * @return ROM output directory
     */
    public Path getRomOutputDir() {
        return romOutputDir;
    }

    /**
     * Checks if ROM caching is enabled.
     *
     * @return true if caching is enabled, false otherwise
     */
    public boolean isCacheEnabled() {
        return cacheRoms;
    }

    /**
     * Gets the cache size (number of cached ROMs).
     *
     * @return cache size
     */
    public int getCacheSize() {
        return romCache.size();
    }
}
