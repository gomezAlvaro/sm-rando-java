package com.maprando.patch;

import com.maprando.data.DataLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Tests for PLM-based ItemPatcher.
 * Tests that items are written to ROM using correct PLM type values
 * and that container type is detected from original PLM.
 */
class PlmItemPatcherTest {

    private Rom baseRom;
    private Rom patchedRom;
    private DataLoader dataLoader;

    @BeforeEach
    void setUp() throws IOException {
        // Create a test ROM (use a small buffer for testing)
        byte[] testRomData = new byte[0x100000]; // 1MB test ROM
        baseRom = new Rom(testRomData);

        // Create a copy for patching
        byte[] patchedRomData = new byte[testRomData.length];
        System.arraycopy(testRomData, 0, patchedRomData, 0, testRomData.length);
        patchedRom = new Rom(patchedRomData);

        // Load data loader
        dataLoader = new DataLoader();
        dataLoader.loadAllData();
    }

    /**
     * Test creating a PLM-based ItemPatcher.
     */
    @Test
    void testCreatePlmItemPatcher() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);
        assertNotNull(patcher);
    }

    /**
     * Test detecting container type from original PLM value.
     */
    @Test
    void testDetectContainerType() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        // Test container 0 detection
        assertEquals(0, patcher.detectContainerType(0xEED7));
        assertEquals(0, patcher.detectContainerType(0xEEDB));
        assertEquals(0, patcher.detectContainerType(0xEF27));

        // Test container 1 detection
        assertEquals(1, patcher.detectContainerType(0xEF2B));
        assertEquals(1, patcher.detectContainerType(0xEF2F));
        assertEquals(1, patcher.detectContainerType(0xEF7B));

        // Test container 2 detection
        assertEquals(2, patcher.detectContainerType(0xEF7F));
        assertEquals(2, patcher.detectContainerType(0xEF83));
        assertEquals(2, patcher.detectContainerType(0xEFCF));

        // Test invalid PLM
        assertEquals(-1, patcher.detectContainerType(0x0000));
        assertEquals(-1, patcher.detectContainerType(0xFFFF));
    }

    /**
     * Test getting PLM type for item with container.
     */
    @Test
    void testGetPlmType() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        // Test container 0
        assertEquals(0xEED7, patcher.getPlmType(0, "Energy Tank"));
        assertEquals(0xEEDB, patcher.getPlmType(0, "Missile"));
        assertEquals(0xEF23, patcher.getPlmType(0, "Morph Ball"));

        // Test container 1
        assertEquals(0xEF2B, patcher.getPlmType(1, "Energy Tank"));
        assertEquals(0xEF2F, patcher.getPlmType(1, "Missile"));
        assertEquals(0xEF77, patcher.getPlmType(1, "Morph Ball"));

        // Test container 2
        assertEquals(0xEF7F, patcher.getPlmType(2, "Energy Tank"));
        assertEquals(0xEF83, patcher.getPlmType(2, "Missile"));
        assertEquals(0xEFCB, patcher.getPlmType(2, "Morph Ball"));
    }

    /**
     * Test patching item with container 0 (pedestal).
     */
    @Test
    void testPatchItemContainer0() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        int plmAddress = Rom.snes2pc(0x8282F5);

        // Set original PLM to Energy Tank pedestal (container 0)
        patchedRom.writeU16(plmAddress, 0xEED7);

        // Patch to Morph Ball (should preserve container 0)
        patcher.patchItem(plmAddress, "Morph Ball");

        // Verify correct PLM value was written
        int newPlm = patchedRom.readU16(plmAddress);
        assertEquals(0xEF23, newPlm); // Morph Ball, container 0
    }

    /**
     * Test patching item with container 1 (Chozo orb).
     */
    @Test
    void testPatchItemContainer1() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        int plmAddress = Rom.snes2pc(0x8282F5);

        // Set original PLM to Energy Tank Chozo orb (container 1)
        patchedRom.writeU16(plmAddress, 0xEF2B);

        // Patch to Grapple Beam (should preserve container 1)
        patcher.patchItem(plmAddress, "Grapple Beam");

        // Verify correct PLM value was written
        int newPlm = patchedRom.readU16(plmAddress);
        assertEquals(0xEF6B, newPlm); // Grapple Beam, container 1
    }

    /**
     * Test patching item with container 2 (hidden block).
     */
    @Test
    void testPatchItemContainer2() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        int plmAddress = Rom.snes2pc(0x8282F5);

        // Set original PLM to Missile hidden block (container 2)
        patchedRom.writeU16(plmAddress, 0xEF83);

        // Patch to Plasma Beam (should preserve container 2)
        patcher.patchItem(plmAddress, "Plasma Beam");

        // Verify correct PLM value was written
        int newPlm = patchedRom.readU16(plmAddress);
        assertEquals(0xEFBB, newPlm); // Plasma Beam, container 2
    }

    /**
     * Test patching all major item types.
     */
    @Test
    void testPatchAllItemTypes() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        int plmAddress = Rom.snes2pc(0x8282F5);
        patchedRom.writeU16(plmAddress, 0xEED7); // Energy Tank pedestal

        // Test each item type
        String[] items = {
            "Energy Tank", "Missile", "Super Missile", "Power Bomb", "Bombs",
            "Charge Beam", "Ice Beam", "Hi-Jump Boots", "Speed Booster",
            "Wave Beam", "Spazer Beam", "Spring Ball", "Varia Suit",
            "Gravity Suit", "X-Ray Scope", "Plasma Beam", "Grapple Beam",
            "Space Jump", "Screw Attack", "Morph Ball", "Reserve Tank",
            "Wall Jump Boots", "Nothing", "Spark Booster", "Blue Booster"
        };

        for (String item : items) {
            patchedRom.writeU16(plmAddress, 0xEED7); // Reset to Energy Tank pedestal
            patcher.patchItem(plmAddress, item);

            int newPlm = patchedRom.readU16(plmAddress);
            assertTrue(PlmTypeTable.isValidPlmValue(newPlm),
                "PLM value should be valid for item: " + item);
        }
    }

    /**
     * Test that patching preserves container type.
     */
    @Test
    void testPreserveContainerType() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        int plmAddress = Rom.snes2pc(0x8282F5);

        // Test all three container types
        int[] originalPlms = {0xEED7, 0xEF2B, 0xEF7F}; // Energy Tank in each container

        for (int originalPlm : originalPlms) {
            patchedRom.writeU16(plmAddress, originalPlm);
            int originalContainer = patcher.detectContainerType(originalPlm);

            // Patch to Morph Ball
            patcher.patchItem(plmAddress, "Morph Ball");

            int newPlm = patchedRom.readU16(plmAddress);
            int newContainer = patcher.detectContainerType(newPlm);

            assertEquals(originalContainer, newContainer,
                "Container type should be preserved. Original PLM: 0x" +
                Integer.toHexString(originalPlm) + " New PLM: 0x" + Integer.toHexString(newPlm));
        }
    }

    /**
     * Test patching with invalid item name.
     */
    @Test
    void testPatchInvalidItem() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        int plmAddress = Rom.snes2pc(0x8282F5);
        patchedRom.writeU16(plmAddress, 0xEED7);

        // Try to patch invalid item
        assertThrows(IllegalArgumentException.class, () -> {
            patcher.patchItem(plmAddress, "Invalid Item");
        });
    }

    /**
     * Test patching with invalid original PLM.
     */
    @Test
    void testPatchInvalidOriginalPlm() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        int plmAddress = Rom.snes2pc(0x8282F5);
        patchedRom.writeU16(plmAddress, 0x0000); // Invalid PLM

        // Should throw exception or handle gracefully
        assertThrows(IllegalArgumentException.class, () -> {
            patcher.patchItem(plmAddress, "Morph Ball");
        });
    }

    /**
     * Test that ItemPatcher uses 16-bit writes, not 8-bit.
     */
    @Test
    void testUses16BitWrite() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        int plmAddress = Rom.snes2pc(0x8282F5);
        patchedRom.writeU16(plmAddress, 0xEED7);

        patcher.patchItem(plmAddress, "Morph Ball");

        // Read as 16-bit value
        int plmValue = patchedRom.readU16(plmAddress);
        assertEquals(0xEF23, plmValue);

        // Verify that bytes are in correct little-endian order
        int lowByte = patchedRom.readU8(plmAddress);
        int highByte = patchedRom.readU8(plmAddress + 1);
        assertEquals(0x23, lowByte);  // Low byte
        assertEquals(0xEF, highByte); // High byte
    }

    /**
     * Test patching multiple items at different addresses.
     */
    @Test
    void testPatchMultipleItems() {
        PlmItemPatcher patcher = new PlmItemPatcher(patchedRom, dataLoader);

        // Set up multiple item locations with different containers
        int[] addresses = {
            Rom.snes2pc(0x8282F5),
            Rom.snes2pc(0x828300),
            Rom.snes2pc(0x828310)
        };

        int[] originalPlms = {0xEED7, 0xEF2B, 0xEF7F}; // Different containers
        String[] items = {"Morph Ball", "Grapple Beam", "Plasma Beam"};

        for (int i = 0; i < addresses.length; i++) {
            patchedRom.writeU16(addresses[i], originalPlms[i]);
            patcher.patchItem(addresses[i], items[i]);
        }

        // Verify all patches
        assertEquals(0xEF23, patchedRom.readU16(addresses[0])); // Morph Ball, container 0
        assertEquals(0xEF6B, patchedRom.readU16(addresses[1])); // Grapple, container 1
        assertEquals(0xEFBB, patchedRom.readU16(addresses[2])); // Plasma, container 2
    }
}
