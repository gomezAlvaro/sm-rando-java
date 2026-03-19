package com.maprando.web.dto;

import java.util.List;

/**
 * Data Transfer Object for quality metrics information.
 * Contains detailed quality assessment data for generated seeds.
 *
 * @param rating                Overall quality rating (Excellent, Good, Fair, Poor)
 * @param overallScore          Overall quality score (0-10)
 * @param reachablePercentage   Percentage of locations reachable from start
 * @param difficultyAssessment  Difficulty assessment description
 * @param backtrackingCount     Number of backtracking events during generation
 * @param warnings              List of quality warnings or issues
 */
public record QualityMetricsDto(
        String rating,
        Double overallScore,
        Double reachablePercentage,
        String difficultyAssessment,
        Integer backtrackingCount,
        List<String> warnings
) {

    /**
     * Creates a QualityMetricsDto with default values for missing data.
     */
    public static QualityMetricsDto empty() {
        return new QualityMetricsDto(
                "Unknown",
                0.0,
                0.0,
                "Unknown",
                0,
                List.of()
        );
    }

    /**
     * Creates a QualityMetricsDto from a string rating and basic metrics.
     */
    public static QualityMetricsDto fromBasicMetrics(
            String rating,
            double overallScore,
            double reachablePercentage,
            String difficultyAssessment
    ) {
        return new QualityMetricsDto(
                rating,
                overallScore,
                reachablePercentage,
                difficultyAssessment,
                0,
                List.of()
        );
    }
}