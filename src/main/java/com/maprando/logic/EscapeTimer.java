package com.maprando.logic;

import com.maprando.model.*;
import java.util.*;

/**
 * Escape timer calculation system using graph traversal and Dijkstra's algorithm.
 * Computes shortest escape path from Mother Brain to ship (with optional animal rescue).
 * Ported from maprando/src/randomize/escape_timer.rs
 */
public class EscapeTimer {

    /**
     * Parse in-game time format (X.YY where X is integer seconds and YY is frames).
     * Converts to seconds.
     *
     * @param rawTime Time in game format
     * @return Time in seconds
     */
    public static float parseInGameTime(float rawTime) {
        float intPart = (float) Math.floor(rawTime);
        float fracPart = rawTime - intPart;
        float frames = Math.round(fracPart * 100.0f);
        //assert frames < 60.0f : "Invalid frame count: " + frames;
        return intPart + frames / 60.0f;
    }

    /**
     * Check if an escape condition requirement is satisfied based on settings, difficulty, and tech.
     *
     * @param req The requirement to check
     * @param settings Randomizer settings
     * @param difficulty Difficulty configuration
     * @return true if requirement is satisfied
     */
    public static boolean isRequirementSatisfied(
        String req,
        RandomizerSettings settings,
        DifficultyConfig difficulty
    ) {
        // Map requirement strings to checks
        switch (req) {
            case "EnemiesCleared":
                return settings.qualityOfLifeSettings.escapeEnemiesCleared;
            case "CanMidAirMorph":
                return difficulty.hasTech("can_mid_air_morph");
            case "CanUsePowerBombs":
                return settings.qualityOfLifeSettings.motherBrainFight ==
                       RandomizerSettings.MotherBrainFight.Skip;
            case "CanAcidDive":
                return difficulty.hasTech("can_suitless_lava_dive");
            case "CanKago":
                return difficulty.hasTech("can_kago");
            case "CanMoonfall":
                return difficulty.hasTech("can_moonfall");
            case "CanOffCameraShot":
                return difficulty.hasTech("can_off_screen_super_shot");
            case "CanReverseGate":
                return difficulty.hasTech("can_hyper_gate_shot");
            case "CanHeroShot":
                return difficulty.hasTech("can_hero_shot");
            case "CanOneTapShortcharge":
                return difficulty.shineChargeTiles <= 25.0f;
            default:
                return false;
        }
    }

    /**
     * Build the base room/door graph from game data.
     *
     * @param gameData Game data containing escape timings
     * @param settings Randomizer settings
     * @param difficulty Difficulty configuration
     * @return Base room door graph
     */
    public static EscapeTimerData.RoomDoorGraph getBaseRoomDoorGraph(
        GameData gameData,
        RandomizerSettings settings,
        DifficultyConfig difficulty
    ) {
        EscapeTimerData.RoomDoorGraph graph = new EscapeTimerData.RoomDoorGraph();

        // Build vertices from escape timing data
        // Note: This assumes gameData has escapeTimings structure similar to Rust version
        // For now, creating a basic structure - this will need to be integrated with actual GameData

        int shipVertexId = -1;
        int animalsVertexId = -1;
        int motherBrainVertexId = -1;

        // TODO: Integrate with actual GameData.escapeTimings when available
        // For now, creating placeholder structure

        return graph;
    }

    /**
     * Build the full room/door graph including map connections.
     *
     * @param gameData Game data
     * @param map Map data
     * @param settings Randomizer settings
     * @param difficulty Difficulty configuration
     * @return Full room door graph
     */
    public static EscapeTimerData.RoomDoorGraph getFullRoomDoorGraph(
        GameData gameData,
        MapData map,
        RandomizerSettings settings,
        DifficultyConfig difficulty
    ) {
        EscapeTimerData.RoomDoorGraph base = getBaseRoomDoorGraph(gameData, settings, difficulty);
        EscapeTimerData.RoomDoorGraph fullGraph = new EscapeTimerData.RoomDoorGraph();

        // Copy base graph
        fullGraph.vertices = new ArrayList<>(base.vertices);
        fullGraph.successors = new ArrayList<>(base.successors);
        fullGraph.motherBrainVertexId = base.motherBrainVertexId;
        fullGraph.shipVertexId = base.shipVertexId;
        fullGraph.animalsVertexId = base.animalsVertexId;

        // Add map connections (doors)
        // TODO: Integrate with actual map.doors when available
        // For each door connection in map, add zero-cost edges

        return fullGraph;
    }

