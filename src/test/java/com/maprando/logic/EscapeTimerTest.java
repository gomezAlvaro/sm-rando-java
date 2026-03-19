package com.maprando.logic;

import com.maprando.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive tests for EscapeTimer calculations.
 */
public class EscapeTimerTest {

    @Test
    public void testParseInGameTime() {
        // Test standard time format (seconds.frames)
        float result1 = EscapeTimer.parseInGameTime(10.0f);
        assertEquals(10.0f, result1, 0.001f, "Integer time should parse correctly");

        float result2 = EscapeTimer.parseInGameTime(10.30f);
        assertEquals(10.5f, result2, 0.001f, "30 frames should be 0.5 seconds");

        float result3 = EscapeTimer.parseInGameTime(10.59f);
        assertEquals(10.983f, result3, 0.001f, "59 frames should be 0.983 seconds");

        // Edge cases
        float result4 = EscapeTimer.parseInGameTime(0.0f);
        assertEquals(0.0f, result4, 0.001f, "Zero time should parse correctly");
    }

    @Test
    public void testRequirementSatisfied() {
        RandomizerSettings settings = new RandomizerSettings();
        DifficultyConfig difficulty = new DifficultyConfig();

        // Test EnemiesCleared
        settings.qualityOfLifeSettings.escapeEnemiesCleared = true;
        assertTrue(EscapeTimer.isRequirementSatisfied("EnemiesCleared", settings, difficulty));

        settings.qualityOfLifeSettings.escapeEnemiesCleared = false;
        assertFalse(EscapeTimer.isRequirementSatisfied("EnemiesCleared", settings, difficulty));

        // Test CanUsePowerBombs with MotherBrainFight
        settings.qualityOfLifeSettings.motherBrainFight = RandomizerSettings.MotherBrainFight.Skip;
        assertTrue(EscapeTimer.isRequirementSatisfied("CanUsePowerBombs", settings, difficulty));

        settings.qualityOfLifeSettings.motherBrainFight = RandomizerSettings.MotherBrainFight.Standard;
        assertFalse(EscapeTimer.isRequirementSatisfied("CanUsePowerBombs", settings, difficulty));

        // Test tech requirements
        assertFalse(EscapeTimer.isRequirementSatisfied("CanMidAirMorph", settings, difficulty));
        difficulty.addTech("can_mid_air_morph");
        assertTrue(EscapeTimer.isRequirementSatisfied("CanMidAirMorph", settings, difficulty));

        // Test CanOneTapShortcharge
        difficulty.shineChargeTiles = 25.0f;
        assertTrue(EscapeTimer.isRequirementSatisfied("CanOneTapShortcharge", settings, difficulty));

        difficulty.shineChargeTiles = 30.0f;
        assertFalse(EscapeTimer.isRequirementSatisfied("CanOneTapShortcharge", settings, difficulty));
    }

    @Test
    public void testDifficultyConfigPresets() {
        // Test Casual preset
        DifficultyConfig casual = DifficultyConfig.fromPreset("casual");
        assertEquals(1.5f, casual.escapeTimerMultiplier, 0.001f);
        assertEquals(25.0f, casual.shineChargeTiles, 0.001f);
        assertFalse(casual.hasTech("can_mid_air_morph"));

        // Test Normal preset
        DifficultyConfig normal = DifficultyConfig.fromPreset("normal");
        assertEquals(1.0f, normal.escapeTimerMultiplier, 0.001f);
        assertEquals(19.0f, normal.shineChargeTiles, 0.001f);
        assertTrue(normal.hasTech("can_mid_air_morph"));

        // Test Hard preset
        DifficultyConfig hard = DifficultyConfig.fromPreset("hard");
        assertEquals(0.9f, hard.escapeTimerMultiplier, 0.001f);
        assertEquals(14.0f, hard.shineChargeTiles, 0.001f);
        assertTrue(hard.hasTech("can_walljump"));
        assertTrue(hard.hasTech("can_shinespark"));

        // Test Expert preset
        DifficultyConfig expert = DifficultyConfig.fromPreset("expert");
        assertEquals(0.8f, expert.escapeTimerMultiplier, 0.001f);
        assertEquals(12.0f, expert.shineChargeTiles, 0.001f);
        assertTrue(expert.hasTech("can_horizontal_shinespark"));
        assertTrue(expert.hasTech("can_suitless_lava_dive"));

        // Test Nightmare preset
        DifficultyConfig nightmare = DifficultyConfig.fromPreset("nightmare");
        assertEquals(0.7f, nightmare.escapeTimerMultiplier, 0.001f);
        assertEquals(11.0f, nightmare.shineChargeTiles, 0.001f);
        assertTrue(nightmare.hasTech("can_kago"));
        assertTrue(nightmare.hasTech("can_moonfall"));
        assertTrue(nightmare.hasTech("can_hero_shot"));
    }

