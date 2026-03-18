package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * JSON data model for items loaded from external files.
 */
public class ItemData {
    @JsonProperty("items")
    private List<ItemDefinition> items;

    public List<ItemDefinition> getItems() {
        return items;
    }

    public void setItems(List<ItemDefinition> items) {
        this.items = items;
    }

    /**
     * Represents a single item definition from JSON.
     */
    public static class ItemDefinition {
        @JsonProperty("id")
        private String id;

        @JsonProperty("displayName")
        private String displayName;

        @JsonProperty("description")
        private String description;

        @JsonProperty("category")
        private String category;

        @JsonProperty("isProgression")
        private boolean isProgression;

        @JsonProperty("index")
        private int index;

        @JsonProperty("damageMultiplier")
        private Double damageMultiplier;

        @JsonProperty("damageBonus")
        private Integer damageBonus;

        @JsonProperty("requires")
        private List<String> requires;

        @JsonProperty("enables")
        private List<String> enables;

        @JsonProperty("damageReduction")
        private Double damageReduction;

        @JsonProperty("resourceType")
        private String resourceType;

        @JsonProperty("capacityIncrease")
        private Integer capacityIncrease;

        // Getters
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public boolean isProgression() { return isProgression; }
        public int getIndex() { return index; }
        public Double getDamageMultiplier() { return damageMultiplier; }
        public Integer getDamageBonus() { return damageBonus; }
        public List<String> getRequires() { return requires; }
        public List<String> getEnables() { return enables; }
        public Double getDamageReduction() { return damageReduction; }
        public String getResourceType() { return resourceType; }
        public Integer getCapacityIncrease() { return capacityIncrease; }

        // Setters
        public void setId(String id) { this.id = id; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public void setDescription(String description) { this.description = description; }
        public void setCategory(String category) { this.category = category; }
        public void setProgression(boolean progression) { isProgression = progression; }
        public void setIndex(int index) { this.index = index; }
        public void setDamageMultiplier(Double damageMultiplier) { this.damageMultiplier = damageMultiplier; }
        public void setDamageBonus(Integer damageBonus) { this.damageBonus = damageBonus; }
        public void setRequires(List<String> requires) { this.requires = requires; }
        public void setEnables(List<String> enables) { this.enables = enables; }
        public void setDamageReduction(Double damageReduction) { this.damageReduction = damageReduction; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public void setCapacityIncrease(Integer capacityIncrease) { this.capacityIncrease = capacityIncrease; }
    }
}
