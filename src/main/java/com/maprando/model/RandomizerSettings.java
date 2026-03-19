package com.maprando.model;

public class RandomizerSettings {
    public QualityOfLifeSettings qualityOfLifeSettings = new QualityOfLifeSettings();
    public OtherSettings otherSettings = new OtherSettings();

    public static class QualityOfLifeSettings {
        public ItemMarkers itemMarkers = ItemMarkers.SIMPLE;
        public InitialMapRevealSettings initialMapRevealSettings = new InitialMapRevealSettings();
        public boolean roomOutlineRevealed = false;
        public DisableETankSetting disableableEtanks = DisableETankSetting.OFF;
        public boolean escapeEnemiesCleared = false;
        public MotherBrainFight motherBrainFight = MotherBrainFight.Standard;
    }

    public enum MotherBrainFight {
        Standard, Skip
    }

    public static class OtherSettings {
        public boolean ultraLowQol = false;
        public DoorLocksSize doorLocksSize = DoorLocksSize.SMALL;
        public MapStationReveal mapStationReveal = MapStationReveal.FULL;
    }

    public enum ItemMarkers {
        SIMPLE, MAJORS, UNIQUES, THREE_TIERED, FOUR_TIERED
    }

    public enum DisableETankSetting {
        OFF, ON
    }

    public enum DoorLocksSize {
        SMALL, LARGE
    }

    public enum MapStationReveal {
        PARTIAL, FULL
    }

    public static class InitialMapRevealSettings {
        public MapRevealLevel areaTransitions = MapRevealLevel.NO;
        public MapRevealLevel mapStations = MapRevealLevel.NO;
        public MapRevealLevel saveStations = MapRevealLevel.NO;
        public MapRevealLevel refillStations = MapRevealLevel.NO;
        public MapRevealLevel ship = MapRevealLevel.NO;
        public MapRevealLevel items1 = MapRevealLevel.NO;
        public MapRevealLevel items2 = MapRevealLevel.NO;
        public MapRevealLevel items3 = MapRevealLevel.NO;
        public MapRevealLevel items4 = MapRevealLevel.NO;
        public MapRevealLevel objectives = MapRevealLevel.NO;
        public MapRevealLevel other = MapRevealLevel.NO;
        public boolean allAreas = false;
    }

    public enum MapRevealLevel {
        NO, PARTIAL, FULL
    }
}
