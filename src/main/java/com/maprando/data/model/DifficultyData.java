package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/**
 * Difficulty preset data loaded from difficulties.json.
 * Contains item pool settings, game balance settings, and starting items.
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

    @JsonProperty("settings")
    private GameSettings settings;

    @JsonProperty("startingItems")
    private List<String> startingItems;

    @JsonProperty("restrictMovement")
    private Boolean restrictMovement;

    @JsonProperty("reduceAmmoDrops")
    private Boolean reduceAmmoDrops;

    // Nested classes for JSON structure

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

    public static class GameSettings {
        @JsonProperty("enemyDamage")
        private double enemyDamage = 1.0;

        @JsonProperty("enemyHealth")
        private double enemyHealth = 1.0;

        @JsonProperty("resourceMultiplier")
        private double resourceMultiplier = 1.0;

        @JsonProperty("techAssumptions")
        private String techAssumptions = "intermediate";

        @JsonProperty("startLocation")
        private String startLocation = "landing_site";

        public double getEnemyDamage() {
            return enemyDamage;
        }

        public void setEnemyDamage(double enemyDamage) {
            this.enemyDamage = enemyDamage;
        }

        public double getEnemyHealth() {
            return enemyHealth;
        }

        public void setEnemyHealth(double enemyHealth) {
            this.enemyHealth = enemyHealth;
        }

        public double getResourceMultiplier() {
            return resourceMultiplier;
        }

        public void setResourceMultiplier(double resourceMultiplier) {
            this.resourceMultiplier = resourceMultiplier;
        }

        public String getTechAssumptions() {
            return techAssumptions;
        }

        public void setTechAssumptions(String techAssumptions) {
            this.techAssumptions = techAssumptions;
        }

        public String getStartLocation() {
            return startLocation;
        }

        public void setStartLocation(String startLocation) {
            this.startLocation = startLocation;
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

    public GameSettings getSettings() {
        return settings;
    }

    public void setSettings(GameSettings settings) {
        this.settings = settings;
    }

    public List<String> getStartingItems() {
        return startingItems;
    }

    public void setStartingItems(List<String> startingItems) {
        this.startingItems = startingItems;
    }

    public Boolean getRestrictMovement() {
        return restrictMovement;
    }

    public void setRestrictMovement(Boolean restrictMovement) {
        this.restrictMovement = restrictMovement;
    }

    public Boolean getReduceAmmoDrops() {
        return reduceAmmoDrops;
    }

    public void setReduceAmmoDrops(Boolean reduceAmmoDrops) {
        this.reduceAmmoDrops = reduceAmmoDrops;
    }

    @Override
    public String toString() {
        return String.format("DifficultyData{id='%s', name='%s', progressionRate=%.2f, fillerRate=%.2f, startingItems=%d}",
            id, name, itemPool.getProgressionRate(), itemPool.getFillerItemRate(),
            startingItems != null ? startingItems.size() : 0);
    }
}
