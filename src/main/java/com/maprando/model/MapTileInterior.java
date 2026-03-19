package com.maprando.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MapTileInterior {
    EMPTY("empty"),
    ITEM("item"),
    DOUBLE_ITEM("doubleItem"),
    HIDDEN_ITEM("hiddenItem"),
    ELEVATOR_PLATFORM_HIGH("elevatorPlatformHigh"),
    ELEVATOR_PLATFORM_LOW("elevatorPlatformLow"),
    SAVE_STATION("saveStation"),
    MAP_STATION("mapStation"),
    ENERGY_REFILL("energyRefill"),
    AMMO_REFILL("ammoRefill"),
    DOUBLE_REFILL("doubleRefill"),
    SHIP("ship"),
    EVENT("event"),
    OBJECTIVE("objective"),
    AMMO_ITEM("ammoItem"),
    MEDIUM_ITEM("mediumItem"),
    MAJOR_ITEM("majorItem");

    private final String value;

    MapTileInterior(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public boolean isItem() {
        return this == ITEM || this == DOUBLE_ITEM || this == HIDDEN_ITEM || 
               this == AMMO_ITEM || this == MEDIUM_ITEM || this == MAJOR_ITEM;
    }
}
