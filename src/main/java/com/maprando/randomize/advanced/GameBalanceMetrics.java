package com.maprando.randomize.advanced;

/**
 * Metrics about game balance.
 */
public class GameBalanceMetrics {
    private final double balanceScore;

    public GameBalanceMetrics(double balanceScore) {
        this.balanceScore = balanceScore;
    }

    public double getBalanceScore() { return balanceScore; }
}
