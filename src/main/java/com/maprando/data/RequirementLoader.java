package com.maprando.data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.maprando.logic.Requirement;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads and parses requirement definitions from requirements.json.
 * Converts requirement strings into Requirement objects.
 */
public class RequirementLoader {

    private final ObjectMapper objectMapper;
    private final Map<String, Requirement> requirementCache;

    public RequirementLoader() {
        this.objectMapper = new ObjectMapper();
        this.requirementCache = new HashMap<>();
    }

    /**
     * Load requirements from requirements.json file.
     */
    public void loadRequirements() throws IOException {
        try (InputStream is = getClass().getResourceAsStream("/data/requirements.json")) {
            if (is == null) {
                throw new IOException("Could not find requirements.json in resources");
            }

            JsonNode root = objectMapper.readTree(is);
            JsonNode requirementsNode = root.get("requirements");

            if (requirementsNode != null && requirementsNode.isObject()) {
                requirementsNode.fields().forEachRemaining(entry -> {
                    String key = entry.getKey();
                    JsonNode reqNode = entry.getValue();
                    Requirement req = parseRequirement(key, reqNode);
                    requirementCache.put(key, req);
                });
            }
        }
    }

    /**
     * Parse a requirement from JSON node.
     */
    private Requirement parseRequirement(String key, JsonNode reqNode) {
        String type = reqNode.has("type") ? reqNode.get("type").asText() : "free";

        return switch (type) {
            case "free" -> Requirement.free();
            case "item" -> {
                String itemId = reqNode.get("itemId").asText();
                yield Requirement.item(itemId);
            }
            case "items" -> {
                List<Requirement> itemReqs = new ArrayList<>();
                JsonNode itemsNode = reqNode.get("items");
                if (itemsNode != null && itemsNode.isArray()) {
                    for (JsonNode itemNode : itemsNode) {
                        String itemId = itemNode.asText();
                        itemReqs.add(Requirement.item(itemId));
                    }
                }
                yield Requirement.and(itemReqs);
            }
            case "logic" -> {
                // Logic requirements are complex - for now return free
                // TODO: Implement full logic parsing
                yield Requirement.free();
            }
            default -> Requirement.free();
        };
    }

    /**
     * Get a requirement by key (e.g., "can_morph").
     */
    public Requirement getRequirement(String key) {
        return requirementCache.getOrDefault(key, Requirement.free());
    }

    /**
     * Parse requirement strings into Requirement objects.
     * Handles both direct keys and complex expressions.
     */
    public Requirement parseRequirementString(String reqString) {
        if (reqString == null || reqString.isEmpty() || reqString.equals("free")) {
            return Requirement.free();
        }

        // Check if it's a cached requirement
        if (requirementCache.containsKey(reqString)) {
            return requirementCache.get(reqString);
        }

        // Parse item requirement directly
        if (reqString.startsWith("has_") || reqString.startsWith("can_")) {
            // Try to map to items
            return mapStringToRequirement(reqString);
        }

        return Requirement.free();
    }

    /**
     * Map requirement strings to actual Requirement objects.
     */
    private Requirement mapStringToRequirement(String reqString) {
        return switch (reqString) {
            case "can_morph" -> Requirement.item("MORPH_BALL");
            case "has_bombs" -> Requirement.item("BOMB");
            case "has_grapple" -> Requirement.item("GRAPPLING_BEAM");
            case "has_ice_beam" -> Requirement.item("ICE_BEAM");
            case "can_survive_heat" -> Requirement.or(
                Requirement.item("VARIA_SUIT"),
                Requirement.item("GRAVITY_SUIT")
            );
            case "can_survive_lava" -> Requirement.item("GRAVITY_SUIT");
            case "can_swim_water" -> Requirement.item("GRAVITY_SUIT");
            case "can_speedbooster" -> Requirement.item("SPEED_BOOSTER");
            case "can_hijump" -> Requirement.item("HI_JUMP_BOOTS");
            case "can_shinespark" -> Requirement.item("SPEED_BOOSTER");
            case "can_spacejump" -> Requirement.item("SPACE_JUMP");
            case "can_screwattack" -> Requirement.item("SCREW_ATTACK");
            case "can_place_bombs" -> Requirement.and(
                Requirement.item("MORPH_BALL"),
                Requirement.item("BOMB")
            );
            case "can_use_power_bombs" -> Requirement.and(
                Requirement.item("MORPH_BALL"),
                Requirement.missiles(1)
            );
            default -> Requirement.free();
        };
    }

    /**
     * Parse a list of requirement strings into a combined Requirement.
     */
    public Requirement parseRequirementList(List<String> reqStrings) {
        if (reqStrings == null || reqStrings.isEmpty()) {
            return Requirement.free();
        }

        List<Requirement> requirements = new ArrayList<>();
        for (String reqString : reqStrings) {
            requirements.add(parseRequirementString(reqString));
        }

        // All requirements must be satisfied (AND)
        return Requirement.and(requirements);
    }
}