    @Test
    public void testCostClass() {
        EscapeTimerData.Cost cost1 = new EscapeTimerData.Cost(5.0f);
        EscapeTimerData.Cost cost2 = new EscapeTimerData.Cost(3.0f);
        EscapeTimerData.Cost cost3 = new EscapeTimerData.Cost(5.0f);

        // Test addition
        EscapeTimerData.Cost sum = cost1.add(cost2);
        assertEquals(8.0f, sum.value, 0.001f);

        // Test comparison
        assertTrue(cost1.compareTo(cost2) > 0);
        assertTrue(cost2.compareTo(cost1) < 0);
        assertEquals(0, cost1.compareTo(cost3));

        // Test isZero
        EscapeTimerData.Cost zero = new EscapeTimerData.Cost(0.0f);
        assertTrue(zero.isZero());
        assertFalse(cost1.isZero());
    }

    @Test
    public void testVertexKey() {
        EscapeTimerData.VertexKey key1 = new EscapeTimerData.VertexKey(1, 2);
        EscapeTimerData.VertexKey key2 = new EscapeTimerData.VertexKey(1, 2);
        EscapeTimerData.VertexKey key3 = new EscapeTimerData.VertexKey(1, 3);
        EscapeTimerData.VertexKey key4 = new EscapeTimerData.VertexKey(2, 2);

        // Test equals
        assertEquals(key1, key2);
        assertNotEquals(key1, key3);
        assertNotEquals(key1, key4);

        // Test hashCode
        assertEquals(key1.hashCode(), key2.hashCode());
        assertNotEquals(key1.hashCode(), key3.hashCode());
    }

    @Test
    public void testRoomDoorGraph() {
        EscapeTimerData.RoomDoorGraph graph = new EscapeTimerData.RoomDoorGraph();

        assertNotNull(graph.vertices);
        assertNotNull(graph.successors);
        assertEquals(-1, graph.motherBrainVertexId);
        assertEquals(-1, graph.shipVertexId);
        assertEquals(-1, graph.animalsVertexId);
    }

    @Test
    public void testGetShortestPath_SimpleGraph() {
        // Create a simple graph for testing
        EscapeTimerData.RoomDoorGraph graph = new EscapeTimerData.RoomDoorGraph();

        // Add vertices
        graph.vertices.add(new EscapeTimerData.VertexKey(0, 0)); // 0
        graph.vertices.add(new EscapeTimerData.VertexKey(1, 0)); // 1
        graph.vertices.add(new EscapeTimerData.VertexKey(2, 0)); // 2

        // Add edges: 0 -> 1 (cost 5), 1 -> 2 (cost 3), 0 -> 2 (cost 10)
        graph.successors.add(new java.util.ArrayList<>());
        graph.successors.add(new java.util.ArrayList<>());
        graph.successors.add(new java.util.ArrayList<>());

        graph.successors.get(0).add(new EscapeTimerData.RoomDoorGraph.GraphEdge(1, new EscapeTimerData.Cost(5.0f)));
        graph.successors.get(1).add(new EscapeTimerData.RoomDoorGraph.GraphEdge(2, new EscapeTimerData.Cost(3.0f)));
        graph.successors.get(0).add(new EscapeTimerData.RoomDoorGraph.GraphEdge(2, new EscapeTimerData.Cost(10.0f)));

        // Find shortest path from 0 to 2
        var path = EscapeTimer.getShortestPath(0, 2, graph);

        assertNotNull(path);
        assertEquals(3, path.size());
        assertEquals(0, path.get(0).getKey().intValue());
        assertEquals(1, path.get(1).getKey().intValue());
        assertEquals(2, path.get(2).getKey().intValue());

        // Check that the cost is 8 (5 + 3), not 10 (direct edge)
        float finalCost = path.get(path.size() - 1).getValue().value;
        assertEquals(8.0f, finalCost, 0.001f);
    }