    /**
     * Find shortest path between two vertices using Dijkstra's algorithm.
     *
     * @param src Source vertex ID
     * @param dst Destination vertex ID
     * @param graph Room door graph
     * @return List of (vertexId, cost) pairs representing the path
     */
    public static List<Map.Entry<Integer, EscapeTimerData.Cost>> getShortestPath(
        int src,
        int dst,
        EscapeTimerData.RoomDoorGraph graph
    ) {
        // Dijkstra's algorithm
        int n = graph.vertices.size();
        float[] dist = new float[n];
        int[] parent = new int[n];
        boolean[] visited = new boolean[n];

        Arrays.fill(dist, Float.MAX_VALUE);
        Arrays.fill(parent, -1);

        dist[src] = 0.0f;

        for (int i = 0; i < n; i++) {
            // Find unvisited vertex with minimum distance
            int u = -1;
            float minDist = Float.MAX_VALUE;
            for (int j = 0; j < n; j++) {
                if (!visited[j] && dist[j] < minDist) {
                    minDist = dist[j];
                    u = j;
                }
            }

            if (u == -1 || u == dst) {
                break;
            }

            visited[u] = true;

            // Relax edges
            for (EscapeTimerData.RoomDoorGraph.GraphEdge edge : graph.successors.get(u)) {
                int v = edge.targetVertexId;
                float newDist = dist[u] + edge.cost.value;
                if (newDist < dist[v]) {
                    dist[v] = newDist;
                    parent[v] = u;
                }
            }
        }

        // Reconstruct path
        List<Map.Entry<Integer, EscapeTimerData.Cost>> path = new ArrayList<>();
        int current = dst;
        while (current != src && current != -1) {
            path.add(new AbstractMap.SimpleEntry<>(current, new EscapeTimerData.Cost(dist[current])));
            current = parent[current];
        }

        if (current == -1) {
            throw new RuntimeException("No escape path found");
        }

        path.add(new AbstractMap.SimpleEntry<>(src, new EscapeTimerData.Cost(dist[src])));
        Collections.reverse(path);

        return path;
    }

