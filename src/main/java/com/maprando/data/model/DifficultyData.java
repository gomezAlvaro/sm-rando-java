package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Difficulty preset data loaded from difficulties.json.
 * Contains item pool settings, tech assumptions, and starting items.
 */
public class DifficultyData {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("itemPool")
    private ItemPoolSettings itemPool;

    @JsonProperty("techAssumptions")
    private String techAssumptions = "intermediate";

    @JsonProperty("startingItems")
    private List<String> startingItems;

    // Nested class for item pool settings

    public static class ItemPoolSettings {
        @JsonProperty("progressionRate")
        private double progressionRate = 1.0;

        @JsonProperty("fillerItemRate")
        private double fillerItemRate = 1.0;

        public double getProgressionRate() {
            return progressionRate;
        }

        public void setProgressionRate(double progressionRate) {
            this.progressionRate = progressionRate;
        }

        public double getFillerItemRate() {
            return fillerItemRate;
        }

        public void setFillerItemRate(double fillerItemRate) {
            this.fillerItemRate = fillerItemRate;
        }
    }

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

    public ItemPoolSettings getItemPool() {
        return itemPool;
    }

    public void setItemPool(ItemPoolSettings itemPool) {
        this.itemPool = itemPool;
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
        return String.format("DifficultyData{id='%s', name='%s', progressionRate=%.2f, fillerRate=%.2f, techAssumptions='%s', startingItems=%d}",
            id, name, itemPool.getProgressionRate(), itemPool.getFillerItemRate(),
            techAssumptions, startingItems != null ? startingItems.size() : 0);
    }
}
