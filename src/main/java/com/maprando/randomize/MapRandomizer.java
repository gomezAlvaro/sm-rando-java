package com.maprando.randomize;

import com.maprando.data.DataLoader;
import com.maprando.data.model.RoomGeometry;
import com.maprando.data.model.VanillaMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates randomized map layouts by swapping room positions.
 *
 * This shuffles:
 * 1. Room positions - entire rooms swap places on the map grid
 * 2. Area assignments (Crateria ↔ Norfair, Brinstar ↔ Maridia, etc.)
 * 3. Subarea assignments (0 ↔ 1 within each area)
 * 4. Subsubarea assignments (0 ↔ 1 within each subarea)
 *
 * Door connections stay the same (based on room IDs) - only room positions change.
 */
public class MapRandomizer {

    private final DataLoader dataLoader;
    private final Random random;
    private final String seed;
    private final String mapPool;

    // Area IDs: 0=Crateria, 1=Brinstar, 2=Norfair, 3=Maridia, 4=Wrecked Ship, 5=Tourian
    private static final int NUM_AREAS = 6;
    private static final int NUM_SUBAREAS = 2;
    private static final int NUM_SUBSUBAREAS = 2;

    /**
     * Record to track a door along with its source room ID.
     */
    private record DoorWithSource(int sourceRoomId, MapData.DoorInfo door) {}

    /**
     * Creates a new MapRandomizer.
     *
     * @param seed the seed string for reproducible randomness
     * @param dataLoader the data loader containing room geometry and vanilla map
     * @param mapPool the map pool to use ("small", "standard", or "wild")
     */
    public MapRandomizer(String seed, DataLoader dataLoader, String mapPool) {
        this.seed = seed;
        this.dataLoader = dataLoader;
        this.mapPool = mapPool != null ? mapPool : "standard";
        // Create seeded random for reproducibility
        this.random = new Random(seed.hashCode());
    }

    /**
     * Creates a new MapRandomizer with default map pool.
     *
     * @param seed the seed string for reproducible randomness
     * @param dataLoader the data loader containing room geometry and vanilla map
     */
    public MapRandomizer(String seed, DataLoader dataLoader) {
        this(seed, dataLoader, "standard");
    }

    /**
     * Generate a randomized map by placing rooms in a simple connected pattern.
     * Places all rooms in a line/snake pattern to ensure full connectivity.
     *
     * Uses ORIGINAL DOOR DATA from room_geometry.json (via exit_ptr/entrance_ptr pairs).
     * This preserves the vanilla game's door topology - rooms connect the same way they do in vanilla.
     *
     * @return MapData with connected room positions centered on the map
     */
    public MapData generateMap() {
        // Try to use ML-generated maps first
        try {
            com.maprando.data.MLMapLoader mlLoader = new com.maprando.data.MLMapLoader();
            com.maprando.data.MLMapLoader.PreGeneratedMLMap mlMap = mlLoader.loadRandomMap(seed, mapPool);

            if (mlMap != null && mlMap.getRoomCount() >= 150) {
                System.out.println("Using ML-generated map with " + mlMap.getRoomCount() + " rooms");
                return useMLGeneratedMap(mlMap);
            }
        } catch (Exception e) {
            System.out.println("Could not load ML map, falling back to algorithmic placement: " + e.getMessage());
        }

        // Fallback to algorithmic placement
        List<MapData.RoomMapping> allRooms = loadRoomPositions();

        // Use topology-first approach: start with room 8 and expand by connecting doors
        List<MapData.RoomMapping> placedRooms = buildMapByDoorConnections(allRooms);

        // Center the map
        placedRooms = centerMap(placedRooms);

        // Update door connection targets based on physical adjacency
        List<MapData.RoomMapping> roomsWithDoors = updateDoorConnectionTargetsFromGeometry(placedRooms, List.of());

        // Keep original area assignments (no area shuffling)
        List<Integer> areas = placedRooms.stream()
                .map(MapData.RoomMapping::originalArea)
                .collect(Collectors.toList());
        List<Integer> subareas = placedRooms.stream()
                .map(MapData.RoomMapping::subarea)
                .collect(Collectors.toList());
        List<Integer> subsubareas = placedRooms.stream()
                .map(MapData.RoomMapping::subsubarea)
                .collect(Collectors.toList());

        // Build door connections from updated rooms
        List<MapData.DoorConnection> doors = buildDoorConnectionsFromRoomData(roomsWithDoors);

        return new MapData(
                seed,
                roomsWithDoors,
                doors,
                areas,
                subareas,
                subsubareas,
                Map.of(),  // No area mapping
                Map.of()   // No subarea mapping
        );
    }

    /**
     * Use an ML-generated map for optimal room placement.
     */
    private MapData useMLGeneratedMap(com.maprando.data.MLMapLoader.PreGeneratedMLMap mlMap) {
        List<MapData.RoomMapping> allRooms = loadRoomPositions();
        Map<Integer, MapData.RoomMapping> roomMap = new HashMap<>();
        for (MapData.RoomMapping room : allRooms) {
            roomMap.put(room.roomId(), room);
        }

        // Create rooms with ML-generated positions
        List<MapData.RoomMapping> placedRooms = new ArrayList<>();
        for (int i = 0; i < mlMap.getRoomCount(); i++) {
            Integer roomId = mlMap.getRoomIds().get(i);
            MapData.RoomMapping original = roomMap.get(roomId);

            if (original != null) {
                int[] position = mlMap.getRoomPosition(i);

                // Convert tiles to ArrayList
                List<List<Integer>> roomTiles = new ArrayList<>();
                if (original.tiles() != null) {
                    for (List<Integer> row : original.tiles()) {
                        roomTiles.add(new ArrayList<>(row));
                    }
                }

                placedRooms.add(new MapData.RoomMapping(
                        roomId,
                        position[0],  // ML-generated X position
                        position[1],  // ML-generated Y position
                        original.width(),
                        original.height(),
                        roomTiles,
                        original.doors(),
                        original.originalArea(),
                        original.originalArea(),
                        original.subarea(),
                        original.subsubarea(),
                        original.roomName()
                ));
            }
        }

        // Center the map
        placedRooms = centerMap(placedRooms);

        // Update door connection targets based on physical adjacency
        List<MapData.RoomMapping> roomsWithDoors = updateDoorConnectionTargetsFromGeometry(placedRooms, List.of());

        // Keep original area assignments
        List<Integer> areas = placedRooms.stream()
                .map(MapData.RoomMapping::originalArea)
                .collect(Collectors.toList());
        List<Integer> subareas = placedRooms.stream()
                .map(MapData.RoomMapping::subarea)
                .collect(Collectors.toList());
        List<Integer> subsubareas = placedRooms.stream()
                .map(MapData.RoomMapping::subsubarea)
                .collect(Collectors.toList());

        // Build door connections from updated rooms
        List<MapData.DoorConnection> doors = buildDoorConnectionsFromRoomData(roomsWithDoors);

        return new MapData(
                seed,
                roomsWithDoors,
                doors,
                areas,
                subareas,
                subsubareas,
                Map.of(),
                Map.of()
        );
    }

    /**
     * Creates a grid layout that places all rooms in one connected cluster.
     * Does NOT use vanilla door connections for topology - ensures all rooms connect together.
     */
    private List<MapData.RoomMapping> createTopologyPreservingLayout(
            List<MapData.RoomMapping> vanillaRooms,
            List<MapData.DoorConnection> vanillaDoors) {

        // Create room map
        Map<Integer, MapData.RoomMapping> roomMap = new HashMap<>();
        for (MapData.RoomMapping room : vanillaRooms) {
            roomMap.put(room.roomId(), room);
        }

        // Track which rooms have been placed
        Set<Integer> placedRooms = new HashSet<>();
        Map<Integer, Point> roomPositions = new HashMap<>();

        // Track occupied grid positions
        Set<String> occupiedPositions = new HashSet<>();

        // Place ALL rooms using BFS from starting room
        // We don't use vanilla door connections - we just place all rooms in one cluster
        int startRoomId = vanillaRooms.get(0).roomId();
        placeAllRoomsInCluster(startRoomId, roomMap, placedRooms, roomPositions, occupiedPositions, vanillaRooms);

        // Create result list
        List<MapData.RoomMapping> gridRooms = new ArrayList<>();
        for (MapData.RoomMapping original : vanillaRooms) {
            Point pos = roomPositions.get(original.roomId());
            if (pos != null) {
                gridRooms.add(new MapData.RoomMapping(
                        original.roomId(),
                        pos.x,
                        pos.y,
                        original.width(),
                        original.height(),
                        original.tiles(),
                        original.doors(),
                        original.originalArea(),
                        original.originalArea(),
                        original.subarea(),
                        original.subsubarea(),
                        original.roomName()
                ));
            }
        }

        return gridRooms;
    }

    /**
     * Finds isolated rooms (with no adjacent neighbors) and repositions them.
     * Ensures every room has at least one adjacent neighbor so doors can be created.
     */
    private void fixIsolatedRooms(
            List<MapData.RoomMapping> vanillaRooms,
            Map<Integer, MapData.RoomMapping> roomMap,
            Set<Integer> placedRooms,
            Map<Integer, Point> roomPositions,
            Set<String> occupiedPositions
    ) {
        // Build a map of room positions
        Map<Integer, MapData.RoomMapping> placedRoomsMap = new HashMap<>();
        for (Integer roomId : placedRooms) {
            Point pos = roomPositions.get(roomId);
            MapData.RoomMapping room = roomMap.get(roomId);
            if (pos != null && room != null) {
                placedRoomsMap.put(roomId, new MapData.RoomMapping(
                        roomId,
                        pos.x,
                        pos.y,
                        room.width(),
                        room.height(),
                        room.tiles(),
                        room.doors(),
                        room.originalArea(),
                        room.originalArea(),
                        room.subarea(),
                        room.subsubarea(),
                        room.roomName()
                ));
            }
        }

        // Check each room for isolation
        for (Integer roomId : placedRooms) {
            MapData.RoomMapping room = placedRoomsMap.get(roomId);
            if (room == null) continue;

            // Check if this room has any adjacent neighbors
            boolean hasNeighbor = false;
            for (MapData.RoomMapping other : placedRoomsMap.values()) {
                if (other.roomId() == roomId) continue;

                String relationship = getRoomRelationship(room, other);
                if (relationship != null) {
                    hasNeighbor = true;
                    break;
                }
            }

            if (!hasNeighbor) {
                // Room is isolated - reposition it adjacent to another room
                repositionIsolatedRoom(room, placedRoomsMap, roomPositions, occupiedPositions);
            }
        }
    }

    /**
     * Repositions an isolated room to be adjacent to another room.
     */
    private void repositionIsolatedRoom(
            MapData.RoomMapping isolatedRoom,
            Map<Integer, MapData.RoomMapping> placedRoomsMap,
            Map<Integer, Point> roomPositions,
            Set<String> occupiedPositions
    ) {
        // Remove old position from occupied set
        markUnoccupied(isolatedRoom, roomPositions.get(isolatedRoom.roomId()).x,
                      roomPositions.get(isolatedRoom.roomId()).y, occupiedPositions);

        // Try to place adjacent to each room until we find a valid position
        for (MapData.RoomMapping other : placedRoomsMap.values()) {
            if (other.roomId() == isolatedRoom.roomId()) continue;

            Point otherPos = roomPositions.get(other.roomId());

            // Try 4 cardinal directions
            List<Point> candidates = new ArrayList<>(List.of(
                new Point(otherPos.x - isolatedRoom.width(), otherPos.y),
                new Point(otherPos.x + other.width(), otherPos.y),
                new Point(otherPos.x, otherPos.y - isolatedRoom.height()),
                new Point(otherPos.x, otherPos.y + other.height())
            ));

            Collections.shuffle(candidates, random);

            for (Point candidate : candidates) {
                if (canPlaceRoom(isolatedRoom, candidate.x, candidate.y, occupiedPositions)) {
                    // Found a valid position - update room position
                    roomPositions.put(isolatedRoom.roomId(), candidate);
                    markOccupied(isolatedRoom, candidate.x, candidate.y, occupiedPositions);
                    return;
                }
            }
        }

        // If we still couldn't place it, put it back at original position
        Point originalPos = roomPositions.get(isolatedRoom.roomId());
        markOccupied(isolatedRoom, originalPos.x, originalPos.y, occupiedPositions);
    }

