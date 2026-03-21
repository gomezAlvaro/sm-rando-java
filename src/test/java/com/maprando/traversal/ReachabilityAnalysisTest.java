package com.maprando.traversal;

import com.maprando.data.DataLoader;
import com.maprando.data.model.ItemData;
import com.maprando.data.model.LocationData;
import com.maprando.model.GameState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Set;

/**
 * Unit tests for the ReachabilityAnalysis class.
 * ReachabilityAnalysis determines which locations are accessible given current items.
 *
 * Requirements have been imported and added to locations.json.
 */
@DisplayName("ReachabilityAnalysis Tests")
class ReachabilityAnalysisTest {

    private ReachabilityAnalysis reachabilityAnalysis;
    private DataLoader dataLoader;
    private TraversalState initialState;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();

        GameState baseState = GameState.standardStart();
        initialState = new TraversalState(baseState);

        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);
    }

    @Test
    @DisplayName("ReachabilityAnalysis should be created successfully")
    void testCreation() {
        assertNotNull(reachabilityAnalysis, "ReachabilityAnalysis should be created");
        assertNotNull(reachabilityAnalysis.getCurrentState(), "Initial state should be stored");
    }

    @Test
    @DisplayName("Initial state should reach early game locations")
    void testInitialEarlyGameReachability() {
        Set<String> reachableLocations = reachabilityAnalysis.getReachableLocations();

        assertNotNull(reachableLocations, "Reachable locations should not be null");
        assertFalse(reachableLocations.isEmpty(), "Should reach some locations initially");

        // Early game locations should be reachable (no requirements)
        assertTrue(reachableLocations.contains("crateria_the_moat"),
            "The Moat should be reachable (no requirements)");
        assertTrue(reachableLocations.contains("crateria_bomb_torizo_room"),
            "Bomb Torizo Room should be reachable (no requirements)");
    }

    @Test
    @DisplayName("Locations with morph requirement should need Morph Ball")
    void testMorphRequiredLocations() {
        Set<String> reachableWithoutMorph = reachabilityAnalysis.getReachableLocations();

        // Locations requiring morph should not be reachable initially
        assertFalse(reachableWithoutMorph.contains("brinstar_morph_ball_room"),
            "Morph Ball Room should not be reachable without Morph Ball");

        // Add Morph Ball
        initialState.collectItem("MORPH_BALL");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        Set<String> reachableWithMorph = reachabilityAnalysis.getReachableLocations();

        // Now morph-required locations should be reachable
        assertTrue(reachableWithMorph.contains("brinstar_morph_ball_room"),
            "Morph Ball Room should be reachable with Morph Ball");
    }

    @Test
    @DisplayName("Locations with bomb requirement should need Bombs")
    void testBombRequiredLocations() {
        // Add Morph Ball but not Bombs
        initialState.collectItem("MORPH_BALL");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        Set<String> reachableWithoutBombs = reachabilityAnalysis.getReachableLocations();

        // Alpha Power Bomb Room requires both morph and bombs
        assertFalse(reachableWithoutBombs.contains("brinstar_alpha_power_bomb_room"),
            "Alpha Power Bomb Room should not be reachable without Bombs");

        // Add Bombs
        initialState.collectItem("BOMB");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        Set<String> reachableWithBombs = reachabilityAnalysis.getReachableLocations();

        assertTrue(reachableWithBombs.contains("brinstar_alpha_power_bomb_room"),
            "Alpha Power Bomb Room should be reachable with Morph Ball + Bombs");
    }

    @Test
    @DisplayName("Locations with heat requirement should need Varia Suit")
    void testHeatRequiredLocations() {
        Set<String> reachableWithoutVaria = reachabilityAnalysis.getReachableLocations();

        // Speed Booster Room requires heat survival
        assertFalse(reachableWithoutVaria.contains("norfair_speed_booster_room"),
            "Speed Booster Room should not be reachable without Varia Suit");

        // Add Varia Suit and Speed Booster
        initialState.collectItem("VARIA_SUIT");
        initialState.collectItem("SPEED_BOOSTER");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        Set<String> reachableWithVaria = reachabilityAnalysis.getReachableLocations();

        assertTrue(reachableWithVaria.contains("norfair_speed_booster_room"),
            "Speed Booster Room should be reachable with Varia Suit + Speed Booster");
    }

    @Test
    @DisplayName("Locations with grapple requirement should need Grapple Beam")
    void testGrappleRequiredLocations() {
        // Add Morph Ball and Varia Suit for basic access (Wave Beam Room requires heat protection + grapple)
        initialState.collectItem("MORPH_BALL");
        initialState.collectItem("VARIA_SUIT");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        Set<String> reachableWithoutGrapple = reachabilityAnalysis.getReachableLocations();

        // Wave Beam Room requires grapple (and heat protection)
        assertFalse(reachableWithoutGrapple.contains("norfair_wave_beam_room"),
            "Wave Beam Room should not be reachable without Grapple");

        // Add Grapple
        initialState.collectItem("GRAPPLE_BEAM");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        Set<String> reachableWithGrapple = reachabilityAnalysis.getReachableLocations();

        assertTrue(reachableWithGrapple.contains("norfair_wave_beam_room"),
            "Wave Beam Room should be reachable with Grapple");
    }

    @Test
    @DisplayName("Locations with water requirement should need Gravity Suit")
    void testWaterRequiredLocations() {
        // No need for prerequisites - just test gravity suit requirement
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        Set<String> reachableWithoutGravity = reachabilityAnalysis.getReachableLocations();

        // Main Street requires water swimming
        assertFalse(reachableWithoutGravity.contains("maridia_main_street"),
            "Main Street should not be reachable without Gravity Suit");

        // Add Gravity Suit
        initialState.collectItem("GRAVITY_SUIT");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        Set<String> reachableWithGravity = reachabilityAnalysis.getReachableLocations();

        assertTrue(reachableWithGravity.contains("maridia_main_street"),
            "Main Street should be reachable with Gravity Suit");
    }

    @Test
    @DisplayName("Complex requirement chains should be satisfied progressively")
    void testComplexRequirementChains() {
        Set<String> reachable = reachabilityAnalysis.getReachableLocations();

        // Initially: only early game locations
        int initialReachable = reachable.size();

        // Add Morph Ball
        initialState.collectItem("MORPH_BALL");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);
        reachable = reachabilityAnalysis.getReachableLocations();

        assertTrue(reachable.size() > initialReachable,
            "Adding Morph Ball should increase reachable locations");

        int morphReachable = reachable.size();

        // Add Bombs
        initialState.collectItem("BOMB");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);
        reachable = reachabilityAnalysis.getReachableLocations();

        assertTrue(reachable.size() > morphReachable,
            "Adding Bombs should increase reachable locations further");

        int bombsReachable = reachable.size();

        // Add Varia Suit
        initialState.collectItem("VARIA_SUIT");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);
        reachable = reachabilityAnalysis.getReachableLocations();

        assertTrue(reachable.size() > bombsReachable,
            "Adding Varia Suit should increase reachable locations further");
    }

    @Test
    @DisplayName("Should identify unreachable locations")
    void testUnreachableLocations() {
        Set<String> unreachable = reachabilityAnalysis.getUnreachableLocations();

        assertNotNull(unreachable, "Unreachable locations should not be null");

        // With starting items, many locations should be unreachable
        assertTrue(unreachable.size() > 0,
            "Should have some unreachable locations initially");

        // Verify specific locations are unreachable
        assertTrue(unreachable.contains("brinstar_morph_ball_room"),
            "Morph Ball Room should be unreachable without Morph Ball");
        assertTrue(unreachable.contains("norfair_speed_booster_room"),
            "Speed Booster Room should be unreachable without Varia Suit");
    }

    @Test
    @DisplayName("Should calculate reachable percentage")
    void testReachablePercentage() {
        double percentage = reachabilityAnalysis.getReachablePercentage();

        assertNotNull(percentage, "Percentage should not be null");
        assertTrue(percentage > 0.0, "Should reach some percentage of locations");
        assertTrue(percentage < 100.0, "Should not reach all locations initially");

        // With all items, should reach all locations
        giveAllItems();
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        percentage = reachabilityAnalysis.getReachablePercentage();

        assertTrue(percentage == 100.0,
            "With all items, should reach 100% of locations");
    }

    @Test
    @DisplayName("Should provide reachability summary")
    void testReachabilitySummary() {
        String summary = reachabilityAnalysis.getReachabilitySummary();

        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.contains("reachable") || summary.contains("locations"),
            "Summary should mention reachable locations");
    }

    @Test
    @DisplayName("Should handle location requirement updates")
    void testRequirementUpdates() {
        // Initially, some locations unreachable
        Set<String> initialUnreachable = reachabilityAnalysis.getUnreachableLocations();

        assertTrue(initialUnreachable.contains("brinstar_morph_ball_room"),
            "Morph Ball Room should initially be unreachable");

        // Satisfy requirement
        initialState.collectItem("MORPH_BALL");
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        Set<String> updatedUnreachable = reachabilityAnalysis.getUnreachableLocations();

        assertFalse(updatedUnreachable.contains("brinstar_morph_ball_room"),
            "Morph Ball Room should become reachable after getting Morph Ball");
    }

    @Test
    @DisplayName("Should detect when all locations reachable")
    void testAllLocationsReachable() {
        assertFalse(reachabilityAnalysis.areAllLocationsReachable(),
            "Not all locations should be reachable initially");

        giveAllItems();
        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);

        assertTrue(reachabilityAnalysis.areAllLocationsReachable(),
            "All locations should be reachable with all items");
    }

    @Test
    @DisplayName("Should handle boss location reachability")
    void testBossLocationReachability() {
        // Boss locations require specific keys
        Set<String> reachable = reachabilityAnalysis.getReachableLocations();

        // Ridley's Lair should not be reachable initially (requires keys + heat/lava survival)
        assertFalse(reachable.contains("lower_norfair_ridley"),
            "Ridley's Lair should not be reachable initially");
    }

    @Test
    @DisplayName("Should track newly reachable locations")
    void testNewlyReachableLocations() {
        Set<String> initialReachable = reachabilityAnalysis.getReachableLocations();

        // Add Morph Ball
        initialState.collectItem("MORPH_BALL");

        reachabilityAnalysis = new ReachabilityAnalysis(dataLoader, initialState);
        Set<String> updatedReachable = reachabilityAnalysis.getReachableLocations();

        Set<String> newlyReachable = reachabilityAnalysis.getNewlyReachableLocations(initialReachable);

        assertFalse(newlyReachable.isEmpty(),
            "Should have newly reachable locations after getting Morph Ball");
        assertTrue(newlyReachable.contains("brinstar_morph_ball_room"),
            "Morph Ball Room should be newly reachable");
    }

    // Helper method to give all items for testing
    private void giveAllItems() {
        String[] allItems = {
            "MORPH_BALL", "BOMB", "CHARGE_BEAM", "ICE_BEAM", "WAVE_BEAM",
            "SPAZER_BEAM", "PLASMA_BEAM", "VARIA_SUIT", "GRAVITY_SUIT",
            "GRAPPLE_BEAM", "XRAY_SCOPE", "SPACE_JUMP", "SCREW_ATTACK",
            "SPEED_BOOSTER", "HIJUMP_BOOTS", "SPRING_BALL"
        };
        for (String item : allItems) {
            initialState.collectItem(item);
        }
    }
}