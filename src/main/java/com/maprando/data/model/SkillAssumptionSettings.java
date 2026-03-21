package com.maprando.data.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Skill assumption settings loaded from skill-presets/*.json.
 * Aligned with Rust MapRandomizer's SkillAssumptionSettings structure.
 *
 * These settings control which tech abilities are assumed available during
 * randomization, along with various leniency settings for tricks.
 */
public class SkillAssumptionSettings {

    @JsonProperty("preset")
    private String preset;

    @JsonProperty("shinespark_tiles")
    private float shinesparkTiles;

    @JsonProperty("heated_shinespark_tiles")
    private float heatedShinesparkTiles;

    @JsonProperty("speed_ball_tiles")
    private float speedBallTiles;

    @JsonProperty("shinecharge_leniency_frames")
    private int shinechargeLeniencyFrames;

    @JsonProperty("resource_multiplier")
    private float resourceMultiplier;

    @JsonProperty("farm_time_limit")
    private float farmTimeLimit;

    @JsonProperty("gate_glitch_leniency")
    private int gateGlitchLeniency;

    @JsonProperty("door_stuck_leniency")
    private int doorStuckLeniency;

    @JsonProperty("bomb_into_cf_leniency")
    private int bombIntoCfLeniency;

    @JsonProperty("jump_into_cf_leniency")
    private int jumpIntoCfLeniency;

    @JsonProperty("flash_suit_distance")
    private int flashSuitDistance;

    @JsonProperty("blue_suit_distance")
    private int blueSuitDistance;

    @JsonProperty("spike_suit_leniency")
    private int spikeSuitLeniency;

    @JsonProperty("spike_xmode_leniency")
    private int spikeXmodeLeniency;

    @JsonProperty("spike_speed_keep_leniency")
    private int spikeSpeedKeepLeniency;

    @JsonProperty("elevator_cf_leniency")
    private int elevatorCfLeniency;

    @JsonProperty("crystal_spark_leniency")
    private int crystalSparkLeniency;

    @JsonProperty("phantoon_proficiency")
    private float phantoonProficiency;

    @JsonProperty("draygon_proficiency")
    private float draygonProficiency;

    @JsonProperty("ridley_proficiency")
    private float ridleyProficiency;

    @JsonProperty("botwoon_proficiency")
    private float botwoonProficiency;

    @JsonProperty("mother_brain_proficiency")
    private float motherBrainProficiency;

    @JsonProperty("escape_timer_multiplier")
    private float escapeTimerMultiplier;

    @JsonProperty("tech_settings")
    private List<TechSetting> techSettings;

    @JsonProperty("notable_settings")
    private List<NotableSetting> notableSettings;

    // Getters and setters

