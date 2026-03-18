package com.maprando.traversal;

/**
 * Difficulty progression analysis for a seed.
 */
public class DifficultyProgression {

    private final String earlyGameDifficulty;
    private final String midGameDifficulty;
    private final String lateGameDifficulty;
    private final double overallDifficultyScore;
    private final String difficultyTrend;

    public DifficultyProgression(String earlyGameDifficulty, String midGameDifficulty,
                               String lateGameDifficulty, double overallDifficultyScore,
                               String difficultyTrend) {
        this.earlyGameDifficulty = earlyGameDifficulty;
        this.midGameDifficulty = midGameDifficulty;
        this.lateGameDifficulty = lateGameDifficulty;
        this.overallDifficultyScore = overallDifficultyScore;
        this.difficultyTrend = difficultyTrend;
    }

    public String getEarlyGameDifficulty() {
        return earlyGameDifficulty;
    }

    public String getMidGameDifficulty() {
        return midGameDifficulty;
    }

    public String getLateGameDifficulty() {
        return lateGameDifficulty;
    }

    public double getOverallDifficultyScore() {
        return overallDifficultyScore;
    }

    public String getDifficultyTrend() {
        return difficultyTrend;
    }

    @Override
    public String toString() {
        return "DifficultyProgression{" +
                "early='" + earlyGameDifficulty + '\'' +
                ", mid='" + midGameDifficulty + '\'' +
                ", late='" + lateGameDifficulty + '\'' +
                ", trend=" + difficultyTrend +
                '}';
    }
}