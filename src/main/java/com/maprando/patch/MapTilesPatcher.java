package com.maprando.patch;

import com.maprando.model.*;
import java.util.*;

public class MapTilesPatcher {
    private Rom rom;
    private GameData gameData;
    private MapData map;
    private RandomizerSettings settings;
    private CustomizeSettings customizeSettings;
    private Randomization randomization;
    
    private Map<String, MapTile> mapTileMap = new HashMap<>();
    private Map<String, Integer> gfxTileMap = new HashMap<>();
    private Map<Integer, int[][]> gfxTileReverseMap = new HashMap<>();
    private List<Integer> freeTiles = new ArrayList<>();
    private List<Integer> lockedDoorStateIndices;
    
    private List<List<Object[]>> dynamicTileData = new ArrayList<>(); // structure: {itemIdx, roomId, MapTile}
    
    private int[] areaMinX = new int[6];
    private int[] areaMaxX = new int[6];
    private int[] areaMinY = new int[6];
    private int[] areaMaxY = new int[6];
    private int[] areaOffsetX = new int[6];
    private int[] areaOffsetY = new int[6];
    
    public MapTilesPatcher(Rom rom, GameData gameData, MapData map, RandomizerSettings settings, CustomizeSettings customizeSettings, Randomization randomization, List<Integer> lockedDoorStateIndices) {
        this.rom = rom;
        this.gameData = gameData;
        this.map = map;
        this.settings = settings;
        this.customizeSettings = customizeSettings;
        this.randomization = randomization;
        this.lockedDoorStateIndices = lockedDoorStateIndices;
        
        for (int i = 0; i < 6; i++) {
            dynamicTileData.add(new ArrayList<>());
            areaMinX[i] = Integer.MAX_VALUE;
            areaMinY[i] = Integer.MAX_VALUE;
        }
        
        Set<Integer> reservedTiles = new HashSet<>(Arrays.asList(
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0B, 0x0C, 0x0D, 0x0E,
            0x0F, 0x10, 0x11, 0x12, 0x1C, 0x1D, 0x1E, 0x1F, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35,
            0x36, 0x38, 0x39, 0x3A, 0x3B, 0x46, 0x47, 0x48, 0x49, 0x4A, 0x4B, 0x4C, 0x4D, 0xA8,
            0xC0, 0xC1, 0xC2, 0xC3, 0xC4, 0xC5, 0xC6, 0xC7, 0xC8, 0xC9, 0xCA, 0xCB, 0xCC, 0xCD,
            0xCE, 0xCF, 0xD1, 0xD2, 0xD3, 0xD4, 0xD5, 0xD6, 0xD7, 0xD8, 0xD9, 0xDA, 0xDD, 0xDE,
            0xDF, 0xE0, 0xE1, 0xE2, 0xE3, 0xE4, 0xE5, 0xE6, 0xE7, 0xE8, 0xE9, 0xEA, 0xEB, 0xEC,
            0xED, 0xEE, 0xEF, 0xF0, 0xF1, 0xF2, 0xF3, 0xF4, 0xF5, 0xF6, 0xF7, 0xF8, 0xF9, 0xFA,
            0xFB, 0xFC, 0xFD, 0xFE, 0xFF, 0x106, 0x10B, 0x10C, 0x10D, 0x10E, 0x11C, 0x11D, 0x11E,
            0x290, 0x291, 0x292, 0x2A0, 0x2A1, 0x2A2, 0x2A3, 0x2B0, 0x2B1, 0x2B2, 0x2B3, 0x2B8,
            0x2C0, 0x2C1, 0x2C2, 0x2C3, 0x228, 0x229, 0x22A, 0x22E, 0x23C, 0x23D, 0x243, 0x251,
            0x29D, 0x29E, 0x2AF, 0x2B4, 0x2B5, 0x2B6, 0x2C4, 0x2C5, 0x2C6, 0x2C7
        ));
        
        if (settings.qualityOfLifeSettings.disableableEtanks != RandomizerSettings.DisableETankSetting.OFF) {
            reservedTiles.add(0x2F);
        }
        
        for (int word = 0; word < 768; word++) {
            if (!reservedTiles.contains(word)) freeTiles.add(word);
        }
        Collections.reverse(freeTiles);
    }
    