    /**
     * Unmarks a room's tiles as occupied (removes them from occupied set).
     */
    private void markUnoccupied(MapData.RoomMapping room, int roomX, int roomY, Set<String> occupiedPositions) {
        for (int dx = 0; dx < room.width(); dx++) {
            for (int dy = 0; dy < room.height(); dy++) {
                occupiedPositions.remove((roomX + dx) + "," + (roomY + dy));
            }
        }
    }

    /**
     * Places ALL rooms in one connected cluster using BFS.
     * Ensures all rooms are placed and connected together.
     */
    private void placeAllRoomsInCluster(
            int startRoomId,
            Map<Integer, MapData.RoomMapping> roomMap,
            Set<Integer> placedRooms,
            Map<Integer, Point> roomPositions,
            Set<String> occupiedPositions,
            List<MapData.RoomMapping> allRooms
    ) {
        // Shuffle all rooms for randomness
        List<MapData.RoomMapping> shuffledRooms = new ArrayList<>(allRooms);
        Collections.shuffle(shuffledRooms, random);

        Queue<Integer> queue = new LinkedList<>();
        queue.add(startRoomId);

        // Place starting room at origin
        roomPositions.put(startRoomId, new Point(0, 0));
        placedRooms.add(startRoomId);
        markOccupied(roomMap.get(startRoomId), 0, 0, occupiedPositions);

        while (!queue.isEmpty() || placedRooms.size() < allRooms.size()) {
            if (!queue.isEmpty()) {
                // Normal BFS: try to place rooms adjacent to current room
                int currentRoomId = queue.poll();
                MapData.RoomMapping currentRoom = roomMap.get(currentRoomId);
                Point currentPos = roomPositions.get(currentRoomId);

                // Try to place unplaced rooms adjacent to current room
                for (MapData.RoomMapping otherRoom : shuffledRooms) {
                    if (placedRooms.contains(otherRoom.roomId())) {
                        continue;
                    }

                    Point adjacentPos = findAdjacentPosition(
                            currentRoom, currentPos,
                            otherRoom,
                            occupiedPositions
                    );

                    if (adjacentPos != null) {
                        roomPositions.put(otherRoom.roomId(), adjacentPos);
                        placedRooms.add(otherRoom.roomId());
                        markOccupied(otherRoom, adjacentPos.x, adjacentPos.y, occupiedPositions);
                        queue.add(otherRoom.roomId());
                        break; // Place one room per current room to spread out
                    }
                }
            } else {
                // Queue is empty but there are still unplaced rooms
                // Find any placed room and try to place remaining rooms adjacent to it
                boolean placedAny = false;
                for (Integer placedRoomId : placedRooms) {
                    if (placedAny) break;

                    MapData.RoomMapping placedRoom = roomMap.get(placedRoomId);
                    Point placedPos = roomPositions.get(placedRoomId);

                    for (MapData.RoomMapping otherRoom : shuffledRooms) {
                        if (placedRooms.contains(otherRoom.roomId())) {
                            continue;
                        }

                        Point adjacentPos = findAdjacentPosition(
                                placedRoom, placedPos,
                                otherRoom,
                                occupiedPositions
                        );

                        if (adjacentPos != null) {
                            roomPositions.put(otherRoom.roomId(), adjacentPos);
                            placedRooms.add(otherRoom.roomId());
                            markOccupied(otherRoom, adjacentPos.x, adjacentPos.y, occupiedPositions);
                            queue.add(otherRoom.roomId());
                            placedAny = true;
                            break;
                        }
                    }
                }

                if (!placedAny && placedRooms.size() < allRooms.size()) {
                    // Still can't place rooms - this shouldn't happen with proper search radius
                    break;
                }
            }
        }
    }

    /**
     * Places all rooms in a snake pattern to ensure full connectivity.
     * Each room is placed adjacent to the previous room, forming a connected chain.
     * Tracks occupied positions to avoid overlaps.
     */
    private List<MapData.RoomMapping> placeRoomsInSnakePattern(List<MapData.RoomMapping> rooms) {
        List<MapData.RoomMapping> placedRooms = new ArrayList<>();
        Set<String> occupiedPositions = new HashSet<>();
        int currentX = 0;
        int currentY = 0;
        int direction = 0; // 0=right, 1=down, 2=left, 3=up
        int roomsInDirection = 0;
        final int ROOMS_PER_DIRECTION = 5;

        for (MapData.RoomMapping room : rooms) {
            // Find a valid position near currentX, currentY
            Point pos = findValidPositionNear(currentX, currentY, room, occupiedPositions);
            if (pos == null) {
                // Couldn't find position - place at (0,0) and hope for the best
                pos = new Point(0, 0);
            }

            // Place room at found position
            placedRooms.add(new MapData.RoomMapping(
                    room.roomId(),
                    pos.x,
                    pos.y,
                    room.width(),
                    room.height(),
                    room.tiles(),
                    room.doors(),
                    room.originalArea(),
                    room.originalArea(),
                    room.subarea(),
                    room.subsubarea(),
                    room.roomName()
            ));

            // Mark tiles as occupied
            markOccupied(room, pos.x, pos.y, occupiedPositions);

            // Move to next position
            roomsInDirection++;
            if (roomsInDirection >= ROOMS_PER_DIRECTION) {
                direction = (direction + 1) % 4;
                roomsInDirection = 0;
            }

            // Calculate next position based on current room's dimensions
            switch (direction) {
                case 0: // Right
                    currentX += room.width();
                    break;
                case 1: // Down
                    currentY += room.height();
                    break;
                case 2: // Left
                    currentX -= room.width();
                    break;
                case 3: // Up
                    currentY -= room.height();
                    break;
            }
        }

        return placedRooms;
    }

    /**
     * Finds a valid position near the target position that doesn't overlap with occupied tiles.
     */
    private Point findValidPositionNear(int targetX, int targetY, MapData.RoomMapping room, Set<String> occupiedPositions) {
        // First try the exact position
        if (canPlaceRoom(room, targetX, targetY, occupiedPositions)) {
            return new Point(targetX, targetY);
        }

        // Try positions in a spiral pattern around the target
        final int MAX_RADIUS = 50;
        for (int radius = 1; radius <= MAX_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int newX = targetX + dx;
                    int newY = targetY + dy;

                    if (newX < 0 || newY < 0) {
                        continue;
                    }

                    if (canPlaceRoom(room, newX, newY, occupiedPositions)) {
                        return new Point(newX, newY);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Centers the map by calculating the bounding box and shifting all rooms.
     * This ensures the map is centered in the display rather than bunched in the corner.
     */
    private List<MapData.RoomMapping> centerMap(List<MapData.RoomMapping> rooms) {
        if (rooms.isEmpty()) {
            return rooms;
        }

        // Calculate bounding box
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (MapData.RoomMapping room : rooms) {
            minX = Math.min(minX, room.roomX());
            maxX = Math.max(maxX, room.roomX() + room.width());
            minY = Math.min(minY, room.roomY());
            maxY = Math.max(maxY, room.roomY() + room.height());
        }

        // Calculate center offset to shift to center
        int mapWidth = maxX - minX;
        int mapHeight = maxY - minY;
        int offsetX = -minX - mapWidth / 2;
        int offsetY = -minY - mapHeight / 2;

        // Shift all rooms
        List<MapData.RoomMapping> centeredRooms = new ArrayList<>();
        for (MapData.RoomMapping room : rooms) {
            centeredRooms.add(new MapData.RoomMapping(
                    room.roomId(),
                    room.roomX() + offsetX,
                    room.roomY() + offsetY,
                    room.width(),
                    room.height(),
                    room.tiles(),
                    room.doors(),
                    room.originalArea(),
                    room.shuffledArea(),
                    room.subarea(),
                    room.subsubarea(),
                    room.roomName()
            ));
        }

        return centeredRooms;
    }

    /**
     * Finds any valid position for a room (doesn't need to be adjacent to anything).
     */
    private Point findAnyPosition(MapData.RoomMapping room, Set<String> occupiedPositions) {
        final int MAX_SEARCH_RADIUS = 500;
        List<Point> validPositions = new ArrayList<>();

        for (int radius = 0; radius <= MAX_SEARCH_RADIUS; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    if (x == 0 && y == 0) continue;
                    if (x < 0 || y < 0) continue;

                    if (canPlaceRoom(room, x, y, occupiedPositions)) {
                        validPositions.add(new Point(x, y));
                    }
                }
            }

            if (!validPositions.isEmpty()) {
                return validPositions.get(random.nextInt(validPositions.size()));
            }
        }

        return null;
    }

    /**
     * Finds a good starting room.
     * All rooms have doors now (rooms without doors are filtered out).
     * Randomized to produce different layouts for different seeds.
     */
    private int findStartRoom(List<MapData.RoomMapping> rooms) {
        if (rooms.isEmpty()) {
            throw new IllegalStateException("No rooms available");
        }
        // Pick a random starting room
        return rooms.get(random.nextInt(rooms.size())).roomId();
    }

    /**
     * Places rooms using BFS traversal, ensuring connected rooms are adjacent.
     */
    private void placeRoomsWithBFS(
            int startRoomId,
            Map<Integer, MapData.RoomMapping> roomMap,
            Map<Integer, Set<Integer>> adjacencyList,
            Set<Integer> placedRooms,
            Map<Integer, Point> roomPositions,
            Set<String> occupiedPositions
    ) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(startRoomId);

        // Place starting room at origin
        roomPositions.put(startRoomId, new Point(0, 0));
        placedRooms.add(startRoomId);
        markOccupied(roomMap.get(startRoomId), 0, 0, occupiedPositions);

        while (!queue.isEmpty()) {
            int currentRoomId = queue.poll();
            MapData.RoomMapping currentRoom = roomMap.get(currentRoomId);
            Point currentPos = roomPositions.get(currentRoomId);

            // Get connected rooms and shuffle the order for randomness
            Set<Integer> connectedRoomsSet = adjacencyList.getOrDefault(currentRoomId, new HashSet<>());
            List<Integer> connectedRooms = new ArrayList<>(connectedRoomsSet);
            Collections.shuffle(connectedRooms, random);

            for (int connectedRoomId : connectedRooms) {
                if (placedRooms.contains(connectedRoomId)) {
                    continue;  // Already placed
                }

                MapData.RoomMapping connectedRoom = roomMap.get(connectedRoomId);
                if (connectedRoom == null) {
                    continue;  // Room doesn't exist in our list, skip
                }

                Point adjacentPos = findAdjacentPosition(
                        currentRoom, currentPos,
                        connectedRoom,
                        occupiedPositions
                );

                if (adjacentPos != null) {
                    roomPositions.put(connectedRoomId, adjacentPos);
                    placedRooms.add(connectedRoomId);
                    markOccupied(connectedRoom, adjacentPos.x, adjacentPos.y, occupiedPositions);
                    queue.add(connectedRoomId);
                }
            }
        }
    }

