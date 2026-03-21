package com.maprando.web.service;

import com.maprando.data.DataLoader;
import com.maprando.randomize.advanced.QualityMetricsCalculator;
import com.maprando.web.dto.SeedRequest;
import com.maprando.web.dto.SeedResponse;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SeedGenerationService.
 * Tests seed generation logic with various algorithms and configurations.
 * Uses real services for integration testing to avoid mocking issues.
 */
class SeedGenerationServiceTest {

    private DataLoader dataLoader;
    private QualityMetricsCalculator qualityMetricsCalculator;
    private FilesystemSeedStorageService storageService;
    private SeedGenerationService seedGenerationService;
    private Path tempStorageDir;

    @BeforeEach
    void setUp() throws IOException {
        // Create temporary storage directory for tests
        tempStorageDir = Files.createTempDirectory("test-seeds-");

        // Use real instances for integration testing
        dataLoader = new DataLoader();
        dataLoader.loadAllData();

        qualityMetricsCalculator = new QualityMetricsCalculator(dataLoader);

        // Create storage service with temporary directory
        storageService = new FilesystemSeedStorageService(
            tempStorageDir.toString(),
            tempStorageDir.resolve("spoilers").toString()
        );

        seedGenerationService = new SeedGenerationService(
                dataLoader,
                qualityMetricsCalculator,
                storageService
        );
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up temporary directory
        if (tempStorageDir != null && Files.exists(tempStorageDir)) {
            Files.walk(tempStorageDir)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        // Ignore cleanup errors
                    }
                });
        }
    }

    @Test
    @DisplayName("Should generate seed with foresight algorithm")
    void testGenerateSeedWithForesight() {
        // Arrange
        SeedRequest request = new SeedRequest(
                "test-seed",
                "foresight",
                "normal",
                true,
                true
        );

        // Act
        SeedResponse response = seedGenerationService.generateSeed(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.seedId());
        assertEquals("test-seed", response.seed());
        assertEquals("foresight", response.algorithmUsed());
        assertTrue(response.successful());
    }

    @Test
    @DisplayName("Should generate seed with basic algorithm")
    void testGenerateSeedWithBasic() {
        // Arrange
        SeedRequest request = new SeedRequest(
                "test-seed",
                "basic",
                "casual",
                true,
                false
        );

        // Act
        SeedResponse response = seedGenerationService.generateSeed(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.seedId());
        assertEquals("test-seed", response.seed());
        assertEquals("basic", response.algorithmUsed());
        assertTrue(response.successful());
    }

    @Test
    @DisplayName("Should generate random seed when seed is null")
    void testGenerateSeedWithNullSeed() {
        // Arrange
        SeedRequest request = new SeedRequest(
                null,
                "foresight",
                "normal",
                true,
                true
        );

        // Act
        SeedResponse response = seedGenerationService.generateSeed(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.seed());
        assertNotEquals("", response.seed());
        assertTrue(response.successful());
    }

    @Test
    @DisplayName("Should generate random seed when seed is blank")
    void testGenerateSeedWithBlankSeed() {
        // Arrange
        SeedRequest request = new SeedRequest(
                "   ",
                "foresight",
                "normal",
                true,
                true
        );

        // Act
        SeedResponse response = seedGenerationService.generateSeed(request);

        // Assert
        assertNotNull(response);
        assertNotNull(response.seed());
        assertNotEquals("   ", response.seed());
        assertTrue(response.successful());
    }

    @Test
    @DisplayName("Should save seed metadata")
    void testSavesSeedMetadata() {
        // Arrange
        SeedRequest request = new SeedRequest(
                "metadata-test",
                "foresight",
                "normal",
                true,
                true
        );

        // Act
        SeedResponse response = seedGenerationService.generateSeed(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.successful());

        // Verify metadata was saved
        assertTrue(storageService.seedExists(response.seedId()));
    }

    @Test
    @DisplayName("Should save spoiler when enabled")
    void testSavesSpoilerWhenEnabled() {
        // Arrange
        SeedRequest request = new SeedRequest(
                "spoiler-test",
                "foresight",
                "normal",
                true,  // enableSpoiler
                true
        );

        // Act
        SeedResponse response = seedGenerationService.generateSeed(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.successful());
        assertTrue(storageService.spoilerExists(response.seedId()));
    }

    @Test
    @DisplayName("Should not save spoiler when disabled")
    void testDoesNotSaveSpoilerWhenDisabled() {
        // Arrange
        SeedRequest request = new SeedRequest(
                "no-spoiler-test",
                "foresight",
                "normal",
                false,  // enableSpoiler
                true
        );

        // Act
        SeedResponse response = seedGenerationService.generateSeed(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.successful());
        assertFalse(storageService.spoilerExists(response.seedId()));
    }

    @Test
    @DisplayName("Should return quality metrics")
    void testReturnsQualityMetrics() {
        // Arrange
        SeedRequest request = new SeedRequest(
                "metrics-test",
                "foresight",
                "normal",
                true,
                true
        );

        // Act
        SeedResponse response = seedGenerationService.generateSeed(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.successful());
        assertNotNull(response.qualityMetrics());
        assertNotNull(response.qualityMetrics().rating());
        assertNotNull(response.qualityMetrics().difficultyAssessment());
    }

    @Test
    @DisplayName("Should include timestamp in response")
    void testIncludesTimestamp() {
        // Arrange
        SeedRequest request = new SeedRequest(
                "timestamp-test",
                "foresight",
                "normal",
                true,
                true
        );

        // Act
        SeedResponse response = seedGenerationService.generateSeed(request);

        // Assert
        assertNotNull(response);
        assertTrue(response.successful());
        assertNotNull(response.timestamp());
    }

    @Test
    @DisplayName("Should generate consistent seeds for same input")
    void testGeneratesConsistentSeeds() {
        // Arrange
        SeedRequest request1 = new SeedRequest(
                "consistency-test",
                "foresight",
                "normal",
                true,
                true
        );

        SeedRequest request2 = new SeedRequest(
                "consistency-test",
                "foresight",
                "normal",
                true,
                true
        );

        // Act
        SeedResponse response1 = seedGenerationService.generateSeed(request1);
        SeedResponse response2 = seedGenerationService.generateSeed(request2);

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertTrue(response1.successful());
        assertTrue(response2.successful());
        // Seeds should be the same for same input
        assertEquals(response1.seed(), response2.seed());
    }

    @Test
    @DisplayName("Should generate different seeds for different inputs")
    void testGeneratesDifferentSeedsForDifferentInputs() {
        // Arrange
        SeedRequest request1 = new SeedRequest(
                "different-test-1",
                "foresight",
                "normal",
                true,
                true
        );

        SeedRequest request2 = new SeedRequest(
                "different-test-2",
                "foresight",
                "normal",
                true,
                true
        );

        // Act
        SeedResponse response1 = seedGenerationService.generateSeed(request1);
        SeedResponse response2 = seedGenerationService.generateSeed(request2);

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertTrue(response1.successful());
        assertTrue(response2.successful());
        // Seeds should be different for different inputs
        assertNotEquals(response1.seed(), response2.seed());
    }
}