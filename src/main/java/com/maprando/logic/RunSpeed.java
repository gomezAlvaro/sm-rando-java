package com.maprando.logic;

/**
 * Run speed calculations based on runway distance and shortcharge skill.
 * Ported from maprando/src/randomize/run_speed.rs
 */
public class RunSpeed {
    /**
     * Runway distance travelled (in subpixels) by frame, with dash held the whole time,
     * with SpeedBooster equipped.
     *
     * Table values from original Rust implementation.
     */
    private static final int[] RUN_SPEED_TABLE = {
        0x14, 0x1C, 0x28, 0x38, 0x4C, 0x64, 0x80, 0xA0, 0xC4, 0xEC, 0x123, 0x15B, 0x194, 0x1CE, 0x209,
        0x245, 0x282, 0x2C0, 0x2FF, 0x33F, 0x380, 0x3C2, 0x405, 0x449, 0x48E, 0x4D4, 0x51B, 0x563,
        0x5AC, 0x5F6, 0x641, 0x68D, 0x6DA, 0x728, 0x777, 0x7C7, 0x818, 0x86A, 0x8BD, 0x911, 0x966,
        0x9BC, 0xA13, 0xA6B, 0xAC4, 0xB1E, 0xB79, 0xBD5, 0xC32, 0xC90, 0xCEF, 0xD4F, 0xDB0, 0xE12,
        0xE75, 0xED9, 0xF3E, 0xFA4, 0x100B, 0x1073, 0x10DC, 0x1146, 0x11B1, 0x121D, 0x128A, 0x12F8,
        0x1367, 0x13D7, 0x1448, 0x14BA, 0x152D, 0x15A1, 0x1616, 0x168C, 0x1703, 0x177B, 0x17F4, 0x186E,
        0x18E9, 0x1965, 0x19E2, 0x1A60, 0x1ADF, 0x1B5F, 0x1BE0, 0x1C62, 0x1CE5, 0x1D69, 0x1DEE, 0x1E74,
        0x1EFB, 0x1F83, 0x200C, 0x2096, 0x2121, 0x21AD, 0x223A, 0x22C8, 0x2357, 0x23E7, 0x2478, 0x250A,
        0x259D, 0x2631, 0x26C6, 0x275C, 0x27F3, 0x288B, 0x2924, 0x29BE, 0x2A59, 0x2AF5
    };

    /**
     * Linear interpolation for table lookups.
     *
     * @param x The x value to interpolate
     * @param table Array of (x, y) pairs
     * @return Interpolated y value
     */
    private static float linearInterpolate(float x, int[][] table) {
        if (x <= table[0][0]) {
            return table[0][1];
        }
        if (x >= table[table.length - 1][0]) {
            return table[table.length - 1][1];
        }

        // Binary search for the correct interval
        int i = 0;
        int j = table.length - 1;
        while (i < j) {
            int mid = (i + j) / 2;
            if (table[mid][0] < x) {
                i = mid + 1;
            } else {
                j = mid;
            }
        }

        // Find the interval containing x
        int idx = i;
        if (table[i][0] > x && i > 0) {
            idx = i - 1;
        }

        float x0 = table[idx][0];
        float x1 = table[idx + 1][0];
        float y0 = table[idx][1];
        float y1 = table[idx + 1][1];

        return (x - x0) / (x1 - x0) * (y1 - y0) + y0;
    }

