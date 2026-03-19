package com.maprando.model;

import java.util.List;

/**
 * Data structures for escape timer calculations.
 * Ported from maprando/src/randomize/escape_timer.rs
 */
public class EscapeTimerData {

    /**
     * Represents a node in the escape route graph (a specific door in a room).
     */
    public static class VertexKey {
        public final int roomId;
        public final int doorIdx;

        public VertexKey(int roomId, int doorIdx) {
            this.roomId = roomId;
            this.doorIdx = doorIdx;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof VertexKey)) {
                return false;
            }
            VertexKey other = (VertexKey) obj;
            return roomId == other.roomId && doorIdx == other.doorIdx;
        }

        @Override
        public int hashCode() {
            return roomId * 31 + doorIdx;
        }

        @Override
        public String toString() {
            return "(" + roomId + "," + doorIdx + ")";
        }
    }

    /**
     * Cost in time units for traversing between vertices.
     */
    public static class Cost implements Comparable<Cost> {
        public final float value;

        public Cost(float value) {
            this.value = value;
        }

        @Override
        public int compareTo(Cost other) {
            return Float.compare(this.value, other.value);
        }

        public Cost add(Cost other) {
            return new Cost(this.value + other.value);
        }

        public boolean isZero() {
            return value == 0.0f;
        }

        @Override
        public String toString() {
            return String.format("%.2f", value);
        }
    }

    /**
     * A node in the spoiler escape route with coordinates.
     */
    public static class SpoilerEscapeRouteNode {
        public final String room;
        public final String node;
        public final int x;
        public final int y;

        public SpoilerEscapeRouteNode(String room, String node, int x, int y) {
            this.room = room;
            this.node = node;
            this.x = x;
            this.y = y;
        }
    }

    /**
     * An entry in the spoiler escape route.
     */
    public static class SpoilerEscapeRouteEntry {
        public final SpoilerEscapeRouteNode from;
        public final SpoilerEscapeRouteNode to;
        public final float time;

        public SpoilerEscapeRouteEntry(SpoilerEscapeRouteNode from, SpoilerEscapeRouteNode to, float time) {
            this.from = from;
            this.to = to;
            this.time = time;
        }
    }

    /**
     * Complete spoiler escape data.
     */
    public static class SpoilerEscape {
        public final float difficultyMultiplier;
        public final float rawTimeSeconds;
        public final float finalTimeSeconds;
        public final List<SpoilerEscapeRouteEntry> animalsRoute;
        public final List<SpoilerEscapeRouteEntry> shipRoute;

        public SpoilerEscape(
            float difficultyMultiplier,
            float rawTimeSeconds,
            float finalTimeSeconds,
            List<SpoilerEscapeRouteEntry> animalsRoute,
            List<SpoilerEscapeRouteEntry> shipRoute
        ) {
            this.difficultyMultiplier = difficultyMultiplier;
            this.rawTimeSeconds = rawTimeSeconds;
            this.finalTimeSeconds = finalTimeSeconds;
            this.animalsRoute = animalsRoute;
            this.shipRoute = shipRoute;
        }
    }

    /**
     * Graph structure for room/door connections.
     */
    public static class RoomDoorGraph {
        public List<VertexKey> vertices;
        public List<List<GraphEdge>> successors;
        public int motherBrainVertexId;
        public int shipVertexId;
        public int animalsVertexId;

        public RoomDoorGraph() {
            vertices = new java.util.ArrayList<>();
            successors = new java.util.ArrayList<>();
            motherBrainVertexId = -1;
            shipVertexId = -1;
            animalsVertexId = -1;
        }

        public static class GraphEdge {
            public final int targetVertexId;
            public final Cost cost;

            public GraphEdge(int targetVertexId, Cost cost) {
                this.targetVertexId = targetVertexId;
                this.cost = cost;
            }
        }
    }
}
