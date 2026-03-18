package com.maprando.traversal;

import java.util.List;

/**
 * Represents a single location in the game graph.
 * Each node corresponds to a location where items can be placed.
 */
public class GameGraphNode {

    private final String id;
    private final String name;
    private final String region;
    private final String area;
    private final List<String> requirements;
    private final boolean earlyGame;
    private final boolean boss;

    public GameGraphNode(String id, String name, String region, String area,
                        List<String> requirements, boolean earlyGame, boolean boss) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.area = area;
        this.requirements = requirements;
        this.earlyGame = earlyGame;
        this.boss = boss;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getArea() {
        return area;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public boolean isEarlyGame() {
        return earlyGame;
    }

    public boolean isBoss() {
        return boss;
    }

    /**
     * Check if this node has no requirements (always accessible).
     */
    public boolean hasNoRequirements() {
        return requirements == null || requirements.isEmpty();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GameGraphNode that = (GameGraphNode) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "GameGraphNode{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                '}';
    }
}