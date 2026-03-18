package com.maprando.randomize.advanced;

/**
 * Quality rating for a seed.
 */
public class QualityRating {
    private final String rating;
    private final double score;

    public QualityRating(String rating, double score) {
        this.rating = rating;
        this.score = score;
    }

    public String getRating() { return rating; }
    public double getScore() { return score; }
}
