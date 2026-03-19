package com.maprando.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MapLiquidType {
    NONE("none"),
    WATER("water"),
    LAVA("lava"),
    ACID("acid");

    private final String value;

    MapLiquidType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
