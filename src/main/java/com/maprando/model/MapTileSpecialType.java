package com.maprando.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;

public class MapTileSpecialType {
    public enum Type {
        SLOPE_UP_FLOOR_LOW("slopeUpFloorLow"),
        SLOPE_UP_FLOOR_HIGH("slopeUpFloorHigh"),
        SLOPE_UP_CEILING_LOW("slopeUpCeilingLow"),
        SLOPE_UP_CEILING_HIGH("slopeUpCeilingHigh"),
        SLOPE_DOWN_FLOOR_LOW("slopeDownFloorLow"),
        SLOPE_DOWN_FLOOR_HIGH("slopeDownFloorHigh"),
        SLOPE_DOWN_CEILING_LOW("slopeDownCeilingLow"),
        SLOPE_DOWN_CEILING_HIGH("slopeDownCeilingHigh"),
        TUBE("tube"),
        ELEVATOR("elevator"),
        BLACK("black"),
        AREA_TRANSITION("areaTransition");

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
            if (value != null && value.startsWith("areaTransition")) {
                return AREA_TRANSITION;
            }
            return null;
        }
    }

    private Type type;
    private Integer areaIndex;
    private Direction direction;

    public MapTileSpecialType() {
    }

    public MapTileSpecialType(Type type) {
        this.type = type;
    }

    public MapTileSpecialType(Integer areaIndex, Direction direction) {
        this.type = Type.AREA_TRANSITION;
        this.areaIndex = areaIndex;
        this.direction = direction;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Integer getAreaIndex() {
        return areaIndex;
    }

    public void setAreaIndex(Integer areaIndex) {
        this.areaIndex = areaIndex;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
