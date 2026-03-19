package com.maprando.web.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for seed generation responses.
 * Contains the results of seed generation including quality metrics and metadata.
 *
 * @param seedId             Unique identifier for the generated seed
 * @param seed               The seed string used for generation
 * @param successful         Whether the seed generation was successful
 * @param algorithmUsed      The randomization algorithm that was used
 * @param qualityMetrics     Quality metrics for the generated seed
 * @param warnings           List of warnings from the generation process
 * @param timestamp          When the seed was generated
 */
public record SeedResponse(
        String seedId,
        String seed,
        boolean successful,
        String algorithmUsed,
        QualityMetricsDto qualityMetrics,
        List<String> warnings,
        LocalDateTime timestamp
) {

    /**
     * Creates a successful SeedResponse with all required fields.
     */
    public static SeedResponse success(
            String seedId,
            String seed,
            String algorithmUsed,
            QualityMetricsDto qualityMetrics,
            List<String> warnings
    ) {
        return new SeedResponse(
                seedId,
                seed,
                true,
                algorithmUsed,
                qualityMetrics,
                warnings,
                LocalDateTime.now()
        );
    }

    /**
     * Creates a failed SeedResponse with error information.
     */
    public static SeedResponse failure(
            String seedId,
            String seed,
            String algorithmUsed,
            List<String> errors
    ) {
        return new SeedResponse(
                seedId,
                seed,
                false,
                algorithmUsed,
                QualityMetricsDto.empty(),
                errors,
                LocalDateTime.now()
        );
    }

    /**
     * Creates a simplified SeedResponse for listing recent seeds.
     */
    public static SeedResponse summary(
            String seedId,
            String seed,
            String algorithmUsed,
            String rating,
            LocalDateTime timestamp
    ) {
        return new SeedResponse(
                seedId,
                seed,
                true,
                algorithmUsed,
                new QualityMetricsDto(rating, null, null, null, null, null),
                List.of(),
                timestamp
        );
    }
}