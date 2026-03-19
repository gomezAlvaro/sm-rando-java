package com.maprando.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum DoorLockType {
    GRAY("gray"),
    WALL("wall"),
    RED("red"),
    GREEN("green"),
    YELLOW("yellow"),
    CHARGE("charge"),
    ICE("ice"),
    WAVE("wave"),
    SPAZER("spazer"),
    PLASMA("plasma");

    private final String value;

    DoorLockType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