    /**
     * Compute escape data including time and route.
     *
     * @param gameData Game data
     * @param map Map data
     * @param settings Randomizer settings
     * @param saveAnimals Whether to include animal rescue route
     * @param difficulty Difficulty configuration
     * @return Spoiler escape data
     */
    public static EscapeTimerData.SpoilerEscape computeEscapeData(
        GameData gameData,
        MapData map,
        RandomizerSettings settings,
        boolean saveAnimals,
        DifficultyConfig difficulty
    ) {
        EscapeTimerData.RoomDoorGraph graph = getFullRoomDoorGraph(
            gameData, map, settings, difficulty
        );

        List<EscapeTimerData.SpoilerEscapeRouteEntry> animalsSpoiler;
        List<EscapeTimerData.SpoilerEscapeRouteEntry> shipSpoiler;
        EscapeTimerData.Cost cost;

        if (saveAnimals) {
            // Path: Mother Brain -> Animals -> Ship
            List<Map.Entry<Integer, EscapeTimerData.Cost>> animalsPath =
                getShortestPath(graph.motherBrainVertexId, graph.animalsVertexId, graph);

            animalsSpoiler = getSpoilerEscapeRoute(animalsPath, graph, gameData, map);

            List<Map.Entry<Integer, EscapeTimerData.Cost>> shipPath =
                getShortestPath(graph.animalsVertexId, graph.shipVertexId, graph);

            shipSpoiler = getSpoilerEscapeRoute(shipPath, graph, gameData, map);

            EscapeTimerData.Cost animalsCost = animalsPath.get(animalsPath.size() - 1).getValue();
            EscapeTimerData.Cost shipCost = shipPath.get(shipPath.size() - 1).getValue();
            cost = animalsCost.add(shipCost);
        } else {
            // Direct path: Mother Brain -> Ship
            animalsSpoiler = null;

            List<Map.Entry<Integer, EscapeTimerData.Cost>> shipPath =
                getShortestPath(graph.motherBrainVertexId, graph.shipVertexId, graph);

            shipSpoiler = getSpoilerEscapeRoute(shipPath, graph, gameData, map);
            cost = shipPath.get(shipPath.size() - 1).getValue();
        }

        float rawTimeSeconds = cost.value;
        float finalTimeSeconds;

        if (difficulty.escapeTimerMultiplier < 1.1f) {
            finalTimeSeconds = (float) Math.ceil(rawTimeSeconds);
        } else {
            finalTimeSeconds = (float) (Math.ceil(rawTimeSeconds / 5.0) * 5.0);
        }

        if (finalTimeSeconds > 5995.0f) {
            finalTimeSeconds = 5995.0f;
        }

        System.out.println("Escape time: " + finalTimeSeconds);

        return new EscapeTimerData.SpoilerEscape(
            difficulty.escapeTimerMultiplier,
            rawTimeSeconds,
            finalTimeSeconds,
            animalsSpoiler,
            shipSpoiler
        );
    }

    /**
     * Convert path to spoiler escape route format.
     *
     * @param path Path from Dijkstra
     * @param graph Room door graph
     * @param gameData Game data
     * @param map Map data
     * @return List of spoiler route entries
     */
    private static List<EscapeTimerData.SpoilerEscapeRouteEntry> getSpoilerEscapeRoute(
        List<Map.Entry<Integer, EscapeTimerData.Cost>> path,
        EscapeTimerData.RoomDoorGraph graph,
        GameData gameData,
        MapData map
    ) {
        List<EscapeTimerData.SpoilerEscapeRouteEntry> out = new ArrayList<>();

        for (int i = 0; i < path.size() - 1; i++) {
            Map.Entry<Integer, EscapeTimerData.Cost> srcEntry = path.get(i);
            Map.Entry<Integer, EscapeTimerData.Cost> dstEntry = path.get(i + 1);

            EscapeTimerData.Cost srcCost = srcEntry.getValue();
            EscapeTimerData.Cost dstCost = dstEntry.getValue();

            // Skip if cost didn't change (zero-cost transition)
            if (srcCost.value == dstCost.value) {
                continue;
            }

            EscapeTimerData.SpoilerEscapeRouteNode from =
                getVertexName(srcEntry.getKey(), graph, gameData, map);
            EscapeTimerData.SpoilerEscapeRouteNode to =
                getVertexName(dstEntry.getKey(), graph, gameData, map);

            out.add(new EscapeTimerData.SpoilerEscapeRouteEntry(from, to, dstCost.value));
        }

        return out;
    }

    /**
     * Get human-readable name for a vertex.
     *
     * @param vertexId Vertex ID
     * @param graph Room door graph
     * @param gameData Game data
     * @param map Map data
     * @return Spoiler escape route node
     */
    private static EscapeTimerData.SpoilerEscapeRouteNode getVertexName(
        int vertexId,
        EscapeTimerData.RoomDoorGraph graph,
        GameData gameData,
        MapData map
    ) {
        // TODO: Implement with actual game data lookup
        // For now, return placeholder
        return new EscapeTimerData.SpoilerEscapeRouteNode("Unknown Room", "Unknown Node", 0, 0);
    }
}
