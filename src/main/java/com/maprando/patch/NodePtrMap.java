package com.maprando.patch;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Node pointer map for Super Metroid ROM item location addressing.
 *
 * Maps (room_id, node_id) tuples to PLM (Programmable Logic Module) pointer addresses.
 * This system is used by the Rust MapRandomizer project for precise item placement.
 *
 * The node pointer map replaces hardcoded ROM addresses with a flexible (room, node) addressing system.
 * Each room in Super Metroid has multiple item nodes, each with a unique PLM pointer.
 *
 * Reference: rust/maprando/src/patch.rs: node_ptr_map
 */
public class NodePtrMap {

    /**
     * Internal storage using string keys "room_node" for efficient lookup.
     */
    private final Map<String, Integer> nodePtrMap;

    /**
     * Reverse lookup: room_id → set of node_ids
     */
    private final Map<Integer, Set<Integer>> roomToNodesMap;

    /**
     * Creates an empty node pointer map.
     */
    public NodePtrMap() {
        this.nodePtrMap = new HashMap<>();
        this.roomToNodesMap = new HashMap<>();
    }

    /**
     * Creates a node pointer map with initial capacity.
     *
     * @param initialCapacity Initial capacity for the map
     */
    public NodePtrMap(int initialCapacity) {
        this.nodePtrMap = new HashMap<>(initialCapacity);
        this.roomToNodesMap = new HashMap<>(initialCapacity);
    }

    /**
     * Adds a node pointer entry.
     *
     * @param roomId Room ID (0-255)
     * @param nodeId Node ID (0-255)
     * @param plmPointer PLM pointer address (SNES format)
     */
    public void put(int roomId, int nodeId, int plmPointer) {
        String key = createLocationKey(roomId, nodeId);
        nodePtrMap.put(key, plmPointer);

        // Update reverse lookup
        roomToNodesMap.computeIfAbsent(roomId, k -> new HashSet<>()).add(nodeId);
    }

    /**
     * Gets the PLM pointer for a given room and node.
     *
     * @param roomId Room ID
     * @param nodeId Node ID
     * @return PLM pointer address, or -1 if not found
     */
    public int get(int roomId, int nodeId) {
        String key = createLocationKey(roomId, nodeId);
        Integer ptr = nodePtrMap.get(key);
        return ptr != null ? ptr : -1;
    }

    /**
     * Checks if the map contains an entry for the given room and node.
     *
     * @param roomId Room ID
     * @param nodeId Node ID
     * @return true if entry exists, false otherwise
     */
    public boolean containsKey(int roomId, int nodeId) {
        String key = createLocationKey(roomId, nodeId);
        return nodePtrMap.containsKey(key);
    }

    /**
     * Removes an entry from the map.
     *
     * @param roomId Room ID
     * @param nodeId Node ID
     * @return the previous PLM pointer, or -1 if not found
     */
    public int remove(int roomId, int nodeId) {
        String key = createLocationKey(roomId, nodeId);
        Integer ptr = nodePtrMap.remove(key);

        if (ptr != null) {
            // Update reverse lookup
            Set<Integer> nodes = roomToNodesMap.get(roomId);
            if (nodes != null) {
                nodes.remove(nodeId);
                if (nodes.isEmpty()) {
                    roomToNodesMap.remove(roomId);
                }
            }
            return ptr;
        }
        return -1;
    }

    /**
     * Clears all entries from the map.
     */
    public void clear() {
        nodePtrMap.clear();
        roomToNodesMap.clear();
    }

    /**
     * Gets the number of entries in the map.
     *
     * @return map size
     */
    public int size() {
        return nodePtrMap.size();
    }

