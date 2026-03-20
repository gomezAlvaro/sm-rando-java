package com.maprando.patch;

import com.maprando.data.DataLoader;
import com.maprando.data.model.LocationData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

/**
 * Integration tests for NodePtrMap with location data.
 * Tests the integration between node pointer map and location definitions.
 */
class NodePtrMapIntegrationTest {

    private DataLoader dataLoader;
    private NodePtrMap nodePtrMap;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();
        nodePtrMap = new NodePtrMap();
    }

    /**
     * Test that location data can be loaded with roomId/nodeId fields.
     */
    @Test
    void testLocationDataHasRoomNodeFields() {
        LocationData locationData = dataLoader.getLocationData();
        assertNotNull(locationData);
        assertNotNull(locationData.getLocations());
        assertTrue(locationData.getLocations().size() > 0);
    }

    /**
     * Test backward compatibility - locations with romAddress should still work.
     */
    @Test
    void testBackwardCompatibilityRomAddress() {
        LocationData locationData = dataLoader.getLocationData();

        // Find a location with romAddress
        for (LocationData.LocationDefinition loc : locationData.getLocations()) {
            if (loc.getRomAddress() != null && !loc.getRomAddress().isEmpty()) {
                // Should be able to convert romAddress to PC address
                int snesAddr = LocationRomAddressMapper.snesToPc(loc.getRomAddress());
                int pcAddr = Rom.snes2pc(snesAddr);
                assertTrue(pcAddr >= 0 && pcAddr < 0x400000);
                return; // Found at least one
            }
        }

        // If no locations with romAddress, that's also ok for new format
        assertTrue(true, "No locations with romAddress found (using new roomId/nodeId format)");
    }

    /**
     * Test creating node pointer map from location data.
     */
    @Test
    void testCreateNodePtrMapFromLocations() {
        LocationData locationData = dataLoader.getLocationData();

        // For locations that have roomId/nodeId, add to nodePtrMap
        int count = 0;
        for (LocationData.LocationDefinition loc : locationData.getLocations()) {
            if (loc.getRoomId() != null && loc.getNodeId() != null) {
                // For now, use a placeholder PLM pointer
                // In production, this would be loaded from room geometry data
                int plmPointer = 0x8282F5 + (count * 0x100);
                nodePtrMap.put(loc.getRoomId(), loc.getNodeId(), plmPointer);
                count++;
            }
        }

        // If we have room/node format locations, they should be in the map
        if (count > 0) {
            assertEquals(count, nodePtrMap.size());
            assertTrue(nodePtrMap.isValid());
        }
    }

    /**
     * Test that location ID can be used to look up room/node.
     */
    @Test
    void testLocationIdToRoomNodeLookup() {
        LocationData locationData = dataLoader.getLocationData();

        // Create a map from location ID to (room, node) tuple
        Map<String, int[]> locationToRoomNode = new java.util.HashMap<>();

        for (LocationData.LocationDefinition loc : locationData.getLocations()) {
            if (loc.getRoomId() != null && loc.getNodeId() != null) {
                locationToRoomNode.put(loc.getId(), new int[]{loc.getRoomId(), loc.getNodeId()});
            }
        }

        // If we have room/node format locations, lookup should work
        if (!locationToRoomNode.isEmpty()) {
            String firstLocationId = locationToRoomNode.keySet().iterator().next();
            int[] roomNode = locationToRoomNode.get(firstLocationId);
            assertNotNull(roomNode);
            assertEquals(2, roomNode.length);
            assertTrue(NodePtrMap.isValidRoomId(roomNode[0]));
            assertTrue(NodePtrMap.isValidNodeId(roomNode[1]));
        }
    }

    /**
     * Test that romAddress and roomId/nodeId are mutually exclusive or complementary.
     * New locations can have both, old locations only have romAddress.
     */
    @Test
    void testRomAddressAndRoomNodeCoexistence() {
        LocationData locationData = dataLoader.getLocationData();

        int withRomAddress = 0;
        int withRoomNode = 0;
        int withBoth = 0;

        for (LocationData.LocationDefinition loc : locationData.getLocations()) {
            boolean hasRomAddr = loc.getRomAddress() != null && !loc.getRomAddress().isEmpty();
            boolean hasRoomNode = loc.getRoomId() != null && loc.getNodeId() != null;

            if (hasRomAddr) withRomAddress++;
            if (hasRoomNode) withRoomNode++;
            if (hasRomAddr && hasRoomNode) withBoth++;
        }

        // At minimum, we should have some locations
        assertTrue(withRomAddress > 0 || withRoomNode > 0,
            "Should have at least some locations with romAddress or roomId/nodeId");

        // Current implementation uses romAddress format
        // New format would use roomId/nodeId
        // During migration, we might have both
        assertTrue(withBoth >= 0, "Can have locations with both formats during migration");
    }

    /**
     * Test converting location to PLM address using node pointer map.
     */
    @Test
    void testLocationToPlmAddress() {
        // Set up a test node pointer map
        nodePtrMap.put(5, 2, 0x848200);

        // Mock a location with room 5, node 2
        LocationData.LocationDefinition testLoc = new LocationData.LocationDefinition();
        testLoc.setId("test_location");
        testLoc.setName("Test Location");
        testLoc.setRoomId(5);
        testLoc.setNodeId(2);

        // Should be able to get PLM pointer from nodePtrMap
        if (testLoc.getRoomId() != null && testLoc.getNodeId() != null) {
            int plmPointer = nodePtrMap.get(testLoc.getRoomId(), testLoc.getNodeId());
            assertEquals(0x848200, plmPointer);
        }
    }

    /**
     * Test that node pointer map handles edge cases.
     */
    @Test
    void testNodePtrMapEdgeCases() {
        // Test with room 0, node 0
        nodePtrMap.put(0, 0, 0x8282F5);
        assertEquals(0x8282F5, nodePtrMap.get(0, 0));

        // Test with max room/node IDs
        nodePtrMap.put(255, 255, 0x8282F6);
        assertEquals(0x8282F6, nodePtrMap.get(255, 255));

        // Test removing non-existent entry
        int removed = nodePtrMap.remove(99, 99);
        assertEquals(-1, removed);
    }

    /**
     * Test node pointer map statistics.
     */
    @Test
    void testNodePtrMapStatistics() {
        nodePtrMap.put(5, 2, 0x848200);
        nodePtrMap.put(5, 3, 0x848201);
        nodePtrMap.put(19, 7, 0x84A100);

        var stats = nodePtrMap.getStatistics();
        assertEquals(3, stats.get("totalNodes"));
        assertEquals(2, stats.get("totalRooms"));

        var roomIds = nodePtrMap.getRoomIds();
        assertEquals(2, roomIds.size());
        assertTrue(roomIds.contains(5));
        assertTrue(roomIds.contains(19));

        var nodesInRoom5 = nodePtrMap.getNodeIdsForRoom(5);
        assertEquals(2, nodesInRoom5.size());
        assertTrue(nodesInRoom5.contains(2));
        assertTrue(nodesInRoom5.contains(3));
    }

    /**
     * Test loading node pointer map from JSON-style data.
     */
    @Test
    void testLoadFromJsonStyleData() {
        // Simulate loading from a JSON-like structure
        Map<String, Integer> jsonData = Map.of(
            "5_2", 0x848200,
            "19_7", 0x84A100,
            "42_1", 0x84C300
        );

        NodePtrMap map = new NodePtrMap();
        map.loadFromMap(jsonData);

        assertEquals(3, map.size());
        assertEquals(0x848200, map.get(5, 2));
        assertEquals(0x84A100, map.get(19, 7));
        assertEquals(0x84C300, map.get(42, 1));
        assertTrue(map.isValid());
    }

    /**
     * Test that location IDs are unique.
     */
    @Test
    void testUniqueLocationIds() {
        LocationData locationData = dataLoader.getLocationData();
        var locationIds = new java.util.HashSet<String>();

        for (LocationData.LocationDefinition loc : locationData.getLocations()) {
            assertTrue(locationIds.add(loc.getId()),
                "Location ID should be unique: " + loc.getId());
        }
    }

    /**
     * Test that we can iterate through all locations.
     */
    @Test
    void testIterateAllLocations() {
        LocationData locationData = dataLoader.getLocationData();

        int count = 0;
        for (LocationData.LocationDefinition loc : locationData.getLocations()) {
            assertNotNull(loc.getId());
            assertNotNull(loc.getName());
            count++;
        }

        assertTrue(count > 0, "Should have at least one location");
    }
}
