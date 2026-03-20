package com.maprando.util;

import com.maprando.data.DataLoader;
import com.maprando.model.ItemRegistry;
import com.maprando.model.ItemDefinition;
import java.io.IOException;
import java.util.List;

/**
 * Test utility for setting up test infrastructure.
 */
public class TestSetup {

    private static boolean dataLoaded = false;

    /**
     * Initialize the ItemRegistry singleton with test data.
     * This loads the actual game data from JSON files.
     */
    public static void initializeItemRegistry() {
        if (!dataLoaded) {
            try {
                DataLoader dataLoader = new DataLoader();
                dataLoader.loadAllData();
                dataLoaded = true;
            } catch (IOException e) {
                throw new RuntimeException("Failed to load test data", e);
            }
        }
    }

    /**
     * Create a minimal ItemRegistry with just the essential items for testing.
     * This is faster than loading full data and doesn't require JSON files.
     * Items match the Rust MapRandomizer (22 items, indices 0-21).
     */
    public static ItemRegistry createMinimalRegistry() {
        ItemRegistry registry = new ItemRegistry();

        // Register essential items with indices matching Rust project
        int index = 0;
        // Tank items with resourceType and capacityIncrease
        registry.registerItem(new ItemDefinition("ENERGY_TANK", "Energy Tank", "Increases max energy",
                "tank", false, index++, null, null, null, null, null, "ENERGY", 100));
        registry.registerItem(new ItemDefinition("MISSILE", "Missile", "Increases missiles",
                "tank", false, index++, null, null, null, null, null, "MISSILE", 5));
        registry.registerItem(new ItemDefinition("SUPER_MISSILE", "Super Missile", "Increases super missiles",
                "tank", false, index++, null, null, null, null, null, "SUPER_MISSILE", 5));
        registry.registerItem(new ItemDefinition("POWER_BOMB", "Power Bomb", "Power bomb",
                "tank", true, index++, null, null, null, null, null, "POWER_BOMB", 5));
        registry.registerItem(new ItemDefinition("BOMBS", "Bombs", "Morph ball bombs",
                "morph", true, index++));
        registry.registerItem(new ItemDefinition("CHARGE_BEAM", "Charge Beam", "Charge your shots",
                "beam", true, index++));
        registry.registerItem(new ItemDefinition("ICE_BEAM", "Ice Beam", "Freeze enemies",
                "beam", true, index++));
        registry.registerItem(new ItemDefinition("HI_JUMP", "Hi-Jump Boots", "Jump higher",
                "movement", true, index++));
        registry.registerItem(new ItemDefinition("SPEED_BOOSTER", "Speed Booster", "Run fast",
                "movement", true, index++));
        registry.registerItem(new ItemDefinition("WAVE_BEAM", "Wave Beam", "Pierces walls",
                "beam", true, index++));
        registry.registerItem(new ItemDefinition("SPAZER_BEAM", "Spazer Beam", "Wide beam",
                "beam", true, index++));
        registry.registerItem(new ItemDefinition("SPRING_BALL", "Spring Ball", "Jump in morph ball",
                "morph", false, index++));
        registry.registerItem(new ItemDefinition("VARIA_SUIT", "Varia Suit", "Heat protection",
                "suit", true, index++));
        registry.registerItem(new ItemDefinition("GRAVITY_SUIT", "Gravity Suit", "Lava protection",
                "suit", true, index++));
        registry.registerItem(new ItemDefinition("XRAY_SCOPE", "X-Ray Scope", "See through blocks",
                "misc", true, index++));
        registry.registerItem(new ItemDefinition("PLASMA_BEAM", "Plasma Beam", "Powerful beam",
                "beam", true, index++));
        registry.registerItem(new ItemDefinition("GRAPPLE_BEAM", "Grapple Beam", "Swing on grappling beam",
                "beam", true, index++));
        registry.registerItem(new ItemDefinition("SPACE_JUMP", "Space Jump", "Jump in mid-air",
                "movement", true, index++));
        registry.registerItem(new ItemDefinition("SCREW_ATTACK", "Screw Attack", "Spin jump attack",
                "movement", true, index++));
        registry.registerItem(new ItemDefinition("MORPH_BALL", "Morph Ball", "Roll into a ball",
                "movement", true, index++));
        registry.registerItem(new ItemDefinition("RESERVE_TANK", "Reserve Tank", "Backup energy tank",
                "tank", false, index++));
        registry.registerItem(new ItemDefinition("WALL_JUMP_BOOTS", "Wall Jump Boots", "Jump off walls",
                "movement", true, index++));

        return registry;
    }

    /**
     * Initialize the ItemRegistry singleton with minimal test data.
     */
    public static void initializeMinimalRegistry() {
        if (!dataLoaded) {
            ItemRegistry registry = createMinimalRegistry();
            ItemRegistry.setInstance(registry);
            dataLoaded = true;
        }
    }
}
