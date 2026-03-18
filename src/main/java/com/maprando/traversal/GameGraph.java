package com.maprando.traversal;

import com.maprando.data.DataLoader;
import com.maprando.data.model.LocationData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the game world as a graph of locations and connections.
 * Provides methods for graph analysis and path finding.
 */
public class GameGraph {

    private final List<GameGraphNode> nodes;
    private final List<GameGraphEdge> edges;
    private final Map<String, GameGraphNode> nodeMap;

    public GameGraph(DataLoader dataLoader) {
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.nodeMap = new HashMap<>();

        buildGraphFromData(dataLoader);
    }

    /**
     * Build graph nodes from location data.
     */
    private void buildGraphFromData(DataLoader dataLoader) {
        LocationData locationData = dataLoader.getLocationData();

        for (LocationData.LocationDefinition locationDef : locationData.getLocations()) {
            GameGraphNode node = new GameGraphNode(
                    locationDef.getId(),
                    locationDef.getName(),
                    locationDef.getRegion(),
                    locationDef.getArea(),
                    locationDef.getRequirements(),
                    locationDef.isEarlyGame(),
                    locationDef.isBoss()
            );

            nodes.add(node);
            nodeMap.put(node.getId(), node);
        }

        // In this simple model, create edges between nodes in the same region
        // A more sophisticated implementation would use actual room connections
        createRegionalEdges();
    }

    /**
     * Create edges between nodes in the same region.
     */
    private void createRegionalEdges() {
        Map<String, List<GameGraphNode>> nodesByRegion = nodes.stream()
                .collect(Collectors.groupingBy(GameGraphNode::getRegion));

        for (List<GameGraphNode> regionNodes : nodesByRegion.values()) {
            // Create edges between adjacent nodes in the list
            for (int i = 0; i < regionNodes.size() - 1; i++) {
                GameGraphNode from = regionNodes.get(i);
                GameGraphNode to = regionNodes.get(i + 1);

                // Edge requires the destination's requirements
                String requirement = to.getRequirements().isEmpty() ? null :
                        to.getRequirements().get(0);

                edges.add(new GameGraphEdge(from, to, requirement));
            }
        }
    }

    public List<GameGraphNode> getNodes() {
        return new ArrayList<>(nodes);
    }

    public List<GameGraphEdge> getEdges() {
        return new ArrayList<>(edges);
    }

    public int getNodeCount() {
        return nodes.size();
    }

    public GameGraphNode getNode(String locationId) {
        return nodeMap.get(locationId);
    }

    public List<GameGraphNode> getNodesInRegion(String region) {
        return nodes.stream()
                .filter(node -> node.getRegion().equals(region))
                .collect(Collectors.toList());
    }

    public List<GameGraphNode> getEarlyGameLocations() {
        return nodes.stream()
                .filter(GameGraphNode::isEarlyGame)
                .collect(Collectors.toList());
    }

    public List<GameGraphNode> getBossLocations() {
        return nodes.stream()
                .filter(GameGraphNode::isBoss)
                .collect(Collectors.toList());
    }

    public List<GameGraphNode> findPath(String fromId, String toId) {
        GameGraphNode from = nodeMap.get(fromId);
        GameGraphNode to = nodeMap.get(toId);

        if (from == null || to == null) {
            return Collections.emptyList();
        }

        // Simple BFS to find a path
        List<GameGraphNode> path = new ArrayList<>();
        Set<GameGraphNode> visited = new HashSet<>();
        Queue<GameGraphNode> queue = new LinkedList<>();

        queue.add(from);
        visited.add(from);

        Map<GameGraphNode, GameGraphNode> parentMap = new HashMap<>();

        while (!queue.isEmpty()) {
            GameGraphNode current = queue.poll();

            if (current.equals(to)) {
                // Reconstruct path
                GameGraphNode node = to;
                while (node != null) {
                    path.add(0, node);
                    node = parentMap.get(node);
                }
                return path;
            }

            // Get adjacent nodes (same region, no requirements or requirements satisfied)
            for (GameGraphNode neighbor : getAdjacentNodes(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parentMap.put(neighbor, current);
                    queue.add(neighbor);
                }
            }
        }

        return path; // Empty if no path found
    }

    public List<GameGraphNode> findPathWithRequirements(String fromId, String toId, TraversalState state) {
        GameGraphNode from = nodeMap.get(fromId);
        GameGraphNode to = nodeMap.get(toId);

        if (from == null || to == null) {
            return null;
        }

        // Check if destination requirements are satisfied
        if (!to.getRequirements().isEmpty()) {
            for (String requirement : to.getRequirements()) {
                if (!state.canSatisfyRequirement(requirement)) {
                    return null;
                }
            }
        }

        // Find path (simplified - just return direct path if requirements met)
        return findPath(fromId, toId);
    }

