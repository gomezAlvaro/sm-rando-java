package com.maprando.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for seed generation requests.
 * Contains parameters for customizing the randomization process.
 *
 * @param seed              Optional seed string. If null or blank, a random seed is generated.
 * @param algorithm         Randomization algorithm to use ("foresight" or "basic")
 * @param difficulty        Difficulty preset ("casual", "normal", "hard", "expert", "nightmare")
 * @param enableSpoiler     Whether to generate a spoiler log
 * @param qualityValidation Whether to validate seed quality during generation
 */
public record SeedRequest(
        String seed,
        @NotBlank(message = "Algorithm is required")
        @Pattern(regexp = "foresight|basic", message = "Algorithm must be 'foresight' or 'basic'")
        String algorithm,
        @Pattern(regexp = "casual|normal|hard|expert|nightmare", message = "Difficulty must be one of: casual, normal, hard, expert, nightmare")
        String difficulty,
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
     * Gets the difficulty with a default value of "normal" if null.
     *
     * @return the difficulty preset
     */
    public String getEffectiveDifficulty() {
        return difficulty == null ? "normal" : difficulty;
    }
}