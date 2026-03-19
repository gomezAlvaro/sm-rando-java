package com.maprando.model;

import java.util.*;

public class GameData {
    public List<Integer> areaMapPtrs = new ArrayList<>();
    public List<Integer> roomPtrs = new ArrayList<>();
    public Map<Integer, Integer> roomPtrById = new HashMap<>(); // roomId -> roomPtr
    public Map<Integer, Integer> roomIdxByPtr = new HashMap<>(); // roomPtr -> roomIdx
    public Map<Integer, Integer> rawRoomIdByPtr = new HashMap<>();
    public List<RoomGeometry> roomGeometry = new ArrayList<>();
    public Map<String, int[]> roomAndDoorIdxsByDoorPtrPair = new HashMap<>(); // DoorPtrPair -> int[]{roomIdx, doorIdx}
    public Map<Integer, Integer> roomIdByPtr = new HashMap<>();
    public List<int[]> itemLocations = new ArrayList<>(); // List of {roomId, nodeId}
    public Map<String, Integer> nodePtrMap = new HashMap<>(); // "roomId-nodeId" -> node_ptr
    public List<MapTileData> mapTileData = new ArrayList<>();

    public static class RoomGeometry {
        public int room_id;
        public int rom_address;
        public Integer twin_rom_address;
        public List<RoomGeometryDoor> doors = new ArrayList<>();
        public List<RoomGeometryItem> items = new ArrayList<>();
    }

    public static class RoomGeometryDoor {
        public int x;
        public int y;
        public Direction direction;
    }

    public static class RoomGeometryItem {
        public int addr;
        public int x;
        public int y;
    }

    public static class MapTileData {
        public int room_id;
        public List<MapTile> map_tiles = new ArrayList<>();
    }
}