    /**
     * Maximum extra run speed (in pixels per frame) obtainable by running on a given length of runway
     * and jumping before the end of it, with SpeedBooster equipped.
     *
     * @param runwayTiles Length of runway in tiles
     * @return Maximum extra run speed in pixels per frame
     */
    public static float getMaxExtraRunSpeed(float runwayTiles) {
        int runwaySubpixels = (int) (runwayTiles * 256.0f);

        // Binary search in RUN_SPEED_TABLE
        int runFrames = 0;
        int left = 0;
        int right = RUN_SPEED_TABLE.length - 1;

        while (left <= right) {
            int mid = (left + right) / 2;
            if (RUN_SPEED_TABLE[mid] < runwaySubpixels) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        runFrames = left;
        if (runFrames >= RUN_SPEED_TABLE.length) {
            runFrames = RUN_SPEED_TABLE.length - 1;
        }

        return runFrames / 16.0f;
    }

    /**
     * Convert extra run speed to equivalent runway tiles.
     *
     * @param extraRunSpeed Extra run speed in pixels per frame
     * @return Equivalent runway tiles
     */
    public static float getExtraRunSpeedTiles(float extraRunSpeed) {
        if (extraRunSpeed < 1.0f / 16.0f) {
            return 0.0f;
        }

        int dashFrames = (int) (extraRunSpeed * 16.0f) - 1;
        if (dashFrames < 0) {
            dashFrames = 0;
        }
        if (dashFrames >= RUN_SPEED_TABLE.length) {
            dashFrames = RUN_SPEED_TABLE.length - 1;
        }

        int subpixels = RUN_SPEED_TABLE[dashFrames];
        return subpixels / 256.0f;
    }

    /**
     * Minimum extra run speed (in pixels per frame) obtainable by gaining a shortcharge
     * at the given skill level (in minimum number of tiles to gain a shortcharge).
     *
     * @param shortchargeTileSkill Minimum tiles for shortcharge at player's skill level
     * @return Minimum extra run speed in pixels per frame
     */
    public static float getShortchargeMinExtraRunSpeed(float shortchargeTileSkill) {
        // Table mapping minimum shortcharge tiles into number of frames with dash held
        int[][] table = {
            {11, 0x07},
            {12, 0x0B},
            {13, 0x0F},
            {14, 0x12},
            {15, 0x16},
            {16, 0x1B},
            {17, 0x1E},
            {20, 0x37},
            {25, 0x48},
            {30, 0x59}
        };

        return linearInterpolate(shortchargeTileSkill, table) / 16.0f;
    }

    /**
     * Maximum extra run speed obtainable from shortcharge at given skill level and runway length.
     *
     * @param shortchargeTileSkill Player's shortcharge skill in tiles
     * @param runwayLength Length of runway in tiles
     * @return Maximum extra run speed, or null if skill exceeds runway length
     */
    public static Float getShortchargeMaxExtraRunSpeed(float shortchargeTileSkill, float runwayLength) {
        if (shortchargeTileSkill > runwayLength) {
            return null;
        }

        if (runwayLength >= 30.0f) {
            return getMaxExtraRunSpeed(runwayLength);
        }

        // Table of maximum run speed obtainable at given shortcharge skill level, at specific runway lengths
        int[][] table;

        if (shortchargeTileSkill >= 25.0f) {
            table = new int[][] {{25, 0x48}, {30, 0x59}};
        } else if (shortchargeTileSkill >= 20.0f) {
            table = new int[][] {{20, 0x3A}, {25, 0x4B}, {30, 0x59}};
        } else if (shortchargeTileSkill >= 16.0f) {
            table = new int[][] {{16, 0x23}, {17, 0x29}, {20, 0x3C}, {25, 0x4C}, {30, 0x59}};
        } else if (shortchargeTileSkill >= 15.0f) {
            table = new int[][] {{15, 0x1B}, {16, 0x2B}, {17, 0x2D}, {20, 0x3E}, {25, 0x4D}, {30, 0x59}};
        } else if (shortchargeTileSkill >= 14.0f) {
            table = new int[][] {{14, 0x1A}, {15, 0x29}, {16, 0x31}, {17, 0x33}, {20, 0x40}, {25, 0x4E}, {30, 0x59}};
        } else if (shortchargeTileSkill >= 13.0f) {
            table = new int[][] {{13, 0x12}, {14, 0x24}, {15, 0x2B}, {16, 0x33}, {17, 0x38}, {20, 0x41}, {25, 0x4E}, {30, 0x59}};
        } else {
            table = new int[][] {{11, 0x07}, {13, 0x0B}, {14, 0x2A}, {15, 0x2F}, {16, 0x35}, {17, 0x3A}, {20, 0x42}, {25, 0x4E}, {30, 0x59}};
        }

        return linearInterpolate(runwayLength, table) / 16.0f;
    }
}
