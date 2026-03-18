package com.maprando.randomize.advanced;

/**
 * Assessment of seed difficulty.
 */
public class DifficultyAssessment {
    private final String overallDifficulty;
    private final double difficultyScore;

    public DifficultyAssessment(String overallDifficulty, double difficultyScore) {
        this.overallDifficulty = overallDifficulty;
        this.difficultyScore = difficultyScore;
    }

    public String getOverallDifficulty() { return overallDifficulty; }
    public double getDifficultyScore() { return difficultyScore; }
}
