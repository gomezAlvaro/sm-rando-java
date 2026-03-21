package com.maprando.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maprando.web.dto.QualityMetricsDto;
import com.maprando.web.dto.SeedRequest;
import com.maprando.web.dto.SeedResponse;
import com.maprando.web.service.SeedGenerationService;
import com.maprando.web.service.FilesystemSeedStorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for SeedApiController.
 * Tests REST API endpoints with MockMvc.
 */
@WebMvcTest(SeedApiController.class)
class SeedApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeedGenerationService seedGenerationService;

    @MockBean
    private FilesystemSeedStorageService storageService;

    @Test
    @DisplayName("POST /api/seeds/generate should create seed successfully")
    void testGenerateSeed_Success() throws Exception {
        // Arrange
        SeedRequest request = new SeedRequest(
                "test-seed",
                "foresight",
                "Hard",
                true,
                true
        );

        QualityMetricsDto metrics = new QualityMetricsDto(
                "Good",
                7.5,
                92.3,
                "Moderate",
                5,
                List.of()
        );

        SeedResponse response = SeedResponse.success(
                "2024-03-test123",
                "test-seed",
                "foresight",
                metrics,
                List.of()
        );

        when(seedGenerationService.generateSeed(any(SeedRequest.class)))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seedId").value("2024-03-test123"))
                .andExpect(jsonPath("$.seed").value("test-seed"))
                .andExpect(jsonPath("$.successful").value(true))
                .andExpect(jsonPath("$.algorithmUsed").value("foresight"))
                .andExpect(jsonPath("$.qualityMetrics.rating").value("Good"));

        verify(seedGenerationService, times(1)).generateSeed(any(SeedRequest.class));
    }

    @Test
    @DisplayName("POST /api/seeds/generate should handle validation errors")
    void testGenerateSeed_ValidationError() throws Exception {
        // Arrange - invalid algorithm
        SeedRequest request = new SeedRequest(
                "test-seed",
                "invalid-algorithm",  // Invalid algorithm
                "Hard",
                true,
                true
        );

        // Act & Assert
        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(seedGenerationService, never()).generateSeed(any(SeedRequest.class));
    }

    @Test
    @DisplayName("POST /api/seeds/generate should require algorithm field")
    void testGenerateSeed_MissingAlgorithm() throws Exception {
        // Arrange - missing algorithm
        String requestBody = """
                {
                    "seed": "test-seed",
                    "difficulty": "Hard",
                    "enableSpoiler": true,
                    "qualityValidation": true
                }
                """;

        // Act & Assert
        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verify(seedGenerationService, never()).generateSeed(any(SeedRequest.class));
    }

    @Test
    @DisplayName("POST /api/seeds/generate should handle generation failures")
    void testGenerateSeed_GenerationFailure() throws Exception {
        // Arrange
        SeedRequest request = new SeedRequest(
                "test-seed",
                "foresight",
                "Hard",
                true,
                true
        );

        SeedResponse errorResponse = SeedResponse.failure(
                "2024-03-failed",
                "test-seed",
                "foresight",
                List.of("Generation failed: insufficient locations")
        );

        when(seedGenerationService.generateSeed(any(SeedRequest.class)))
                .thenReturn(errorResponse);

        // Act & Assert
        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.successful").value(false))
                .andExpect(jsonPath("$.warnings").isArray());

        verify(seedGenerationService, times(1)).generateSeed(any(SeedRequest.class));
    }

    @Test
    @DisplayName("GET /api/seeds/{seedId} should return seed details")
    void testGetSeed_Success() throws Exception {
        // Arrange
        String seedId = "2024-03-test123";
        QualityMetricsDto metrics = new QualityMetricsDto(
                "Good",
                7.5,
                92.3,
                "Moderate",
                5,
                List.of()
        );

        SeedResponse response = SeedResponse.success(
                seedId,
                "test-seed",
                "foresight",
                metrics,
                List.of()
        );

        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.getSeedMetadata(seedId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/seeds/{seedId}", seedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seedId").value(seedId))
                .andExpect(jsonPath("$.seed").value("test-seed"))
                .andExpect(jsonPath("$.algorithmUsed").value("foresight"));

        verify(storageService, times(1)).seedExists(seedId);
        verify(storageService, times(1)).getSeedMetadata(seedId);
    }

    @Test
    @DisplayName("GET /api/seeds/{seedId} should return 404 for non-existent seed")
    void testGetSeed_NotFound() throws Exception {
        // Arrange
        String seedId = "2024-03-nonexistent";
        when(storageService.seedExists(seedId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/seeds/{seedId}", seedId))
                .andExpect(status().isNotFound());

        verify(storageService, times(1)).seedExists(seedId);
        verify(storageService, never()).getSeedMetadata(any());
    }

    @Test
    @DisplayName("GET /api/seeds/recent should return recent seeds")
    void testGetRecentSeeds_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/seeds/recent")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/seeds/recent should use default limit")
    void testGetRecentSeeds_DefaultLimit() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/seeds/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("GET /api/seeds/{seedId}/spoiler should check spoiler availability")
    void testHasSpoiler_Success() throws Exception {
        // Arrange
        String seedId = "2024-03-test123";
        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/seeds/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(storageService, times(1)).seedExists(seedId);
        verify(storageService, times(1)).spoilerExists(seedId);
    }

    @Test
    @DisplayName("GET /api/seeds/{seedId}/spoiler should return false when no spoiler")
    void testHasSpoiler_NoSpoiler() throws Exception {
        // Arrange
        String seedId = "2024-03-nospoiler";
        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/seeds/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(storageService, times(1)).seedExists(seedId);
        verify(storageService, times(1)).spoilerExists(seedId);
    }

    @Test
    @DisplayName("GET /api/seeds/{seedId}/spoiler should return 404 for non-existent seed")
    void testHasSpoiler_SeedNotFound() throws Exception {
        // Arrange
        String seedId = "2024-03-nonexistent";
        when(storageService.seedExists(seedId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/seeds/{seedId}/spoiler", seedId))
                .andExpect(status().isNotFound());

        verify(storageService, times(1)).seedExists(seedId);
        verify(storageService, never()).spoilerExists(any());
    }

    @Test
    @DisplayName("POST should accept valid difficulty levels")
    void testGenerateSeed_ValidDifficulties() throws Exception {
        // Arrange
        String[] validDifficulties = {"Basic", "Hard", "Very Hard", "Expert+", "Beyond"};

        for (String difficulty : validDifficulties) {
            SeedRequest request = new SeedRequest(
                    "test-seed",
                    "foresight",
                    difficulty,
                    true,
                    true
            );

            QualityMetricsDto metrics = new QualityMetricsDto(
                    "Good",
                    7.5,
                    92.3,
                    difficulty,
                    5,
                    List.of()
            );

            SeedResponse response = SeedResponse.success(
                    "2024-03-test123",
                    "test-seed",
                    "foresight",
                    metrics,
                    List.of()
            );

            when(seedGenerationService.generateSeed(any(SeedRequest.class)))
                    .thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/seeds/generate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    @DisplayName("POST should reject invalid difficulty levels")
    void testGenerateSeed_InvalidDifficulty() throws Exception {
        // Arrange - invalid difficulty
        SeedRequest request = new SeedRequest(
                "test-seed",
                "foresight",
                "impossible",  // Invalid difficulty
                true,
                true
        );

        // Act & Assert
        mockMvc.perform(post("/api/seeds/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("CORS headers should be present")
    void testCorsHeaders() throws Exception {
        // Act & Assert
        mockMvc.perform(options("/api/seeds/generate")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());  // CORS preflight should succeed
    }
}