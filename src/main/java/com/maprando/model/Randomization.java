package com.maprando.model;

import java.util.*;

public class Randomization {
    public MapData map = new MapData();
    public List<LockedDoor> lockedDoors = new ArrayList<>();
    public List<Objective> objectives = new ArrayList<>();
    public List<Item> itemPlacement = new ArrayList<>();

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

    public enum Item {
        NOTHING, MISSILE, SUPER, POWER_BOMB, ETANK, RESERVE_TANK;
        public boolean isUnique() {
            return this != NOTHING && this != MISSILE && this != SUPER && 
                   this != POWER_BOMB && this != ETANK && this != RESERVE_TANK;
        }
    }
}