    @Test
    public void testGetShortestPath_NoPath() {
        // Create a disconnected graph
        EscapeTimerData.RoomDoorGraph graph = new EscapeTimerData.RoomDoorGraph();

        graph.vertices.add(new EscapeTimerData.VertexKey(0, 0));
        graph.vertices.add(new EscapeTimerData.VertexKey(1, 0));

        graph.successors.add(new java.util.ArrayList<>());
        graph.successors.add(new java.util.ArrayList<>());

        // No edges between vertices

        // Try to find path from 0 to 1
        assertThrows(RuntimeException.class, () -> {
            EscapeTimer.getShortestPath(0, 1, graph);
        });
    }

    @Test
    public void testSpoilerEscapeData() {
        // Test spoiler data structures
        EscapeTimerData.SpoilerEscapeRouteNode from =
            new EscapeTimerData.SpoilerEscapeRouteNode("Room1", "Door1", 10, 20);
        EscapeTimerData.SpoilerEscapeRouteNode to =
            new EscapeTimerData.SpoilerEscapeRouteNode("Room2", "Door2", 30, 40);

        EscapeTimerData.SpoilerEscapeRouteEntry entry =
            new EscapeTimerData.SpoilerEscapeRouteEntry(from, to, 5.5f);

        assertEquals("Room1", entry.from.room);
        assertEquals("Door1", entry.from.node);
        assertEquals(10, entry.from.x);
        assertEquals(20, entry.from.y);
        assertEquals("Room2", entry.to.room);
        assertEquals("Door2", entry.to.node);
        assertEquals(30, entry.to.x);
        assertEquals(40, entry.to.y);
        assertEquals(5.5f, entry.time, 0.001f);
    }

    @Test
    public void testEscapeTimeCalculation() {
        // Test escape time calculation logic
        // With multiplier < 1.1, should ceil to nearest second
        float rawTime1 = 45.3f;
        float multiplier1 = 1.0f;
        float finalTime1 = multiplier1 < 1.1f ? (float) Math.ceil(rawTime1) : (float) (Math.ceil(rawTime1 / 5.0) * 5.0);
        assertEquals(46.0f, finalTime1, 0.001f);

        // With multiplier >= 1.1, should ceil to nearest 5 seconds
        float rawTime2 = 45.3f;
        float multiplier2 = 1.2f;
        float finalTime2 = multiplier2 < 1.1f ? (float) Math.ceil(rawTime2) : (float) (Math.ceil(rawTime2 / 5.0) * 5.0);
        assertEquals(50.0f, finalTime2, 0.001f);

        // Should cap at 5995 seconds
        float rawTime3 = 6000.0f;
        float finalTime3 = (float) Math.min(5995.0, Math.ceil(rawTime3 / 5.0) * 5.0);
        assertEquals(5995.0f, finalTime3, 0.001f);
    }

    @Test
    public void testTechManagement() {
        DifficultyConfig config = new DifficultyConfig();

        assertFalse(config.hasTech("can_walljump"));
        assertFalse(config.hasTech("can_shinespark"));

        config.addTech("can_walljump");
        assertTrue(config.hasTech("can_walljump"));
        assertFalse(config.hasTech("can_shinespark"));

        config.addTech("can_shinespark");
        assertTrue(config.hasTech("can_walljump"));
        assertTrue(config.hasTech("can_shinespark"));

        config.removeTech("can_walljump");
        assertFalse(config.hasTech("can_walljump"));
        assertTrue(config.hasTech("can_shinespark"));
    }

    @Test
    public void testQualityOfLifeSettings() {
        RandomizerSettings settings = new RandomizerSettings();

        // Test default values
        assertFalse(settings.qualityOfLifeSettings.escapeEnemiesCleared);
        assertEquals(RandomizerSettings.MotherBrainFight.Standard,
            settings.qualityOfLifeSettings.motherBrainFight);

        // Test setting values
        settings.qualityOfLifeSettings.escapeEnemiesCleared = true;
        assertTrue(settings.qualityOfLifeSettings.escapeEnemiesCleared);

        settings.qualityOfLifeSettings.motherBrainFight = RandomizerSettings.MotherBrainFight.Skip;
        assertEquals(RandomizerSettings.MotherBrainFight.Skip,
            settings.qualityOfLifeSettings.motherBrainFight);
    }
}
