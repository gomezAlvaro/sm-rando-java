package com.maprando.web.service;

import com.maprando.web.dto.SeedResponse;
import com.maprando.web.dto.QualityMetricsDto;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for FilesystemSeedStorageService.
 * Tests filesystem-based seed storage and retrieval functionality.
 */
class FilesystemSeedStorageServiceTest {

    private FilesystemSeedStorageService storageService;
    private Path tempDir;
    private Path seedsDir;
    private Path spoilersDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create temporary directories for testing
        tempDir = Files.createTempDirectory("seed-test");
        seedsDir = tempDir.resolve("seeds");
        spoilersDir = tempDir.resolve("spoilers");

        // Create service with test directories
        storageService = new FilesystemSeedStorageService(
                seedsDir.toString(),
                spoilersDir.toString()
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temporary directories
        if (Files.exists(tempDir)) {
            Files.walk(tempDir)
                    .sorted((a, b) -> b.compareTo(a)) // Delete files before directories
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            // Ignore cleanup errors
                        }
                    });
        }
    }

    @Test
    @DisplayName("Should generate unique seed IDs")
    void testGenerateSeedId() {
        String seedId1 = storageService.generateSeedId();
        String seedId2 = storageService.generateSeedId();

        assertNotNull(seedId1);
        assertNotNull(seedId2);
        assertNotEquals(seedId1, seedId2);

        // Check format: YYYY-MM-XXXXXXXX
        assertTrue(seedId1.matches("\\d{4}-\\d{2}-[a-f0-9]{8}"));
    }

    @Test
    @DisplayName("Should save seed metadata successfully")
    void testSaveSeedMetadata() throws IOException {
        // Arrange
        String seedId = "2024-03-testseed";
        SeedResponse response = createTestSeedResponse(seedId);

        // Act
        storageService.saveSeedMetadata(seedId, response);

        // Assert
        assertTrue(storageService.seedExists(seedId));

        Path metadataPath = seedsDir.resolve("2024/03/" + seedId + ".json");
        assertTrue(Files.exists(metadataPath));

        String content = Files.readString(metadataPath);
        assertTrue(content.contains("\"seedId\""));
        assertTrue(content.contains(seedId));
    }

    @Test
    @DisplayName("Should save spoiler log successfully")
    void testSaveSpoilerLog() throws IOException {
        // Arrange
        String seedId = "2024-03-testseed";
        String spoilerLog = "Test Spoiler Log\nItem Placement:\n- Location 1: Item A";

        // Act
        storageService.saveSpoilerLog(seedId, spoilerLog);

        // Assert
        assertTrue(storageService.spoilerExists(seedId));

        Path spoilerPath = spoilersDir.resolve("2024/03/" + seedId + ".txt");
        assertTrue(Files.exists(spoilerPath));

        String content = Files.readString(spoilerPath);
        assertEquals(spoilerLog, content);
    }

    @Test
    @DisplayName("Should retrieve seed metadata successfully")
    void testGetSeedMetadata() throws IOException {
        // Arrange
        String seedId = "2024-03-testseed";
        SeedResponse originalResponse = createTestSeedResponse(seedId);
        storageService.saveSeedMetadata(seedId, originalResponse);

        // Act
        SeedResponse retrievedResponse = storageService.getSeedMetadata(seedId);

        // Assert
        assertNotNull(retrievedResponse);
        assertEquals(originalResponse.seedId(), retrievedResponse.seedId());
        assertEquals(originalResponse.seed(), retrievedResponse.seed());
        assertEquals(originalResponse.algorithmUsed(), retrievedResponse.algorithmUsed());
    }

    @Test
    @DisplayName("Should retrieve spoiler log successfully")
    void testGetSpoilerLog() throws IOException {
        // Arrange
        String seedId = "2024-03-testseed";
        String originalSpoiler = "Test Spoiler Log";
        storageService.saveSpoilerLog(seedId, originalSpoiler);

        // Act
        String retrievedSpoiler = storageService.getSpoilerLog(seedId);

        // Assert
        assertNotNull(retrievedSpoiler);
        assertEquals(originalSpoiler, retrievedSpoiler);
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent seed")
    void testGetNonExistentSeed() {
        // Arrange
        String nonExistentSeedId = "2024-03-nonexistent";

        // Act & Assert
        assertThrows(IOException.class, () -> {
            storageService.getSeedMetadata(nonExistentSeedId);
        });
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent spoiler")
    void testGetNonExistentSpoiler() {
        // Arrange
        String nonExistentSeedId = "2024-03-nonexistent";

        // Act & Assert
        assertThrows(IOException.class, () -> {
            storageService.getSpoilerLog(nonExistentSeedId);
        });
    }

    @Test
    @DisplayName("Should check if seed exists")
    void testSeedExists() throws IOException {
        // Arrange
        String seedId = "2024-03-testseed";
        SeedResponse response = createTestSeedResponse(seedId);

        // Act & Assert
        assertFalse(storageService.seedExists(seedId));

        storageService.saveSeedMetadata(seedId, response);
        assertTrue(storageService.seedExists(seedId));
    }

    @Test
    @DisplayName("Should check if spoiler exists")
    void testSpoilerExists() throws IOException {
        // Arrange
        String seedId = "2024-03-testseed";

        // Act & Assert
        assertFalse(storageService.spoilerExists(seedId));

        storageService.saveSpoilerLog(seedId, "Test spoiler");
        assertTrue(storageService.spoilerExists(seedId));
    }

    @Test
    @DisplayName("Should handle multiple seeds in same month")
    void testMultipleSeedsSameMonth() throws IOException {
        // Arrange
        String seedId1 = "2024-03-seed0001";
        String seedId2 = "2024-03-seed0002";
        SeedResponse response1 = createTestSeedResponse(seedId1);
        SeedResponse response2 = createTestSeedResponse(seedId2);

        // Act
        storageService.saveSeedMetadata(seedId1, response1);
        storageService.saveSeedMetadata(seedId2, response2);

        // Assert
        assertTrue(storageService.seedExists(seedId1));
        assertTrue(storageService.seedExists(seedId2));

        SeedResponse retrieved1 = storageService.getSeedMetadata(seedId1);
        SeedResponse retrieved2 = storageService.getSeedMetadata(seedId2);

        assertEquals(seedId1, retrieved1.seedId());
        assertEquals(seedId2, retrieved2.seedId());
    }

    @Test
    @DisplayName("Should handle seeds across different months")
    void testSeedsDifferentMonths() throws IOException {
        // Arrange
        String seedId1 = "2024-02-seed0001";
        String seedId2 = "2024-03-seed0002";
        SeedResponse response1 = createTestSeedResponse(seedId1);
        SeedResponse response2 = createTestSeedResponse(seedId2);

        // Act
        storageService.saveSeedMetadata(seedId1, response1);
        storageService.saveSeedMetadata(seedId2, response2);

        // Assert
        assertTrue(storageService.seedExists(seedId1));
        assertTrue(storageService.seedExists(seedId2));

        Path path1 = seedsDir.resolve("2024/02/" + seedId1 + ".json");
        Path path2 = seedsDir.resolve("2024/03/" + seedId2 + ".json");

        assertTrue(Files.exists(path1));
        assertTrue(Files.exists(path2));
    }

    /**
     * Helper method to create a test SeedResponse.
     */
    private SeedResponse createTestSeedResponse(String seedId) {
        QualityMetricsDto metrics = new QualityMetricsDto(
                "Good",
                7.5,
                92.3,
                "Moderate",
                5,
                List.of()
        );

        return SeedResponse.success(
                seedId,
                "test-seed",
                "foresight",
                metrics,
                List.of()
        );
    }
}