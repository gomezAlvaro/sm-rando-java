package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Difficulty preset data loaded from difficulties.json.
 * Contains tech assumptions and starting items (aligned with Rust MapRandomizer).
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

    @JsonProperty("startingItems")
    private List<String> startingItems;

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

    public List<String> getStartingItems() {
        return startingItems;
    }

    public void setStartingItems(List<String> startingItems) {
        this.startingItems = startingItems;
    }

    @Override
    public String toString() {
        return String.format("DifficultyData{id='%s', name='%s', techAssumptions='%s', startingItems=%d}",
            id, name, techAssumptions, startingItems != null ? startingItems.size() : 0);
    }
}
