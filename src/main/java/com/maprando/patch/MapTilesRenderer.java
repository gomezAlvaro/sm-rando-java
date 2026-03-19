package com.maprando.patch;

import com.maprando.model.*;
import java.util.*;

public class MapTilesRenderer {

    public static int[][] hflipTile(int[][] tile) {
        int[][] out = new int[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                out[y][x] = tile[y][7 - x];
            }
        }
        return out;
    }

    public static int[][] vflipTile(int[][] tile) {
        int[][] out = new int[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                out[y][x] = tile[7 - y][x];
            }
        }
        return out;
    }

    public static int[][] diagonalFlipTile(int[][] tile) {
        int[][] out = new int[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                out[y][x] = tile[x][y];
            }
        }
        return out;
    }

    public static int[][] readTile4bpp(Rom rom, int baseAddr, int idx) {
        int[][] out = new int[8][8];
        for (int y = 0; y < 8; y++) {
            int addr = baseAddr + idx * 32 + y * 2;
            int data0 = rom.readU8(addr);
            int data1 = rom.readU8(addr + 1);
            int data2 = rom.readU8(addr + 16);
            int data3 = rom.readU8(addr + 17);
            for (int x = 0; x < 8; x++) {
                int bit0 = (data0 >> (7 - x)) & 1;
                int bit1 = (data1 >> (7 - x)) & 1;
                int bit2 = (data2 >> (7 - x)) & 1;
                int bit3 = (data3 >> (7 - x)) & 1;
                out[y][x] = bit0 | (bit1 << 1) | (bit2 << 2) | (bit3 << 3);
            }
        }
        return out;
    }

    public static void writeTile4bpp(Rom rom, int baseAddr, int[][] data) {
        for (int y = 0; y < 8; y++) {
            int addr = baseAddr + y * 2;
            int data0 = 0, data1 = 0, data2 = 0, data3 = 0;
            for(int x = 0; x < 8; x++) {
                data0 |= (data[y][x] & 1) << (7 - x);
                data1 |= ((data[y][x] >> 1) & 1) << (7 - x);
                data2 |= ((data[y][x] >> 2) & 1) << (7 - x);
                data3 |= ((data[y][x] >> 3) & 1) << (7 - x);
            }
            rom.writeU8(addr, data0);
            rom.writeU8(addr + 1, data1);
            rom.writeU8(addr + 16, data2);
            rom.writeU8(addr + 17, data3);
        }
    }

    // Additional rendering helper logic...
}
