package com.maprando.web.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maprando.web.dto.SeedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.UUID;

/**
 * Service for storing and retrieving seed data on the filesystem.
 * Seeds are organized in a directory structure by year and month,
 * with separate files for metadata (JSON) and spoiler logs (text).
 */
@Service
public class FilesystemSeedStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FilesystemSeedStorageService.class);

    private final String seedsBaseDir;
    private final String spoilersDir;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new FilesystemSeedStorageService.
     *
     * @param seedsBaseDir base directory for seed storage
     * @param spoilersDir  directory for spoiler log storage
     */
    public FilesystemSeedStorageService(
            @Value("${app.storage.seeds-dir:seeds}") String seedsBaseDir,
            @Value("${app.storage.spoiler-dir:seeds/spoilers}") String spoilersDir
    ) {
        this.seedsBaseDir = seedsBaseDir;
        this.spoilersDir = spoilersDir;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.objectMapper.registerModule(new JavaTimeModule());

        // Create directories if they don't exist
        createDirectoriesIfNeeded();
    }

    /**
     * Generates a unique seed ID based on UUID and current timestamp.
     *
     * @return unique seed ID
     */
    public String generateSeedId() {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        YearMonth now = YearMonth.now();
        return String.format("%d-%02d-%s", now.getYear(), now.getMonthValue(), uuid);
    }

    /**
     * Saves seed metadata as JSON file.
     *
     * @param seedId  unique seed identifier
     * @param response seed response data to save
     * @throws IOException if file writing fails
     */
    public void saveSeedMetadata(String seedId, SeedResponse response) throws IOException {
        Path metadataPath = getMetadataPath(seedId);
        ensureDirectoryExists(metadataPath.getParent());

        objectMapper.writeValue(metadataPath.toFile(), response);
        logger.info("Saved seed metadata: {}", metadataPath);
    }

    /**
     * Saves a spoiler log as a text file.
     *
     * @param seedId     unique seed identifier
     * @param spoilerLog spoiler log content
     * @throws IOException if file writing fails
     */
    public void saveSpoilerLog(String seedId, String spoilerLog) throws IOException {
        Path spoilerPath = getSpoilerPath(seedId);
        ensureDirectoryExists(spoilerPath.getParent());

        Files.writeString(spoilerPath, spoilerLog);
        logger.info("Saved spoiler log: {}", spoilerPath);
    }

    /**
     * Retrieves seed metadata by seed ID.
     *
     * @param seedId unique seed identifier
     * @return SeedResponse containing seed metadata
     * @throws IOException if file reading fails
     */
    public SeedResponse getSeedMetadata(String seedId) throws IOException {
        Path metadataPath = getMetadataPath(seedId);
        if (!Files.exists(metadataPath)) {
            throw new IOException("Seed not found: " + seedId);
        }

        return objectMapper.readValue(metadataPath.toFile(), SeedResponse.class);
    }

    /**
     * Retrieves a spoiler log by seed ID.
     *
     * @param seedId unique seed identifier
     * @return spoiler log content
     * @throws IOException if file reading fails
     */
    public String getSpoilerLog(String seedId) throws IOException {
        Path spoilerPath = getSpoilerPath(seedId);
        if (!Files.exists(spoilerPath)) {
            throw new IOException("Spoiler log not found: " + seedId);
        }

        return Files.readString(spoilerPath);
    }

    /**
     * Checks if a seed exists.
     *
     * @param seedId unique seed identifier
     * @return true if seed exists, false otherwise
     */
    public boolean seedExists(String seedId) {
        return Files.exists(getMetadataPath(seedId));
    }

    /**
     * Checks if a spoiler log exists for a seed.
     *
     * @param seedId unique seed identifier
     * @return true if spoiler log exists, false otherwise
     */
    public boolean spoilerExists(String seedId) {
        return Files.exists(getSpoilerPath(seedId));
    }

    /**
     * Gets the file path for seed metadata.
     */
    private Path getMetadataPath(String seedId) {
        YearMonth date = parseYearMonthFromSeedId(seedId);
        String dateDir = String.format("%d/%02d", date.getYear(), date.getMonthValue());
        return Paths.get(seedsBaseDir, dateDir, seedId + ".json");
    }

    /**
     * Gets the file path for spoiler log.
     */
    private Path getSpoilerPath(String seedId) {
        YearMonth date = parseYearMonthFromSeedId(seedId);
        String dateDir = String.format("%d/%02d", date.getYear(), date.getMonthValue());
        return Paths.get(spoilersDir, dateDir, seedId + ".txt");
    }

    /**
     * Parses YearMonth from a seed ID.
     */
    private YearMonth parseYearMonthFromSeedId(String seedId) {
        try {
            String[] parts = seedId.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            return YearMonth.of(year, month);
        } catch (Exception e) {
            logger.warn("Could not parse date from seed ID {}, using current date", seedId);
            return YearMonth.now();
        }
    }

    /**
     * Creates base directories if they don't exist.
     */
    private void createDirectoriesIfNeeded() {
        try {
            Files.createDirectories(Paths.get(seedsBaseDir));
            Files.createDirectories(Paths.get(spoilersDir));
            logger.info("Ensured storage directories exist: {}, {}", seedsBaseDir, spoilersDir);
        } catch (IOException e) {
            logger.error("Failed to create storage directories", e);
            throw new RuntimeException("Failed to create storage directories", e);
        }
    }

    /**
     * Ensures a directory exists, creating it if necessary.
     */
    private void ensureDirectoryExists(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
    }
}