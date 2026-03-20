package com.maprando.patch;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PLM (Programmable Logic Module) type table.
 * PLM types are 16-bit values that define which item appears in which container type.
 *
 * Container types:
 * - 0 = None/Pedestal (item on pedestal or in plain sight)
 * - 1 = Chozo Orb (item inside glowing Chozo ball)
 * - 2 = Hidden Shot Block (item inside breakable block)
 *
 * Based on Rust project's PLM type table at:
 * rust/maprando/src/patch.rs: item_to_plm_type()
 */
class PlmTypeTableTest {

    // Container constants
    private static final int CONTAINER_NONE = 0;
    private static final int CONTAINER_CHOZO_ORB = 1;
    private static final int CONTAINER_HIDDEN_BLOCK = 2;

    /**
     * Test that all PLM values are valid 16-bit values.
     */
    @Test
    void testPlmValuesAre16Bit() {
        for (int container = 0; container < 3; container++) {
            for (int item = 0; item < PlmTypeTable.getItemCount(); item++) {
                int plmType = PlmTypeTable.getPlmType(container, item);
                assertTrue(plmType >= 0 && plmType <= 0xFFFF,
                    "PLM type must be 16-bit: container=" + container + " item=" + item + " plm=0x" + Integer.toHexString(plmType));
            }
        }
    }

    /**
     * Test that PLM values are within expected range.
     * All known PLM types are between 0xEED7 and 0xF0F6.
     */
    @Test
    void testPlmValueRanges() {
        for (int container = 0; container < 3; container++) {
            for (int item = 0; item < PlmTypeTable.getItemCount(); item++) {
                int plmType = PlmTypeTable.getPlmType(container, item);
                assertTrue(plmType >= 0xEED7 && plmType <= 0xF0F6,
                    "PLM type out of expected range: container=" + container + " item=" + item + " plm=0x" + Integer.toHexString(plmType));
            }
        }
    }