    /**
     * Finds an adjacent position for toRoom such that it touches fromRoom.
     * Tries the 4 cardinal directions first, then expands the search along each edge.
     * Randomized to produce different layouts for different seeds.
     */
    private Point findAdjacentPosition(
            MapData.RoomMapping fromRoom,
            Point fromPos,
            MapData.RoomMapping toRoom,
            Set<String> occupiedPositions
    ) {
        // First, try the 4 cardinal directions (place toRoom adjacent to fromRoom)
        List<Point> candidates = new ArrayList<>(List.of(
            // Left: toRoom's right edge touches fromRoom's left edge
            new Point(fromPos.x - toRoom.width(), fromPos.y),
            // Right: toRoom's left edge touches fromRoom's right edge
            new Point(fromPos.x + fromRoom.width(), fromPos.y),
            // Up: toRoom's bottom edge touches fromRoom's top edge
            new Point(fromPos.x, fromPos.y - toRoom.height()),
            // Down: toRoom's top edge touches fromRoom's bottom edge
            new Point(fromPos.x, fromPos.y + fromRoom.height())
        ));

        // Shuffle candidates for randomness
        Collections.shuffle(candidates, random);

        // Try each candidate position
        for (Point candidate : candidates) {
            if (canPlaceRoom(toRoom, candidate.x, candidate.y, occupiedPositions)) {
                return candidate;
            }
        }

        // If none of the 4 cardinal positions work, try sliding along each edge
        // Use a very large search range to find valid positions
        final int SEARCH_RANGE = 200;

        // Collect all valid positions along all edges
        List<Point> edgePositions = new ArrayList<>();

        // Slide along left edge (vary Y position)
        for (int yOffset = -SEARCH_RANGE; yOffset <= SEARCH_RANGE; yOffset++) {
            Point leftCandidate = new Point(fromPos.x - toRoom.width(), fromPos.y + yOffset);
            if (canPlaceRoom(toRoom, leftCandidate.x, leftCandidate.y, occupiedPositions)) {
                edgePositions.add(leftCandidate);
            }
        }

        // Slide along right edge (vary Y position)
        for (int yOffset = -SEARCH_RANGE; yOffset <= SEARCH_RANGE; yOffset++) {
            Point rightCandidate = new Point(fromPos.x + fromRoom.width(), fromPos.y + yOffset);
            if (canPlaceRoom(toRoom, rightCandidate.x, rightCandidate.y, occupiedPositions)) {
                edgePositions.add(rightCandidate);
            }
        }

        // Slide along top edge (vary X position)
        for (int xOffset = -SEARCH_RANGE; xOffset <= SEARCH_RANGE; xOffset++) {
            Point topCandidate = new Point(fromPos.x + xOffset, fromPos.y - toRoom.height());
            if (canPlaceRoom(toRoom, topCandidate.x, topCandidate.y, occupiedPositions)) {
                edgePositions.add(topCandidate);
            }
        }

        // Slide along bottom edge (vary X position)
        for (int xOffset = -SEARCH_RANGE; xOffset <= SEARCH_RANGE; xOffset++) {
            Point bottomCandidate = new Point(fromPos.x + xOffset, fromPos.y + fromRoom.height());
            if (canPlaceRoom(toRoom, bottomCandidate.x, bottomCandidate.y, occupiedPositions)) {
                edgePositions.add(bottomCandidate);
            }
        }

        // If we found valid positions, pick one at random
        if (!edgePositions.isEmpty()) {
            return edgePositions.get(random.nextInt(edgePositions.size()));
        }

        return null;  // No valid adjacent position found
    }

    /**
     * Checks if a room can be placed at the given position without overlapping occupied tiles.
     */
    private boolean canPlaceRoom(MapData.RoomMapping room, int roomX, int roomY, Set<String> occupiedPositions) {
        // Check if position is valid (not negative)
        if (roomX < 0 || roomY < 0) {
            return false;
        }

        // Check if any tile of the room overlaps with occupied positions
        for (int dx = 0; dx < room.width(); dx++) {
            for (int dy = 0; dy < room.height(); dy++) {
                String key = (roomX + dx) + "," + (roomY + dy);
                if (occupiedPositions.contains(key)) {
                    return false;  // Overlaps with occupied tile
                }
            }
        }

        return true;  // Can place here
    }

    /**
     * Marks a room's tiles as occupied.
     */
    private void markOccupied(MapData.RoomMapping room, int roomX, int roomY, Set<String> occupiedPositions) {
        for (int dx = 0; dx < room.width(); dx++) {
            for (int dy = 0; dy < room.height(); dy++) {
                occupiedPositions.add((roomX + dx) + "," + (roomY + dy));
            }
        }
    }

    /**
     * Load room positions from vanilla map data.
     * Returns list of RoomMapping with original area assignments.
     * Only includes rooms that have at least one door.
     */
    private List<MapData.RoomMapping> loadRoomPositions() {
        VanillaMap vanillaMap = dataLoader.getVanillaMap();
        List<RoomGeometry> roomGeometries = dataLoader.getRoomGeometries();

        if (vanillaMap == null || vanillaMap.getRooms() == null) {
            throw new IllegalStateException("Vanilla map data not loaded");
        }

        System.out.println("loadRoomPositions: vanillaMap has " + vanillaMap.getRooms().size() + " room entries");
        System.out.println("loadRoomPositions: roomGeometries has " + roomGeometries.size() + " geometries");

        List<MapData.RoomMapping> rooms = new ArrayList<>();

        // Room IDs come from room_geometry.json, NOT array indices
        for (int arrayIndex = 0; arrayIndex < vanillaMap.getRooms().size(); arrayIndex++) {
            // Skip invalid rooms (if room_mask exists and indicates invalid)
            if (vanillaMap.getRoomMask() != null && arrayIndex < vanillaMap.getRoomMask().size()) {
                if (!vanillaMap.getRoomMask().get(arrayIndex)) {
                    continue;  // Skip invalid rooms
                }
            }

            // Get room position
            List<Integer> roomPos = vanillaMap.getRooms().get(arrayIndex);
            if (roomPos == null || roomPos.size() < 2) {
                continue;  // Skip invalid positions
            }

            int roomX = roomPos.get(0);
            int roomY = roomPos.get(1);

            // Get room geometry for area info and dimensions
            // The arrayIndex corresponds to the index in roomGeometries list
            RoomGeometry geom = arrayIndex < roomGeometries.size() ? roomGeometries.get(arrayIndex) : null;

            // Use room_id from geometry, not array index
            if (geom == null) {
                continue;  // Skip rooms without geometry data
            }
            int roomId = geom.getRoomId();

            // Use area from room geometry, or default to 0 (Crateria)
            int originalArea = 0;
            String roomName = "Room " + roomId;

            // Get room dimensions from map data
            int width = 1;  // Default to 1x1
            int height = 1;
            List<List<Integer>> tiles = new ArrayList<>();  // Use ArrayList for Jackson serialization
            tiles.add(new ArrayList<>(List.of(1)));  // Default single tile
            List<MapData.DoorInfo> doors = new ArrayList<>();  // Use ArrayList for Jackson serialization

            if (geom != null) {
                originalArea = geom.getArea();
                roomName = geom.getName();

                // Extract tile map - convert to ArrayList for Jackson
                if (geom.getMap() != null && !geom.getMap().isEmpty()) {
                    tiles = new ArrayList<>();
                    for (List<Integer> row : geom.getMap()) {
                        tiles.add(new ArrayList<>(row));
                    }
                    height = tiles.size();  // Number of rows
                    // Find maximum width (some rows may be longer than others)
                    width = tiles.stream()
                            .mapToInt(List::size)
                            .max()
                            .orElse(1);
                }

                // Extract door information - convert to ArrayList for Jackson
                if (geom.getDoors() != null && !geom.getDoors().isEmpty()) {
                    doors = new ArrayList<>(geom.getDoors().stream()
                            .map(door -> new MapData.DoorInfo(
                                    door.getDirection(),
                                    door.getX(),
                                    door.getY(),
                                    door.getSubtype() != null ? door.getSubtype() : "normal",
                                    null  // Will be filled in when building connections
                            ))
                            .collect(Collectors.toList()));
                }
            }

            // Skip rooms without doors, UNLESS it's room 8 (Landing Site - start room)
            // Include all rooms, even ones without doors
            // Room 8 is the Landing Site in room_geometry.json

            // Default subarea and subsubarea
            int subarea = roomId % NUM_SUBAREAS;
            int subsubarea = (roomId / NUM_SUBAREAS) % NUM_SUBSUBAREAS;

            rooms.add(new MapData.RoomMapping(
                    roomId,
                    roomX,
                    roomY,
                    width,
                    height,
                    tiles,
                    doors,
                    originalArea,
                    originalArea,  // Initially same as original
                    subarea,
                    subsubarea,
                    roomName
            ));
        }

        return rooms;
    }

    /**
     * Creates a room permutation for swapping within dimension-compatible groups.
     * Rooms are grouped by (width, height) and only shuffled within each group
     * to prevent overlapping issues.
     */
    private List<Integer> createRoomPermutation(List<MapData.RoomMapping> rooms) {
        int numRooms = rooms.size();
        List<Integer> permutation = new ArrayList<>();

        // Group rooms by dimensions
        Map<String, List<Integer>> dimensionGroups = new HashMap<>();
        for (int i = 0; i < numRooms; i++) {
            MapData.RoomMapping room = rooms.get(i);
            String key = room.width() + "x" + room.height();
            dimensionGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(i);
        }

        // Shuffle within each dimension group
        Map<Integer, Integer> roomMapping = new HashMap<>();
        for (List<Integer> group : dimensionGroups.values()) {
            List<Integer> shuffled = new ArrayList<>(group);
            Collections.shuffle(shuffled, random);

            for (int i = 0; i < group.size(); i++) {
                roomMapping.put(group.get(i), shuffled.get(i));
            }
        }

        // Build permutation list
        for (int i = 0; i < numRooms; i++) {
            permutation.add(roomMapping.get(i));
        }

        return permutation;
    }

    /**
     * Applies room permutation to swap room positions.
     * Each room takes the position of the room it's swapping with.
     *
     * Example: If room at index 50 swaps with room at index 89:
     * - Room at index 50 gets room at index 89's original position
     * - Room at index 89 gets room at index 50's original position
     * - Both keep their room IDs, door connections, and all other properties
     */
    private List<MapData.RoomMapping> applyRoomPermutation(
            List<MapData.RoomMapping> originalRooms,
            List<Integer> permutation
    ) {
        // Build position lookup: list index -> (roomX, roomY)
        Map<Integer, Point> positionMap = new HashMap<>();
        for (int i = 0; i < originalRooms.size(); i++) {
            MapData.RoomMapping room = originalRooms.get(i);
            positionMap.put(i, new Point(room.roomX(), room.roomY()));
        }

        List<MapData.RoomMapping> swappedRooms = new ArrayList<>();

        for (int i = 0; i < originalRooms.size(); i++) {
            MapData.RoomMapping original = originalRooms.get(i);
            int originalRoomId = original.roomId();

            // permutation[i] = index of room whose position we should take
            int targetIndex = permutation.get(i);

            // Get target room's position
            Point targetPosition = positionMap.get(targetIndex);

            // Create new room mapping with swapped position
            swappedRooms.add(new MapData.RoomMapping(
                    originalRoomId,  // Keep original room ID
                    targetPosition.x,  // Take target's X position
                    targetPosition.y,  // Take target's Y position
                    original.width(),
                    original.height(),
                    original.tiles(),
                    original.doors(),
                    original.originalArea(),
                    original.shuffledArea(),
                    original.subarea(),
                    original.subsubarea(),
                    original.roomName()
            ));
        }

        return swappedRooms;
    }

    /**
     * Helper class for storing room positions.
     */
    private static class Point {
        final int x;
        final int y;

        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Shuffle door connections - randomize which rooms connect to each other.
     * This keeps rooms in their original positions but randomizes the door network.
     * DEPRECATED: Not used in room swapping approach.
     */
    private List<MapData.DoorConnection> shuffleDoorConnections(List<MapData.DoorConnection> originalDoors, int numRooms) {
        List<MapData.DoorConnection> shuffled = new ArrayList<>();

        // Create a list of all valid room IDs (0 to numRooms-1)
        List<Integer> roomIds = new ArrayList<>();
        for (int i = 0; i < numRooms; i++) {
            roomIds.add(i);
        }

        // Shuffle room IDs for random connections
        List<Integer> shuffledRoomIds = new ArrayList<>(roomIds);
        Collections.shuffle(shuffledRoomIds, random);

        // Build a mapping from original room ID to shuffled room ID
        Map<Integer, Integer> roomMapping = new HashMap<>();
        for (int i = 0; i < roomIds.size(); i++) {
            roomMapping.put(roomIds.get(i), shuffledRoomIds.get(i));
        }

        // Apply mapping to door connections
        for (MapData.DoorConnection door : originalDoors) {
            int newToRoomId;

            if (door.toRoomId() == -1) {
                // Keep unconnected doors (elevators, start points) unconnected
                newToRoomId = -1;
            } else {
                // Map the target room to its shuffled ID
                newToRoomId = roomMapping.getOrDefault(door.toRoomId(), door.toRoomId());
            }

            shuffled.add(new MapData.DoorConnection(
                    door.fromRoomId(),
                    door.fromDoorId(),
                    newToRoomId,  // Shuffled target room
                    door.toDoorId(),
                    door.bidirectional(),
                    door.doorType(),
                    door.direction(),
                    door.x(),
                    door.y()
            ));
        }

        return shuffled;
    }

