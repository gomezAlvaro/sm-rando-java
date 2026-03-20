package com.maprando.model;

import java.util.Objects;

/**
 * Definition of a tech ability from Rust tech_data.json.
 * Techs represent player techniques/skills (e.g., canHeatRun, canWalljump).
 * Aligned with Rust MapRandomizer tech system.
 */
public class TechDefinition {
    private final int techId;
    private final String name;
    private final String difficulty;
    private final Integer videoId; // Can be null

    /**
     * Creates a new tech definition from Rust tech_data.json format.
     */
    public TechDefinition(int techId, String name, String difficulty, Integer videoId) {
        this.techId = techId;
        this.name = name;
        this.difficulty = difficulty;
        this.videoId = videoId;
    }

    public int getTechId() { return techId; }
    public String getName() { return name; }
    public String getDifficulty() { return difficulty; }
    public Integer getVideoId() { return videoId; }

    // Compatibility: map techId to index for existing code
    public int getIndex() { return techId; }
    public String getId() { return name; } // Use name as ID for compatibility
    public String getDescription() {
        return difficulty + (videoId != null ? " (video: " + videoId + ")" : "");
    }

    @Override
    public String toString() {
        return "TechDefinition{" +
                "techId=" + techId +
                ", name='" + name + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", videoId=" + videoId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TechDefinition that = (TechDefinition) o;
        return techId == that.techId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(techId);
    }
}
