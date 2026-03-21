package com.maprando.web.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maprando.web.dto.SeedRequest;
import com.maprando.web.dto.SeedResponse;
import com.maprando.web.service.FilesystemSeedStorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the complete API flow.
 * Tests end-to-end scenarios from request generation to retrieval and download.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FilesystemSeedStorageService storageService;

    private static final String TEST_SEEDS_DIR = "test-seeds-integration";
    private Path testSeedsPath;

    @BeforeEach
    void setUp() throws Exception {
        // Setup test directory
        testSeedsPath = Paths.get(TEST_SEEDS_DIR);
        if (Files.exists(testSeedsPath)) {
            Files.walk(testSeedsPath)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            // Ignore
                        }
                    });
        }
        Files.createDirectories(testSeedsPath);
    }

    @AfterEach
    void tearDown() throws Exception {
        // Cleanup test directory
        if (Files.exists(testSeedsPath)) {
            Files.walk(testSeedsPath)
                    .sorted((a, b) -> b.compareTo(a))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception e) {
                            // Ignore
                        }
                    });
        }
    }

    @Test
    @DisplayName("Integration test: Complete seed generation and retrieval flow")
    void testCompleteSeedFlow() throws Exception {
        // Step 1: Generate a seed
        SeedRequest request = new SeedRequest(
                "integration-test-seed",
                "foresight",
                "Hard",
                true,
                true
        );

        MvcResult generateResult = mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seedId").exists())
                .andExpect(jsonPath("$.successful").value(true))
                .andReturn();

        // Extract seed ID from response
        String responseContent = generateResult.getResponse().getContentAsString();
        SeedResponse seedResponse = objectMapper.readValue(responseContent, SeedResponse.class);
        String seedId = seedResponse.seedId();

        // Step 2: Retrieve the seed details
        mockMvc.perform(get("/api/seeds/{seedId}", seedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seedId").value(seedId))
                .andExpect(jsonPath("$.seed").value("integration-test-seed"));

        // Step 3: Check if spoiler exists
        mockMvc.perform(get("/api/seeds/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Step 4: Download the spoiler log
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentType("text/plain"));
    }

    @Test
    @DisplayName("Integration test: Generate multiple seeds and list recent")
    void testMultipleSeedsFlow() throws Exception {
        // Generate multiple seeds
        for (int i = 1; i <= 3; i++) {
            SeedRequest request = new SeedRequest(
                    "multi-test-" + i,
                    "foresight",
                    "Hard",
                    true,
                    true
            );

            mockMvc.perform(post("/api/seeds/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // List recent seeds
        mockMvc.perform(get("/api/seeds/recent")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Integration test: Generate seed without spoiler")
    void testSeedGenerationWithoutSpoiler() throws Exception {
        // Generate seed without spoiler
        SeedRequest request = new SeedRequest(
                "no-spoiler-test",
                "basic",
                "Basic",
                false,  // enableSpoiler = false
                false
        );

        MvcResult result = mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract seed ID
        String responseContent = result.getResponse().getContentAsString();
        SeedResponse seedResponse = objectMapper.readValue(responseContent, SeedResponse.class);
        String seedId = seedResponse.seedId();

        // Verify spoiler doesn't exist
        mockMvc.perform(get("/api/seeds/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("Integration test: Generate seeds with different algorithms")
    void testDifferentAlgorithms() throws Exception {
        // Test with both algorithms
        String[] algorithms = {"foresight", "basic"};

        for (String algorithm : algorithms) {
            SeedRequest request = new SeedRequest(
                    "algo-test-" + algorithm,
                    algorithm,
                    "Hard",
                    true,
                    true
            );

            mockMvc.perform(post("/api/seeds/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.algorithmUsed").value(algorithm));
        }
    }

    @Test
    @DisplayName("Integration test: Generate seeds with different difficulties")
    void testDifferentDifficulties() throws Exception {
        String[] difficulties = {"Basic", "Hard", "Very Hard", "Expert+"};

        for (String difficulty : difficulties) {
            SeedRequest request = new SeedRequest(
                    "diff-test-" + difficulty,
                    "foresight",
                    difficulty,
                    true,
                    true
            );

            mockMvc.perform(post("/api/seeds/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.qualityMetrics.difficultyAssessment").exists());
        }
    }

    @Test
    @DisplayName("Integration test: Handle non-existent seed retrieval")
    void testNonExistentSeedRetrieval() throws Exception {
        String nonExistentSeedId = "2024-03-nonexistent";

        // Try to retrieve non-existent seed
        mockMvc.perform(get("/api/seeds/{seedId}", nonExistentSeedId))
                .andExpect(status().isNotFound());

        // Try to download spoiler for non-existent seed
        mockMvc.perform(get("/seed/{seedId}/spoiler", nonExistentSeedId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Integration test: Validation errors return 400")
    void testValidationErrors() throws Exception {
        // Test missing algorithm
        String missingAlgorithm = """
                {
                    "seed": "test",
                    "difficulty": "Hard"
                }
                """;

        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingAlgorithm))
                .andExpect(status().isBadRequest());

        // Test invalid algorithm
        SeedRequest invalidAlgorithm = new SeedRequest(
                "test",
                "invalid-algo",
                "Hard",
                true,
                true
        );

        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAlgorithm)))
                .andExpect(status().isBadRequest());

        // Test invalid difficulty
        SeedRequest invalidDifficulty = new SeedRequest(
                "test",
                "foresight",
                "impossible",
                true,
                true
        );

        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDifficulty)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Integration test: Auto-generate seed when not provided")
    void testAutoGenerateSeed() throws Exception {
        // Don't provide a seed - should auto-generate
        SeedRequest request = new SeedRequest(
                null,  // No seed provided
                "foresight",
                "Hard",
                true,
                true
        );

        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seed").exists())
                .andExpect(jsonPath("$.seed").isNotEmpty());
    }

    @Test
    @DisplayName("Integration test: Health check endpoint")
    void testHealthCheck() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.application").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Integration test: Seed persistence across requests")
    void testSeedPersistence() throws Exception {
        // Generate a seed
        SeedRequest request = new SeedRequest(
                "persistence-test",
                "basic",
                "Hard",
                true,
                true
        );

        MvcResult result = mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract seed ID
        String responseContent = result.getResponse().getContentAsString();
        SeedResponse seedResponse = objectMapper.readValue(responseContent, SeedResponse.class);
        String seedId = seedResponse.seedId();

        // Retrieve seed multiple times to verify persistence
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/seeds/{seedId}", seedId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.seedId").value(seedId))
                    .andExpect(jsonPath("$.seed").value("persistence-test"));
        }
    }

    @Test
    @DisplayName("Integration test: Quality metrics are returned")
    void testQualityMetricsReturned() throws Exception {
        SeedRequest request = new SeedRequest(
                "metrics-test",
                "foresight",
                "Hard",
                true,
                true
        );

        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.qualityMetrics").exists())
                .andExpect(jsonPath("$.qualityMetrics.rating").exists())
                .andExpect(jsonPath("$.qualityMetrics.overallScore").exists())
                .andExpect(jsonPath("$.qualityMetrics.reachablePercentage").exists())
                .andExpect(jsonPath("$.qualityMetrics.difficultyAssessment").exists());
    }

    @Test
    @DisplayName("Integration test: Timestamp is included in response")
    void testTimestampIncluded() throws Exception {
        SeedRequest request = new SeedRequest(
                "timestamp-test",
                "foresight",
                "Hard",
                true,
                true
        );

        MvcResult result = mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        SeedResponse seedResponse = objectMapper.readValue(responseContent, SeedResponse.class);

        assertNotNull(seedResponse.timestamp());
    }

    @Test
    @DisplayName("Integration test: CORS headers allow frontend access")
    void testCorsHeaders() throws Exception {
        // Test CORS preflight
        mockMvc.perform(options("/api/seeds/generate")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());

        mockMvc.perform(options("/api/seeds/{id}", "test-id")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());
    }
}