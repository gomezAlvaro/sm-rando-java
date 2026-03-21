package com.maprando.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for seed generation requests.
 * Contains parameters for customizing the randomization process.
 *
 * @param seed              Optional seed string. If null or blank, a random seed is generated.
 * @param algorithm         Randomization algorithm to use ("foresight" or "basic")
 * @param skillPreset       Skill preset (Basic, Medium, Hard, Very Hard, Expert, Expert+, Extreme, Extreme+, Insane, Insane+, Beyond)
 * @param enableSpoiler     Whether to generate a spoiler log
 * @param qualityValidation Whether to validate seed quality during generation
 */
public record SeedRequest(
        String seed,
        @NotBlank(message = "Algorithm is required")
        @Pattern(regexp = "foresight|basic", message = "Algorithm must be 'foresight' or 'basic'")
        String algorithm,
        @Pattern(regexp = "Basic|Medium|Hard|Very Hard|Expert|Expert\\+|Extreme|Extreme\\+|Insane|Insane\\+|Beyond",
                 message = "Skill preset must be one of: Basic, Medium, Hard, Very Hard, Expert, Expert+, Extreme, Extreme+, Insane, Insane+, Beyond")
        String skillPreset,
        Boolean enableSpoiler,
        Boolean qualityValidation
) {

    /**
     * Gets the seed string, providing a default random seed if null or blank.
     *
     * @return the seed string, or a random UUID if not provided
     */
    public String getEffectiveSeed() {
        return seed == null || seed.isBlank() ?
               java.util.UUID.randomUUID().toString().substring(0, 8) :
               seed;
    }

    /**
     * Checks if spoiler should be enabled, with a default value of true if null.
     *
     * @return true if spoiler should be generated, false otherwise
     */
    public boolean isSpoilerEnabled() {
        return enableSpoiler != null && enableSpoiler;
    }

    /**
     * Checks if quality validation should be performed, with a default value of true if null.
     *
     * @return true if quality validation should be performed, false otherwise
     */
    public boolean isQualityValidationEnabled() {
        return qualityValidation == null || qualityValidation;
    }

    /**
     * Gets the skill preset with a default value of "Hard" if null.
     *
     * @return the skill preset name
     */
    public String getEffectiveSkillPreset() {
        return skillPreset == null ? "Hard" : skillPreset;
    }
}