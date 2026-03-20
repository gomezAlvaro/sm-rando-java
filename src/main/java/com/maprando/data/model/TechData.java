package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * JSON data model for techs loaded from Rust tech_data.json.
 * Aligned with Rust MapRandomizer tech structure.
 */
public class TechData {
    private List<TechDefinition> techs;

    public List<TechDefinition> getTechs() {
        return techs;
    }

    public void setTechs(List<TechDefinition> techs) {
        this.techs = techs;
    }

    /**
     * Represents a single tech definition from Rust tech_data.json.
     * Techs are player techniques/skills, not game mechanics.
     */
    public static class TechDefinition {
        @JsonProperty("tech_id")
        private int techId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("difficulty")
        private String difficulty;

        @JsonProperty("video_id")
        private Integer videoId; // Can be null

        // Getters
        public int getTechId() { return techId; }
        public String getName() { return name; }
        public String getDifficulty() { return difficulty; }
        public Integer getVideoId() { return videoId; }

        // Setters (for Jackson deserialization)
        public void setTechId(int techId) { this.techId = techId; }
        public void setName(String name) { this.name = name; }
        public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
        public void setVideoId(Integer videoId) { this.videoId = videoId; }
    }
}
