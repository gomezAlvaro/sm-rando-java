package com.maprando.randomize.advanced;

/**
 * Clustering metrics for item placement.
 */
public class ClusteringMetrics {
    private final double clusteringScore;

    public ClusteringMetrics(double clusteringScore) {
        this.clusteringScore = clusteringScore;
    }

    public double getClusteringScore() { return clusteringScore; }
}
