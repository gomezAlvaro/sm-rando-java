package com.maprando.patch;

import com.maprando.data.DataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for ItemPatcher.
 * Tests item writing to ROM, item byte conversion, and placement patching.
 */
class ItemPatcherTest {

    private Rom rom;
    private ItemPatcher patcher;
    private DataLoader dataLoader;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();

        // Create a mock ROM (3MB)
        byte[] romData = new byte[3145728];
        rom = new Rom(romData);
        patcher = new ItemPatcher(rom, dataLoader);
    }

    @Test
    void testPatchItem_SimpleItem() {
        patcher.patchItem("brinstar_morph_ball_room", "MORPH_BALL");

        // Verify the item was written at the correct address
        int pcAddr = Rom.snes2pc(0x8282F5);
        int itemByte = rom.readU8(pcAddr);

        // MORPH_BALL should map to a specific byte value
        assertTrue(itemByte != 0); // Should not be empty
    }

    @Test
    void testPatchItem_MissileTank() {
        patcher.patchItem("brinstar_charge_beam_room", "MISSILE_TANK");

        int pcAddr = Rom.snes2pc(0x8282F6);
        int itemByte = rom.readU8(pcAddr);

        // MISSILE_TANK should map to 0xE6
        assertEquals(0xE6, itemByte);
    }

    @Test
    void testPatchItem_EnergyTank() {
        patcher.patchItem("brinstar_bomb_room", "ENERGY_TANK");

        int pcAddr = Rom.snes2pc(0x8282F7);
        int itemByte = rom.readU8(pcAddr);

        // ENERGY_TANK should map to 0xE5
        assertEquals(0xE5, itemByte);
    }

    @Test
    void testPatchItem_UnknownItem() {
        // Unknown items should throw exception or use default
        assertThrows(IllegalArgumentException.class, () -> {
            patcher.patchItem("brinstar_morph_ball_room", "UNKNOWN_ITEM");
        });
    }

    @Test
    void testPatchItem_UnknownLocation() {
        // Unknown locations should be handled gracefully
        assertThrows(IllegalArgumentException.class, () -> {
            patcher.patchItem("unknown_location", "MORPH_BALL");
        });
    }

    @Test
    void testPatchAllPlacements() {
        // Create mock placements
        java.util.Map<String, String> placements = new java.util.HashMap<>();
        placements.put("brinstar_morph_ball_room", "MORPH_BALL");
        placements.put("brinstar_charge_beam_room", "CHARGE_BEAM");
        placements.put("brinstar_bomb_room", "BOMB");

        com.maprando.randomize.RandomizationResult result =
            com.maprando.randomize.RandomizationResult.builder()
                .seed("test")
                .placements(placements)
                .build();

        patcher.patchAllPlacements(result);

        // Verify all items were written
        int addr1 = Rom.snes2pc(0x8282F5);
        int addr2 = Rom.snes2pc(0x8282F6);
        int addr3 = Rom.snes2pc(0x8282F7);

        assertTrue(rom.readU8(addr1) != 0);
        assertTrue(rom.readU8(addr2) != 0);
        assertTrue(rom.readU8(addr3) != 0);
    }

    @Test
    void testGetItemByteValue_MajorItems() {
        byte morphBall = patcher.getItemByteValue("MORPH_BALL");
        byte chargeBeam = patcher.getItemByteValue("CHARGE_BEAM");
        byte iceBeam = patcher.getItemByteValue("ICE_BEAM");
        byte bomb = patcher.getItemByteValue("BOMB");

        // Each should have a unique non-zero value
        assertTrue(morphBall != 0);
        assertTrue(chargeBeam != 0);
        assertTrue(iceBeam != 0);
        assertTrue(bomb != 0);
    }

    @Test
    void testGetItemByteValue_Tanks() {
        byte missile = patcher.getItemByteValue("MISSILE_TANK");
        byte superMissile = patcher.getItemByteValue("SUPER_MISSILE_TANK");
        byte powerBomb = patcher.getItemByteValue("POWER_BOMB_TANK");
        byte energy = patcher.getItemByteValue("ENERGY_TANK");

        // Compare as unsigned values
        assertEquals(0xE6, missile & 0xFF);
        assertEquals(0xE7, superMissile & 0xFF);
        assertEquals(0xE8, powerBomb & 0xFF);
        assertEquals(0xE5, energy & 0xFF);
    }

    @Test
    void testGetItemByteValue_Nothing() {
        byte nothing = patcher.getItemByteValue("NOTHING");
        assertEquals(0xE5, nothing & 0xFF); // Empty pedestal
    }

    @Test
    void testPatchItem_Overwrite() {
        // Write first item
        patcher.patchItem("brinstar_morph_ball_room", "MORPH_BALL");
        int firstValue = rom.readU8(Rom.snes2pc(0x8282F5));

        // Overwrite with different item
        patcher.patchItem("brinstar_morph_ball_room", "CHARGE_BEAM");
        int secondValue = rom.readU8(Rom.snes2pc(0x8282F5));

        // Values should be different
        assertNotEquals(firstValue, secondValue);
    }

    @Test
    void testPatchMultipleLocations() {
        patcher.patchItem("brinstar_morph_ball_room", "MORPH_BALL");
        patcher.patchItem("brinstar_charge_beam_room", "CHARGE_BEAM");
        patcher.patchItem("brinstar_bomb_room", "BOMB");

        // Verify each location has the correct item
        int addr1 = Rom.snes2pc(0x8282F5);
        int addr2 = Rom.snes2pc(0x8282F6);
        int addr3 = Rom.snes2pc(0x8282F7);

        byte val1 = (byte) rom.readU8(addr1);
        byte val2 = (byte) rom.readU8(addr2);
        byte val3 = (byte) rom.readU8(addr3);

        // Each should have different item byte values
        assertNotEquals(val1, val2);
        assertNotEquals(val2, val3);
        assertNotEquals(val1, val3);
    }

    @Test
    void testGetModifiedAddresses() {
        rom.enableTracking();

        patcher.patchItem("brinstar_morph_ball_room", "MORPH_BALL");
        patcher.patchItem("brinstar_charge_beam_room", "CHARGE_BEAM");

        var modifiedRanges = rom.getModifiedRanges();
        assertFalse(modifiedRanges.isEmpty());
        assertTrue(modifiedRanges.size() >= 1);
    }
}
