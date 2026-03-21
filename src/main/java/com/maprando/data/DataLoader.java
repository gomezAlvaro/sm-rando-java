package com.maprando.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.maprando.data.model.ItemData;
import com.maprando.data.model.LocationData;
import com.maprando.data.model.TechData;
import com.maprando.data.model.SkillAssumptionSettings;
import com.maprando.model.ItemDefinition;
import com.maprando.model.ItemRegistry;
import com.maprando.model.TechDefinition;
import com.maprando.model.TechRegistry;
import com.maprando.model.ResourceType;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads game data from external JSON files.
 * This allows for flexible configuration without code changes.
 */
public class DataLoader {
    private static final String DATA_PATH = "/data/";
    private final ObjectMapper objectMapper;
    private final Map<String, ItemData.ItemDefinition> itemDefinitions;
    private final Map<String, LocationData.LocationDefinition> locationDefinitions;
    private final Map<String, SkillAssumptionSettings> skillPresets;
    private final ItemRegistry itemRegistry;
    private final TechRegistry techRegistry;

    private ItemData itemData;
    private LocationData locationData;
    private TechData techData;

    public DataLoader() {
        this.objectMapper = new ObjectMapper();
        // Configure Jackson for our use case
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        objectMapper.findAndRegisterModules();

        this.itemDefinitions = new HashMap<>();
        this.locationDefinitions = new HashMap<>();
        this.skillPresets = new HashMap<>();
        this.itemRegistry = new ItemRegistry();
        this.techRegistry = new TechRegistry();
    }

    /**
     * Load all data files from the resources directory.
     */
    public void loadAllData() throws IOException {
        loadItemData();
        loadLocationData();
        loadTechData();
        loadSkillPresets();
        System.out.println("Successfully loaded " + itemData.getItems().size() + " items");
        System.out.println("Successfully loaded " + locationData.getLocations().size() + " locations");
        System.out.println("Successfully loaded " + techRegistry.getTechCount() + " techs");
        System.out.println("Successfully loaded " + skillPresets.size() + " skill presets");
    }

