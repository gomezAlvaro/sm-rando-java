package com.maprando.demo;

import com.maprando.data.DataLoader;
import com.maprando.data.model.ItemData;
import com.maprando.data.model.LocationData;
import com.maprando.model.GameState;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Demonstrates the JSON data loading system.
 * Shows how to load items and locations from external JSON files.
 */
public class JsonDataDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║     Super Metroid Map Randomizer - JSON Data Demo               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");

        try {
            // Step 1: Load JSON data
            System.out.println("\n=== Step 1: Loading JSON Data ===");
            DataLoader dataLoader = new DataLoader();
            dataLoader.loadAllData();

            // Validate the loaded data
            if (!dataLoader.validateData()) {
                System.err.println("Data validation failed!");
                return;
            }

            // Step 2: Display loaded items
            System.out.println("\n=== Step 2: Loaded Items ===");
            displayItems(dataLoader.getItemData());

            // Step 3: Display loaded locations
            System.out.println("\n=== Step 3: Loaded Locations ===");
            displayLocations(dataLoader.getLocationData());

            // Step 4: Create game state using JSON data
            System.out.println("\n=== Step 4: Creating Game State from JSON ===");
            GameState state = createGameStateFromJson(dataLoader);
            System.out.println(state);

            // Step 5: Create item pool from JSON
            System.out.println("\n=== Step 5: Creating Item Pool from JSON ===");
            ItemPool pool = createItemPoolFromJson(dataLoader);
            System.out.println(pool);

            // Step 6: Create locations from JSON
            System.out.println("\n=== Step 6: Creating Locations from JSON ===");
            List<Location> locations = createLocationsFromJson(dataLoader);
            System.out.println("Created " + locations.size() + " locations from JSON data");

            // Step 7: Demonstrate progression items
            System.out.println("\n=== Step 7: Progression Items ===");
            displayProgressionItems(dataLoader.getItemData());

            // Step 8: Demonstrate item requirements
            System.out.println("\n=== Step 8: Item Requirements ===");
            displayItemRequirements(dataLoader.getItemData());

            // Step 9: Demonstrate location requirements
            System.out.println("\n=== Step 9: Location Requirements ===");
            displayLocationRequirements(dataLoader.getLocationData());

            System.out.println("\n✅ JSON Data Loading System Working Successfully!");

        } catch (Exception e) {
            System.err.println("Error loading JSON data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Display all loaded items with their properties.
     */
    private static void displayItems(ItemData itemData) {
        System.out.println("Total items: " + itemData.getItems().size());

        // Count by category
        long beamCount = itemData.getItems().stream()
                .filter(item -> "beam".equals(item.getCategory()))
                .count();
        long tankCount = itemData.getItems().stream()
                .filter(item -> "tank".equals(item.getCategory()))
                .count();
        long progressionCount = itemData.getItems().stream()
                .filter(ItemData.ItemDefinition::isProgression)
                .count();

        System.out.println("  Beams: " + beamCount);
        System.out.println("  Tanks: " + tankCount);
        System.out.println("  Progression items: " + progressionCount);

        // Show some examples
        System.out.println("\nExample items:");
        itemData.getItems().stream()
                .filter(item -> item.isProgression())
                .limit(5)
                .forEach(item -> System.out.println("  - " + item.getDisplayName() +
                        " (" + item.getCategory() + "): " + item.getDescription()));
    }

    /**
     * Display all loaded locations.
     */
    private static void displayLocations(LocationData locationData) {
        System.out.println("Total locations: " + locationData.getLocations().size());

        // Count by region
        long brinstarCount = locationData.getLocations().stream()
                .filter(loc -> "Brinstar".equals(loc.getRegion()))
                .count();
        long norfairCount = locationData.getLocations().stream()
                .filter(loc -> "Norfair".equals(loc.getRegion()))
                .count();
        long earlyGameCount = locationData.getLocations().stream()
                .filter(LocationData.LocationDefinition::isEarlyGame)
                .count();

        System.out.println("  Brinstar: " + brinstarCount);
        System.out.println("  Norfair: " + norfairCount);
        System.out.println("  Early game accessible: " + earlyGameCount);

        // Show early game locations
        System.out.println("\nEarly game locations:");
        locationData.getLocations().stream()
                .filter(LocationData.LocationDefinition::isEarlyGame)
                .limit(5)
                .forEach(loc -> System.out.println("  - " + loc.getName() +
                        " (" + loc.getRegion() + "): " +
                        (loc.getRequirements().isEmpty() ? "No requirements" : loc.getRequirements().size() + " requirements")));
    }

    /**
     * Create a game state with items from JSON data.
     */
    private static GameState createGameStateFromJson(DataLoader dataLoader) {
        // Start with basic items
        GameState state = new GameState();

        // Add some progression items from JSON
        dataLoader.getItemData().getItems().stream()
                .filter(ItemData.ItemDefinition::isProgression)
                .filter(item -> item.getCategory().equals("beam") ||
                              item.getCategory().equals("movement"))
                .limit(2)
                .forEach(itemDef -> {
                    String itemId = itemDef.getId();
                    state.collectItem(itemId);
                });

        return state;
    }

    /**
     * Create an item pool from JSON data.
     */
    private static ItemPool createItemPoolFromJson(DataLoader dataLoader) {
        ItemPool pool = new ItemPool();

        dataLoader.getItemData().getItems().forEach(itemDef -> {
            String itemId = itemDef.getId();
            // Add item to pool with progression flag from JSON
            pool.addItem(itemId, 1, itemDef.isProgression());
        });

        return pool;
    }

    /**
     * Create locations from JSON data.
     */
    private static List<Location> createLocationsFromJson(DataLoader dataLoader) {
        List<Location> locations = new ArrayList<>();

        dataLoader.getLocationData().getLocations().forEach(locDef -> {
            Location location = Location.builder()
                    .id(locDef.getId())
                    .name(locDef.getName())
                    .region(locDef.getRegion())
                    .requirements(locDef.getRequirements() != null ?
                            Set.copyOf(locDef.getRequirements()) : Set.of())
                    .build();
            locations.add(location);
        });

        return locations;
    }

    /**
     * Display progression items from JSON.
     */
    private static void displayProgressionItems(ItemData itemData) {
        System.out.println("Progression items from JSON:");
        itemData.getItems().stream()
                .filter(ItemData.ItemDefinition::isProgression)
                .forEach(item -> System.out.println("  ✓ " + item.getDisplayName() +
                        " - " + item.getDescription()));
    }

    /**
     * Display item requirements from JSON.
     */
    private static void displayItemRequirements(ItemData itemData) {
        System.out.println("Items with requirements:");
        itemData.getItems().stream()
                .filter(item -> item.getRequires() != null && !item.getRequires().isEmpty())
                .forEach(item -> {
                    System.out.println("  " + item.getDisplayName() + " requires:");
                    item.getRequires().forEach(req ->
                        System.out.println("    - " + req));
                });
    }

    /**
     * Display location requirements from JSON.
     */
    private static void displayLocationRequirements(LocationData locationData) {
        System.out.println("Locations with requirements:");
        locationData.getLocations().stream()
                .filter(loc -> loc.getRequirements() != null && !loc.getRequirements().isEmpty())
                .limit(8)
                .forEach(loc -> {
                    System.out.println("  " + loc.getName() + " (" + loc.getRegion() + "):");
                    loc.getRequirements().forEach(req ->
                        System.out.println("    - " + req));
                });
    }
}