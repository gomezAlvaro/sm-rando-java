package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Difficulty preset data loaded from difficulties.json.
 * Contains tech assumptions (aligned with Rust MapRandomizer).
 *
 * Note: Starting items are a separate setting in the Rust project, not part of difficulty.
 * All difficulty presets start with no items by default.
 */
public class DifficultyData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("techAssumptions")
    private String techAssumptions = "intermediate";

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTechAssumptions() {
        return techAssumptions;
    }

    public void setTechAssumptions(String techAssumptions) {
        this.techAssumptions = techAssumptions;
    }

    @Override
    public String toString() {
        return String.format("DifficultyData{id='%s', name='%s', techAssumptions='%s'}",
            id, name, techAssumptions);
    }
}