    /**
     * Checks if the map is empty.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return nodePtrMap.isEmpty();
    }

    /**
     * Loads entries from a map with string keys "room_node".
     *
     * @param data Map of location keys to PLM pointers
     */
    public void loadFromMap(Map<String, Integer> data) {
        if (data == null) {
            return;
        }

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int[] ids = parseLocationKey(entry.getKey());
            if (ids != null) {
                put(ids[0], ids[1], entry.getValue());
            }
        }
    }

    /**
     * Converts the node pointer map to a standard map with string keys.
     *
     * @return Map of "room_node" → PLM pointer
     */
    public Map<String, Integer> toMap() {
        return new HashMap<>(nodePtrMap);
    }

    /**
     * Gets all room IDs in the map.
     *
     * @return Set of room IDs
     */
    public Set<Integer> getRoomIds() {
        return new HashSet<>(roomToNodesMap.keySet());
    }

    /**
     * Gets all node IDs for a specific room.
     *
     * @param roomId Room ID
     * @return Set of node IDs, or empty set if room not found
     */
    public Set<Integer> getNodeIdsForRoom(int roomId) {
        Set<Integer> nodes = roomToNodesMap.get(roomId);
        return nodes != null ? new HashSet<>(nodes) : new HashSet<>();
    }

    /**
     * Gets statistics about the node pointer map.
     *
     * @return Map containing "totalNodes" and "totalRooms" counts
     */
    public Map<String, Integer> getStatistics() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalNodes", nodePtrMap.size());
        stats.put("totalRooms", roomToNodesMap.size());
        return stats;
    }

    /**
     * Creates a location key string from room and node IDs.
     * Format: "room_node"
     *
     * @param roomId Room ID
     * @param nodeId Node ID
     * @return Location key string
     */
    public static String createLocationKey(int roomId, int nodeId) {
        return roomId + "_" + nodeId;
    }

    /**
     * Parses a location key string into room and node IDs.
     *
     * @param key Location key string (format: "room_node")
     * @return int array of [room_id, node_id], or null if invalid
     */
    public static int[] parseLocationKey(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }

        String[] parts = key.split("_");
        if (parts.length != 2) {
            return null;
        }

        try {
            int roomId = Integer.parseInt(parts[0]);
            int nodeId = Integer.parseInt(parts[1]);
            return new int[]{roomId, nodeId};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Validates if a room ID is within valid range.
     *
     * @param roomId Room ID to validate
     * @return true if valid (0-255), false otherwise
     */
    public static boolean isValidRoomId(int roomId) {
        return roomId >= 0 && roomId <= 255;
    }

    /**
     * Validates if a node ID is within valid range.
     *
     * @param nodeId Node ID to validate
     * @return true if valid (0-255), false otherwise
     */
    public static boolean isValidNodeId(int nodeId) {
        return nodeId >= 0 && nodeId <= 255;
    }

    /**
     * Validates if a PLM pointer is a valid SNES address.
     * SNES HiROM addresses are in the range 0x800000-0xFFFFFF.
     *
     * @param plmPointer PLM pointer to validate
     * @return true if valid SNES address, false otherwise
     */
    public static boolean isValidPlmPointer(int plmPointer) {
        return plmPointer >= 0x800000 && plmPointer <= 0xFFFFFF;
    }

    /**
     * Checks if all entries in the map have valid IDs and pointers.
     *
     * @return true if all entries are valid, false otherwise
     */
    public boolean isValid() {
        for (Map.Entry<String, Integer> entry : nodePtrMap.entrySet()) {
            int[] ids = parseLocationKey(entry.getKey());
            if (ids == null) {
                return false;
            }
            if (!isValidRoomId(ids[0]) || !isValidNodeId(ids[1])) {
                return false;
            }
            if (!isValidPlmPointer(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the internal node pointer map (for debugging/testing).
     *
     * @return Copy of internal map
     */
    public Map<String, Integer> getInternalMap() {
        return new HashMap<>(nodePtrMap);
    }

    /**
     * Gets the internal room-to-nodes map (for debugging/testing).
     *
     * @return Copy of internal room-to-nodes mapping
     */
    public Map<Integer, Set<Integer>> getRoomToNodesMap() {
        Map<Integer, Set<Integer>> copy = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : roomToNodesMap.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }

    @Override
    public String toString() {
        return "NodePtrMap{" +
                "size=" + size() +
                ", rooms=" + roomToNodesMap.size() +
                '}';
    }
}
