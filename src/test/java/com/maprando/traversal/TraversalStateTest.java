package com.maprando.traversal;

import com.maprando.model.GameState;
import com.maprando.model.ResourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TraversalState class.
 * TraversalState tracks player state during graph traversal analysis.
 */
@DisplayName("TraversalState Tests")
class TraversalStateTest {

    private TraversalState traversalState;
    private GameState baseGameState;

    @BeforeEach
    void setUp() {
        baseGameState = GameState.standardStart();
        traversalState = new TraversalState(baseGameState);
    }

    @Test
    @DisplayName("TraversalState should be created from GameState")
    void testCreationFromGameState() {
        assertNotNull(traversalState, "TraversalState should be created");
        assertNotNull(traversalState.getGameState(), "GameState should be stored");
        assertTrue(traversalState.getCollectedItemIds().isEmpty(), "No items should be collected yet");
    }

    @Test
    @DisplayName("Starting state should have basic capabilities")
    void testStartingCapabilities() {
        // Standard start should have movement but not advanced tech
        assertTrue(traversalState.canMove(), "Should be able to move");
        assertFalse(traversalState.canMorph(), "Should not be able to morph at start");
        assertFalse(traversalState.canSurviveHeat(), "Should not survive heat at start");
        assertFalse(traversalState.hasGrapple(), "Should not have grapple at start");
    }

    @Test
    @DisplayName("Collecting Morph Ball should enable morph capabilities")
    void testCollectMorphBall() {
        traversalState.collectItem("MORPH_BALL");

        assertTrue(traversalState.canMorph(), "Should be able to morph after collecting Morph Ball");
        assertTrue(traversalState.getCollectedItemIds().contains("MORPH_BALL"),
            "Collected items should include Morph Ball");
    }

    @Test
    @DisplayName("Collecting Varia Suit should enable heat survival")
    void testCollectVariaSuit() {
        traversalState.collectItem("VARIA_SUIT");

        assertTrue(traversalState.canSurviveHeat(), "Should survive heat with Varia Suit");
        assertTrue(traversalState.getCollectedItemIds().contains("VARIA_SUIT"));
    }

    @Test
    @DisplayName("Collecting Grapple should enable grapple capabilities")
    void testCollectGrapple() {
        traversalState.collectItem("GRAPPLE_BEAM");

        assertTrue(traversalState.hasGrapple(), "Should have grapple after collecting Grapple Beam");
        assertTrue(traversalState.getCollectedItemIds().contains("GRAPPLE_BEAM"));
    }

    @Test
    @DisplayName("Collecting multiple items should accumulate capabilities")
    void testCollectMultipleItems() {
        traversalState.collectItem("MORPH_BALL");
        traversalState.collectItem("VARIA_SUIT");
        traversalState.collectItem("GRAPPLE_BEAM");

        assertTrue(traversalState.canMorph(), "Should be able to morph");
        assertTrue(traversalState.canSurviveHeat(), "Should survive heat");
        assertTrue(traversalState.hasGrapple(), "Should have grapple");
        assertEquals(3, traversalState.getCollectedItemIds().size(), "Should have 3 items");
    }

    @Test
    @DisplayName("Can satisfy requirement should check correctly")
    void testCanSatisfyRequirement() {
        // Starting state can't satisfy morph requirement
        assertFalse(traversalState.canSatisfyRequirement("can_morph"),
            "Should not satisfy can_morph at start");

        traversalState.collectItem("MORPH_BALL");

        assertTrue(traversalState.canSatisfyRequirement("can_morph"),
            "Should satisfy can_morph after collecting Morph Ball");
    }

    @Test
    @DisplayName("Can satisfy requirement should handle complex requirements")
    void testCanSatisfyComplexRequirements() {
        traversalState.collectItem("MORPH_BALL");
        traversalState.collectItem("BOMB");
        traversalState.collectItem("VARIA_SUIT");

        assertTrue(traversalState.canSatisfyRequirement("can_morph"),
            "Should satisfy can_morph");
        assertTrue(traversalState.canSatisfyRequirement("has_bombs"),
            "Should satisfy has_bombs");
        assertTrue(traversalState.canSatisfyRequirement("can_survive_heat"),
            "Should satisfy can_survive_heat");
    }

