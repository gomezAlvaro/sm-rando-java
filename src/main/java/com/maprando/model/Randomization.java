package com.maprando.model;

import java.util.*;

/**
 * Randomization data structures.
 * This class holds data for ROM randomization.
 */
public class Randomization {
    public MapData map = new MapData();
    public List<LockedDoor> lockedDoors = new ArrayList<>();
    public List<Objective> objectives = new ArrayList<>();
    public List<String> itemPlacement = new ArrayList<>();

    public static class LockedDoor {
        public String srcPtrPair;
        public String dstPtrPair;
        public boolean bidirectional;
        public DoorType doorType;
    }

    public enum DoorType {
        BLUE, GRAY, WALL, RED, GREEN, YELLOW, CHARGE, ICE, WAVE, SPAZER, PLASMA
    }

    public enum Objective {
        KRAID, PHANTOON, DRAYGON, RIDLEY, SPORE_SPAWN, CROCOMIRE, BOTWOON,
        GOLDEN_TORIZO, METROID_ROOM_1, METROID_ROOM_2, METROID_ROOM_3, METROID_ROOM_4,
        BOMB_TORIZO, BOWLING_STATUE, ACID_CHOZO_STATUE, PIT_ROOM, BABY_KRAID_ROOM,
        PLASMA_ROOM, METAL_PIRATES_ROOM
    }

    // Item constants for randomization
    public static final String ITEM_NOTHING = "NOTHING";
    public static final String ITEM_MISSILE = "MISSILE";
    public static final String ITEM_SUPER = "SUPER";
    public static final String ITEM_POWER_BOMB = "POWER_BOMB";
    public static final String ITEM_ETANK = "ETANK";
    public static final String ITEM_RESERVE_TANK = "RESERVE_TANK";

    /**
     * Checks if an item is unique (non-consumable).
     * @param itemId The item ID to check
     * @return true if the item is unique
     */
    public static boolean isUnique(String itemId) {
        return !ITEM_NOTHING.equals(itemId) &&
               !ITEM_MISSILE.equals(itemId) &&
               !ITEM_SUPER.equals(itemId) &&
               !ITEM_POWER_BOMB.equals(itemId) &&
               !ITEM_ETANK.equals(itemId) &&
               !ITEM_RESERVE_TANK.equals(itemId);
    }
}
