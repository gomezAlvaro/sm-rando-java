package com.maprando.web.controller;

import com.maprando.web.service.FilesystemSeedStorageService;
import com.maprando.web.service.RomGenerationService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test suite for DownloadController.
 * Tests file download endpoints, primarily spoiler logs.
 */
@WebMvcTest(DownloadController.class)
class DownloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FilesystemSeedStorageService storageService;

    @MockBean
    private RomGenerationService romGenerationService;

    @Test
    @DisplayName("GET /seed/{seedId}/spoiler should download spoiler log")
    void testDownloadSpoiler_Success() throws Exception {
        // Arrange
        String seedId = "2024-03-test123";
        String spoilerContent = """
                Super Metroid Map Randomizer - Spoiler Log
                ==========================================
                Seed: test-seed
                Algorithm: Foresight Randomizer

                Item Placements:
                - Location 1: Morph Ball
                - Location 2: Missiles
                """;

        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(true);
        when(storageService.getSpoilerLog(seedId)).thenReturn(spoilerContent);

        // Act & Assert
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(content().contentType("text/plain"))
                .andExpect(content().string(spoilerContent));

        verify(storageService, times(1)).seedExists(seedId);
        verify(storageService, times(1)).spoilerExists(seedId);
        verify(storageService, times(1)).getSpoilerLog(seedId);
    }

    @Test
    @DisplayName("GET /seed/{seedId}/spoiler should return 404 for non-existent seed")
    void testDownloadSpoiler_SeedNotFound() throws Exception {
        // Arrange
        String seedId = "2024-03-nonexistent";
        when(storageService.seedExists(seedId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isNotFound());

        verify(storageService, times(1)).seedExists(seedId);
        verify(storageService, never()).spoilerExists(any());
        verify(storageService, never()).getSpoilerLog(any());
    }

    @Test
    @DisplayName("GET /seed/{seedId}/spoiler should return 404 when spoiler doesn't exist")
    void testDownloadSpoiler_SpoilerNotFound() throws Exception {
        // Arrange
        String seedId = "2024-03-nospoiler";
        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isNotFound());

        verify(storageService, times(1)).seedExists(seedId);
        verify(storageService, times(1)).spoilerExists(seedId);
        verify(storageService, never()).getSpoilerLog(any());
    }

    @Test
    @DisplayName("GET /seed/{seedId}/spoiler should handle IO errors")
    void testDownloadSpoiler_IOException() throws Exception {
        // Arrange
        String seedId = "2024-03-error";
        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(true);
        when(storageService.getSpoilerLog(seedId))
                .thenThrow(new java.io.IOException("File read error"));

        // Act & Assert
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isInternalServerError());

        verify(storageService, times(1)).seedExists(seedId);
        verify(storageService, times(1)).spoilerExists(seedId);
        verify(storageService, times(1)).getSpoilerLog(seedId);
    }

    @Test
    @DisplayName("GET /seed/{seedId}/spoiler should set proper content disposition")
    void testDownloadSpoiler_ContentDisposition() throws Exception {
        // Arrange
        String seedId = "2024-03-cdtest";
        String spoilerContent = "Test spoiler content";

        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(true);
        when(storageService.getSpoilerLog(seedId)).thenReturn(spoilerContent);

        // Act & Assert
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("attachment")));
    }

    @Test
    @DisplayName("CORS headers should be present on download endpoint")
    void testDownloadSpoiler_CorsHeaders() throws Exception {
        // Arrange
        String seedId = "2024-03-cors";
        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(true);
        when(storageService.getSpoilerLog(seedId)).thenReturn("Test content");

        // Act & Assert
        mockMvc.perform(options("/seed/{seedId}/spoiler", seedId)
                        .header("Access-Control-Request-Method", "GET")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk());  // CORS preflight should succeed
    }

    @Test
    @DisplayName("GET /seed/{seedId}/spoiler should handle special characters in seed ID")
    void testDownloadSpoiler_SpecialCharacters() throws Exception {
        // Arrange
        String seedId = "2024-03-special-123";
        String spoilerContent = "Test spoiler with special characters: !@#$%^&*()";

        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(true);
        when(storageService.getSpoilerLog(seedId)).thenReturn(spoilerContent);

        // Act & Assert
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(content().string(spoilerContent));

        verify(storageService, times(1)).getSpoilerLog(seedId);
    }

    @Test
    @DisplayName("GET /seed/{seedId}/spoiler should handle empty spoiler content")
    void testDownloadSpoiler_EmptyContent() throws Exception {
        // Arrange
        String seedId = "2024-03-empty";
        String spoilerContent = "";

        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(true);
        when(storageService.getSpoilerLog(seedId)).thenReturn(spoilerContent);

        // Act & Assert
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(content().string(""));

        verify(storageService, times(1)).getSpoilerLog(seedId);
    }

    @Test
    @DisplayName("GET /seed/{seedId}/spoiler should handle multi-line spoiler content")
    void testDownloadSpoiler_MultiLineContent() throws Exception {
        // Arrange
        String seedId = "2024-03-multiline";
        String spoilerContent = """
                Line 1
                Line 2
                Line 3
                Line 4
                Line 5
                """;

        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(true);
        when(storageService.getSpoilerLog(seedId)).thenReturn(spoilerContent);

        // Act & Assert
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(content().string(spoilerContent));

        verify(storageService, times(1)).getSpoilerLog(seedId);
    }

    @Test
    @DisplayName("GET /seed/{seedId}/spoiler should return proper content type")
    void testDownloadSpoiler_ContentType() throws Exception {
        // Arrange
        String seedId = "2024-03-ctype";
        String spoilerContent = "Test content";

        when(storageService.seedExists(seedId)).thenReturn(true);
        when(storageService.spoilerExists(seedId)).thenReturn(true);
        when(storageService.getSpoilerLog(seedId)).thenReturn(spoilerContent);

        // Act & Assert
        mockMvc.perform(get("/seed/{seedId}/spoiler", seedId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain"));

        verify(storageService, times(1)).getSpoilerLog(seedId);
    }
}