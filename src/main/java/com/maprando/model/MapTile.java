package com.maprando.model;

public class MapTile {
    public int[] coords = new int[2];
    public Integer area;
    public MapTileEdge left = new MapTileEdge(MapTileEdge.Type.EMPTY);
    public MapTileEdge right = new MapTileEdge(MapTileEdge.Type.EMPTY);
    public MapTileEdge top = new MapTileEdge(MapTileEdge.Type.EMPTY);
    public MapTileEdge bottom = new MapTileEdge(MapTileEdge.Type.EMPTY);
    public MapTileInterior interior = MapTileInterior.EMPTY;
    public boolean heated = false;
    public MapLiquidType liquidType = MapLiquidType.NONE;
    public Float liquidLevel;
    public MapTileSpecialType specialType;
    public boolean faded = false;

    public MapTile() {
    }
}
