package com.maprando.model;

import java.util.*;

/**
 * Difficulty configuration for randomizer settings.
 * Used in escape timer calculations and difficulty assessments.
 */
public class DifficultyConfig {
    public float escapeTimerMultiplier = 1.0f;
    public float shineChargeTiles = 19.0f; // Default: requires at least 19 tiles for shortcharge
    public Set<String> tech = new HashSet<>();

    public DifficultyConfig() {
        // Default tech set - empty means no advanced tech
    }

    /**
     * Check if player has a specific tech ability.
     *
     * @param techId Tech identifier (e.g., "can_mid_air_morph", "can_suitless_lava_dive")
     * @return true if tech is available
     */
    public boolean hasTech(String techId) {
        return tech.contains(techId);
    }

    /**
     * Add a tech ability to the config.
     *
     * @param techId Tech identifier
     */
    public void addTech(String techId) {
        tech.add(techId);
    }

    /**
     * Remove a tech ability from the config.
     *
     * @param techId Tech identifier
     */
    public void removeTech(String techId) {
        tech.remove(techId);
    }

    /**
     * Create a difficulty config from preset.
     *
     * @param preset Difficulty preset name
     * @return Configured DifficultyConfig
     */
    public static DifficultyConfig fromPreset(String preset) {
        DifficultyConfig config = new DifficultyConfig();

        switch (preset.toLowerCase()) {
            case "casual", "beginner":
                config.escapeTimerMultiplier = 1.5f;
                config.shineChargeTiles = 25.0f; // Easier shortcharge
                // No advanced tech
                break;

            case "normal", "intermediate":
                config.escapeTimerMultiplier = 1.0f;
                config.shineChargeTiles = 19.0f;
                config.addTech("can_mid_air_morph");
                break;

            case "hard", "advanced":
                config.escapeTimerMultiplier = 0.9f;
                config.shineChargeTiles = 14.0f; // Harder shortcharge
                config.addTech("can_mid_air_morph");
                config.addTech("can_walljump");
                config.addTech("can_shinespark");
                break;

            case "expert":
                config.escapeTimerMultiplier = 0.8f;
                config.shineChargeTiles = 12.0f;
                config.addTech("can_mid_air_morph");
                config.addTech("can_walljump");
                config.addTech("can_shinespark");
                config.addTech("can_horizontal_shinespark");
                config.addTech("can_suitless_lava_dive");
                break;

            case "nightmare":
                config.escapeTimerMultiplier = 0.7f;
                config.shineChargeTiles = 11.0f; // Very hard shortcharge
                config.addTech("can_mid_air_morph");
                config.addTech("can_walljump");
                config.addTech("can_shinespark");
                config.addTech("can_horizontal_shinespark");
                config.addTech("can_suitless_lava_dive");
                config.addTech("can_kago");
                config.addTech("can_moonfall");
                config.addTech("can_off_screen_super_shot");
                config.addTech("can_hyper_gate_shot");
                config.addTech("can_hero_shot");
                break;

            default:
                // Default to normal
                config.escapeTimerMultiplier = 1.0f;
                config.shineChargeTiles = 19.0f;
        }

        return config;
    }

    @Override
    public String toString() {
        return String.format(
            "DifficultyConfig{escapeTimerMultiplier=%.2f, shineChargeTiles=%.1f, techCount=%d}",
            escapeTimerMultiplier,
            shineChargeTiles,
            tech.size()
        );
    }
}