    public String getPreset() {
        return preset;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public float getShinesparkTiles() {
        return shinesparkTiles;
    }

    public void setShinesparkTiles(float shinesparkTiles) {
        this.shinesparkTiles = shinesparkTiles;
    }

    public float getHeatedShinesparkTiles() {
        return heatedShinesparkTiles;
    }

    public void setHeatedShinesparkTiles(float heatedShinesparkTiles) {
        this.heatedShinesparkTiles = heatedShinesparkTiles;
    }

    public float getSpeedBallTiles() {
        return speedBallTiles;
    }

    public void setSpeedBallTiles(float speedBallTiles) {
        this.speedBallTiles = speedBallTiles;
    }

    public int getShinechargeLeniencyFrames() {
        return shinechargeLeniencyFrames;
    }

    public void setShinechargeLeniencyFrames(int shinechargeLeniencyFrames) {
        this.shinechargeLeniencyFrames = shinechargeLeniencyFrames;
    }

    public float getResourceMultiplier() {
        return resourceMultiplier;
    }

    public void setResourceMultiplier(float resourceMultiplier) {
        this.resourceMultiplier = resourceMultiplier;
    }

    public float getFarmTimeLimit() {
        return farmTimeLimit;
    }

    public void setFarmTimeLimit(float farmTimeLimit) {
        this.farmTimeLimit = farmTimeLimit;
    }

    public int getGateGlitchLeniency() {
        return gateGlitchLeniency;
    }

    public void setGateGlitchLeniency(int gateGlitchLeniency) {
        this.gateGlitchLeniency = gateGlitchLeniency;
    }

    public int getDoorStuckLeniency() {
        return doorStuckLeniency;
    }

    public void setDoorStuckLeniency(int doorStuckLeniency) {
        this.doorStuckLeniency = doorStuckLeniency;
    }

    public int getBombIntoCfLeniency() {
        return bombIntoCfLeniency;
    }

    public void setBombIntoCfLeniency(int bombIntoCfLeniency) {
        this.bombIntoCfLeniency = bombIntoCfLeniency;
    }

    public int getJumpIntoCfLeniency() {
        return jumpIntoCfLeniency;
    }

    public void setJumpIntoCfLeniency(int jumpIntoCfLeniency) {
        this.jumpIntoCfLeniency = jumpIntoCfLeniency;
    }

    public int getFlashSuitDistance() {
        return flashSuitDistance;
    }

    public void setFlashSuitDistance(int flashSuitDistance) {
        this.flashSuitDistance = flashSuitDistance;
    }

    public int getBlueSuitDistance() {
        return blueSuitDistance;
    }

    public void setBlueSuitDistance(int blueSuitDistance) {
        this.blueSuitDistance = blueSuitDistance;
    }

    public int getSpikeSuitLeniency() {
        return spikeSuitLeniency;
    }

    public void setSpikeSuitLeniency(int spikeSuitLeniency) {
        this.spikeSuitLeniency = spikeSuitLeniency;
    }

    public int getSpikeXmodeLeniency() {
        return spikeXmodeLeniency;
    }

    public void setSpikeXmodeLeniency(int spikeXmodeLeniency) {
        this.spikeXmodeLeniency = spikeXmodeLeniency;
    }

    public int getSpikeSpeedKeepLeniency() {
        return spikeSpeedKeepLeniency;
    }

    public void setSpikeSpeedKeepLeniency(int spikeSpeedKeepLeniency) {
        this.spikeSpeedKeepLeniency = spikeSpeedKeepLeniency;
    }

    public int getElevatorCfLeniency() {
        return elevatorCfLeniency;
    }

    public void setElevatorCfLeniency(int elevatorCfLeniency) {
        this.elevatorCfLeniency = elevatorCfLeniency;
    }

    public int getCrystalSparkLeniency() {
        return crystalSparkLeniency;
    }

    public void setCrystalSparkLeniency(int crystalSparkLeniency) {
        this.crystalSparkLeniency = crystalSparkLeniency;
    }

    public float getPhantoonProficiency() {
        return phantoonProficiency;
    }

    public void setPhantoonProficiency(float phantoonProficiency) {
        this.phantoonProficiency = phantoonProficiency;
    }

    public float getDraygonProficiency() {
        return draygonProficiency;
    }

    public void setDraygonProficiency(float draygonProficiency) {
        this.draygonProficiency = draygonProficiency;
    }

    public float getRidleyProficiency() {
        return ridleyProficiency;
    }

    public void setRidleyProficiency(float ridleyProficiency) {
        this.ridleyProficiency = ridleyProficiency;
    }

    public float getBotwoonProficiency() {
        return botwoonProficiency;
    }

    public void setBotwoonProficiency(float botwoonProficiency) {
        this.botwoonProficiency = botwoonProficiency;
    }

    public float getMotherBrainProficiency() {
        return motherBrainProficiency;
    }

    public void setMotherBrainProficiency(float motherBrainProficiency) {
        this.motherBrainProficiency = motherBrainProficiency;
    }

    public float getEscapeTimerMultiplier() {
        return escapeTimerMultiplier;
    }

    public void setEscapeTimerMultiplier(float escapeTimerMultiplier) {
        this.escapeTimerMultiplier = escapeTimerMultiplier;
    }

    public List<TechSetting> getTechSettings() {
        return techSettings;
    }

    public void setTechSettings(List<TechSetting> techSettings) {
        this.techSettings = techSettings;
    }

    public List<NotableSetting> getNotableSettings() {
        return notableSettings;
    }

    public void setNotableSettings(List<NotableSetting> notableSettings) {
        this.notableSettings = notableSettings;
    }

    /**
     * Inner class for tech settings.
     */
    public static class TechSetting {
        @JsonProperty("id")
        private int id;

        @JsonProperty("name")
        private String name;

        @JsonProperty("enabled")
        private boolean enabled;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    /**
     * Inner class for notable settings.
     */
    public static class NotableSetting {
        @JsonProperty("room_id")
        private int roomId;

        @JsonProperty("notable_id")
        private int notableId;

        @JsonProperty("enabled")
        private boolean enabled;

        public int getRoomId() {
            return roomId;
        }

        public void setRoomId(int roomId) {
            this.roomId = roomId;
        }

        public int getNotableId() {
            return notableId;
        }

        public void setNotableId(int notableId) {
            this.notableId = notableId;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    @Override
    public String toString() {
        return String.format("SkillAssumptionSettings{preset='%s', techSettings=%d, notableSettings=%d}",
            preset, techSettings != null ? techSettings.size() : 0, notableSettings != null ? notableSettings.size() : 0);
    }
}
