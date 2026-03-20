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
     * Items match the Rust MapRandomizer naming (no _TANK suffixes).
     */
    public static ItemRegistry createMinimalRegistry() {
        ItemRegistry registry = new ItemRegistry();

        // Register essential items with indices matching Rust project
        int index = 0;
        // Tank items with resourceType and capacityIncrease
        registry.registerItem(new ItemDefinition("ENERGY_TANK", "Energy Tank", "Increases max energy",
                "tank", true, index++, null, null, null, null, null, "ENERGY", 100));
        registry.registerItem(new ItemDefinition("MISSILE", "Missile", "Increases missiles",
                "tank", true, index++, null, null, null, null, null, "MISSILE", 5));
        registry.registerItem(new ItemDefinition("SUPER_MISSILE", "Super Missile", "Increases super missiles",
                "tank", true, index++, null, null, null, null, null, "SUPER_MISSILE", 5));
        registry.registerItem(new ItemDefinition("POWER_BOMB", "Power Bomb", "Place power bombs in morph ball",
                "morph", true, index++, null, null, null, null, List.of("can_use_power_bombs"), "POWER_BOMB", 5));
        // Other items
        registry.registerItem(new ItemDefinition("BOMBS", "Bombs", "Place bombs in morph ball",
                "morph", true, index++, null, null, null, List.of("can_morph"), List.of("can_place_bombs", "can_bomb_weak_walls"), null, null));
        registry.registerItem(new ItemDefinition("CHARGE_BEAM", "Charge Beam", "Charge your shots",
                "beam", true, index++, 3.0, null, null, null, null, null, null));
        registry.registerItem(new ItemDefinition("ICE_BEAM", "Ice Beam", "Freeze enemies",
                "beam", true, index++, null, 5, null, null, null, null, null));
        registry.registerItem(new ItemDefinition("HI_JUMP", "Hi-Jump", "Jump higher",
                "movement", true, index++));
        registry.registerItem(new ItemDefinition("SPEED_BOOSTER", "Speed Booster", "Run fast",
                "movement", true, index++, null, null, null, null, List.of("can_speed_booster", "can_shinespark"), null, null));
        registry.registerItem(new ItemDefinition("WAVE_BEAM", "Wave Beam", "Pierces walls",
                "beam", true, index++));
        registry.registerItem(new ItemDefinition("SPAZER_BEAM", "Spazer Beam", "Wide beam",
                "beam", true, index++));
        registry.registerItem(new ItemDefinition("SPRING_BALL", "Spring Ball", "Jump in morph ball",
                "morph", true, index++, null, null, null, List.of("can_morph"), null, null, null));
        registry.registerItem(new ItemDefinition("VARIA_SUIT", "Varia Suit", "Heat protection",
                "suit", true, index++, null, null, 0.5, null, null, null, null));
        registry.registerItem(new ItemDefinition("GRAVITY_SUIT", "Gravity Suit", "Lava protection",
                "suit", true, index++, null, null, 0.0, null, List.of("can_swim_lava", "can_move_underwater"), null, null));
        registry.registerItem(new ItemDefinition("XRAY_SCOPE", "X-Ray Scope", "See through blocks",
                "misc", true, index++));
        registry.registerItem(new ItemDefinition("PLASMA_BEAM", "Plasma Beam", "Powerful beam",
                "beam", true, index++));
        registry.registerItem(new ItemDefinition("GRAPPLE_BEAM", "Grapple Beam", "Swing on grappling beam",
                "beam", true, index++, null, null, null, null, List.of("can_grapple"), null, null));
        registry.registerItem(new ItemDefinition("SPACE_JUMP", "Space Jump", "Jump in mid-air",
                "movement", true, index++, null, null, null, null, List.of("can_space_jump"), null, null));
        registry.registerItem(new ItemDefinition("SCREW_ATTACK", "Screw Attack", "Spin jump attack",
                "movement", true, index++, null, null, null, null, List.of("can_screw_attack"), null, null));
        registry.registerItem(new ItemDefinition("MORPH_BALL", "Morph Ball", "Roll into a ball",
                "movement", true, index++, null, null, null, null, List.of("can_morph", "can_fit_small_spaces"), null, null));
        registry.registerItem(new ItemDefinition("RESERVE_TANK", "Reserve Tank", "Backup energy tank",
                "tank", true, index++));
        registry.registerItem(new ItemDefinition("WALL_JUMP_BOOTS", "Wall Jump Boots", "Jump off walls",
                "movement", true, index++, null, null, null, null, List.of("can_wall_jump"), null, null));

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
