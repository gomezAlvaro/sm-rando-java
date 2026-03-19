package com.maprando.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public class MapTileEdge {
    public enum Type {
        EMPTY("empty"),
        QOL_EMPTY("qolEmpty"),
        PASSAGE("passage"),
        QOL_PASSAGE("qolPassage"),
        DOOR("door"),
        QOL_DOOR("qolDoor"),
        WALL("wall"),
        QOL_WALL("qolWall"),
        ELEVATOR_ENTRANCE("elevatorEntrance"),
        SAND("sand"),
        QOL_SAND("qolSand"),
        LOCKED_DOOR("lockedDoor");

        private final String value;

        Type(String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
        
        @JsonCreator
        public static Type fromString(String value) {
            for (Type type : Type.values()) {
                if (type.value.equals(value)) {
                    return type;
                }
            }
            if (value != null && value.startsWith("lockedDoor")) {
                return LOCKED_DOOR;
            }
            return null;
        }
    }

    private Type type = Type.EMPTY;
    private DoorLockType lockType;

    public MapTileEdge() {
    }

    public MapTileEdge(Type type) {
        this.type = type;
    }

    public MapTileEdge(DoorLockType lockType) {
        this.type = Type.LOCKED_DOOR;
        this.lockType = lockType;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public DoorLockType getLockType() {
        return lockType;
    }

    public void setLockType(DoorLockType lockType) {
        this.lockType = lockType;
    }
}
