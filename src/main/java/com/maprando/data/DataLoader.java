package com.maprando.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.maprando.data.model.ItemData;
import com.maprando.data.model.LocationData;
import com.maprando.data.model.TechData;
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
        System.out.println("Successfully loaded " + itemData.getItems().size() + " items");
        System.out.println("Successfully loaded " + locationData.getLocations().size() + " locations");
        System.out.println("Successfully loaded " + techData.getTechs().size() + " techs");
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
     * Load tech definitions from JSON.
     */
    private void loadTechData() throws IOException {
        try (InputStream is = getClass().getResourceAsStream(DATA_PATH + "tech.json")) {
            if (is == null) {
                throw new IOException("Could not find tech.json in resources");
            }
            techData = objectMapper.readValue(is, TechData.class);

            // Populate TechRegistry
            for (TechData.TechDefinition techDef : techData.getTechs()) {
                TechDefinition modelDef = new TechDefinition(
                    techDef.getId(),
                    techDef.getName(),
                    techDef.getDescription(),
                    techDef.getIndex(),
                    techDef.getRequires()
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
