package com.maprando.randomize.advanced;

/**
 * Difficulty curve metrics.
 */
public class DifficultyCurve {
    private final double averageDifficulty;

    public DifficultyCurve(double averageDifficulty) {
        this.averageDifficulty = averageDifficulty;
    }

    public double getAverageDifficulty() { return averageDifficulty; }
}