    /**
     * Load item definitions from JSON.
     */
    private void loadItemData() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DATA_PATH + "items.json")) {
            if (is == null) {
                throw new IOException("Could not find items.json in resources");
            }
            itemData = objectMapper.readValue(is, ItemData.class);

            // Build quick lookup map and populate ItemRegistry
            for (ItemData.ItemDefinition itemDef : itemData.getItems()) {
                itemDefinitions.put(itemDef.getId(), itemDef);

                // Create ItemDefinition with all enhanced fields and register it
                ItemDefinition modelDef = new ItemDefinition(
                    itemDef.getId(),
                    itemDef.getDisplayName(),
                    itemDef.getDescription(),
                    itemDef.getCategory(),
                    itemDef.isProgression(),
                    itemDef.getIndex(),
                    itemDef.getDamageMultiplier(),
                    itemDef.getDamageBonus(),
                    itemDef.getDamageReduction(),
                    itemDef.getRequires(),
                    itemDef.getEnables(),
                    itemDef.getResourceType(),
                    itemDef.getCapacityIncrease()
                );
                itemRegistry.registerItem(modelDef);
            }
        }

        // Set singleton instance for convenience
        ItemRegistry.setInstance(itemRegistry);
    }

    /**
     * Load tech definitions from Rust tech_data.json format.
     * Rust format is a raw JSON array: [{"tech_id": 1, "name": "canHeatRun", ...}, ...]
     */
    private void loadTechData() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DATA_PATH + "tech.json")) {
            if (is == null) {
                throw new IOException("Could not find tech.json in resources");
            }

            // Rust tech.json is a raw array, not an object with a "techs" property
            // Use TypeReference to deserialize directly to List
            java.util.List<TechData.TechDefinition> techDefinitions =
                objectMapper.readValue(is,
                    objectMapper.getTypeFactory().constructCollectionType(
                        java.util.List.class,
                        TechData.TechDefinition.class
                    )
                );

            // Populate TechRegistry with Rust tech format
            for (TechData.TechDefinition techDef : techDefinitions) {
                TechDefinition modelDef = new TechDefinition(
                    techDef.getTechId(),
                    techDef.getName(),
                    techDef.getDifficulty(),
                    techDef.getVideoId()
                );
                techRegistry.registerTech(modelDef);
            }
        }

        // Set singleton instance for convenience
        TechRegistry.setInstance(techRegistry);
    }

    /**
     * Load location definitions from JSON.
     */
    private void loadLocationData() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DATA_PATH + "locations.json")) {
            if (is == null) {
                throw new IOException("Could not find locations.json in resources");
            }
            locationData = objectMapper.readValue(is, LocationData.class);

            // Build quick lookup map
            for (LocationData.LocationDefinition locDef : locationData.getLocations()) {
                locationDefinitions.put(locDef.getId(), locDef);
            }
        }
    }

    /**
     * Load skill presets from JSON files.
     * Loads all skill preset files from /data/skill-presets/*.json
     */
    private void loadSkillPresets() throws IOException {
        String[] presetNames = {
            "Basic", "Medium", "Hard", "Very Hard", "Expert",
            "Expert+", "Extreme", "Extreme+", "Insane", "Insane+", "Beyond"
        };

        for (String presetName : presetNames) {
            String fileName = presetName.replace("+", "%2B"); // URL-encode the plus sign
            try (InputStream is = getClass().getResourceAsStream(DATA_PATH + "skill-presets/" + presetName + ".json")) {
                if (is == null) {
                    throw new IOException("Could not find " + presetName + ".json in resources");
                }
                SkillAssumptionSettings preset = objectMapper.readValue(is, SkillAssumptionSettings.class);
                skillPresets.put(presetName, preset);
            }
        }
    }

    /**
     * Get item definition by ID.
     */
    public ItemData.ItemDefinition getItemDefinition(String id) {
        return itemDefinitions.get(id);
    }

    /**
     * Get location definition by ID.
     */
    public LocationData.LocationDefinition getLocationDefinition(String id) {
        return locationDefinitions.get(id);
    }

    /**
     * Get all item definitions.
     */
    public ItemData getItemData() {
        return itemData;
    }

    /**
     * Get all location definitions.
     */
    public LocationData getLocationData() {
        return locationData;
    }

    /**
     * Get the item registry.
     */
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    /**
     * Get the tech registry.
     */
    public TechRegistry getTechRegistry() {
        return techRegistry;
    }

    /**
     * Get skill preset by name.
     *
     * @param name Skill preset name (e.g., "Basic", "Hard", "Expert", "Beyond")
     * @return SkillAssumptionSettings preset, or null if not found
     */
    public SkillAssumptionSettings getSkillPreset(String name) {
        return skillPresets.get(name);
    }

    /**
     * Get all skill presets.
     */
    public java.util.Collection<SkillAssumptionSettings> getAllSkillPresets() {
        return skillPresets.values();
    }

    /**
     * Convert JSON resource type string to ResourceType enum.
     */
    public ResourceType jsonStringToResourceType(String resourceTypeStr) {
        try {
            return ResourceType.valueOf(resourceTypeStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: No matching ResourceType for string: " + resourceTypeStr);
            return null;
        }
    }

    /**
     * Validate that all JSON references are valid.
     */
    public boolean validateData() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        // Validate item definitions
        for (ItemData.ItemDefinition itemDef : itemData.getItems()) {
            // Check if item ID exists in registry
            if (itemRegistry.getById(itemDef.getId()) == null) {
                errors.append("Invalid item ID: ").append(itemDef.getId()).append("\n");
                isValid = false;
            }

            // Validate requirements reference
            if (itemDef.getRequires() != null) {
                for (String req : itemDef.getRequires()) {
                    // Could add validation logic here
                }
            }
        }

        // Validate location definitions
        for (LocationData.LocationDefinition locDef : locationData.getLocations()) {
            // Could add validation logic here
        }

        if (!isValid) {
            System.err.println("Data validation errors:\n" + errors);
        }

        return isValid;
    }
}
