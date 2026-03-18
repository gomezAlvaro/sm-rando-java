package com.maprando.randomize.advanced;

/**
 * Metrics about progression pacing through the game.
 */
public class ProgressionPacing {
    private final double earlyGameProgression;
    private final double midGameProgression;
    private final double lateGameProgression;

    public ProgressionPacing(double earlyGameProgression, double midGameProgression, double lateGameProgression) {
        this.earlyGameProgression = earlyGameProgression;
        this.midGameProgression = midGameProgression;
        this.lateGameProgression = lateGameProgression;
    }

    public double getEarlyGameProgression() { return earlyGameProgression; }
    public double getMidGameProgression() { return midGameProgression; }
    public double getLateGameProgression() { return lateGameProgression; }
}
