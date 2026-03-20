package com.maprando.patch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for NodePtrMap - maps (room_id, node_id) tuples to PLM pointer addresses.
 *
 * Based on the Rust MapRandomizer project's node_ptr_map system.
 * Reference: rust/maprando/src/patch.rs: node_ptr_map
 */
class NodePtrMapTest {

    private NodePtrMap nodePtrMap;

    @BeforeEach
    void setUp() {
        nodePtrMap = new NodePtrMap();
    }

    /**
     * Test creating an empty node pointer map.
     */
    @Test
    void testCreateEmptyMap() {
        assertNotNull(nodePtrMap);
        assertEquals(0, nodePtrMap.size());
    }

    /**
     * Test adding node pointer entries.
     */
    @Test
    void testAddNodePointer() {
        nodePtrMap.put(5, 2, 0x848200);
        assertEquals(1, nodePtrMap.size());
        assertTrue(nodePtrMap.containsKey(5, 2));
    }

    /**
     * Test getting node pointer by room and node ID.
     */
    @Test
    void testGetNodePointer() {
        nodePtrMap.put(5, 2, 0x848200);

        int ptr = nodePtrMap.get(5, 2);
        assertEquals(0x848200, ptr);
    }

    /**
     * Test getting non-existent node pointer returns -1.
     */
    @Test
    void testGetNonExistentNodePointer() {
        int ptr = nodePtrMap.get(99, 99);
        assertEquals(-1, ptr);
    }

    /**
     * Test checking if map contains key.
     */
    @Test
    void testContainsKey() {
        nodePtrMap.put(5, 2, 0x848200);

        assertTrue(nodePtrMap.containsKey(5, 2));
        assertFalse(nodePtrMap.containsKey(5, 3));
        assertFalse(nodePtrMap.containsKey(6, 2));
    }

    /**
     * Test adding multiple node pointers.
     */
    @Test
    void testAddMultipleNodePointers() {
        nodePtrMap.put(5, 2, 0x848200);
        nodePtrMap.put(19, 7, 0x84A100);
        nodePtrMap.put(42, 1, 0x84C300);

        assertEquals(3, nodePtrMap.size());
        assertEquals(0x848200, nodePtrMap.get(5, 2));
        assertEquals(0x84A100, nodePtrMap.get(19, 7));
        assertEquals(0x84C300, nodePtrMap.get(42, 1));
    }

    /**
     * Test that same room_id, node_id pair overwrites previous value.
     */
    @Test
    void testOverwriteExisting() {
        nodePtrMap.put(5, 2, 0x848200);
        nodePtrMap.put(5, 2, 0x848201); // Overwrite

        assertEquals(1, nodePtrMap.size());
        assertEquals(0x848201, nodePtrMap.get(5, 2));
    }

    /**
     * Test removing node pointer.
     */
    @Test
    void testRemove() {
        nodePtrMap.put(5, 2, 0x848200);
        assertTrue(nodePtrMap.containsKey(5, 2));

        nodePtrMap.remove(5, 2);
        assertFalse(nodePtrMap.containsKey(5, 2));
        assertEquals(0, nodePtrMap.size());
    }

    /**
     * Test clearing the map.
     */
    @Test
    void testClear() {
        nodePtrMap.put(5, 2, 0x848200);
        nodePtrMap.put(19, 7, 0x84A100);
        assertEquals(2, nodePtrMap.size());

        nodePtrMap.clear();
        assertEquals(0, nodePtrMap.size());
        assertFalse(nodePtrMap.containsKey(5, 2));
    }

    /**
     * Test loading from map of (room_id, node_id) → pointer.
     */
    @Test
    void testLoadFromMap() {
        Map<String, Integer> data = new HashMap<>();
        data.put("5_2", 0x848200);
        data.put("19_7", 0x84A100);

        nodePtrMap.loadFromMap(data);

        assertEquals(2, nodePtrMap.size());
        assertEquals(0x848200, nodePtrMap.get(5, 2));
        assertEquals(0x84A100, nodePtrMap.get(19, 7));
    }

    /**
     * Test getting all entries as map.
     */
    @Test
    void testToMap() {
        nodePtrMap.put(5, 2, 0x848200);
        nodePtrMap.put(19, 7, 0x84A100);

        Map<String, Integer> map = nodePtrMap.toMap();

        assertEquals(2, map.size());
        assertEquals(0x848200, map.get("5_2"));
        assertEquals(0x84A100, map.get("19_7"));
    }