    public void applyPatches() {
        updateAcidPalette();
        fixEquipmentGraphics();
        initializeTiles();
        indexFixedTiles();
        fixPausePalettes();
        fixMessageBoxes();
        fixHudBlack();
        darkenHudGrid();
        if (settings.qualityOfLifeSettings.disableableEtanks != RandomizerSettings.DisableETankSetting.OFF) {
            writeDisabledEtankTile();
        }
        applyRoomTiles();
        indicateObjectiveTiles();
        if (!settings.otherSettings.ultraLowQol) {
            indicateGrayDoors();
            indicateLockedDoors();
        }
        addCrossAreaArrows();
        setMapActivationBehavior();
        indicateItems();
        computeAreaBounds();
        writeMapTiles();
        setInitialMap();
        if (settings.qualityOfLifeSettings.roomOutlineRevealed) {
            setupSpecialDoorReveal();
        }
        sortDynamicTileData();
        writeDynamicTileData();
        createRoomMapTilemaps();
        writeHazardTiles();
        fixKraid();
        fixItemColors();
    }
    
    private void updateAcidPalette() {
        for (int addr = Rom.snes2pc(0x8A8840); addr < Rom.snes2pc(0x8A9080); addr += 2) {
            int word = rom.readU16(addr);
            if ((word & 0x1C00) == 0x0000) {
                rom.writeU16(addr, word | 0x1800);
            }
        }
    }
    
    private void fixEquipmentGraphics() {
        List<Integer> equipTileIdxs = Arrays.asList(
            0x119, 0x11A, 0x11B, 0x173, 0x174, 0x178, 0x184, 0x185, 0x188, 0x195, 0x196, 0x197,
            0x19C, 0x19D, 0x1E3, 0x1E4, 0x1E5, 0x1E6, 0x1E7, 0x17C, 0x17D, 0x17E, 0x17F, 0x1A0,
            0x1A1, 0x1ED, 0x1EE, 0x1FE
        );
        for (int idx : equipTileIdxs) {
            int[][] data = MapTilesRenderer.readTile4bpp(rom, Rom.snes2pc(0xB68000), idx);
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    if (data[y][x] == 14) data[y][x] = 15;
                }
            }
            MapTilesRenderer.writeTile4bpp(rom, Rom.snes2pc(0xB68000) + idx * 32, data);
        }
    }
    
    private void initializeTiles() {
        int srcAddr = Rom.snes2pc(0xB68000);
        int dstAddr = Rom.snes2pc(0xE28000); // TILE_GFX_ADDR_4BPP
        for (int i = 0; i < 0x6000; i += 2) {
            int word = rom.readU16(srcAddr + i);
            rom.writeU16(dstAddr + i, word);
        }
    }
    
    private void indexFixedTiles() {}
    private void fixPausePalettes() {}
    private void fixMessageBoxes() {}
    private void fixHudBlack() {}
    private void darkenHudGrid() {}
    private void writeDisabledEtankTile() {}
    private void applyRoomTiles() {}
    private void indicateObjectiveTiles() {}
    private void indicateGrayDoors() {}
    private void indicateLockedDoors() {}
    private void addCrossAreaArrows() {}
    private void setMapActivationBehavior() {}
    private void indicateItems() {}
    private void computeAreaBounds() {}
    private void writeMapTiles() {}
    private void setInitialMap() {}
    private void setupSpecialDoorReveal() {}
    private void sortDynamicTileData() {}
    private void writeDynamicTileData() {}
    private void createRoomMapTilemaps() {}
    
    private void writeHazardTiles() {
        int baseAddr = Rom.snes2pc(0xE98000);
        MapTilesRenderer.writeTile4bpp(rom, baseAddr, MapTilesArrays.HAZARD_TILE1);
        MapTilesRenderer.writeTile4bpp(rom, baseAddr + 0x20, MapTilesArrays.HAZARD_TILE2);
        // Additional implementations...
    }
    
    private void fixKraid() {
        rom.writeU16(Rom.snes2pc(0x8FB81C), 0x0C00);
        rom.writeU16(Rom.snes2pc(0x8FB847), 0x0C00);
    }
    
    private void fixItemColors() {}
}
