package com.maprando.patch;

import com.maprando.model.Direction;
import java.util.*;

public class MapTilesData {
    public static class RoomCoord {
        public int roomId;
        public int x;
        public int y;
        public Direction dir;
        public RoomCoord(int r, int x, int y) { this(r, x, y, null); }
        public RoomCoord(int r, int x, int y, Direction d) { this.roomId=r; this.x=x; this.y=y; this.dir=d; }
    }

    public static List<RoomCoord> getGrayDoors() {
        return Arrays.asList(
            new RoomCoord(12, 0, 0, Direction.LEFT), new RoomCoord(12, 2, 0, Direction.RIGHT),
            new RoomCoord(82, 0, 0, Direction.LEFT), new RoomCoord(82, 5, 0, Direction.RIGHT),
            new RoomCoord(219, 0, 0, Direction.LEFT),
            new RoomCoord(139, 0, 0, Direction.LEFT), new RoomCoord(139, 2, 0, Direction.RIGHT),
            new RoomCoord(84, 0, 1, Direction.LEFT), new RoomCoord(84, 1, 1, Direction.RIGHT),
            new RoomCoord(158, 0, 0, Direction.LEFT),
            new RoomCoord(193, 0, 1, Direction.LEFT), new RoomCoord(193, 1, 0, Direction.RIGHT),
            new RoomCoord(142, 0, 0, Direction.RIGHT), new RoomCoord(142, 0, 1, Direction.LEFT),
            new RoomCoord(19, 0, 0, Direction.LEFT),
            new RoomCoord(57, 0, 2, Direction.DOWN),
            new RoomCoord(122, 3, 0, Direction.UP),
            new RoomCoord(185, 0, 0, Direction.LEFT),
            new RoomCoord(150, 1, 1, Direction.RIGHT)
        );
    }

    public static final int[][] AREA_TRANSITION_0 = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 3, 3, 3, 3, 0, 0}, {0, 3, 3, 0, 0, 3, 3, 0},
        {0, 3, 3, 0, 0, 0, 0, 0}, {0, 3, 3, 0, 0, 0, 0, 0}, {0, 3, 3, 0, 0, 3, 3, 0},
        {0, 0, 3, 3, 3, 3, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0}
    };
    public static final int[][] AREA_TRANSITION_1 = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {0, 3, 3, 3, 3, 3, 0, 0}, {0, 3, 3, 0, 0, 3, 3, 0},
        {0, 3, 3, 3, 3, 3, 0, 0}, {0, 3, 3, 0, 0, 3, 3, 0}, {0, 3, 3, 0, 0, 3, 3, 0},
        {0, 3, 3, 3, 3, 3, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0}
    };
    public static final int[][] AREA_TRANSITION_2 = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {0, 3, 3, 0, 0, 0, 3, 0}, {0, 3, 3, 3, 0, 0, 3, 0},
        {0, 3, 3, 3, 3, 0, 3, 0}, {0, 3, 3, 0, 3, 3, 3, 0}, {0, 3, 3, 0, 0, 3, 3, 0},
        {0, 3, 3, 0, 0, 0, 3, 0}, {0, 0, 0, 0, 0, 0, 0, 0}
    };
    public static final int[][] AREA_TRANSITION_3 = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {0, 3, 3, 0, 0, 0, 3, 0}, {0, 3, 3, 0, 0, 0, 3, 0},
        {0, 3, 3, 0, 3, 0, 3, 0}, {0, 3, 3, 3, 3, 3, 3, 0}, {0, 3, 3, 3, 0, 3, 3, 0},
        {0, 3, 3, 0, 0, 0, 3, 0}, {0, 0, 0, 0, 0, 0, 0, 0}
    };
    public static final int[][] AREA_TRANSITION_4 = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {0, 3, 3, 0, 0, 0, 3, 0}, {0, 3, 3, 3, 0, 3, 3, 0},
        {0, 3, 3, 3, 3, 3, 3, 0}, {0, 3, 3, 0, 3, 0, 3, 0}, {0, 3, 3, 0, 0, 0, 3, 0},
        {0, 3, 3, 0, 0, 0, 3, 0}, {0, 0, 0, 0, 0, 0, 0, 0}
    };
    public static final int[][] AREA_TRANSITION_5 = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {0, 3, 3, 3, 3, 3, 3, 0}, {0, 0, 0, 3, 3, 0, 0, 0},
        {0, 0, 0, 3, 3, 0, 0, 0}, {0, 0, 0, 3, 3, 0, 0, 0}, {0, 0, 0, 3, 3, 0, 0, 0},
        {0, 0, 0, 3, 3, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0}
    };
    public static final int[][] DIR_RIGHT = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 3, 0, 0}, {0, 0, 0, 0, 0, 3, 3, 0},
        {0, 3, 3, 3, 3, 3, 3, 3}, {0, 3, 3, 3, 3, 3, 3, 3}, {0, 0, 0, 0, 0, 3, 3, 0},
        {0, 0, 0, 0, 0, 3, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0}
    };
    public static final int[][] DIR_LEFT = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 3, 0, 0, 0, 0, 0}, {0, 3, 3, 0, 0, 0, 0, 0},
        {3, 3, 3, 3, 3, 3, 3, 0}, {3, 3, 3, 3, 3, 3, 3, 0}, {0, 3, 3, 0, 0, 0, 0, 0},
        {0, 0, 3, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0}
    };
    public static final int[][] DIR_DOWN = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 3, 3, 0, 0, 0}, {0, 0, 0, 3, 3, 0, 0, 0},
        {0, 0, 0, 3, 3, 0, 0, 0}, {0, 0, 0, 3, 3, 0, 0, 0}, {0, 3, 3, 3, 3, 3, 3, 0},
        {0, 0, 3, 3, 3, 3, 0, 0}, {0, 0, 0, 3, 3, 0, 0, 0}
    };
    public static final int[][] DIR_UP = {
        {0, 0, 0, 3, 3, 0, 0, 0}, {0, 0, 3, 3, 3, 3, 0, 0}, {0, 3, 3, 3, 3, 3, 3, 0},
        {0, 0, 0, 3, 3, 0, 0, 0}, {0, 0, 0, 3, 3, 0, 0, 0}, {0, 0, 0, 3, 3, 0, 0, 0},
        {0, 0, 0, 3, 3, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0}
    };
    public static final int[][] BLACK = {
        {0, 0, 0, 0, 0, 0, 0, 0}, {1, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0},
        {1, 0, 0, 0, 0, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0}, {1, 0, 0, 0, 0, 0, 0, 0},
        {0, 0, 0, 0, 0, 0, 0, 0}, {1, 0, 1, 0, 1, 0, 1, 0}
    };
    public static final int[][] ELEVATOR = {
        {0, 3, 1, 4, 4, 1, 3, 0}, {0, 3, 4, 4, 4, 4, 3, 0}, {0, 3, 1, 4, 4, 1, 3, 0},
        {0, 3, 4, 4, 4, 4, 3, 0}, {0, 3, 1, 4, 4, 1, 3, 0}, {0, 3, 4, 4, 4, 4, 3, 0},
        {0, 3, 1, 4, 4, 1, 3, 0}, {0, 3, 4, 4, 4, 4, 3, 0}
    };
}
