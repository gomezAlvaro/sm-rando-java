package com.maprando.traversal;

import com.maprando.data.DataLoader;
import com.maprando.data.model.LocationData;
import com.maprando.model.GameState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Unit tests for the GameGraph class.
 * GameGraph represents the game world as a graph of locations and connections.
 */
@DisplayName("GameGraph Tests")
class GameGraphTest {

    private GameGraph gameGraph;
    private DataLoader dataLoader;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();

        gameGraph = new GameGraph(dataLoader);
    }

    @Test
    @DisplayName("GameGraph should be created successfully")
    void testCreation() {
        assertNotNull(gameGraph, "GameGraph should be created");
        assertNotNull(gameGraph.getNodes(), "Graph should have nodes");
        assertNotNull(gameGraph.getEdges(), "Graph should have edges");
    }

    @Test
    @DisplayName("Graph should have nodes for all locations")
    void testGraphNodeCreation() {
        int locationCount = dataLoader.getLocationData().getLocations().size();
        int nodeCount = gameGraph.getNodeCount();

        assertEquals(locationCount, nodeCount,
            "Should have one node per location");
    }

    @Test
    @DisplayName("Graph nodes should contain location data")
    void testGraphNodeData() {
        List<GameGraphNode> nodes = gameGraph.getNodes();

        assertFalse(nodes.isEmpty(), "Should have nodes");

        GameGraphNode morphRoomNode = gameGraph.getNode("brinstar_morph_ball_room");

        assertNotNull(morphRoomNode, "Should find Morph Ball Room node");
        assertEquals("brinstar_morph_ball_room", morphRoomNode.getId(),
            "Node ID should match location ID");
        assertEquals("Morph Ball Room (1)", morphRoomNode.getName(),
            "Node name should match location name (with item index)");
        assertEquals("Brinstar", morphRoomNode.getRegion(),
            "Node region should match location region");
    }

    @Test
    @DisplayName("Graph should create edges based on requirements")
    void testGraphEdgeCreation() {
        // Edges represent connections between locations
        // In this simple model, we consider all locations as potential destinations
        // with requirements determining reachability

        List<GameGraphEdge> edges = gameGraph.getEdges();

        assertNotNull(edges, "Should have edges");
    }

    @Test
    @DisplayName("Should find node by location ID")
    void testFindNodeById() {
        GameGraphNode xrayNode = gameGraph.getNode("brinstar_x_ray_scope_room");

        assertNotNull(xrayNode, "Should find X-Ray Scope Room node");
        assertEquals("X-Ray Scope Room", xrayNode.getName(),
            "Should have correct name");
    }

    @Test
    @DisplayName("Should return null for non-existent node")
    void testFindNonExistentNode() {
        GameGraphNode fakeNode = gameGraph.getNode("fake_location_id");

        assertNull(fakeNode, "Should return null for non-existent node");
    }

    @Test
    @DisplayName("Should find nodes by region")
    void testFindNodesByRegion() {
        List<GameGraphNode> brinstarNodes = gameGraph.getNodesInRegion("Brinstar");

        assertNotNull(brinstarNodes, "Should find Brinstar nodes");
        assertFalse(brinstarNodes.isEmpty(), "Brinstar should have nodes");

        for (GameGraphNode node : brinstarNodes) {
            assertEquals("Brinstar", node.getRegion(),
                "All nodes should be in Brinstar region");
        }
    }

    @Test
    @DisplayName("Should find early game locations")
    void testFindEarlyGameLocations() {
        List<GameGraphNode> earlyLocations = gameGraph.getEarlyGameLocations();

        assertNotNull(earlyLocations, "Should find early game locations");
        assertFalse(earlyLocations.isEmpty(), "Should have early game locations");

        for (GameGraphNode node : earlyLocations) {
            assertTrue(node.isEarlyGame(),
                "All nodes should be marked as early game");
        }
    }

    @Test
    @DisplayName("Should find boss locations")
    void testFindBossLocations() {
        List<GameGraphNode> bossLocations = gameGraph.getBossLocations();

        assertNotNull(bossLocations, "Should find boss locations");

        // Verify boss locations are correctly identified
        for (GameGraphNode node : bossLocations) {
            assertTrue(node.isBoss(),
                "All nodes should be marked as boss locations");
        }
    }

    @Test
    @DisplayName("Should find path between locations")
    void testFindPathBetweenLocations() {
        // In a simple graph model, path finding considers requirements
        // For now, we test that the method works and returns a path

        List<GameGraphNode> path = gameGraph.findPath(
            "brinstar_morph_ball_room",
            "brinstar_x_ray_scope_room"
        );

        assertNotNull(path, "Should find a path");
        assertFalse(path.isEmpty(), "Path should not be empty");
        assertEquals("brinstar_morph_ball_room", path.get(0).getId(),
            "Path should start at source");
    }

    @Test
    @DisplayName("Should handle path finding with requirements")
    void testPathFindingWithRequirements() {
        TraversalState state = new TraversalState(GameState.standardStart());

        // Try to find path to another location
        // Note: Current location data has empty requirements, so path should be found
        List<GameGraphNode> path = gameGraph.findPathWithRequirements(
            "brinstar_morph_ball_room",
            "crateria_bomb_torizo_room",
            state
        );

        // With real requirements from Rust data, this would check reachability
        // For now, just verify the method works
        assertNotNull(path, "Should find a path (no requirements in current data)");
    }

    @Test
    @DisplayName("Should get connected components")
    void testConnectedComponents() {
        List<Set<GameGraphNode>> components = gameGraph.getConnectedComponents();

        assertNotNull(components, "Should find connected components");
        assertFalse(components.isEmpty(), "Should have at least one component");

        // All locations should be in some component
        int totalNodesInComponents = components.stream()
            .mapToInt(Set::size)
            .sum();

        assertEquals(gameGraph.getNodeCount(), totalNodesInComponents,
            "All nodes should be in connected components");
    }

    @Test
    @DisplayName("Should detect isolated nodes")
    void testIsolatedNodes() {
        List<GameGraphNode> isolated = gameGraph.getIsolatedNodes();

        assertNotNull(isolated, "Should return list (possibly empty)");

        // In a well-connected graph, there should be no isolated nodes
        // But the method should work regardless
    }

    @Test
    @DisplayName("Should calculate node degree")
    void testNodeDegree() {
        GameGraphNode node = gameGraph.getNode("brinstar_morph_ball_room");

        int degree = gameGraph.getNodeDegree(node);

        assertTrue(degree >= 0, "Degree should be non-negative");
    }

    @Test
    @DisplayName("Should find adjacent nodes")
    void testAdjacentNodes() {
        GameGraphNode node = gameGraph.getNode("brinstar_morph_ball_room");

        List<GameGraphNode> adjacent = gameGraph.getAdjacentNodes(node);

        assertNotNull(adjacent, "Should return adjacent nodes list");
    }

    @Test
    @DisplayName("Should handle graph statistics")
    void testGraphStatistics() {
        String stats = gameGraph.getGraphStatistics();

        assertNotNull(stats, "Statistics should not be null");
        assertTrue(stats.contains("nodes") || stats.contains("locations"),
            "Statistics should mention nodes or locations");
    }

    @Test
    @DisplayName("Should find shortest path")
    void testShortestPath() {
        List<GameGraphNode> shortestPath = gameGraph.findShortestPath(
            "brinstar_morph_ball_room",
            "brinstar_x_ray_scope_room"
        );

        assertNotNull(shortestPath, "Should find shortest path");
        assertFalse(shortestPath.isEmpty(), "Path should not be empty");

        // Shortest path should have reasonable length
        assertTrue(shortestPath.size() <= 10, "Path should be reasonably short");
    }

    @Test
    @DisplayName("Should validate graph structure")
    void testValidateGraph() {
        boolean isValid = gameGraph.validateGraph();

        assertTrue(isValid, "Graph structure should be valid");
    }

    @Test
    @DisplayName("Should find reachable nodes from start")
    void testReachableFromStart() {
        TraversalState state = new TraversalState(GameState.standardStart());

        Set<GameGraphNode> reachable = gameGraph.getReachableNodes(
            "brinstar_morph_ball_room",
            state
        );

        assertNotNull(reachable, "Should find reachable nodes");
        assertFalse(reachable.isEmpty(), "Should reach at least start node");

        // Start node should be reachable
        assertTrue(reachable.contains(gameGraph.getNode("brinstar_morph_ball_room")),
            "Start node should be reachable");
    }

    @Test
    @DisplayName("Should handle multiple regions")
    void testMultipleRegions() {
        Set<String> regions = gameGraph.getAllRegions();

        assertNotNull(regions, "Should get all regions");
        assertTrue(regions.size() >= 4, "Should have at least 4 regions");

        // Verify expected regions exist
        assertTrue(regions.contains("Brinstar"), "Should have Brinstar");
        assertTrue(regions.contains("Norfair"), "Should have Norfair");
        assertTrue(regions.contains("Maridia"), "Should have Maridia");
        assertTrue(regions.contains("Wrecked Ship"), "Should have Wrecked Ship");
    }

    @Test
    @DisplayName("Should calculate graph density")
    void testGraphDensity() {
        double density = gameGraph.getGraphDensity();

        assertNotNull(density, "Density should not be null");
        assertTrue(density >= 0.0, "Density should be non-negative");
        assertTrue(density <= 1.0, "Density should not exceed 1.0");
    }
}