    /**
     * Test specific PLM values for Container 0 (None/Pedestal).
     * These are the baseline PLM values from the Rust project.
     */
    @Test
    void testContainer0PlmValues() {
        assertEquals(0xEED7, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.ENERGY_TANK));
        assertEquals(0xEEDB, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.MISSILE));
        assertEquals(0xEEDF, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.SUPER_MISSILE));
        assertEquals(0xEEE3, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.POWER_BOMB));
        assertEquals(0xEEE7, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.BOMBS));
        assertEquals(0xEEEB, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.CHARGE_BEAM));
        assertEquals(0xEEEF, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.ICE_BEAM));
        assertEquals(0xEEF3, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.HI_JUMP));
        assertEquals(0xEEF7, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.SPEED_BOOSTER));
        assertEquals(0xEEFB, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.WAVE_BEAM));
        assertEquals(0xEEFF, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.SPAZER_BEAM));
        assertEquals(0xEF03, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.SPRING_BALL));
        assertEquals(0xEF07, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.VARIA_SUIT));
        assertEquals(0xEF0B, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.GRAVITY_SUIT));
        assertEquals(0xEF0F, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.XRAY_SCOPE));
        assertEquals(0xEF13, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.PLASMA_BEAM));
        assertEquals(0xEF17, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.GRAPPLE_BEAM));
        assertEquals(0xEF1B, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.SPACE_JUMP));
        assertEquals(0xEF1F, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.SCREW_ATTACK));
        assertEquals(0xEF23, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.MORPH_BALL));
        assertEquals(0xEF27, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.RESERVE_TANK));
        assertEquals(0xF000, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.WALL_JUMP_BOOTS));
        assertEquals(0xEEDB, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.NOTHING)); // Same as missile
        assertEquals(0xF0E2, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.SPARK_BOOSTER));
        assertEquals(0xF0EE, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.BLUE_BOOSTER));
    }

    /**
     * Test specific PLM values for Container 1 (Chozo Orb).
     * Chozo orb PLM values from Rust project.
     */
    @Test
    void testContainer1PlmValues() {
        assertEquals(0xEF2B, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.ENERGY_TANK));
        assertEquals(0xEF2F, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.MISSILE));
        assertEquals(0xEF33, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.SUPER_MISSILE));
        assertEquals(0xEF37, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.POWER_BOMB));
        assertEquals(0xEF3B, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.BOMBS));
        assertEquals(0xEF3F, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.CHARGE_BEAM));
        assertEquals(0xEF43, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.ICE_BEAM));
        assertEquals(0xEF47, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.HI_JUMP));
        assertEquals(0xEF4B, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.SPEED_BOOSTER));
        assertEquals(0xEF4F, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.WAVE_BEAM));
        assertEquals(0xEF53, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.SPAZER_BEAM));
        assertEquals(0xEF57, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.SPRING_BALL));
        assertEquals(0xEF5B, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.VARIA_SUIT));
        assertEquals(0xEF5F, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.GRAVITY_SUIT));
        assertEquals(0xEF63, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.XRAY_SCOPE));
        assertEquals(0xEF67, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.PLASMA_BEAM));
        assertEquals(0xEF6B, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.GRAPPLE_BEAM));
        assertEquals(0xEF6F, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.SPACE_JUMP));
        assertEquals(0xEF73, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.SCREW_ATTACK));
        assertEquals(0xEF77, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.MORPH_BALL));
        assertEquals(0xEF7B, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.RESERVE_TANK));
        assertEquals(0xF004, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.WALL_JUMP_BOOTS));
        assertEquals(0xEF2F, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.NOTHING)); // Same as missile
        assertEquals(0xF0E6, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.SPARK_BOOSTER));
        assertEquals(0xF0F2, PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, PlmTypeTable.BLUE_BOOSTER));
    }

    /**
     * Test that container 1 PLM values are generally 0x54 higher than container 0.
     * Most items follow this pattern, but there are exceptions (Wall Jump Boots, Spark Booster, Blue Booster).
     */
    @Test
    void testContainer1OffsetMostly() {
        int exceptions = 0;
        for (int item = 0; item < PlmTypeTable.getItemCount(); item++) {
            int container0Plm = PlmTypeTable.getPlmType(CONTAINER_NONE, item);
            int container1Plm = PlmTypeTable.getPlmType(CONTAINER_CHOZO_ORB, item);
            int offset = container1Plm - container0Plm;
            if (offset != 0x54) {
                exceptions++;
                // Verify known exceptions
                if (item == PlmTypeTable.WALL_JUMP_BOOTS) {
                    assertEquals(0x04, offset, "Wall Jump Boots offset should be 0x04");
                } else if (item == PlmTypeTable.NOTHING) {
                    assertEquals(0x54, offset, "Nothing offset should be 0x54");
                } else if (item == PlmTypeTable.SPARK_BOOSTER) {
                    assertEquals(0x04, offset, "Spark Booster offset should be 0x04");
                } else if (item == PlmTypeTable.BLUE_BOOSTER) {
                    assertEquals(0x04, offset, "Blue Booster offset should be 0x04");
                }
            }
        }
        // We expect exactly 3 exceptions: Wall Jump Boots, Spark Booster, Blue Booster
        assertTrue(exceptions <= 3, "Should have at most 3 exceptions to 0x54 offset");
    }

    /**
     * Test that we have exactly 25 items.
     */
    @Test
    void testItemCount() {
        assertEquals(25, PlmTypeTable.getItemCount(),
            "PLM type table should have exactly 25 items to match Rust project");
    }

    /**
     * Test getting item name from item ID.
     */
    @Test
    void testGetItemName() {
        assertEquals("Energy Tank", PlmTypeTable.getItemName(PlmTypeTable.ENERGY_TANK));
        assertEquals("Missile", PlmTypeTable.getItemName(PlmTypeTable.MISSILE));
        assertEquals("Super Missile", PlmTypeTable.getItemName(PlmTypeTable.SUPER_MISSILE));
        assertEquals("Power Bomb", PlmTypeTable.getItemName(PlmTypeTable.POWER_BOMB));
        assertEquals("Morph Ball", PlmTypeTable.getItemName(PlmTypeTable.MORPH_BALL));
        assertEquals("Nothing", PlmTypeTable.getItemName(PlmTypeTable.NOTHING));
    }

    /**
     * Test getting item ID from item name.
     */
    @Test
    void testGetItemId() {
        assertEquals(PlmTypeTable.ENERGY_TANK, PlmTypeTable.getItemId("Energy Tank"));
        assertEquals(PlmTypeTable.MISSILE, PlmTypeTable.getItemId("Missile"));
        assertEquals(PlmTypeTable.MORPH_BALL, PlmTypeTable.getItemId("Morph Ball"));
        assertEquals(PlmTypeTable.NOTHING, PlmTypeTable.getItemId("Nothing"));
    }

    /**
     * Test that item name lookup is case-insensitive.
     */
    @Test
    void testGetItemIdCaseInsensitive() {
        assertEquals(PlmTypeTable.MISSILE, PlmTypeTable.getItemId("missile"));
        assertEquals(PlmTypeTable.MISSILE, PlmTypeTable.getItemId("MISSILE"));
        assertEquals(PlmTypeTable.MISSILE, PlmTypeTable.getItemId("MiSsIlE"));
    }

    /**
     * Test that invalid item names return -1.
     */
    @Test
    void testGetItemIdInvalid() {
        assertEquals(-1, PlmTypeTable.getItemId("Invalid Item"));
        assertEquals(-1, PlmTypeTable.getItemId(""));
        assertEquals(-1, PlmTypeTable.getItemId(null));
    }

    /**
     * Test that all 25 items have unique names.
     */
    @Test
    void testUniqueItemNames() {
        java.util.Set<String> names = new java.util.HashSet<>();
        for (int item = 0; item < PlmTypeTable.getItemCount(); item++) {
            String name = PlmTypeTable.getItemName(item);
            assertTrue(names.add(name), "Duplicate item name: " + name);
        }
    }

    /**
     * Test container type detection from PLM value.
     * Container = (plmType - 0xEED7) / 84
     */
    @Test
    void testDetectContainerType() {
        assertEquals(CONTAINER_NONE, PlmTypeTable.detectContainerType(0xEED7));
        assertEquals(CONTAINER_NONE, PlmTypeTable.detectContainerType(0xEEDB));
        assertEquals(CONTAINER_NONE, PlmTypeTable.detectContainerType(0xF0EE));

        assertEquals(CONTAINER_CHOZO_ORB, PlmTypeTable.detectContainerType(0xEF2B));
        assertEquals(CONTAINER_CHOZO_ORB, PlmTypeTable.detectContainerType(0xEF2F));
        assertEquals(CONTAINER_CHOZO_ORB, PlmTypeTable.detectContainerType(0xF0F2));

        // Container 2 (hidden block) would be 0x54 higher than container 1
        // 0xEF7F = 0xEF2B + 0x54
        assertEquals(CONTAINER_HIDDEN_BLOCK, PlmTypeTable.detectContainerType(0xEF7F));
    }

    /**
     * Test invalid container type detection.
     */
    @Test
    void testDetectContainerTypeInvalid() {
        // PLM values below range should return -1
        assertEquals(-1, PlmTypeTable.detectContainerType(0xEED6));
        assertEquals(-1, PlmTypeTable.detectContainerType(0x0000));
        assertEquals(-1, PlmTypeTable.detectContainerType(0xFFFF));
    }

    /**
     * Test that major items (not tanks) have sequential PLM values.
     * Note: Wall Jump Boots (item 21) breaks the sequence (jumps from 0xEF27 to 0xF000).
     * Nothing (item 22) breaks the sequence (goes back to 0xEEDB).
     */
    @Test
    void testMajorItemPlmSequence() {
        // From Bombs (0xEEE7) to Reserve Tank (0xEF27), PLM values should increase sequentially
        int prevPlm = PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.BOMBS);
        for (int item = PlmTypeTable.BOMBS + 1; item <= PlmTypeTable.RESERVE_TANK; item++) {
            int currPlm = PlmTypeTable.getPlmType(CONTAINER_NONE, item);
            assertTrue(currPlm > prevPlm,
                "PLM values should be sequential for items " + (item-1) + " and " + item);
            prevPlm = currPlm;
        }

        // Wall Jump Boots jumps from 0xEF27 to 0xF000 (large gap)
        int wallJumpPlm = PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.WALL_JUMP_BOOTS);
        assertTrue(wallJumpPlm > prevPlm, "Wall Jump Boots should be higher than Reserve Tank");

        // Nothing goes back to missile tank value (0xEEDB)
        int nothingPlm = PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.NOTHING);
        assertEquals(0xEEDB, nothingPlm, "Nothing should be same as missile tank");

        // Spark Booster and Blue Booster are near end of range
        int sparkBoosterPlm = PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.SPARK_BOOSTER);
        int blueBoosterPlm = PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.BLUE_BOOSTER);
        assertTrue(sparkBoosterPlm < blueBoosterPlm, "Spark Booster should be before Blue Booster");
    }

    /**
     * Test that tank items have specific PLM values.
     */
    @Test
    void testTankPlmValues() {
        // Tanks should have PLM values: 0xEED7 (ETank), 0xEEDB (Missile), 0xEEDF (Super), 0xEEE3 (PB)
        assertEquals(0xEED7, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.ENERGY_TANK));
        assertEquals(0xEEDB, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.MISSILE));
        assertEquals(0xEEDF, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.SUPER_MISSILE));
        assertEquals(0xEEE3, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.POWER_BOMB));

        // Reserve tank should also be a "tank"
        assertEquals(0xEF27, PlmTypeTable.getPlmType(CONTAINER_NONE, PlmTypeTable.RESERVE_TANK));
    }
}
