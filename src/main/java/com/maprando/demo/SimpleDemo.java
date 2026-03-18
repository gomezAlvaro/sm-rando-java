package com.maprando.demo;

import com.maprando.model.GameState;
import com.maprando.model.ResourceType;
import com.maprando.logic.*;
import com.maprando.randomize.*;

/**
 * Demonstration program showing the Map Randomizer proof-of-concept.
 * This program:
 * 1. Creates a game state with starting items
 * 2. Defines a set of item locations
 * 3. Runs basic randomization
 * 4. Prints spoiler log
 * 5. Demonstrates progression verification
 */
public class SimpleDemo {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║     Super Metroid Map Randomizer - Java Proof of Concept      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
        System.out.println();

        // Step 1: Create initial game state
        System.out.println("=== Step 1: Creating Game State ===");
        GameState gameState = GameState.standardStart();
        gameState.setCurrentNode("Landing Site");
        System.out.println(gameState);
        System.out.println();

        // Step 2: Demonstrate item collection
        System.out.println("=== Step 2: Demonstrating Item Collection ===");
        demonstrateItemCollection(gameState);
        System.out.println();

        // Step 3: Demonstrate resource management
        System.out.println("=== Step 3: Demonstrating Resource Management ===");
        demonstrateResourceManagement(gameState);
        System.out.println();

        // Step 4: Create item pool
        System.out.println("=== Step 4: Creating Item Pool ===");
        ItemPool itemPool = ItemPool.createMinimalPool();
        System.out.println(itemPool);
        System.out.println();

        // Step 5: Create locations
        System.out.println("=== Step 5: Creating Locations ===");
        java.util.List<Location> locations = createDemoLocations();
        System.out.println("Created " + locations.size() + " locations");
        for (Location loc : locations) {
            System.out.println("  " + loc);
        }
        System.out.println();

        // Step 6: Run randomization
        System.out.println("=== Step 6: Running Randomization ===");
        BasicRandomizer randomizer = new BasicRandomizer("demo-seed-123");
        for (Location loc : locations) {
            randomizer.addLocation(loc);
        }
        randomizer.setItemPool(itemPool);

        RandomizationResult result = randomizer.randomize();
        System.out.println("Randomization completed: " + (result.isSuccessful() ? "SUCCESS" : "FAILED"));
        System.out.println("Placed " + result.getPlacementCount() + " items");
        System.out.println();

        // Step 7: Print spoiler log
        System.out.println("=== Step 7: Spoiler Log ===");
        System.out.println();
        System.out.println(result.generateSpoilerLog());
        System.out.println();

        // Step 8: Verify completion
        System.out.println("=== Step 8: Verification ===");
        boolean completable = randomizer.validateBeatable(result);
        System.out.println("Seed appears completable: " + (completable ? "YES" : "NO"));
        System.out.println();

        System.out.println("════════════════════════════════════════════════════════════════");
        System.out.println("Demo completed successfully!");
        System.out.println("════════════════════════════════════════════════════════════════");
    }

    /**
     * Demonstrates collecting items and updating inventory.
     */
    private static void demonstrateItemCollection(GameState state) {
        System.out.println("Starting items: " + state.getInventory().getItemCount());

        // Collect some items
        ItemCollector.collectItem(state, "BOMB");
        System.out.println("Collected: BOMB");
        System.out.println("Can place bombs: " + RequirementChecker.canPlaceBombs(state));

        ItemCollector.collectItem(state, "VARIA_SUIT");
        System.out.println("Collected: VARIA_SUIT");
        System.out.println("Has damage reduction: " + RequirementChecker.hasDamageReduction(state));

        ItemCollector.collectItem(state, "ICE_BEAM");
        System.out.println("Collected: ICE_BEAM");
        System.out.println("Has ice beam: " + RequirementChecker.hasIceBeam(state));

        System.out.println("Total items: " + state.getInventory().getItemCount());
    }

    /**
     * Demonstrates resource management and damage calculation.
     */
    private static void demonstrateResourceManagement(GameState state) {
        // Show initial resources
        System.out.println("Initial Energy: " + state.getEnergy());

        // Take some damage
        int baseDamage = 30;
        int actualDamage = DamageCalculator.calculateDamageTaken(state, baseDamage);
        System.out.println("Taking " + baseDamage + " damage (reduced to " + actualDamage + ")");
        state.takeDamage(actualDamage);
        System.out.println("Energy after damage: " + state.getEnergy());

        // Use missiles
        System.out.println("Can shoot missiles: " + RequirementChecker.canShootMissiles(state));
        ResourceManager.consumeResource(state, ResourceType.MISSILE, 5);
        System.out.println("Missiles remaining: " +
                ResourceManager.getAvailableAmount(state, ResourceType.MISSILE));

        // Show shot damage
        int shotDamage = DamageCalculator.calculateShotDamage(state);
        System.out.println("Shot damage: " + shotDamage);
    }

    /**
     * Creates a set of demo locations for randomization.
     */
    private static java.util.List<Location> createDemoLocations() {
        java.util.List<Location> locations = new java.util.ArrayList<>();

        // Early game (no requirements)
        locations.add(Location.builder()
                .id("brin_morph")
                .name("Morph Ball Room (Brinstar)")
                .region("Brinstar")
                .build());

        locations.add(Location.builder()
                .id("brin_charge")
                .name("Charge Beam Room (Brinstar)")
                .region("Brinstar")
                .build());

        // Mid game (requires morph)
        locations.add(Location.builder()
                .id("brin_bombs")
                .name("Bomb Room (Brinstar)")
                .region("Brinstar")
                .requirements(java.util.Set.of("can_morph"))
                .build());

        locations.add(Location.builder()
                .id("norf_ice")
                .name("Ice Beam Room (Norfair)")
                .region("Norfair")
                .requirements(java.util.Set.of("can_morph", "has_bombs"))
                .build());

        // Late game (requires more)
        locations.add(Location.builder()
                .id("norf_speed")
                .name("Speed Booster Room (Norfair)")
                .region("Norfair")
                .requirements(java.util.Set.of("can_survive_heat"))
                .build());

        locations.add(Location.builder()
                .id("mari_gravity")
                .name("Gravity Suit Room (Maridia)")
                .region("Maridia")
                .requirements(java.util.Set.of("can_morph", "has_grapple"))
                .build());

        locations.add(Location.builder()
                .id("wreck_main")
                .name("Wrecked Ship Main Hall (Wrecked Ship)")
                .region("Wrecked Ship")
                .build());

        // Filler locations
        locations.add(Location.builder()
                .id("misc_01")
                .name("Secret Room 1 (Misc)")
                .region("Misc")
                .build());

        locations.add(Location.builder()
                .id("misc_02")
                .name("Secret Room 2 (Misc)")
                .region("Misc")
                .build());

        locations.add(Location.builder()
                .id("misc_03")
                .name("Secret Room 3 (Misc)")
                .region("Misc")
                .build());

        return locations;
    }
}
