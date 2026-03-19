package com.maprando.model;

import java.util.*;

public class MapData {
    public List<int[]> rooms = new ArrayList<>(); // {x, y}
    public List<Integer> area = new ArrayList<>(); // roomIdx -> areaIdx
    public List<Boolean> roomMask = new ArrayList<>(); // roomIdx -> is included
    public List<Object[]> doors = new ArrayList<>(); // {src_ptr_pair, dst_ptr_pair, bidirectional}
}