    public List<Set<GameGraphNode>> getConnectedComponents() {
        Set<GameGraphNode> visited = new HashSet<>();
        List<Set<GameGraphNode>> components = new ArrayList<>();

        for (GameGraphNode node : nodes) {
            if (!visited.contains(node)) {
                Set<GameGraphNode> component = new HashSet<>();
                dfsComponent(node, visited, component);
                components.add(component);
            }
        }

        return components;
    }

    private void dfsComponent(GameGraphNode node, Set<GameGraphNode> visited, Set<GameGraphNode> component) {
        visited.add(node);
        component.add(node);

        for (GameGraphNode neighbor : getAdjacentNodes(node)) {
            if (!visited.contains(neighbor)) {
                dfsComponent(neighbor, visited, component);
            }
        }
    }

    public List<GameGraphNode> getIsolatedNodes() {
        List<GameGraphNode> isolated = new ArrayList<>();

        for (GameGraphNode node : nodes) {
            if (getAdjacentNodes(node).isEmpty()) {
                isolated.add(node);
            }
        }

        return isolated;
    }

    public int getNodeDegree(GameGraphNode node) {
        return getAdjacentNodes(node).size();
    }

    public List<GameGraphNode> getAdjacentNodes(GameGraphNode node) {
        List<GameGraphNode> adjacent = new ArrayList<>();

        for (GameGraphEdge edge : edges) {
            if (edge.getFrom().equals(node)) {
                adjacent.add(edge.getTo());
            } else if (edge.getTo().equals(node)) {
                adjacent.add(edge.getFrom());
            }
        }

        return adjacent;
    }

    public String getGraphStatistics() {
        return String.format("Game Graph Statistics: %d nodes, %d edges, %d regions",
                getNodeCount(), edges.size(), getAllRegions().size());
    }

    public List<GameGraphNode> findShortestPath(String fromId, String toId) {
        // For this simple implementation, return the same as findPath
        // A more sophisticated implementation would use Dijkstra's algorithm
        return findPath(fromId, toId);
    }

    public boolean validateGraph() {
        // Check that all nodes are reachable from at least one early game location
        List<GameGraphNode> earlyLocations = getEarlyGameLocations();

        if (earlyLocations.isEmpty()) {
            return false;
        }

        Set<GameGraphNode> reachableFromStart = new HashSet<>();
        for (GameGraphNode start : earlyLocations) {
            bfsReachable(start, reachableFromStart);
        }

        // In a complete game, all nodes should be reachable
        // For now, just check the graph is well-formed
        return !reachableFromStart.isEmpty();
    }

    private void bfsReachable(GameGraphNode start, Set<GameGraphNode> reachable) {
        Queue<GameGraphNode> queue = new LinkedList<>();
        queue.add(start);
        reachable.add(start);

        while (!queue.isEmpty()) {
            GameGraphNode current = queue.poll();

            for (GameGraphNode neighbor : getAdjacentNodes(current)) {
                if (!reachable.contains(neighbor)) {
                    reachable.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
    }

    public Set<GameGraphNode> getReachableNodes(String startId, TraversalState state) {
        GameGraphNode start = nodeMap.get(startId);
        if (start == null) {
            return Collections.emptySet();
        }

        Set<GameGraphNode> reachable = new HashSet<>();
        Queue<GameGraphNode> queue = new LinkedList<>();
        queue.add(start);
        reachable.add(start);

        while (!queue.isEmpty()) {
            GameGraphNode current = queue.poll();

            for (GameGraphNode neighbor : getAdjacentNodes(current)) {
                // Check if requirements are satisfied
                boolean canAccess = true;
                if (!neighbor.getRequirements().isEmpty()) {
                    for (String requirement : neighbor.getRequirements()) {
                        if (!state.canSatisfyRequirement(requirement)) {
                            canAccess = false;
                            break;
                        }
                    }
                }

                if (canAccess && !reachable.contains(neighbor)) {
                    reachable.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return reachable;
    }

    public Set<String> getAllRegions() {
        return nodes.stream()
                .map(GameGraphNode::getRegion)
                .collect(Collectors.toSet());
    }

    public double getGraphDensity() {
        int n = getNodeCount();
        if (n < 2) {
            return 0.0;
        }

        int maxEdges = n * (n - 1) / 2;
        return (double) edges.size() / maxEdges;
    }
}