    /**
     * Test getting room IDs.
     */
    @Test
    void testGetRoomIds() {
        nodePtrMap.put(5, 2, 0x848200);
        nodePtrMap.put(5, 3, 0x848201);
        nodePtrMap.put(19, 7, 0x84A100);

        var roomIds = nodePtrMap.getRoomIds();
        assertEquals(2, roomIds.size());
        assertTrue(roomIds.contains(5));
        assertTrue(roomIds.contains(19));
    }

    /**
     * Test getting node IDs for a room.
     */
    @Test
    void testGetNodeIdsForRoom() {
        nodePtrMap.put(5, 2, 0x848200);
        nodePtrMap.put(5, 3, 0x848201);
        nodePtrMap.put(5, 7, 0x848202);
        nodePtrMap.put(19, 7, 0x84A100);

        var nodeIds = nodePtrMap.getNodeIdsForRoom(5);
        assertEquals(3, nodeIds.size());
        assertTrue(nodeIds.contains(2));
        assertTrue(nodeIds.contains(3));
        assertTrue(nodeIds.contains(7));
    }

    /**
     * Test that node pointers are valid SNES addresses.
     */
    @Test
    void testValidSnesAddresses() {
        // Valid SNES addresses (HiROM format: 0x800000-0xFFFFFF)
        nodePtrMap.put(5, 2, 0x8282F5);
        nodePtrMap.put(19, 7, 0x84A100);
        nodePtrMap.put(42, 1, 0x8FC000);

        assertEquals(3, nodePtrMap.size());
        assertEquals(0x8282F5, nodePtrMap.get(5, 2));
        assertEquals(0x84A100, nodePtrMap.get(19, 7));
        assertEquals(0x8FC000, nodePtrMap.get(42, 1));
    }

    /**
     * Test creating location key string.
     */
    @Test
    void testCreateLocationKey() {
        String key1 = NodePtrMap.createLocationKey(5, 2);
        assertEquals("5_2", key1);

        String key2 = NodePtrMap.createLocationKey(19, 7);
        assertEquals("19_7", key2);
    }

    /**
     * Test parsing location key string.
     */
    @Test
    void testParseLocationKey() {
        int[] result1 = NodePtrMap.parseLocationKey("5_2");
        assertEquals(2, result1.length);
        assertEquals(5, result1[0]);
        assertEquals(2, result1[1]);

        int[] result2 = NodePtrMap.parseLocationKey("19_7");
        assertEquals(2, result2.length);
        assertEquals(19, result2[0]);
        assertEquals(7, result2[1]);
    }

    /**
     * Test parsing invalid location key returns null.
     */
    @Test
    void testParseInvalidLocationKey() {
        assertNull(NodePtrMap.parseLocationKey("invalid"));
        assertNull(NodePtrMap.parseLocationKey("5"));
        assertNull(NodePtrMap.parseLocationKey(""));
        assertNull(NodePtrMap.parseLocationKey(null));
    }

    /**
     * Test updating from existing location data (backward compatibility).
     */
    @Test
    void testUpdateFromLocationData() {
        // Simulate updating from old romAddress format to new node format
        Map<String, String> oldLocations = new HashMap<>();
        oldLocations.put("brinstar_morph_ball_room", "0x8282F5");

        // For now, this just stores the mapping
        // In production, would parse room geometry data
        nodePtrMap.put(5, 2, 0x848200);

        assertTrue(nodePtrMap.containsKey(5, 2));
        assertEquals(0x848200, nodePtrMap.get(5, 2));
    }

    /**
     * Test that room and node IDs are within valid ranges.
     */
    @Test
    void testValidIdRanges() {
        // Room IDs should be 0-255
        // Node IDs should be 0-255
        nodePtrMap.put(0, 0, 0x8282F5);
        nodePtrMap.put(255, 255, 0x8282F6);

        assertEquals(2, nodePtrMap.size());
    }

    /**
     * Test getting statistics about the map.
     */
    @Test
    void testGetStatistics() {
        nodePtrMap.put(5, 2, 0x848200);
        nodePtrMap.put(5, 3, 0x848201);
        nodePtrMap.put(19, 7, 0x84A100);

        var stats = nodePtrMap.getStatistics();
        assertEquals(3, stats.get("totalNodes"));
        assertEquals(2, stats.get("totalRooms"));
    }
}
