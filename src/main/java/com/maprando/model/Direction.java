package com.maprando.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Direction {
    LEFT("left"),
    RIGHT("right"),
    UP("up"),
    DOWN("down");

    private final String value;

    Direction(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