    /**
     * Get room geometry by room ID.
     */
    private RoomGeometry getRoomGeometry(int roomId, List<RoomGeometry> geometries) {
        return geometries.stream()
                .filter(rg -> rg.getRoomId() == roomId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Create area mapping: [0,1,2,3,4,5] → [2,4,1,0,5,3] (example)
     * This defines which areas get swapped.
     */
    private Map<Integer, Integer> createAreaMapping() {
        List<Integer> areas = new ArrayList<>();
        for (int i = 0; i < NUM_AREAS; i++) {
            areas.add(i);
        }

        // Shuffle to create mapping
        List<Integer> shuffled = new ArrayList<>(areas);
        Collections.shuffle(shuffled, random);

        Map<Integer, Integer> mapping = new HashMap<>();
        for (int i = 0; i < areas.size(); i++) {
            mapping.put(areas.get(i), shuffled.get(i));
        }

        return mapping;
    }

    /**
     * Create subarea mapping: [0,1] → [1,0] or [0,1]
     * Swaps subareas within each area.
     */
    private Map<Integer, Integer> createSubareaMapping() {
        Map<Integer, Integer> mapping = new HashMap<>();

        // For each subarea, decide whether to swap
        for (int i = 0; i < NUM_SUBAREAS; i++) {
            if (random.nextBoolean()) {
                // Swap: 0 → 1, 1 → 0
                mapping.put(i, (i + 1) % NUM_SUBAREAS);
            } else {
                // No swap: 0 → 0, 1 → 1
                mapping.put(i, i);
            }
        }

        return mapping;
    }

    /**
     * Apply area mapping to all rooms.
     */
    private List<Integer> shuffleAreas(List<Integer> originalAreas, Map<Integer, Integer> areaMapping) {
        return originalAreas.stream()
                .map(areaMapping::get)
                .collect(Collectors.toList());
    }

    /**
     * Apply subarea mapping to all rooms.
     */
    private List<Integer> shuffleSubareas(List<Integer> originalSubareas, Map<Integer, Integer> subareaMapping) {
        return originalSubareas.stream()
                .map(subareaMapping::get)
                .collect(Collectors.toList());
    }

    /**
     * Shuffle subsubarea assignments (0 ↔ 1 within each subarea).
     */
    private List<Integer> shuffleSubsubareas(List<Integer> originalSubsubareas) {
        return originalSubsubareas.stream()
                .map(ss -> random.nextBoolean() ? (ss + 1) % NUM_SUBSUBAREAS : ss)
                .collect(Collectors.toList());
    }

    /**
     * Apply all shuffling to room mappings.
     */
    private List<MapData.RoomMapping> applyShuffling(
            List<MapData.RoomMapping> originalRooms,
            List<Integer> shuffledAreas,
            List<Integer> shuffledSubareas,
            List<Integer> shuffledSubsubareas
    ) {
        List<MapData.RoomMapping> result = new ArrayList<>();

        for (int i = 0; i < originalRooms.size(); i++) {
            MapData.RoomMapping original = originalRooms.get(i);

            result.add(new MapData.RoomMapping(
                    original.roomId(),
                    original.roomX(),
                    original.roomY(),
                    original.width(),
                    original.height(),
                    original.tiles(),  // Keep original tile layout
                    original.doors(),  // Keep original door positions
                    original.originalArea(),
                    shuffledAreas.get(i),  // Shuffled area
                    shuffledSubareas.get(i),  // Shuffled subarea
                    shuffledSubsubareas.get(i),  // Shuffled subsubarea
                    original.roomName()
            ));
        }

        return result;
    }

    /**
     * Build door connections from room geometry data.
     * This creates the door network by matching exit_ptr with entrance_ptr.
     */
    private List<MapData.DoorConnection> buildDoorConnections(List<MapData.RoomMapping> rooms) {
        List<MapData.DoorConnection> connections = new ArrayList<>();
        List<RoomGeometry> roomGeometries = dataLoader.getRoomGeometries();

        System.out.println("buildDoorConnections: " + roomGeometries.size() + " room geometries loaded");

        // Build a map of entrance_ptr -> (roomId, doorId, door) for quick lookup
        Map<Integer, RoomDoorInfo> entranceMap = new HashMap<>();

        for (RoomGeometry geom : roomGeometries) {
            if (geom.getDoors() == null) {
                continue;
            }

            for (int doorId = 0; doorId < geom.getDoors().size(); doorId++) {
                RoomGeometry.Door door = geom.getDoors().get(doorId);
                if (door.getEntrancePtr() != null) {
                    entranceMap.put(door.getEntrancePtr(), new RoomDoorInfo(geom.getRoomId(), doorId, door));
                }
            }
        }

        System.out.println("buildDoorConnections: " + entranceMap.size() + " entrance pointers mapped");

        // Now build connections by matching exit_ptr with entrance_ptr
        for (RoomGeometry geom : roomGeometries) {
            if (geom.getDoors() == null) {
                continue;
            }

            for (int doorId = 0; doorId < geom.getDoors().size(); doorId++) {
                RoomGeometry.Door door = geom.getDoors().get(doorId);

                // Skip doors without exit pointer
                if (door.getExitPtr() == null) {
                    continue;
                }

                int fromRoomId = geom.getRoomId();

                // Find the connected room using exit_ptr
                RoomDoorInfo targetInfo = entranceMap.get(door.getExitPtr());

                if (targetInfo != null) {
                    // Found connection! Create DoorConnection
                    int toRoomId = targetInfo.roomId;
                    int toDoorId = targetInfo.doorId;

                    // Determine door type from subtype
                    MapData.DoorType doorType = parseDoorType(door.getSubtype());

                    connections.add(new MapData.DoorConnection(
                            fromRoomId,
                            doorId,
                            toRoomId,
                            toDoorId,
                            true,  // bidirectional
                            doorType,
                            door.getDirection(),
                            door.getX(),
                            door.getY()
                    ));
                } else {
                    // No connection found - create unconnected door (e.g., elevator, start point)
                    connections.add(new MapData.DoorConnection(
                            fromRoomId,
                            doorId,
                            -1,  // No connected room
                            -1,
                            false,
                            parseDoorType(door.getSubtype()),
                            door.getDirection(),
                            door.getX(),
                            door.getY()
                    ));
                }
            }
        }

        System.out.println("buildDoorConnections: created " + connections.size() + " door connections");
        return connections;
    }

    /**
     * Parse door type from subtype string.
     * Maps vanilla subtypes and DoorRandomizer types to DoorType enum.
     */
    private MapData.DoorType parseDoorType(String subtype) {
        if (subtype == null) {
            return MapData.DoorType.NORMAL;
        }

        return switch (subtype.toLowerCase()) {
            // Vanilla door subtypes
            case "normal" -> MapData.DoorType.NORMAL;
            case "missile", "red" -> MapData.DoorType.RED;
            case "super", "super_missile", "green" -> MapData.DoorType.GREEN;
            case "powerbomb", "power_bomb", "yellow" -> MapData.DoorType.YELLOW;
            // DoorRandomizer types (beam doors)
            case "charge", "charge_beam" -> MapData.DoorType.CHARGE;
            case "ice", "ice_beam" -> MapData.DoorType.ICE;
            case "wave", "wave_beam" -> MapData.DoorType.WAVE;
            case "spazer", "spazer_beam" -> MapData.DoorType.SPAZER;
            case "plasma", "plasma_beam" -> MapData.DoorType.PLASMA;
            // Special terrain types (treated as normal)
            case "elevator", "sand" -> MapData.DoorType.NORMAL;
            default -> MapData.DoorType.NORMAL;
        };
    }

    /**
     * Helper class to store room and door information.
     */
    private static class RoomDoorInfo {
        final int roomId;
        final int doorId;
        final RoomGeometry.Door door;

        RoomDoorInfo(int roomId, int doorId, RoomGeometry.Door door) {
            this.roomId = roomId;
            this.doorId = doorId;
            this.door = door;
        }
    }

    /**
     * Determines the spatial relationship between two rooms.
     * Returns "left", "right", "up", "down", "overlap", or null if no relationship.
     */
    private String getRoomRelationship(MapData.RoomMapping r1, MapData.RoomMapping r2) {
        int r1Left = r1.roomX();
        int r1Right = r1.roomX() + r1.width() - 1;
        int r1Top = r1.roomY();
        int r1Bottom = r1.roomY() + r1.height() - 1;

        int r2Left = r2.roomX();
        int r2Right = r2.roomX() + r2.width() - 1;
        int r2Top = r2.roomY();
        int r2Bottom = r2.roomY() + r2.height() - 1;

        // Calculate overlap amount
        int overlapLeft = Math.max(r1Left, r2Left);
        int overlapRight = Math.min(r1Right, r2Right);
        int overlapTop = Math.max(r1Top, r2Top);
        int overlapBottom = Math.min(r1Bottom, r2Bottom);

        int overlapWidth = overlapRight - overlapLeft + 1;
        int overlapHeight = overlapBottom - overlapTop + 1;

        // Minimum overlap required for a valid door connection
        final int MIN_OVERLAP = 2;

        // Check for overlap
        boolean overlapX = r1Left <= r2Right && r1Right >= r2Left;
        boolean overlapY = r1Top <= r2Bottom && r1Bottom >= r2Top;

        if (overlapX && overlapY) {
            // Rooms overlap - check if they're also adjacent (touching at edges)
            if (r1Right == r2Left || r1Left == r2Right) {
                // Touching vertically while overlapping
                if (overlapHeight >= MIN_OVERLAP) {
                    return r1Right < r2Right ? "right" : "left";
                }
            }
            if (r1Bottom == r2Top || r1Top == r2Bottom) {
                // Touching horizontally while overlapping
                if (overlapWidth >= MIN_OVERLAP) {
                    return r1Bottom < r2Bottom ? "down" : "up";
                }
            }
            // Pure overlap (not touching at edges)
            return "overlap";
        }

        // Check for adjacency (touching at edges, not overlapping)
        // Calculate overlap for adjacency check
        int adjOverlapLeft = Math.max(r1Left, r2Left);
        int adjOverlapRight = Math.min(r1Right, r2Right);
        int adjOverlapTop = Math.max(r1Top, r2Top);
        int adjOverlapBottom = Math.min(r1Bottom, r2Bottom);

        int adjOverlapWidth = adjOverlapRight - adjOverlapLeft + 1;
        int adjOverlapHeight = adjOverlapBottom - adjOverlapTop + 1;

        // r1 is left of r2
        if (r1Right + 1 == r2Left && adjOverlapHeight >= MIN_OVERLAP) {
            return "right";
        }
        // r1 is right of r2
        if (r1Left == r2Right + 1 && adjOverlapHeight >= MIN_OVERLAP) {
            return "left";
        }
        // r1 is above r2
        if (r1Bottom + 1 == r2Top && adjOverlapWidth >= MIN_OVERLAP) {
            return "down";
        }
        // r1 is below r2
        if (r1Top == r2Bottom + 1 && adjOverlapWidth >= MIN_OVERLAP) {
            return "up";
        }

        // No relationship
        return null;
    }

    /**
     * Calculates the door position based on the actual overlap between two rooms.
     * For adjacent rooms, uses the center of the touching edge.
     * For overlapping rooms, uses the center of the overlap.
     */
    private Point calculateDoorPosition(MapData.RoomMapping room, MapData.RoomMapping other, String relationship) {
        int roomLeft = room.roomX();
        int roomRight = room.roomX() + room.width() - 1;
        int roomTop = room.roomY();
        int roomBottom = room.roomY() + room.height() - 1;

        int otherLeft = other.roomX();
        int otherRight = other.roomX() + other.width() - 1;
        int otherTop = other.roomY();
        int otherBottom = other.roomY() + other.height() - 1;

        int doorX, doorY;

        if (relationship.equals("left")) {
            // Door on room's left edge (room is to the right of other)
            doorX = 0;
            // Calculate the overlapping region in Y
            int overlapTop = Math.max(roomTop, otherTop);
            int overlapBottom = Math.min(roomBottom, otherBottom);
            // Position at center of overlap
            doorY = (overlapTop + overlapBottom) / 2 - roomTop;
            // Clamp to room bounds
            doorY = Math.max(0, Math.min(doorY, room.height() - 1));
        } else if (relationship.equals("right")) {
            // Door on room's right edge (room is to the left of other)
            doorX = room.width() - 1;
            // Calculate the overlapping region in Y
            int overlapTop = Math.max(roomTop, otherTop);
            int overlapBottom = Math.min(roomBottom, otherBottom);
            // Position at center of overlap
            doorY = (overlapTop + overlapBottom) / 2 - roomTop;
            // Clamp to room bounds
            doorY = Math.max(0, Math.min(doorY, room.height() - 1));
        } else if (relationship.equals("up")) {
            // Door on room's top edge (room is below other, going up)
            doorY = 0;
            // Calculate the overlapping region in X
            int overlapLeft = Math.max(roomLeft, otherLeft);
            int overlapRight = Math.min(roomRight, otherRight);
            // Position at center of overlap
            doorX = (overlapLeft + overlapRight) / 2 - roomLeft;
            // Clamp to room bounds
            doorX = Math.max(0, Math.min(doorX, room.width() - 1));
        } else if (relationship.equals("down")) {
            // Door on room's bottom edge (room is above other, going down)
            doorY = room.height() - 1;
            // Calculate the overlapping region in X
            int overlapLeft = Math.max(roomLeft, otherLeft);
            int overlapRight = Math.min(roomRight, otherRight);
            // Position at center of overlap
            doorX = (overlapLeft + overlapRight) / 2 - roomLeft;
            // Clamp to room bounds
            doorX = Math.max(0, Math.min(doorX, room.width() - 1));
        } else if (relationship.equals("overlap")) {
            // For overlapping rooms, use center of overlap in both dimensions
            int overlapLeft = Math.max(roomLeft, otherLeft);
            int overlapRight = Math.min(roomRight, otherRight);
            int overlapTop = Math.max(roomTop, otherTop);
            int overlapBottom = Math.min(roomBottom, otherBottom);
            int overlapCenterX = (overlapLeft + overlapRight) / 2;
            int overlapCenterY = (overlapTop + overlapBottom) / 2;
            doorX = overlapCenterX - roomLeft;
            doorY = overlapCenterY - roomTop;
            // Clamp to room bounds
            doorX = Math.max(0, Math.min(doorX, room.width() - 1));
            doorY = Math.max(0, Math.min(doorY, room.height() - 1));
        } else {
            doorX = 0;
            doorY = 0;
        }

        return new Point(doorX, doorY);
    }

    /**
     * Helper method to find a room by ID in the list.
     */
    private MapData.RoomMapping findRoomById(List<MapData.RoomMapping> rooms, int roomId) {
        return rooms.stream()
                .filter(r -> r.roomId() == roomId)
                .findFirst()
                .orElse(null);
    }

    /**
     * Build map topology-first by ensuring all doors connect.
     * Starts with room 8 and places rooms so that doors actually align.
     */
    private List<MapData.RoomMapping> buildMapTopologyFirst(List<MapData.RoomMapping> allRooms) {
        // Create a map of roomId -> room
        Map<Integer, MapData.RoomMapping> roomById = new HashMap<>();
        for (MapData.RoomMapping room : allRooms) {
            roomById.put(room.roomId(), room);
        }

        // Track placed rooms and their positions
        Set<Integer> placedRoomIds = new HashSet<>();
        Map<Integer, Point> roomPositions = new HashMap<>();

        // Track which doors have been connected: "roomId,doorIndex" -> true
        Set<String> connectedDoors = new HashSet<>();

        // Unplaced rooms
        Set<Integer> unplacedRoomIds = allRooms.stream()
                .map(MapData.RoomMapping::roomId)
                .collect(Collectors.toSet());

        // Start with room 8 (Landing Site) at origin
        MapData.RoomMapping startRoom = roomById.get(8);
        if (startRoom == null) {
            throw new IllegalStateException("Room 8 (Landing Site) not found");
        }

        placedRoomIds.add(8);
        unplacedRoomIds.remove(8);
        roomPositions.put(8, new Point(0, 0));

        // Track occupied positions
        Set<String> occupiedPositions = new HashSet<>();
        markRoomOccupied(startRoom, 0, 0, occupiedPositions);

        System.out.println("Room 8: position (0,0), size " + startRoom.width() + "x" + startRoom.height() + ", spans x=[0," + (startRoom.width()-1) + "], y=[0," + (startRoom.height()-1) + "]");

        System.out.println("Starting topology-first map build with room 8");
        System.out.println("Room 8 has " + (startRoom.doors() != null ? startRoom.doors().size() : 0) + " doors");
        if (startRoom.doors() != null) {
            for (MapData.DoorInfo d : startRoom.doors()) {
                System.out.println("  Door: " + d.direction() + " at (" + d.x() + "," + d.y() + ")");
            }
        }

        // Keep trying to connect doors until we can't anymore
        boolean madeProgress = true;
        int iterations = 0;
        int maxIterations = 10000;

        while (madeProgress && iterations < maxIterations) {
            iterations++;
            madeProgress = false;

            // Try to connect doors from all placed rooms
            for (Integer sourceRoomId : new ArrayList<>(placedRoomIds)) {
                MapData.RoomMapping sourceRoom = roomById.get(sourceRoomId);
                Point sourcePos = roomPositions.get(sourceRoomId);

                if (sourceRoom.doors() == null || sourceRoom.doors().isEmpty()) {
                    continue;
                }

                // Try each door in this room
                for (int doorIndex = 0; doorIndex < sourceRoom.doors().size(); doorIndex++) {
                    String doorKey = sourceRoomId + "," + doorIndex;
                    if (connectedDoors.contains(doorKey)) {
                        continue;  // Already connected
                    }

                    MapData.DoorInfo sourceDoor = sourceRoom.doors().get(doorIndex);
                    String oppositeDirection = getOppositeDirection(sourceDoor.direction());

                    // Find an unplaced room with a door in the opposite direction
                    for (Integer candidateRoomId : new ArrayList<>(unplacedRoomIds)) {
                        MapData.RoomMapping candidateRoom = roomById.get(candidateRoomId);

                        if (candidateRoom.doors() == null || candidateRoom.doors().isEmpty()) {
                            continue;
                        }

                        // Find a door in this room that faces the opposite direction
                        for (int candidateDoorIndex = 0; candidateDoorIndex < candidateRoom.doors().size(); candidateDoorIndex++) {
                            MapData.DoorInfo candidateDoor = candidateRoom.doors().get(candidateDoorIndex);

                            if (!candidateDoor.direction().equals(oppositeDirection)) {
                                continue;
                            }

                            if (sourceRoomId == 8) {
                                System.out.println("  Found candidate room " + candidateRoomId + " with " + candidateDoor.direction() + " door at (" + candidateDoor.x() + "," + candidateDoor.y() + ")");
                            }

                            // Calculate where to place the candidate room so doors align
                            int sourceDoorWorldX = sourcePos.x + sourceDoor.x();
                            int sourceDoorWorldY = sourcePos.y + sourceDoor.y();

                            // Candidate room should be placed so its door is adjacent
                            int candidateRoomX = sourceDoorWorldX - candidateDoor.x();
                            int candidateRoomY = sourceDoorWorldY - candidateDoor.y();

                            // Apply offset based on door direction
                            switch (candidateDoor.direction()) {
                                case "left" -> candidateRoomX -= 1;
                                case "right" -> candidateRoomX += 1;
                                case "up" -> candidateRoomY -= 1;
                                case "down" -> candidateRoomY += 1;
                            }

                            // Check if this position is valid
                            if (!canPlaceRoomAt(candidateRoom, candidateRoomX, candidateRoomY, occupiedPositions)) {
                                if (sourceRoomId == 8 && candidateRoomId == 140) {
                                    System.out.println("    Room " + candidateRoomId + " can't be placed at (" + candidateRoomX + "," + candidateRoomY + ")");
                                }
                                continue;
                            }

                            // Place the room!
                            placedRoomIds.add(candidateRoomId);
                            unplacedRoomIds.remove(candidateRoomId);
                            roomPositions.put(candidateRoomId, new Point(candidateRoomX, candidateRoomY));
                            markRoomOccupied(candidateRoom, candidateRoomX, candidateRoomY, occupiedPositions);
                            connectedDoors.add(doorKey);
                            connectedDoors.add(candidateRoomId + "," + candidateDoorIndex);

                            System.out.println("Connected room " + sourceRoomId + " door " + doorIndex + " to room " + candidateRoomId + " at (" + candidateRoomX + "," + candidateRoomY + ")");
                            madeProgress = true;
                            break;
                        }

                        if (madeProgress) {
                            break;
                        }
                    }
                }
            }
        }

        System.out.println("Placed " + placedRoomIds.size() + " rooms out of " + allRooms.size());
        System.out.println("Remaining unplaced: " + unplacedRoomIds.size());

        // Build the final list of placed rooms
        List<MapData.RoomMapping> result = new ArrayList<>();
        for (Integer roomId : placedRoomIds) {
            Point pos = roomPositions.get(roomId);
            MapData.RoomMapping original = roomById.get(roomId);

            // Convert tiles
            List<List<Integer>> roomTiles = new ArrayList<>();
            if (original.tiles() != null) {
                for (List<Integer> row : original.tiles()) {
                    roomTiles.add(new ArrayList<>(row));
                }
            }

            result.add(new MapData.RoomMapping(
                    roomId,
                    pos.x,
                    pos.y,
                    original.width(),
                    original.height(),
                    roomTiles,
                    original.doors(),
                    original.originalArea(),
                    original.originalArea(),
                    original.subarea(),
                    original.subsubarea(),
                    original.roomName()
            ));
        }

        return result;
    }

    /**
     * Place rooms ensuring all doors get connected.
     * Starts with room 8 and places rooms at each door position.
     */
    private List<MapData.RoomMapping> placeRoomsWithDoorConnections(List<MapData.RoomMapping> allRooms) {
        // Create a map of roomId -> room
        Map<Integer, MapData.RoomMapping> roomById = new HashMap<>();
        for (MapData.RoomMapping room : allRooms) {
            roomById.put(room.roomId(), room);
        }

        // Track placed rooms and their positions
        Set<Integer> placedRoomIds = new HashSet<>();
        Map<Integer, Point> roomPositions = new HashMap<>();

        // Track which doors have been connected
        // Key: "roomId:doorX:doorY:direction", Value: true if connected
        Set<String> connectedDoors = new HashSet<>();

        // Unplaced rooms (shuffle for randomness)
        List<MapData.RoomMapping> unplacedRooms = allRooms.stream()
                .filter(r -> r.roomId() != 8)
                .collect(Collectors.toList());
        Collections.shuffle(unplacedRooms, random);

        // Track occupied positions
        Set<String> occupiedPositions = new HashSet<>();

        // Start with room 8 at origin
        MapData.RoomMapping startRoom = roomById.get(8);
        if (startRoom == null) {
            throw new IllegalStateException("Room 8 not found");
        }

        placedRoomIds.add(8);
        roomPositions.put(8, new Point(0, 0));
        markRoomOccupied(startRoom, 0, 0, occupiedPositions);

        System.out.println("Starting with room 8, " + unplacedRooms.size() + " rooms to place");

        // Place rooms by connecting to doors
        int iterations = 0;
        int maxIterations = 5000;  // Reduced for faster testing
        int lastPlacementCount = 0;

        while (!unplacedRooms.isEmpty() && iterations < maxIterations) {
            iterations++;

            // Find a door that needs a room
            // PRIORITY: Always try room 8's doors first to ensure the starting room is well-connected
            boolean placedAny = false;

            // Create ordered list of source rooms, with room 8 first
            List<Integer> orderedRoomIds = new ArrayList<>(placedRoomIds);
            orderedRoomIds.sort((a, b) -> {
                if (a == 8) return -1;  // Room 8 comes first
                if (b == 8) return 1;
                return a.compareTo(b);  // Then sort by ID for consistency
            });

            for (Integer sourceRoomId : orderedRoomIds) {
                if (placedAny) break;

                MapData.RoomMapping sourceRoom = roomById.get(sourceRoomId);
                Point sourcePos = roomPositions.get(sourceRoomId);

                if (sourceRoom.doors() == null) continue;

                for (MapData.DoorInfo door : sourceRoom.doors()) {
                    // Skip if this door is already connected
                    String connectedDoorKey = sourceRoomId + ":" + door.x() + ":" + door.y() + ":" + door.direction();
                    if (connectedDoors.contains(connectedDoorKey)) {
                        continue;
                    }
                    // Calculate where to place room so EDGES TOUCH (no gap)
                    int sourceRoomRight = sourcePos.x + sourceRoom.width();
                    int sourceRoomBottom = sourcePos.y + sourceRoom.height();
                    int sourceRoomLeft = sourcePos.x;
                    int sourceRoomTop = sourcePos.y;

                    int newRoomX = sourceRoomLeft;
                    int newRoomY = sourceRoomTop;

                    // Position new room adjacent to source room (edges touching)
                    switch (door.direction()) {
                        case "left"  -> newRoomX = sourceRoomLeft - 1;  // Place to the left
                        case "right" -> newRoomX = sourceRoomRight;   // Place to the right (touching)
                        case "up"    -> newRoomY = sourceRoomTop - 1;    // Place above
                        case "down"  -> newRoomY = sourceRoomBottom;  // Place below (touching)
                    }

                    // Try to place any unplaced room here (random order for variety)
                    List<MapData.RoomMapping> shuffledCandidates = new ArrayList<>(unplacedRooms);
                    Collections.shuffle(shuffledCandidates, random);

                    for (MapData.RoomMapping candidate : shuffledCandidates) {
                        int originalIndex = unplacedRooms.indexOf(candidate);

                        if (canPlaceRoomAt(candidate, newRoomX, newRoomY, occupiedPositions)) {
                            // Place it!
                            placedRoomIds.add(candidate.roomId());
                            roomPositions.put(candidate.roomId(), new Point(newRoomX, newRoomY));
                            markRoomOccupied(candidate, newRoomX, newRoomY, occupiedPositions);
                            unplacedRooms.remove(originalIndex);

                            // Mark the source door as connected
                            connectedDoors.add(connectedDoorKey);

                            System.out.println("Placed room " + candidate.roomId() + " at (" + newRoomX + "," + newRoomY + ") for door from room " + sourceRoomId);
                            placedAny = true;
                            break;
                        }
                    }

                    if (placedAny) break;
                }
            }

            if (!placedAny) {
                // Couldn't place any room this iteration, but keep trying
                // Maybe shuffling the order will help find valid placements
                if (placedRoomIds.size() == lastPlacementCount) {
                    // No progress for multiple iterations - try shuffling unplaced rooms
                    Collections.shuffle(unplacedRooms, random);
                }
                lastPlacementCount = placedRoomIds.size();

                // Give up after trying for many iterations with no progress
                if (iterations > 2000 && !placedAny) {
                    System.out.println("Could not place " + unplacedRooms.size() + " remaining rooms (no valid door connections after " + iterations + " iterations)");
                    break;
                }
            }
        }

        System.out.println("Placed " + placedRoomIds.size() + " rooms in " + iterations + " iterations");

        // PHASE 2: Iteratively remove rooms with null door connections
        System.out.println("PHASE 2: Removing rooms with null door connections...");
        boolean foundNullDoors = true;
        int cleanupIterations = 0;
        int maxCleanupIterations = 50;  // Allow many iterations to handle cascading removals

        while (foundNullDoors && cleanupIterations < maxCleanupIterations && placedRoomIds.size() > 1) {
            cleanupIterations++;
            foundNullDoors = false;

            // Build temporary room list to check door connections
            List<MapData.RoomMapping> tempRooms = new ArrayList<>();
            for (Integer roomId : new ArrayList<>(placedRoomIds)) {
                Point pos = roomPositions.get(roomId);
                MapData.RoomMapping original = roomById.get(roomId);
                tempRooms.add(createRoomMappingWithPosition(original, pos.x, pos.y));
            }

            // Update door connections
            List<MapData.RoomMapping> updatedRooms = updateDoorConnectionTargetsFromGeometry(tempRooms, new ArrayList<>());

            // Find rooms with null doors (except room 8 which must stay)
            Set<Integer> roomsToRemove = new HashSet<>();
            for (MapData.RoomMapping room : updatedRooms) {
                if (room.roomId() == 8) continue;  // Never remove room 8

                if (room.doors() != null) {
                    for (MapData.DoorInfo door : room.doors()) {
                        if (door.connectsToRoomId() == null) {
                            roomsToRemove.add(room.roomId());
                            break;
                        }
                    }
                }
            }

            // Remove rooms with null doors
            if (!roomsToRemove.isEmpty()) {
                foundNullDoors = true;
                System.out.println("  Cleanup iteration " + cleanupIterations + ": Removing " + roomsToRemove.size() + " rooms with null doors (remaining: " + (placedRoomIds.size() - roomsToRemove.size()) + ")");

                for (Integer roomIdToRemove : roomsToRemove) {
                    Point pos = roomPositions.get(roomIdToRemove);
                    MapData.RoomMapping roomToRemove = roomById.get(roomIdToRemove);

                    // Clear occupied positions for this room
                    for (int y = 0; y < roomToRemove.height(); y++) {
                        for (int x = 0; x < roomToRemove.width(); x++) {
                            int worldX = pos.x + x;
                            int worldY = pos.y + y;
                            occupiedPositions.remove(worldX + "," + worldY);
                        }
                    }

                    // Remove from tracking
                    placedRoomIds.remove(roomIdToRemove);
                    roomPositions.remove(roomIdToRemove);
                    connectedDoors.removeIf(key -> key.startsWith(roomIdToRemove + ":"));
                }
            }
        }

        System.out.println("After cleanup: " + placedRoomIds.size() + " rooms remain (removed " + (allRooms.size() - placedRoomIds.size()) + " total)");

        // Build final list
        List<MapData.RoomMapping> result = new ArrayList<>();
        for (Integer roomId : placedRoomIds) {
            Point pos = roomPositions.get(roomId);
            MapData.RoomMapping original = roomById.get(roomId);

            List<List<Integer>> roomTiles = new ArrayList<>();
            if (original.tiles() != null) {
                for (List<Integer> row : original.tiles()) {
                    roomTiles.add(new ArrayList<>(row));
                }
            }

            result.add(new MapData.RoomMapping(
                    roomId,
                    pos.x,
                    pos.y,
                    original.width(),
                    original.height(),
                    roomTiles,
                    original.doors(),
                    original.originalArea(),
                    original.originalArea(),
                    original.subarea(),
                    original.subsubarea(),
                    original.roomName()
            ));
        }

        return result;
    }

    /**
     * Get the opposite direction (e.g., left ↔ right).
     */
    private String getOppositeDirection(String direction) {
        return switch (direction) {
            case "left" -> "right";
            case "right" -> "left";
            case "up" -> "down";
            case "down" -> "up";
            default -> direction;
        };
    }

    /**
     * Build map by attaching rooms to existing doors (topology-first approach).
     * Starts with room 8 (Landing Site) and expands outward by placing rooms at door positions.
     */
    private List<MapData.RoomMapping> buildMapByDoorConnections(List<MapData.RoomMapping> allRooms) {
        // Create a map of roomId -> room
        Map<Integer, MapData.RoomMapping> roomById = new HashMap<>();
        for (MapData.RoomMapping room : allRooms) {
            roomById.put(room.roomId(), room);
        }

        // Track placed rooms and their positions
        Set<Integer> placedRoomIds = new HashSet<>();
        Map<Integer, Point> roomPositions = new HashMap<>();  // roomId -> (x, y)

        // Track unplaced doors with their source room: "roomId,doorIndex" -> DoorInfo
        List<DoorWithSource> unplacedDoors = new ArrayList<>();

        // Start with room 8 (Landing Site) at origin
        MapData.RoomMapping startRoom = roomById.get(8);
        if (startRoom == null) {
            throw new IllegalStateException("Room 8 (Landing Site) not found in room data");
        }

        placedRoomIds.add(8);
        roomPositions.put(8, new Point(0, 0));

        // Add all doors from start room to unplaced list
        if (startRoom.doors() != null) {
            for (MapData.DoorInfo door : startRoom.doors()) {
                unplacedDoors.add(new DoorWithSource(8, door));
            }
        }

        // Shuffle remaining rooms for randomness
        List<Integer> remainingRoomIdsList = allRooms.stream()
                .filter(r -> r.roomId() != 8)
                .map(MapData.RoomMapping::roomId)
                .collect(Collectors.toList());
        Collections.shuffle(remainingRoomIdsList, random);

        // Create remaining room set for quick lookup and removal
        Set<Integer> remainingRoomIds = new HashSet<>(remainingRoomIdsList);

        // Track occupied positions: "x,y" -> roomId
        Set<String> occupiedPositions = new HashSet<>();
        markRoomOccupied(startRoom, 0, 0, occupiedPositions);

        // Try to place rooms by attaching to unplaced doors
        int maxIterations = 10000;  // Prevent infinite loops
        int iterations = 0;

        while (!unplacedDoors.isEmpty() && !remainingRoomIds.isEmpty() && iterations < maxIterations) {
            iterations++;

            // Pick a random unplaced door
            int doorListIndex = random.nextInt(unplacedDoors.size());
            DoorWithSource doorWithSource = unplacedDoors.get(doorListIndex);

            int sourceRoomId = doorWithSource.sourceRoomId;
            MapData.DoorInfo door = doorWithSource.door;
            Point sourcePos = roomPositions.get(sourceRoomId);

            if (sourcePos == null) {
                unplacedDoors.remove(doorListIndex);
                continue;
            }

            MapData.RoomMapping sourceRoom = roomById.get(sourceRoomId);

            // Calculate where the new room should be placed based on door direction
            int doorWorldX = sourcePos.x + door.x();
            int doorWorldY = sourcePos.y + door.y();

            int newRoomX = doorWorldX;
            int newRoomY = doorWorldY;

            // Offset based on door direction
            int offsetX = 0, offsetY = 0;
            switch (door.direction()) {
                case "left" -> offsetX = -1;
                case "right" -> offsetX = 1;
                case "up" -> offsetY = -1;
                case "down" -> offsetY = 1;
            }

            newRoomX += offsetX;
            newRoomY += offsetY;

            // Try to find a room that fits here
            MapData.RoomMapping placedRoom = null;
            Integer placedRoomId = null;

            if (iterations <= 20) {  // Log first 20 iterations
                System.out.println("Iteration " + iterations + ": Trying to place room at door (" + doorWorldX + "," + doorWorldY +
                        ") direction=" + door.direction() + " from room " + sourceRoomId);
            }

            int candidatesChecked = 0;
            int positionsTried = 0;

            // Try remaining rooms randomly
            for (Integer candidateId : remainingRoomIdsList) {
                if (!remainingRoomIds.contains(candidateId)) {
                    continue;  // Already placed
                }

                candidatesChecked++;

                MapData.RoomMapping candidate = roomById.get(candidateId);
                if (candidate == null) {
                    continue;
                }

                // Try different positions around the door
                // The door could be at any edge of the new room depending on its direction
                List<Point> candidatePositions = getCandidatePositions(candidate, door, doorWorldX, doorWorldY);

                if (iterations <= 20 && !candidatePositions.isEmpty()) {
                    System.out.println("  Room " + candidate.roomId() + ": " + candidatePositions.size() + " candidate positions");
                }

                for (Point candidatePos : candidatePositions) {
                    positionsTried++;
                    if (canPlaceRoomAt(candidate, candidatePos.x, candidatePos.y, occupiedPositions)) {
                        // Place this room
                        placedRoom = placeRoomAt(candidate, candidatePos.x, candidatePos.y);
                        placedRoomId = candidate.roomId();

                        placedRoomIds.add(placedRoomId);
                        roomPositions.put(placedRoomId, candidatePos);
                        remainingRoomIds.remove(placedRoomId);
                        markRoomOccupied(candidate, candidatePos.x, candidatePos.y, occupiedPositions);

                        // Add this room's doors to unplaced list
                        if (candidate.doors() != null) {
                            for (MapData.DoorInfo newDoor : candidate.doors()) {
                                unplacedDoors.add(new DoorWithSource(placedRoomId, newDoor));
                            }
                        }

                        System.out.println("Placed room " + placedRoomId + " at (" + candidatePos.x + "," + candidatePos.y + ")");
                        break;
                    }
                }

                if (placedRoom != null) {
                    break;
                }
            }

            if (iterations <= 20) {
                System.out.println("  Checked " + candidatesChecked + " rooms, tried " + positionsTried + " positions, result: " +
                        (placedRoom != null ? "SUCCESS" : "FAILED"));
            }

            if (placedRoom != null) {
                // Successfully placed a room
                unplacedDoors.remove(doorListIndex);
            } else {
                // Couldn't place any room here, skip this door for now
                // Move it to the end of the list to try again later
                unplacedDoors.remove(doorListIndex);
            }
        }

        System.out.println("Placed " + placedRoomIds.size() + " rooms out of " + allRooms.size());
        System.out.println("Remaining unplaced doors: " + unplacedDoors.size());

        // Phase 2: Try to place remaining rooms more aggressively
        // Prioritize rooms with more doors and try harder to place them
        System.out.println("Starting Phase 2: Aggressive room placement...");

        int phase2Placed = 0;
        int phase2Attempts = 0;
        int maxPhase2Attempts = 10000;

        // Sort remaining rooms by door count (most doors first) - they're more valuable for connectivity
        List<Integer> sortedRemainingRooms = remainingRoomIds.stream()
                .sorted((a, b) -> {
                    int doorsA = roomById.get(a) != null && roomById.get(a).doors() != null ? roomById.get(a).doors().size() : 0;
                    int doorsB = roomById.get(b) != null && roomById.get(b).doors() != null ? roomById.get(b).doors().size() : 0;
                    return Integer.compare(doorsB, doorsA);  // Descending order
                })
                .collect(Collectors.toList());

        while (!sortedRemainingRooms.isEmpty() && phase2Attempts < maxPhase2Attempts) {
            phase2Attempts++;

            // Try to place each room in order of priority
            boolean anyPlaced = false;

            for (int i = 0; i < sortedRemainingRooms.size(); i++) {
                Integer roomId = sortedRemainingRooms.get(i);
                if (!remainingRoomIds.contains(roomId)) {
                    continue;  // Already placed
                }

                MapData.RoomMapping room = roomById.get(roomId);
                if (room == null) continue;

                // Try to place this room adjacent to any placed room with door compatibility
                boolean placed = false;

                // Try different positions around existing rooms
                for (Integer placedRoomId : new ArrayList<>(placedRoomIds)) {
                    if (placed) break;

                    Point placedRoomPos = roomPositions.get(placedRoomId);
                    MapData.RoomMapping placedRoom = roomById.get(placedRoomId);

                    // Check if there are compatible doors between the two rooms
                    boolean hasCompatibleDoors = false;
                    String compatibleDirection = null;

                    if (room.doors() != null && placedRoom.doors() != null) {
                        for (MapData.DoorInfo roomDoor : room.doors()) {
                            for (MapData.DoorInfo placedDoor : placedRoom.doors()) {
                                if (isOppositeDirection(roomDoor.direction(), placedDoor.direction())) {
                                    hasCompatibleDoors = true;
                                    compatibleDirection = roomDoor.direction();
                                    break;
                                }
                            }
                            if (hasCompatibleDoors) break;
                        }
                    }

                    // Try positions: left, right, up, down of the placed room
                    int[][] offsets = {
                        {-room.width(), 0},  // Left
                        {placedRoom.width(), 0},  // Right
                        {0, -room.height()},  // Up
                        {0, placedRoom.height()}  // Down
                    };

                    String[] offsetDirections = {"right", "left", "down", "up"};

                    for (int j = 0; j < offsets.length; j++) {
                        int[] offset = offsets[j];
                        String requiredDirection = offsetDirections[j];

                        // Skip if we have compatible door info and this direction doesn't match
                        if (hasCompatibleDoors && compatibleDirection != null &&
                                !compatibleDirection.equals(requiredDirection)) {
                            continue;
                        }

                        int candidateX = placedRoomPos.x + offset[0];
                        int candidateY = placedRoomPos.y + offset[1];

                        if (canPlaceRoomAt(room, candidateX, candidateY, occupiedPositions)) {
                            // Place the room
                            placedRoomIds.add(roomId);
                            roomPositions.put(roomId, new Point(candidateX, candidateY));
                            remainingRoomIds.remove(roomId);
                            markRoomOccupied(room, candidateX, candidateY, occupiedPositions);

                            phase2Placed++;
                            placed = true;
                            anyPlaced = true;

                            if (phase2Placed <= 10) {
                                System.out.println("Phase 2: Placed room " + roomId + " at (" + candidateX + "," + candidateY + ")");
                            }
                            break;
                        }
                    }
                }
            }

            // If no rooms were placed in this pass, we're stuck
            if (!anyPlaced) {
                break;
            }
        }

        System.out.println("Phase 2 placed " + phase2Placed + " additional rooms after " + phase2Attempts + " attempts");
        System.out.println("Total rooms after Phase 2: " + placedRoomIds.size() + "/" + allRooms.size());

        // Phase 3: Remove rooms with null door connections
        System.out.println("Starting Phase 3: Cleanup null doors...");

        // Build temporary map data to check door connections
        List<MapData.RoomMapping> tempRooms = new ArrayList<>();
        for (Integer roomId : placedRoomIds) {
            Point pos = roomPositions.get(roomId);
            MapData.RoomMapping original = roomById.get(roomId);
            tempRooms.add(new MapData.RoomMapping(
                    roomId, pos.x, pos.y, original.width(), original.height(),
                    original.tiles(), original.doors(), original.originalArea(),
                    original.originalArea(), original.subarea(), original.subsubarea(), original.roomName()
            ));
        }

        // Update door connections based on physical adjacency
        tempRooms = updateDoorConnectionTargetsFromGeometry(tempRooms, new ArrayList<>());

        // Iteratively remove rooms with null doors until no more null doors exist
        // This handles the cascading effect where removing a room can create new null doors
        Set<Integer> roomsToRemove = new HashSet<>();
        boolean changed;
        int iteration = 0;

        do {
            changed = false;
            iteration++;

            // Rebuild tempRooms with current set of rooms
            List<MapData.RoomMapping> currentRooms = tempRooms.stream()
                    .filter(room -> !roomsToRemove.contains(room.roomId()))
                    .collect(Collectors.toList());

            // Update door connections again (some may have changed due to removed rooms)
            currentRooms = updateDoorConnectionTargetsFromGeometry(currentRooms, new ArrayList<>());

            // Find rooms with null doors or doors pointing to removed rooms
            for (MapData.RoomMapping room : currentRooms) {
                if (room.doors() != null) {
                    for (MapData.DoorInfo door : room.doors()) {
                        Integer targetRoomId = door.connectsToRoomId();
                        if (targetRoomId == null || roomsToRemove.contains(targetRoomId)) {
                            if (!roomsToRemove.contains(room.roomId()) && room.roomId() != 8) {
                                roomsToRemove.add(room.roomId());
                                changed = true;
                            }
                            break;
                        }
                    }
                }
            }

            tempRooms = currentRooms;
        } while (changed && iteration < 100);

        System.out.println("Cleanup completed after " + iteration + " iterations");
        System.out.println("Removing " + roomsToRemove.size() + " rooms with null doors");

        // Remove rooms with null doors
        placedRoomIds.removeAll(roomsToRemove);

        System.out.println("Final room count after cleanup: " + placedRoomIds.size() + "/" + allRooms.size());

        // Build the final list using tempRooms (which has updated door connections)
        // Filter out rooms that were removed
        List<MapData.RoomMapping> result = tempRooms.stream()
                .filter(room -> placedRoomIds.contains(room.roomId()))
                .collect(Collectors.toList());

        return result;
    }

    /**
     * Get candidate positions for placing a room based on a door position.
     * The new room must have one of its doors at the target position.
     */
    private List<Point> getCandidatePositions(MapData.RoomMapping room, MapData.DoorInfo targetDoor, int doorWorldX, int doorWorldY) {
        List<Point> positions = new ArrayList<>();

        // If the room has doors, try to align one of them with the target position
        if (room.doors() != null && !room.doors().isEmpty()) {
            for (MapData.DoorInfo roomDoor : room.doors()) {
                // Calculate where the room would be if this door is at the target position
                // The door direction should be opposite to the target door direction
                if (isOppositeDirection(roomDoor.direction(), targetDoor.direction())) {
                    // For the candidate room's door to align with the source door:
                    // 1. The candidate door's world position should be at (doorWorldX, doorWorldY)
                    // 2. The room's position is: roomPos = doorWorldPos - doorLocalPos
                    // 3. But we also need to offset the room so the doors are adjacent (touching)

                    // First, calculate room position if door was at the target world position
                    int roomX = doorWorldX - roomDoor.x();
                    int roomY = doorWorldY - roomDoor.y();

                    // Now offset the room based on door direction to make rooms adjacent
                    // Example: If source door is "right" and candidate door is "left":
                    // - Source room's right door is at its right edge
                    // - Candidate room's left door is at its left edge
                    // - We need to place candidate room to the right of source room
                    // - candidate room X = source right edge + 1 = source room X + source width + 1
                    // Wait, that's not right either...

                    // Actually, let me think differently:
                    // doorWorldX/doorWorldY is the position of the SOURCE door in world coordinates
                    // We want to place the candidate room such that its door is adjacent to this door
                    // For a "right" door connecting to a "left" door:
                    // - Source door at (x, y) points right
                    // - Candidate door should be at (x+1, y) pointing left
                    // - So candidate door's world position is (doorWorldX+1, doorWorldY)
                    // - And candidate room's position is (doorWorldX+1 - roomDoor.x, doorWorldY - roomDoor.y)

                    int targetDoorWorldX = doorWorldX;
                    int targetDoorWorldY = doorWorldY;

                    // Offset target door position based on source door direction
                    switch (targetDoor.direction()) {
                        case "left" -> targetDoorWorldX -= 1;
                        case "right" -> targetDoorWorldX += 1;
                        case "up" -> targetDoorWorldY -= 1;
                        case "down" -> targetDoorWorldY += 1;
                    }

                    roomX = targetDoorWorldX - roomDoor.x();
                    roomY = targetDoorWorldY - roomDoor.y();

                    positions.add(new Point(roomX, roomY));
                }
            }
        } else {
            // Room has no doors, just try to place it adjacent
            positions.add(new Point(doorWorldX, doorWorldY));
        }

        return positions;
    }

    /**
     * Check if two door directions are opposite (e.g., left ↔ right).
     */
    private boolean isOppositeDirection(String dir1, String dir2) {
        return switch (dir1) {
            case "left" -> "right".equals(dir2);
            case "right" -> "left".equals(dir2);
            case "up" -> "down".equals(dir2);
            case "down" -> "up".equals(dir2);
            default -> false;
        };
    }

    /**
     * Check if a room can be placed at the given position without overlapping.
     */
    private boolean canPlaceRoomAt(MapData.RoomMapping room, int x, int y, Set<String> occupiedPositions) {
        for (int dy = 0; dy < room.height(); dy++) {
            for (int dx = 0; dx < room.width(); dx++) {
                String posKey = (x + dx) + "," + (y + dy);
                if (occupiedPositions.contains(posKey)) {
                    // System.out.println("  Collision at " + posKey + " for room " + room.roomId());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Place a room at the given position (returns a new RoomMapping with updated position).
     */
    private MapData.RoomMapping placeRoomAt(MapData.RoomMapping room, int x, int y) {
        // Convert tiles to ArrayList
        List<List<Integer>> roomTiles = new ArrayList<>();
        if (room.tiles() != null) {
            for (List<Integer> row : room.tiles()) {
                roomTiles.add(new ArrayList<>(row));
            }
        }

        return new MapData.RoomMapping(
                room.roomId(),
                x,
                y,
                room.width(),
                room.height(),
                roomTiles,
                room.doors(),
                room.originalArea(),
                room.originalArea(),
                room.subarea(),
                room.subsubarea(),
                room.roomName()
        );
    }

    /**
     * Mark all tiles of a room as occupied.
     */
    private void markRoomOccupied(MapData.RoomMapping room, int roomX, int roomY, Set<String> occupiedPositions) {
        for (int y = 0; y < room.height(); y++) {
            for (int x = 0; x < room.width(); x++) {
                String posKey = (roomX + x) + "," + (roomY + y);
                occupiedPositions.add(posKey);
            }
        }
    }

    /**
     * Create a RoomMapping with a specific position.
     * Helper method for door connection checking.
     */
    private MapData.RoomMapping createRoomMappingWithPosition(MapData.RoomMapping original, int roomX, int roomY) {
        // Copy tiles if present
        List<List<Integer>> roomTiles = new ArrayList<>();
        if (original.tiles() != null) {
            for (List<Integer> row : original.tiles()) {
                roomTiles.add(new ArrayList<>(row));
            }
        }

        return new MapData.RoomMapping(
                original.roomId(),
                roomX,
                roomY,
                original.width(),
                original.height(),
                roomTiles,
                original.doors(),  // Keep original door data (will be updated later)
                original.originalArea(),
                original.originalArea(),  // shuffledArea same as original for now
                original.subarea(),
                original.subsubarea(),
                original.roomName()
        );
    }

    /**
     * Build door connections from room door data.
     * Creates DoorConnection objects from the DoorInfo in each room.
     */
    private List<MapData.DoorConnection> buildDoorConnectionsFromRoomData(List<MapData.RoomMapping> rooms) {
        List<MapData.DoorConnection> connections = new ArrayList<>();
        int doorIdCounter = 0;

        for (MapData.RoomMapping room : rooms) {
            if (room.doors() == null) continue;

            for (MapData.DoorInfo door : room.doors()) {
                if (door.connectsToRoomId() == null) continue;

                MapData.DoorType doorType = parseDoorType(door.subtype());
                connections.add(new MapData.DoorConnection(
                        room.roomId(),
                        doorIdCounter,
                        door.connectsToRoomId(),
                        doorIdCounter,
                        true,
                        doorType,
                        door.direction(),
                        door.x(),
                        door.y()
                ));
                doorIdCounter++;
            }
        }

        return connections;
    }

    /**
     * Update door connection targets based on PHYSICAL ADJACENCY.
     * Keeps ORIGINAL door positions from room_geometry.json, but updates target room IDs
     * to point to rooms that are actually adjacent in the current layout.
     */
    private List<MapData.RoomMapping> updateDoorConnectionTargetsFromGeometry(
            List<MapData.RoomMapping> rooms,
            List<MapData.DoorConnection> connections
    ) {
        System.out.println("updateDoorConnectionTargetsFromGeometry: " + rooms.size() + " rooms");

        // Build spatial map for quick adjacency lookups: worldX,worldY -> room
        Map<String, MapData.RoomMapping> roomByPosition = new HashMap<>();
        for (MapData.RoomMapping room : rooms) {
            for (int y = 0; y < room.height(); y++) {
                for (int x = 0; x < room.width(); x++) {
                    int worldX = room.roomX() + x;
                    int worldY = room.roomY() + y;
                    roomByPosition.put(worldX + "," + worldY, room);
                }
            }
        }

        // Update each room's doors based on physical adjacency
        List<MapData.RoomMapping> updatedRooms = new ArrayList<>();

        for (MapData.RoomMapping room : rooms) {
            List<MapData.DoorInfo> updatedDoors = new ArrayList<>();

            // Get original doors from geometry (loaded from room_geometry.json)
            List<MapData.DoorInfo> originalDoors = room.doors();
            if (originalDoors == null || originalDoors.isEmpty()) {
                // No doors in this room
                originalDoors = List.of();
            }

            for (MapData.DoorInfo door : originalDoors) {
                // Calculate door position in world coordinates
                int doorWorldX = room.roomX() + door.x();
                int doorWorldY = room.roomY() + door.y();

                // Find adjacent room based on door direction
                int targetWorldX = doorWorldX;
                int targetWorldY = doorWorldY;

                switch (door.direction()) {
                    case "left" -> targetWorldX -= 1;
                    case "right" -> targetWorldX += 1;
                    case "up" -> targetWorldY -= 1;
                    case "down" -> targetWorldY += 1;
                }

                // Find room at target position
                MapData.RoomMapping targetRoom = roomByPosition.get(targetWorldX + "," + targetWorldY);
                Integer targetRoomId = (targetRoom != null) ? targetRoom.roomId() : null;

                // Create door with same position but updated target
                updatedDoors.add(new MapData.DoorInfo(
                        door.direction(),
                        door.x(),
                        door.y(),
                        door.subtype(),
                        targetRoomId
                ));
            }

            // Convert tiles to ArrayList if needed
            List<List<Integer>> roomTiles = new ArrayList<>();
            if (room.tiles() != null) {
                for (List<Integer> row : room.tiles()) {
                    roomTiles.add(new ArrayList<>(row));
                }
            }

            // Create new RoomMapping with updated door targets
            updatedRooms.add(new MapData.RoomMapping(
                    room.roomId(),
                    room.roomX(),
                    room.roomY(),
                    room.width(),
                    room.height(),
                    roomTiles,
                    updatedDoors,
                    room.originalArea(),
                    room.shuffledArea(),
                    room.subarea(),
                    room.subsubarea(),
                    room.roomName()
            ));
        }

        return updatedRooms;
    }

    /**
     * Shuffle room positions randomly while keeping door data intact.
     * This randomizes the physical layout of the map.
     */
    private List<MapData.RoomMapping> shuffleRoomPositions(List<MapData.RoomMapping> allRooms) {
        List<int[]> roomPositions = new ArrayList<>();  // [x, y] pairs

        for (MapData.RoomMapping room : allRooms) {
            roomPositions.add(new int[]{room.roomX(), room.roomY()});
        }

        // Shuffle the positions
        Collections.shuffle(roomPositions, random);

        // Reassign positions to rooms
        List<MapData.RoomMapping> shuffledRooms = new ArrayList<>();
        for (int i = 0; i < allRooms.size(); i++) {
            MapData.RoomMapping original = allRooms.get(i);
            int[] newPos = roomPositions.get(i);

            // Create new room mapping with shuffled position
            List<List<Integer>> roomTiles = new ArrayList<>();
            if (original.tiles() != null) {
                for (List<Integer> row : original.tiles()) {
                    roomTiles.add(new ArrayList<>(row));
                }
            }

            shuffledRooms.add(new MapData.RoomMapping(
                    original.roomId(),
                    newPos[0],  // Shuffled X
                    newPos[1],  // Shuffled Y
                    original.width(),
                    original.height(),
                    roomTiles,
                    original.doors(),  // Keep original door data
                    original.originalArea(),
                    original.originalArea(),
                    original.subarea(),
                    original.subsubarea(),
                    original.roomName()
            ));
        }

        return shuffledRooms;
    }

    /**
     * Convert DoorType enum to subtype string for DoorInfo.
     */
    private String doorTypeToSubtype(MapData.DoorType doorType) {
        return switch (doorType) {
            case NORMAL -> "normal";
            case RED -> "missile";
            case GREEN -> "super";
            case YELLOW -> "powerbomb";
            case CHARGE -> "charge";
            case ICE -> "ice";
            case WAVE -> "wave";
            case SPAZER -> "spazer";
            case PLASMA -> "plasma";
        };
    }

    /**
     * DEPRECATED: This method uses physical adjacency to create door connections,
     * which is fundamentally flawed. Use buildDoorConnections() instead.
     *
     * Build door connections based on PHYSICAL ADJACENCY after room swapping.
     * This finds which rooms are actually next to each other OR overlapping and creates doors between them.
     * Creates ONE door per adjacent/overlapping room (not per tile).
     *
     * @deprecated Use buildDoorConnections() instead, which uses original door geometry data.
     */
    @Deprecated
    private List<MapData.DoorConnection> buildDoorConnectionsByAdjacency(List<MapData.RoomMapping> rooms) {
        List<MapData.DoorConnection> connections = new ArrayList<>();

        // Track which room pairs have been connected: "roomId1,roomId2" -> true
        Set<String> connectedPairs = new HashSet<>();
        int doorIdCounter = 0;

        // For each room, check all other rooms for adjacency or overlap
        for (MapData.RoomMapping room : rooms) {
            for (MapData.RoomMapping other : rooms) {
                if (room.roomId() == other.roomId()) {
                    continue;  // Skip self
                }

                // Check if already connected this pair
                String pairKey = Math.min(room.roomId(), other.roomId()) + "," +
                                Math.max(room.roomId(), other.roomId());
                if (connectedPairs.contains(pairKey)) {
                    continue;  // Already created door for this pair
                }

                // Check if rooms are adjacent OR overlapping
                String relationship = getRoomRelationship(room, other);
                if (relationship != null) {
                    connectedPairs.add(pairKey);

                    // Calculate door position based on relationship AND overlap
                    Point doorPos = calculateDoorPosition(room, other, relationship);

                    connections.add(new MapData.DoorConnection(
                            room.roomId(),
                            doorIdCounter,
                            other.roomId(),
                            doorIdCounter,
                            true,
                            MapData.DoorType.NORMAL,
                            relationship,  // Use relationship as direction
                            doorPos.x,
                            doorPos.y
                    ));
                    doorIdCounter++;
                }
            }
        }

        return connections;
    }

    /**
     * Generate a map with vanilla area assignments (no shuffling).
     * Useful for testing or when map randomization is disabled.
     */
    public MapData generateVanillaMap() {
        List<MapData.RoomMapping> rooms = loadRoomPositions();

        // Build door connections from ORIGINAL door geometry data
        List<MapData.DoorConnection> doors = buildDoorConnections(rooms);

        // Update door connection targets using geometry data
        List<MapData.RoomMapping> roomsWithDoors = updateDoorConnectionTargetsFromGeometry(rooms, doors);

        // No shuffling - use original assignments
        List<Integer> originalAreas = rooms.stream()
                .map(MapData.RoomMapping::originalArea)
                .collect(Collectors.toList());

        List<Integer> originalSubareas = rooms.stream()
                .map(MapData.RoomMapping::subarea)
                .collect(Collectors.toList());

        List<Integer> originalSubsubareas = rooms.stream()
                .map(MapData.RoomMapping::subsubarea)
                .collect(Collectors.toList());

        // Identity mappings
        Map<Integer, Integer> identityAreaMapping = new HashMap<>();
        for (int i = 0; i < NUM_AREAS; i++) {
            identityAreaMapping.put(i, i);
        }

        Map<Integer, Integer> identitySubareaMapping = new HashMap<>();
        for (int i = 0; i < NUM_SUBAREAS; i++) {
            identitySubareaMapping.put(i, i);
        }

        return new MapData(
                seed,
                roomsWithDoors,
                doors,
                originalAreas,
                originalSubareas,
                originalSubsubareas,
                identityAreaMapping,
                identitySubareaMapping
        );
    }
}
