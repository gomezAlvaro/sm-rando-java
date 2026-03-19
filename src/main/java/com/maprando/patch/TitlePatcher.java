package com.maprando.patch;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TitlePatcher {
    private final Rom rom;
    public int nextFreeSpacePc;
    public int endFreeSpacePc;

    public TitlePatcher(Rom rom) {
        this.rom = rom;
        this.nextFreeSpacePc = Rom.snes2pc(0xE98400);
        this.endFreeSpacePc = Rom.snes2pc(0xEA8000);
    }

    private static int rgbToU16(int[] rgb) {
        return rgb[0] | (rgb[1] << 5) | (rgb[2] << 10);
    }

    private static class Graphics {
        List<int[]> palette;
        List<byte[][]> tiles;
        int[][] tilemap;
    }

    private static class IndexedVec<T> {
        List<T> keys = new ArrayList<>();
        Map<T, Integer> map = new HashMap<>();

        int add(T item) {
            if (map.containsKey(item)) {
                return map.get(item);
            }
            int idx = keys.size();
            keys.add(item);
            map.put(item, idx);
            return idx;
        }
    }

    private static class Color {
        int r, g, b;
        Color(int r, int g, int b) { this.r=r; this.g=g; this.b=b; }
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Color color = (Color) o;
            return r == color.r && g == color.g && b == color.b;
        }
        @Override
        public int hashCode() {
            return 31 * (31 * r + g) + b;
        }
    }

    private Graphics encodeMode7Graphics(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();

        int[][] tilemap = new int[height / 8][width / 8];
        IndexedVec<String> tileIsv = new IndexedVec<>(); // Use String representation of tile for indexing

        Runnable processTile = () -> {};
        // We will process elements manually
        for (int[] pos : new int[][]{{20, 15}, {20, 16}, {21, 15}, {21, 16}, {16, 15}}) {
            processTileAt(pos[0], pos[1], image, tilemap, tileIsv);
        }

        for (int tileY = 0; tileY < height / 8; tileY++) {
            for (int tileX = 0; tileX < width / 8; tileX++) {
                processTileAt(tileY, tileX, image, tilemap, tileIsv);
            }
        }

        if (tileIsv.keys.size() > 256) {
            throw new IllegalStateException("Too many tiles");
        }

        List<byte[][]> newTiles = new ArrayList<>();
        IndexedVec<Color> colorIsv = new IndexedVec<>();
        colorIsv.add(new Color(0, 0, 0));

        for (String tileStr : tileIsv.keys) {
            int[][][] tile = deserializeTile(tileStr);
            byte[][] newTile = new byte[8][8];
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    int r = tile[y][x][0] / 8;
                    int g = tile[y][x][1] / 8;
                    int b = tile[y][x][2] / 8;
                    int idx = colorIsv.add(new Color(r, g, b));
                    newTile[y][x] = (byte) idx;
                }
            }
            newTiles.add(newTile);
        }

        Graphics g = new Graphics();
        g.palette = colorIsv.keys.stream().map(c -> new int[]{c.r, c.g, c.b}).toList();
        g.tiles = newTiles;
        g.tilemap = tilemap;
        return g;
    }

    private void processTileAt(int tileY, int tileX, BufferedImage image, int[][] tilemap, IndexedVec<String> tileIsv) {
        int[][][] tile = new int[8][8][3];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int rgb = image.getRGB(tileX * 8 + x, tileY * 8 + y);
                tile[y][x][0] = (rgb >> 16) & 0xFF; // R
                tile[y][x][1] = (rgb >> 8) & 0xFF;  // G
                tile[y][x][2] = rgb & 0xFF;         // B
            }
        }
        int tileIdx = tileIsv.add(serializeTile(tile));
        tilemap[tileY][tileX] = tileIdx;
    }

    private String serializeTile(int[][][] tile) {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                sb.append(tile[y][x][0]).append(",").append(tile[y][x][1]).append(",").append(tile[y][x][2]).append(";");
            }
        }
        return sb.toString();
    }

    private int[][][] deserializeTile(String str) {
        int[][][] tile = new int[8][8][3];
        String[] parts = str.split(";");
        int i = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                String[] colors = parts[i++].split(",");
                tile[y][x][0] = Integer.parseInt(colors[0]);
                tile[y][x][1] = Integer.parseInt(colors[1]);
                tile[y][x][2] = Integer.parseInt(colors[2]);
            }
        }
        return tile;
    }

    public static byte[][] decodeTile4bpp(byte[] tile) {
        byte[][] out = new byte[8][8];
        for (int y = 0; y < 8; y++) {
            int i = y * 2;
            for (int x = 0; x < 8; x++) {
                int b0 = (tile[i] >> (7 - x)) & 1;
                int b1 = (tile[i + 1] >> (7 - x)) & 1;
                int b2 = (tile[i + 16] >> (7 - x)) & 1;
                int b3 = (tile[i + 17] >> (7 - x)) & 1;
                out[y][x] = (byte) (b0 | (b1 << 1) | (b2 << 2) | (b3 << 3));
            }
        }
        return out;
    }

    public static byte[] encodeTile4bpp(byte[][] tile) {
        byte[] out = new byte[32];
        int[] offsets = {0, 1, 16, 17};
        for (int p = 0; p < 4; p++) {
            for (int y = 0; y < 8; y++) {
                int c = 0;
                for (int x = 0; x < 8; x++) {
                    c |= ((tile[y][x] >> p) & 1) << (7 - x);
                }
                out[y * 2 + offsets[p]] = (byte) c;
            }
        }
        return out;
    }

    public static class SpriteMapEntry {
        short x;
        byte y;
        short c;
        byte palette;
        byte priority;
        boolean size16;
        boolean xFlip;
        boolean yFlip;
    }

    private int writeToFreeSpace(byte[] data) {
        int freeSpace = nextFreeSpacePc;
        rom.writeN(freeSpace, data);
        nextFreeSpacePc += data.length;
        if (nextFreeSpacePc > endFreeSpacePc) {
            throw new IllegalStateException("Not enough free space for title screen data");
        }
        return freeSpace;
    }

    private void writePalette(int addr, List<int[]> palette) {
        for (int i = 0; i < palette.size(); i++) {
            rom.writeU16(addr + i * 2, rgbToU16(palette.get(i)));
        }
    }

    private void writeTitleBackgroundTiles(List<byte[][]> tiles) {
        byte[] flatTiles = new byte[tiles.size() * 64];
        int idx = 0;
        for (byte[][] tile : tiles) {
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    flatTiles[idx++] = tile[y][x];
                }
            }
        }
        byte[] compressed = Compression.compress(flatTiles);
        int gfxPcAddr = writeToFreeSpace(compressed);
        int gfxSnesAddr = Rom.pc2snes(gfxPcAddr);

        rom.writeU8(Rom.snes2pc(0x8B9BA8), gfxSnesAddr >> 16);
        rom.writeU16(Rom.snes2pc(0x8B9BAC), gfxSnesAddr & 0xFFFF);
    }

    private void writeTitleBackgroundTilemap(int[][] tilemap) {
        int paddedRows = tilemap.length + 4;
        int paddedCols = tilemap[0].length + 96;
        byte[] padded = new byte[paddedRows * paddedCols];

        for (int r = 0; r < paddedRows; r++) {
            for (int c = 0; c < paddedCols; c++) {
                if (r >= tilemap.length || c >= tilemap[0].length) {
                    padded[r * paddedCols + c] = 4;
                } else {
                    padded[r * paddedCols + c] = (byte) tilemap[r][c];
                }
            }
        }

        byte[] compressed = Compression.compress(padded);
        int tilemapPcAddr = writeToFreeSpace(compressed);
        int tilemapSnesAddr = Rom.pc2snes(tilemapPcAddr);

        rom.writeU8(Rom.snes2pc(0x8B9BB9), tilemapSnesAddr >> 16);
        rom.writeU16(Rom.snes2pc(0x8B9BBD), tilemapSnesAddr & 0xFFFF);
    }

    public void patchTitleBlueLight() {
        rom.writeN(Rom.snes2pc(0x8B9D43), new byte[]{(byte)0xEA, (byte)0xEA, (byte)0xEA});
    }

    public void patchTitleGradient() {
        for (int i = 3; i < 16; i++) {
            int baseAddrSnes = rom.readU16(Rom.snes2pc(0x8CBC5D + i * 2)) + 0x8c0000;
            for (int j = 0; j < 256; j++) {
                int addrPc = Rom.snes2pc(baseAddrSnes + j * 2);
                int numLines = rom.readU8(addrPc);
                int c = rom.readU8(addrPc + 1);
                int colorPlaneMask = c & 0xE0;
                int intensity = c & 0x1F;

                if (colorPlaneMask == 0xE0) {
                    intensity /= 3;
                    if (intensity > 3) intensity = 3;
                } else if (colorPlaneMask == 0xC0) {
                    colorPlaneMask = 0xE0;
                    if (intensity > 3) numLines += 3;
                } else if (c == 0) {
                    break;
                } else {
                    throw new IllegalStateException("Unexpected title screen gradient control: " + Integer.toHexString(c));
                }

                c = colorPlaneMask | intensity;
                rom.writeU8(addrPc, numLines);
                rom.writeU8(addrPc + 1, c);
            }
        }
    }

    public void patchTitleBackground(BufferedImage img) {
        if (img.getWidth() != 256 || img.getHeight() != 224) {
            throw new IllegalArgumentException();
        }

        Graphics graphics = encodeMode7Graphics(img);
        writePalette(0x661E9, graphics.palette);
        writeTitleBackgroundTiles(graphics.tiles);
        writeTitleBackgroundTilemap(graphics.tilemap);

        rom.writeN(Rom.snes2pc(0x8B9A34), new byte[]{(byte)0xEA, (byte)0xEA, (byte)0xEA, (byte)0xEA});
        rom.writeU16(0x661E9 + 0xC9 * 2, 0x7FFF);
    }

    private List<byte[][]> readCompressedTiles(int pcAddr) {
        byte[] decompressed = Compression.decompress(rom, pcAddr);
        List<byte[][]> tiles = new ArrayList<>();
        if (decompressed.length != 16384) throw new IllegalStateException();

        for (int i = 0; i < 512; i++) {
            byte[] buf = new byte[32];
            System.arraycopy(decompressed, i * 32, buf, 0, 32);
            tiles.add(decodeTile4bpp(buf));
        }
        return tiles;
    }

    private List<SpriteMapEntry> readSpritemap(int pcAddr) {
        int numTiles = rom.readU16(pcAddr);
        List<SpriteMapEntry> out = new ArrayList<>();
        pcAddr += 2;

        for (int i = 0; i < numTiles; i++) {
            int x0 = rom.readU16(pcAddr);
            int x = x0 & 0x1FF;
            if (x >= 256) x -= 512;
            boolean size16 = (x0 >> 15) != 0;
            
            int y = rom.readU8(pcAddr + 2);
            if (y >= 128) y -= 256;
            
            int a = rom.readU8(pcAddr + 3);
            int b = rom.readU8(pcAddr + 4);
            
            boolean yFlip = (b >> 7) != 0;
            boolean xFlip = ((b >> 6) & 1) != 0;
            int palette = (b >> 1) & 7;
            int priority = (b >> 4) & 3;
            int c = ((b & 1) << 8) | a;

            SpriteMapEntry entry = new SpriteMapEntry();
            entry.x = (short) x;
            entry.y = (byte) y;
            entry.c = (short) c;
            entry.palette = (byte) palette;
            entry.priority = (byte) priority;
            entry.size16 = size16;
            entry.xFlip = xFlip;
            entry.yFlip = yFlip;
            out.add(entry);

            pcAddr += 5;
        }
        return out;
    }

    private void writeSpritemap(int pcAddr, List<SpriteMapEntry> spritemap) {
        rom.writeU16(pcAddr, spritemap.size());
        pcAddr += 2;

        for (SpriteMapEntry entry : spritemap) {
            int x0 = (entry.x + 0x200) & 0x1FF;
            if (entry.size16) x0 |= 0x8000;
            rom.writeU16(pcAddr, x0);
            rom.writeU8(pcAddr + 2, (entry.y + 0x100) & 0xFF);
            rom.writeU8(pcAddr + 3, entry.c & 0xFF);
            rom.writeU8(pcAddr + 4, ((entry.c >> 8) & 1) | (entry.palette << 1) | (entry.priority << 4) | (entry.xFlip ? (1 << 6) : 0) | (entry.yFlip ? (1 << 7) : 0));
            pcAddr += 5;
        }
    }

    public void patchTitleForeground(File imageFile) throws Exception {
        List<byte[][]> tiles = readCompressedTiles(Rom.snes2pc(0x9580D8));
        List<SpriteMapEntry> spritemap = readSpritemap(Rom.snes2pc(0x8C879D));

        BufferedImage img = ImageIO.read(imageFile);
        if (img.getWidth() != 256 || img.getHeight() != 224) throw new IllegalStateException();

        Map<Color, Integer> palMap = new HashMap<>();
        palMap.put(new Color(0, 1, 0), 0);
        palMap.put(new Color(205, 207, 152), 13);
        palMap.put(new Color(206, 208, 153), 1);

        int[] freeTiles = {
            0xA0, 0xA2, 0xA4, 0xA6, 0xA8, 0xAA, 0xAC, 0xAE, 0xC0, 0xC2, 0xC4, 0xC6, 0xC8, 0xCA,
            0xCC, 0xCE, 0xE0, 0xE2, 0xE4, 0xE6, 0xE8, 0xEA, 0xEC, 0xEE, 0x102, 0x104, 0x106, 0x108,
            0x10A, 0x10C, 0x10E, 0x122, 0x124, 0x126, 0x128, 0x12A, 0x12C, 0x12E, 0x140, 0x142,
            0x144, 0x146, 0x148, 0x14A, 0x14C, 0x14E, 0x160, 0x162, 0x164, 0x180, 0x182, 0x184,
            0x1A0, 0x1A2, 0x1A4, 0x1AC, 0x1AE, 0x1CC, 0x1CE, 0x1E0, 0x1E2, 0x1E4, 0x1E6, 0x1E8,
            0x1EA, 0x1EC, 0x1EE
        };

        int freeTileIdx = 0;
        int yShift = 0x10;

        for (int tileY = 0; tileY < 14; tileY++) {
            for (int tileX = 0; tileX < 16; tileX++) {
                byte[][] tile00 = getTile(img, palMap, tileY * 2, tileX * 2);
                byte[][] tile01 = getTile(img, palMap, tileY * 2, tileX * 2 + 1);
                byte[][] tile10 = getTile(img, palMap, tileY * 2 + 1, tileX * 2);
                byte[][] tile11 = getTile(img, palMap, tileY * 2 + 1, tileX * 2 + 1);

                if (isEmpty(tile00) && isEmpty(tile01) && isEmpty(tile10) && isEmpty(tile11)) {
                    continue;
                }

                int tileIdx = freeTiles[freeTileIdx++];
                SpriteMapEntry entry = new SpriteMapEntry();
                entry.x = (short) (tileX * 16 - 0x80);
                entry.y = (byte) (tileY * 16 - (0x30 + yShift));
                entry.c = (short) tileIdx;
                entry.palette = 2;
                entry.priority = 1;
                entry.size16 = true;
                entry.xFlip = false;
                entry.yFlip = false;
                spritemap.add(entry);

                tiles.set(tileIdx, tile00);
                tiles.set(tileIdx + 1, tile01);
                tiles.set(tileIdx + 16, tile10);
                tiles.set(tileIdx + 17, tile11);
            }
        }

        byte[] encodedTiles = new byte[tiles.size() * 32];
        int idx = 0;
        for (byte[][] tile : tiles) {
            byte[] encoded = encodeTile4bpp(tile);
            System.arraycopy(encoded, 0, encodedTiles, idx, 32);
            idx += 32;
        }

        int newGfxPcAddr = writeToFreeSpace(Compression.compress(encodedTiles));
        int newGfxSnesAddr = Rom.pc2snes(newGfxPcAddr);
        int newSpritemapSnesAddr = 0x8CF3E9;
        
        writeSpritemap(Rom.snes2pc(newSpritemapSnesAddr), spritemap);

        rom.writeU8(Rom.snes2pc(0x8B9BCA), newGfxSnesAddr >> 16);
        rom.writeU16(Rom.snes2pc(0x8B9BCE), newGfxSnesAddr & 0xFFFF);
        
        rom.writeU16(Rom.snes2pc(0x8BA0C7), newSpritemapSnesAddr & 0xFFFF);
        rom.writeU16(Rom.snes2pc(0x8BA0CD), newSpritemapSnesAddr & 0xFFFF);

        rom.writeU16(Rom.snes2pc(0x8B9B21), 0x30 + yShift);
        rom.writeU16(Rom.snes2pc(0x8B9EBA), 0x30 + yShift);
    }

    private byte[][] getTile(BufferedImage img, Map<Color, Integer> palMap, int tileY, int tileX) {
        byte[][] tile = new byte[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int rgb = img.getRGB(tileX * 8 + x, tileY * 8 + y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                Integer c = palMap.get(new Color(r, g, b));
                if (c == null) c = 0;
                tile[y][x] = c.byteValue();
            }
        }
        return tile;
    }

    private boolean isEmpty(byte[][] tile) {
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if (tile[y][x] != 0) return false;
            }
        }
        return true;
    }
}
