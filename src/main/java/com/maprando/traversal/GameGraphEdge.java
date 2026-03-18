package com.maprando.traversal;

/**
 * Represents a connection between two locations in the game graph.
 * In this simplified model, edges represent potential connections
 * rather than explicit paths, with requirements determining actual reachability.
 */
public class GameGraphEdge {

    private final GameGraphNode from;
    private final GameGraphNode to;
    private final String requirement;

    public GameGraphEdge(GameGraphNode from, GameGraphNode to, String requirement) {
        this.from = from;
        this.to = to;
        this.requirement = requirement;
    }

    public GameGraphEdge(GameGraphNode from, GameGraphNode to) {
        this(from, to, null);
    }

    public GameGraphNode getFrom() {
        return from;
    }

    public GameGraphNode getTo() {
        return to;
    }

    public String getRequirement() {
        return requirement;
    }

    public boolean hasRequirement() {
        return requirement != null && !requirement.isEmpty();
    }

    @Override
    public String toString() {
        return "GameGraphEdge{" +
                "from=" + from.getId() +
                ", to=" + to.getId() +
                (hasRequirement() ? ", requirement='" + requirement + '\'' : "") +
                '}';
    }
}