package com.maprando.patch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SuffixTree {

    private static final int DATA_END = Integer.MAX_VALUE;
    private static final int NODE_UNDEFINED = -1;
    private static final int EDGE_UNDEFINED = -1;

    public final byte[] data;
    private int dataSize = 0;
    private final List<Node> nodes = new ArrayList<>();
    private Position cut;

    private static class Position {
        int nodeId;
        int edgeId;
        int length;

        Position(int nodeId, int edgeId, int length) {
            this.nodeId = nodeId;
            this.edgeId = edgeId;
            this.length = length;
        }
    }

    private static class Node {
        final List<Edge> edges = new ArrayList<>();
        final Map<Byte, Integer> edgeLookup = new HashMap<>();
        int suffixLink = NODE_UNDEFINED;
    }

    private static class Edge {
        int start;
        int end;
        int nodeId;

        Edge(int start, int end, int nodeId) {
            this.start = start;
            this.end = end;
            this.nodeId = nodeId;
        }
    }

    public SuffixTree(byte[] inputData) {
        this.data = new byte[inputData.length];
        nodes.add(new Node());
        cut = new Position(0, EDGE_UNDEFINED, 0);

        for (byte b : inputData) {
            pushByte(b);
        }
    }

    public record MatchResult(int start, int matchLength) {}

    public MatchResult findLongestPrefix(byte[] query, int queryStart, int queryEnd) {
        int nodeId = 0;
        int pos = 0;
        int queryLen = queryEnd - queryStart;
        if (queryLen <= 0) {
            return new MatchResult(0, 0);
        }

        while (true) {
            Node node = nodes.get(nodeId);
            byte b = query[queryStart + pos];
            Integer edgeId = node.edgeLookup.get(b);
            if (edgeId != null) {
                Edge edge = node.edges.get(edgeId);
                int edgeLen = edge.end == DATA_END ? dataSize - edge.start : edge.end - edge.start;
                for (int i = 0; i < edgeLen; i++) {
                    boolean wholeQueryMatch = pos + i == queryLen;
                    boolean reachedDataEnd = edge.start + i == dataSize;
                    if (wholeQueryMatch || reachedDataEnd || data[edge.start + i] != query[queryStart + pos + i]) {
                        return new MatchResult(edge.start - pos, pos + i);
                    }
                }

                pos += edgeLen;
                nodeId = edge.nodeId;
                if (pos == queryLen) {
                    return new MatchResult(edge.end - queryLen, queryLen);
                }
            } else {
                if (node.edges.isEmpty()) {
                    if (nodeId == 0 && dataSize == 0) {
                        return new MatchResult(0, 0);
                    }
                    throw new IllegalStateException("Unexpected empty edges");
                }
                Edge edge = node.edges.get(0);
                return new MatchResult(edge.start - pos, pos);
            }
        }
    }

    private Position followData(int nodeId, int start, int length) {
        while (true) {
            if (length == 0) {
                return new Position(nodeId, EDGE_UNDEFINED, 0);
            }
            Node node = nodes.get(nodeId);
            byte b = data[start];
            int edgeId = node.edgeLookup.get(b);
            Edge edge = node.edges.get(edgeId);
            int edgeLen = edge.end == DATA_END ? dataSize - edge.start : edge.end - edge.start;
            if (length < edgeLen) {
                return new Position(nodeId, edgeId, length);
            }
            start += edgeLen;
            length -= edgeLen;
            nodeId = edge.nodeId;
        }
    }

    private void pushByte(byte b) {
        data[dataSize] = b;
        dataSize++;

        while (true) {
            int numNodes = nodes.size();
            Node node = nodes.get(cut.nodeId);
            int nodeSuffixLink = node.suffixLink;

            if (cut.length == 0) {
                if (node.edgeLookup.containsKey(b)) {
                    cut.edgeId = node.edgeLookup.get(b);
                    cut.length = 1;
                    Edge edge = node.edges.get(cut.edgeId);
                    int edgeLen = edge.end == DATA_END ? dataSize - edge.start : edge.end - edge.start;
                    if (edgeLen == 1) {
                        cut.length = 0;
                        cut.nodeId = edge.nodeId;
                        cut.edgeId = EDGE_UNDEFINED;
                    }
                    return;
                } else {
                    int edgeId = node.edges.size();
                    Edge edge = new Edge(dataSize - 1, DATA_END, NODE_UNDEFINED);
                    node.edges.add(edge);
                    node.edgeLookup.put(b, edgeId);

                    if (node.suffixLink == NODE_UNDEFINED) {
                        assert cut.nodeId == 0;
                        cut.edgeId = edgeId;
                        cut.length = 1;
                        return;
                    } else {
                        cut = new Position(node.suffixLink, EDGE_UNDEFINED, 0);
                    }
                }
            } else {
                Edge edge = node.edges.get(cut.edgeId);
                int edgeStart = edge.start;

                if (edge.start + cut.length == dataSize - 1) {
                    assert edge.end == DATA_END;

                    if (nodeSuffixLink == NODE_UNDEFINED) {
                        assert cut.nodeId == 0;
                        int newStart = edge.start + 1;
                        int newLength = cut.length - 1;
                        cut = followData(0, newStart, newLength);
                    } else {
                        cut = followData(nodeSuffixLink, edgeStart, cut.length);
                    }
                } else {
                    byte c = data[edge.start + cut.length];
                    if (c == b) {
                        cut.length++;
                        int edgeLen = edge.end == DATA_END ? dataSize - edge.start : edge.end - edge.start;
                        if (cut.length == edgeLen) {
                            cut.length = 0;
                            cut.nodeId = edge.nodeId;
                            cut.edgeId = EDGE_UNDEFINED;
                        }
                        return;
                    } else {
                        Edge tailEdge = new Edge(edge.start + cut.length, edge.end, edge.nodeId);
                        Edge leafEdge = new Edge(dataSize - 1, DATA_END, NODE_UNDEFINED);

                        Node newNode = new Node();
                        newNode.edges.add(tailEdge);
                        newNode.edges.add(leafEdge);
                        newNode.edgeLookup.put(c, 0);
                        newNode.edgeLookup.put(b, 1);

                        int newNodeId = numNodes;
                        edge.end = edge.start + cut.length;
                        edge.nodeId = newNodeId;

                        Position newCut;
                        if (nodeSuffixLink == NODE_UNDEFINED) {
                            assert cut.nodeId == 0;
                            int newStart = edge.start + 1;
                            int newLength = cut.length - 1;
                            newCut = followData(0, newStart, newLength);
                        } else {
                            newCut = followData(nodeSuffixLink, edgeStart, cut.length);
                        }

                        if (newCut.length == 0) {
                            newNode.suffixLink = newCut.nodeId;
                        } else {
                            newNode.suffixLink = numNodes + 1;
                        }

                        nodes.add(newNode);
                        cut = newCut;
                    }
                }
            }
        }
    }
}