    @Test
    @DisplayName("Can satisfy all requirements should check conjunction")
    void testCanSatisfyAllRequirements() {
        traversalState.collectItem("MORPH_BALL");
        traversalState.collectItem("BOMB");

        assertTrue(traversalState.canSatisfyAllRequirements(),
            "No requirements means satisfiable");

        assertTrue(traversalState.canSatisfyAllRequirements("can_morph"),
            "Single satisfiable requirement");

        assertTrue(traversalState.canSatisfyAllRequirements("can_morph", "has_bombs"),
            "Multiple satisfiable requirements");

        assertFalse(traversalState.canSatisfyAllRequirements("can_morph", "has_grapple"),
            "Should fail if any requirement unsatisfied");
    }

    @Test
    @DisplayName("Cloning state should create independent copy")
    void testCloneState() {
        traversalState.collectItem("MORPH_BALL");
        traversalState.collectItem("VARIA_SUIT");

        TraversalState cloned = traversalState.clone();

        assertEquals(traversalState.getCollectedItemIds(), cloned.getCollectedItemIds(),
            "Cloned state should have same items");
        assertEquals(traversalState.canMorph(), cloned.canMorph(),
            "Cloned state should have same capabilities");

        // Modify original
        traversalState.collectItem("GRAPPLE_BEAM");

        assertNotEquals(traversalState.getCollectedItemIds().size(),
            cloned.getCollectedItemIds().size(),
            "Cloned state should be independent");
    }

    @Test
    @DisplayName("State should track visited locations")
    void testVisitedLocations() {
        String location1 = "brinstar_morph_ball_room";
        String location2 = "norfair_ice_beam_room";

        assertFalse(traversalState.hasVisitedLocation(location1),
            "Location should not be visited initially");

        traversalState.markLocationVisited(location1);

        assertTrue(traversalState.hasVisitedLocation(location1),
            "Location should be marked as visited");
        assertFalse(traversalState.hasVisitedLocation(location2),
            "Other locations should remain unvisited");

        traversalState.markLocationVisited(location2);

        assertTrue(traversalState.hasVisitedLocation(location2),
            "Second location should also be visited");
        assertEquals(2, traversalState.getVisitedLocations().size(),
            "Should track 2 visited locations");
    }

    @Test
    @DisplayName("State should handle resource tracking")
    void testResourceTracking() {
        // Starting state should have basic resources
        assertTrue(traversalState.hasResource(ResourceType.ENERGY, 100),
            "Should have starting energy");
        assertTrue(traversalState.hasResource(ResourceType.MISSILE, 0),
            "Should have zero missiles initially");
    }

    @Test
    @DisplayName("State should provide capability summary")
    void testCapabilitySummary() {
        traversalState.collectItem("MORPH_BALL");
        traversalState.collectItem("VARIA_SUIT");

        String summary = traversalState.getCapabilitySummary();

        assertNotNull(summary, "Summary should not be null");
        assertTrue(summary.contains("Morph") || summary.contains("morph"),
            "Summary should mention morph capability");
        assertTrue(summary.contains("Heat") || summary.contains("heat") || summary.contains("Protection"),
            "Summary should mention heat protection");
    }

    @Test
    @DisplayName("State should handle swim water capability")
    void testCanSwimWater() {
        assertFalse(traversalState.canSwimWater(),
            "Should not swim water at start");

        traversalState.collectItem("GRAVITY_SUIT");

        assertTrue(traversalState.canSwimWater(),
            "Should swim water with Gravity Suit");
    }

    @Test
    @DisplayName("State should handle speed booster capability")
    void testCanSpeedBoost() {
        assertFalse(traversalState.canUseSpeedBooster(),
            "Should not use speed booster at start");

        traversalState.collectItem("SPEED_BOOSTER");

        assertTrue(traversalState.canUseSpeedBooster(),
            "Should use speed booster after collecting");
    }

    @Test
    @DisplayName("State should handle space jump capability")
    void testCanSpaceJump() {
        assertFalse(traversalState.canSpaceJump(),
            "Should not space jump at start");

        traversalState.collectItem("SPACE_JUMP");

        assertTrue(traversalState.canSpaceJump(),
            "Should space jump after collecting");
    }

    @Test
    @DisplayName("State should handle screw attack capability")
    void testCanScrewAttack() {
        assertFalse(traversalState.canScrewAttack(),
            "Should not screw attack at start");

        traversalState.collectItem("SCREW_ATTACK");

        assertTrue(traversalState.canScrewAttack(),
            "Should screw attack after collecting");
    }
}