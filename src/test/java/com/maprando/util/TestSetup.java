package com.maprando.util;

import com.maprando.data.DataLoader;
import com.maprando.model.ItemRegistry;
import com.maprando.model.ItemDefinition;
import java.io.IOException;

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
     */
    public static ItemRegistry createMinimalRegistry() {
        ItemRegistry registry = new ItemRegistry();

        // Register essential items with indices
        int index = 0;
        registry.registerItem(new ItemDefinition("CHARGE_BEAM", "Charge Beam", "Charge your shots", "beam", true, index++));
        registry.registerItem(new ItemDefinition("ICE_BEAM", "Ice Beam", "Freeze enemies", "beam", true, index++));
        registry.registerItem(new ItemDefinition("WAVE_BEAM", "Wave Beam", "Pierces walls", "beam", true, index++));
        registry.registerItem(new ItemDefinition("SPAZER_BEAM", "Spazer Beam", "Wide beam", "beam", true, index++));
        registry.registerItem(new ItemDefinition("PLASMA_BEAM", "Plasma Beam", "Powerful beam", "beam", true, index++));
        registry.registerItem(new ItemDefinition("MORPH_BALL", "Morph Ball", "Roll into a ball", "movement", true, index++));
        registry.registerItem(new ItemDefinition("BOMB", "Bomb", "Place bombs in morph ball", "movement", true, index++));
        registry.registerItem(new ItemDefinition("POWER_BOMB", "Power Bomb", "Place power bombs in morph ball", "movement", true, index++));
        registry.registerItem(new ItemDefinition("VARIA_SUIT", "Varia Suit", "Heat protection", "suit", true, index++));
        registry.registerItem(new ItemDefinition("GRAVITY_SUIT", "Gravity Suit", "Lava protection", "suit", true, index++));
        registry.registerItem(new ItemDefinition("ENERGY_TANK", "Energy Tank", "Increases max energy", "tank", true, index++));
        registry.registerItem(new ItemDefinition("MISSILE_TANK", "Missile Tank", "Increases missiles", "tank", true, index++));
        registry.registerItem(new ItemDefinition("SUPER_MISSILE_TANK", "Super Missile Tank", "Increases super missiles", "tank", true, index++));
        registry.registerItem(new ItemDefinition("POWER_BOMB_TANK", "Power Bomb Tank", "Increases power bombs", "tank", true, index++));

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
