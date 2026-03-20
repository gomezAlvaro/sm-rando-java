package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * JSON data model for locations loaded from external files.
 */
public class LocationData {
    @JsonProperty("locations")
    private List<LocationDefinition> locations;

    public List<LocationDefinition> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationDefinition> locations) {
        this.locations = locations;
    }

    /**
     * Represents a single location definition from JSON.
     */
    public static class LocationDefinition {
        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("region")
        private String region;

        @JsonProperty("area")
        private String area;

        @JsonProperty("requirements")
        private List<String> requirements;

        @JsonProperty("isEarlyGame")
        private boolean isEarlyGame;

        @JsonProperty("isBoss")
        private boolean isBoss;

        @JsonProperty("romAddress")
        private String romAddress;

        @JsonProperty("roomId")
        private Integer roomId;

        @JsonProperty("nodeId")
        private Integer nodeId;

        @JsonProperty("pcAddress")
        private Integer pcAddress;

        @JsonProperty("roomName")
        private String roomName;

        // Getters
        public String getId() { return id; }
        public String getName() { return name; }
        public String getRegion() { return region; }
        public String getArea() { return area; }
        public List<String> getRequirements() { return requirements; }
        public boolean isEarlyGame() { return isEarlyGame; }
        public boolean isBoss() { return isBoss; }
        public String getRomAddress() { return romAddress; }
        public Integer getRoomId() { return roomId; }
        public Integer getNodeId() { return nodeId; }
        public Integer getPcAddress() { return pcAddress; }
        public String getRoomName() { return roomName; }

        // Setters
        public void setId(String id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setRegion(String region) { this.region = region; }
        public void setArea(String area) { this.area = area; }
        public void setRequirements(List<String> requirements) { this.requirements = requirements; }
        public void setEarlyGame(boolean earlyGame) { isEarlyGame = earlyGame; }
        public void setBoss(boolean boss) { isBoss = boss; }
        public void setRomAddress(String romAddress) { this.romAddress = romAddress; }
        public void setRoomId(Integer roomId) { this.roomId = roomId; }
        public void setNodeId(Integer nodeId) { this.nodeId = nodeId; }
        public void setPcAddress(Integer pcAddress) { this.pcAddress = pcAddress; }
        public void setRoomName(String roomName) { this.roomName = roomName; }
    }
}
