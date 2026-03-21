package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Root container for difficulty presets loaded from difficulties.json.
 * JSON format: {"difficultyPresets": [...]}
 */
public class DifficultyList {

    @JsonProperty("difficultyPresets")
    private List<DifficultyData> difficultyPresets;

    public List<DifficultyData> getDifficultyPresets() {
        return difficultyPresets;
    }

    public void setDifficultyPresets(List<DifficultyData> difficultyPresets) {
        this.difficultyPresets = difficultyPresets;
    }
}
