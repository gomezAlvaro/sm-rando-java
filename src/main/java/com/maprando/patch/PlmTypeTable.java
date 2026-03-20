package com.maprando.patch;

import java.util.HashMap;
import java.util.Map;

/**
 * PLM (Programmable Logic Module) type table for Super Metroid item placement.
 *
 * PLM types are 16-bit values that define which item appears in which container type.
 * This table is derived from the Rust MapRandomizer project to ensure compatibility.
 *
 * Container types:
 * - 0 = None/Pedestal (item on pedestal or in plain sight)
 * - 1 = Chozo Orb (item inside glowing Chozo ball)
 * - 2 = Hidden Shot Block (item inside breakable block)
 *
 * Reference: rust/maprando/src/patch.rs: item_to_plm_type()
 */
public class PlmTypeTable {

    // Item indices (must match Rust Item enum ordering)
    public static final int ENERGY_TANK = 0;
    public static final int MISSILE = 1;
    public static final int SUPER_MISSILE = 2;
    public static final int POWER_BOMB = 3;
    public static final int BOMBS = 4;
    public static final int CHARGE_BEAM = 5;
    public static final int ICE_BEAM = 6;
    public static final int HI_JUMP = 7;
    public static final int SPEED_BOOSTER = 8;
    public static final int WAVE_BEAM = 9;
    public static final int SPAZER_BEAM = 10;
    public static final int SPRING_BALL = 11;
    public static final int VARIA_SUIT = 12;
    public static final int GRAVITY_SUIT = 13;
    public static final int XRAY_SCOPE = 14;
    public static final int PLASMA_BEAM = 15;
    public static final int GRAPPLE_BEAM = 16;
    public static final int SPACE_JUMP = 17;
    public static final int SCREW_ATTACK = 18;
    public static final int MORPH_BALL = 19;
    public static final int RESERVE_TANK = 20;
    public static final int WALL_JUMP_BOOTS = 21;
    public static final int NOTHING = 22;
    public static final int SPARK_BOOSTER = 23;
    public static final int BLUE_BOOSTER = 24;

    private static final int ITEM_COUNT = 25;
    private static final int CONTAINER_COUNT = 3;

    // PLM type table: plmTable[containerType][itemId] = PLM value
    private static final int[][] PLM_TABLE = new int[CONTAINER_COUNT][ITEM_COUNT];

    // Item name to ID mapping
    private static final Map<String, Integer> ITEM_NAME_TO_ID;

    // Item ID to name mapping
    private static final String[] ITEM_NAMES = new String[ITEM_COUNT];

    // Base PLM value for container 0
    private static final int BASE_PLM_VALUE = 0xEED7;

    // Offset between container types (84 = 0x54)
    private static final int CONTAINER_OFFSET = 0x54;

    static {
        // Container 0 (None/Pedestal) - exact values from Rust project
        PLM_TABLE[0][ENERGY_TANK] = 0xEED7;
        PLM_TABLE[0][MISSILE] = 0xEEDB;
        PLM_TABLE[0][SUPER_MISSILE] = 0xEEDF;
        PLM_TABLE[0][POWER_BOMB] = 0xEEE3;
        PLM_TABLE[0][BOMBS] = 0xEEE7;
        PLM_TABLE[0][CHARGE_BEAM] = 0xEEEB;
        PLM_TABLE[0][ICE_BEAM] = 0xEEEF;
        PLM_TABLE[0][HI_JUMP] = 0xEEF3;
        PLM_TABLE[0][SPEED_BOOSTER] = 0xEEF7;
        PLM_TABLE[0][WAVE_BEAM] = 0xEEFB;
        PLM_TABLE[0][SPAZER_BEAM] = 0xEEFF;
        PLM_TABLE[0][SPRING_BALL] = 0xEF03;
        PLM_TABLE[0][VARIA_SUIT] = 0xEF07;
        PLM_TABLE[0][GRAVITY_SUIT] = 0xEF0B;
        PLM_TABLE[0][XRAY_SCOPE] = 0xEF0F;
        PLM_TABLE[0][PLASMA_BEAM] = 0xEF13;
        PLM_TABLE[0][GRAPPLE_BEAM] = 0xEF17;
        PLM_TABLE[0][SPACE_JUMP] = 0xEF1B;
        PLM_TABLE[0][SCREW_ATTACK] = 0xEF1F;
        PLM_TABLE[0][MORPH_BALL] = 0xEF23;
        PLM_TABLE[0][RESERVE_TANK] = 0xEF27;
        PLM_TABLE[0][WALL_JUMP_BOOTS] = 0xF000;
        PLM_TABLE[0][NOTHING] = 0xEEDB; // Same as missile tank
        PLM_TABLE[0][SPARK_BOOSTER] = 0xF0E2;
        PLM_TABLE[0][BLUE_BOOSTER] = 0xF0EE;

        // Container 1 (Chozo Orb) - exact values from Rust project
        PLM_TABLE[1][ENERGY_TANK] = 0xEF2B;
        PLM_TABLE[1][MISSILE] = 0xEF2F;
        PLM_TABLE[1][SUPER_MISSILE] = 0xEF33;
        PLM_TABLE[1][POWER_BOMB] = 0xEF37;
        PLM_TABLE[1][BOMBS] = 0xEF3B;
        PLM_TABLE[1][CHARGE_BEAM] = 0xEF3F;
        PLM_TABLE[1][ICE_BEAM] = 0xEF43;
        PLM_TABLE[1][HI_JUMP] = 0xEF47;
        PLM_TABLE[1][SPEED_BOOSTER] = 0xEF4B;
        PLM_TABLE[1][WAVE_BEAM] = 0xEF4F;
        PLM_TABLE[1][SPAZER_BEAM] = 0xEF53;
        PLM_TABLE[1][SPRING_BALL] = 0xEF57;
        PLM_TABLE[1][VARIA_SUIT] = 0xEF5B;
        PLM_TABLE[1][GRAVITY_SUIT] = 0xEF5F;
        PLM_TABLE[1][XRAY_SCOPE] = 0xEF63;
        PLM_TABLE[1][PLASMA_BEAM] = 0xEF67;
        PLM_TABLE[1][GRAPPLE_BEAM] = 0xEF6B;
        PLM_TABLE[1][SPACE_JUMP] = 0xEF6F;
        PLM_TABLE[1][SCREW_ATTACK] = 0xEF73;
        PLM_TABLE[1][MORPH_BALL] = 0xEF77;
        PLM_TABLE[1][RESERVE_TANK] = 0xEF7B;
        PLM_TABLE[1][WALL_JUMP_BOOTS] = 0xF004;
        PLM_TABLE[1][NOTHING] = 0xEF2F; // Same as missile tank
        PLM_TABLE[1][SPARK_BOOSTER] = 0xF0E6;
        PLM_TABLE[1][BLUE_BOOSTER] = 0xF0F2;

        // Container 2 (Hidden Shot Block) - exact values from Rust project
        PLM_TABLE[2][ENERGY_TANK] = 0xEF7F;
        PLM_TABLE[2][MISSILE] = 0xEF83;
        PLM_TABLE[2][SUPER_MISSILE] = 0xEF87;
        PLM_TABLE[2][POWER_BOMB] = 0xEF8B;
        PLM_TABLE[2][BOMBS] = 0xEF8F;
        PLM_TABLE[2][CHARGE_BEAM] = 0xEF93;
        PLM_TABLE[2][ICE_BEAM] = 0xEF97;
        PLM_TABLE[2][HI_JUMP] = 0xEF9B;
        PLM_TABLE[2][SPEED_BOOSTER] = 0xEF9F;
        PLM_TABLE[2][WAVE_BEAM] = 0xEFA3;
        PLM_TABLE[2][SPAZER_BEAM] = 0xEFA7;
        PLM_TABLE[2][SPRING_BALL] = 0xEFAB;
        PLM_TABLE[2][VARIA_SUIT] = 0xEFAF;
        PLM_TABLE[2][GRAVITY_SUIT] = 0xEFB3;
        PLM_TABLE[2][XRAY_SCOPE] = 0xEFB7;
        PLM_TABLE[2][PLASMA_BEAM] = 0xEFBB;
        PLM_TABLE[2][GRAPPLE_BEAM] = 0xEFBF;
        PLM_TABLE[2][SPACE_JUMP] = 0xEFC3;
        PLM_TABLE[2][SCREW_ATTACK] = 0xEFC7;
        PLM_TABLE[2][MORPH_BALL] = 0xEFCB;
        PLM_TABLE[2][RESERVE_TANK] = 0xEFCF;
        PLM_TABLE[2][WALL_JUMP_BOOTS] = 0xF008;
        PLM_TABLE[2][NOTHING] = 0xEF83; // Same as missile tank
        PLM_TABLE[2][SPARK_BOOSTER] = 0xF0EA;
        PLM_TABLE[2][BLUE_BOOSTER] = 0xF0F6;

        // Initialize item name mappings
        ITEM_NAMES[ENERGY_TANK] = "Energy Tank";
        ITEM_NAMES[MISSILE] = "Missile";
        ITEM_NAMES[SUPER_MISSILE] = "Super Missile";
        ITEM_NAMES[POWER_BOMB] = "Power Bomb";
        ITEM_NAMES[BOMBS] = "Bombs";
        ITEM_NAMES[CHARGE_BEAM] = "Charge Beam";
        ITEM_NAMES[ICE_BEAM] = "Ice Beam";
        ITEM_NAMES[HI_JUMP] = "Hi-Jump Boots";
        ITEM_NAMES[SPEED_BOOSTER] = "Speed Booster";
        ITEM_NAMES[WAVE_BEAM] = "Wave Beam";
        ITEM_NAMES[SPAZER_BEAM] = "Spazer Beam";
        ITEM_NAMES[SPRING_BALL] = "Spring Ball";
        ITEM_NAMES[VARIA_SUIT] = "Varia Suit";
        ITEM_NAMES[GRAVITY_SUIT] = "Gravity Suit";
        ITEM_NAMES[XRAY_SCOPE] = "X-Ray Scope";
        ITEM_NAMES[PLASMA_BEAM] = "Plasma Beam";
        ITEM_NAMES[GRAPPLE_BEAM] = "Grapple Beam";
        ITEM_NAMES[SPACE_JUMP] = "Space Jump";
        ITEM_NAMES[SCREW_ATTACK] = "Screw Attack";
        ITEM_NAMES[MORPH_BALL] = "Morph Ball";
        ITEM_NAMES[RESERVE_TANK] = "Reserve Tank";
        ITEM_NAMES[WALL_JUMP_BOOTS] = "Wall Jump Boots";
        ITEM_NAMES[NOTHING] = "Nothing";
        ITEM_NAMES[SPARK_BOOSTER] = "Spark Booster";
        ITEM_NAMES[BLUE_BOOSTER] = "Blue Booster";

        // Build name to ID map
        ITEM_NAME_TO_ID = new HashMap<>();
        for (int item = 0; item < ITEM_COUNT; item++) {
            // Add display name (lowercase, with spaces)
            ITEM_NAME_TO_ID.put(ITEM_NAMES[item].toLowerCase(), item);

            // Add ID format (uppercase, with underscores)
            String idName = convertToIdFormat(ITEM_NAMES[item]);
            if (idName != null) {
                ITEM_NAME_TO_ID.put(idName, item);
            }
        }
    }

    /**
     * Converts a display name to ID format.
     * Example: "Power Bomb" → "POWER_BOMB"
     *
     * @param displayName Display name with spaces
     * @return ID format with underscores, or null if no mapping
     */
    private static String convertToIdFormat(String displayName) {
        // Special mappings for non-standard names
        Map<String, String> specialCases = Map.ofEntries(
            Map.entry("Energy Tank", "ENERGY_TANK"),
            Map.entry("Hi-Jump Boots", "HI_JUMP_BOOTS"),
            Map.entry("Super Missile", "SUPER_MISSILE"),
            Map.entry("Super Missile Tank", "SUPER_MISSILE_TANK"),
            Map.entry("Spazer Beam", "SPAZER_BEAM"),
            Map.entry("Spring Ball", "SPRING_BALL"),
            Map.entry("Varia Suit", "VARIA_SUIT"),
            Map.entry("Gravity Suit", "GRAVITY_SUIT"),
            Map.entry("X-Ray Scope", "XRAY_SCOPE"),
            Map.entry("Plasma Beam", "PLASMA_BEAM"),
            Map.entry("Grapple Beam", "GRAPPLE_BEAM"),
            Map.entry("Space Jump", "SPACE_JUMP"),
            Map.entry("Screw Attack", "SCREW_ATTACK"),
            Map.entry("Morph Ball", "MORPH_BALL"),
            Map.entry("Reserve Tank", "RESERVE_TANK"),
            Map.entry("Wall Jump Boots", "WALL_JUMP_BOOTS"),
            Map.entry("Spark Booster", "SPARK_BOOSTER"),
            Map.entry("Blue Booster", "BLUE_BOOSTER"),
            Map.entry("Charge Beam", "CHARGE_BEAM"),
            Map.entry("Ice Beam", "ICE_BEAM"),
            Map.entry("Wave Beam", "WAVE_BEAM"),
            Map.entry("Speed Booster", "SPEED_BOOSTER"),
            Map.entry("Missile", "MISSILE"),
            Map.entry("Missile Tank", "MISSILE_TANK"),
            Map.entry("Power Bomb", "POWER_BOMB"),
            Map.entry("Power Bomb Tank", "POWER_BOMB_TANK"),
            Map.entry("Bombs", "BOMB"),
            Map.entry("Bomb", "BOMB")
        );

        return specialCases.get(displayName);
    }

    /**
     * Gets the PLM type value for a given container and item.
     *
     * @param containerType 0=none/pedestal, 1=chozo orb, 2=hidden block
     * @param itemId Item ID (0-24)
     * @return PLM type value (16-bit)
     * @throws IllegalArgumentException if containerType or itemId is invalid
     */
    public static int getPlmType(int containerType, int itemId) {
        if (containerType < 0 || containerType >= CONTAINER_COUNT) {
            throw new IllegalArgumentException("Invalid container type: " + containerType);
        }
        if (itemId < 0 || itemId >= ITEM_COUNT) {
            throw new IllegalArgumentException("Invalid item ID: " + itemId);
        }
        return PLM_TABLE[containerType][itemId];
    }

    /**
     * Gets the PLM type value for an item name and container type.
     *
     * @param containerType 0=none/pedestal, 1=chozo orb, 2=hidden block
     * @param itemName Item name (case-insensitive)
     * @return PLM type value (16-bit), or -1 if item name not found
     */
    public static int getPlmType(int containerType, String itemName) {
        Integer itemId = getItemId(itemName);
        if (itemId == -1) {
            return -1;
        }
        return getPlmType(containerType, itemId);
    }

    /**
     * Detects the container type from a PLM value.
     * Uses range checks since PLM values are not strictly sequential.
     *
     * Container 0 (None/Pedestal): 0xEED7-0xEED7, 0xEEDB-0xEF27, 0xF000, 0xF0E2-0xF0EE
     * Container 1 (Chozo Orb): 0xEF2B-0xEF7B, 0xF004, 0xF0E6-0xF0F2
     * Container 2 (Hidden Block): 0xEF7F-0xEFCF, 0xF008, 0xF0EA-0xF0F6
     *
     * @param plmType PLM type value from ROM
     * @return Container type (0, 1, 2) or -1 if PLM value is out of range
     */
    public static int detectContainerType(int plmType) {
        // Container 0 ranges
        if ((plmType >= 0xEED7 && plmType <= 0xEF27) ||
            (plmType >= 0xF000 && plmType <= 0xF000) ||
            (plmType >= 0xF0E2 && plmType <= 0xF0EE)) {
            return 0;
        }

        // Container 1 ranges
        if ((plmType >= 0xEF2B && plmType <= 0xEF7B) ||
            (plmType >= 0xF004 && plmType <= 0xF004) ||
            (plmType >= 0xF0E6 && plmType <= 0xF0F2)) {
            return 1;
        }

        // Container 2 ranges
        if ((plmType >= 0xEF7F && plmType <= 0xEFCF) ||
            (plmType >= 0xF008 && plmType <= 0xF008) ||
            (plmType >= 0xF0EA && plmType <= 0xF0F6)) {
            return 2;
        }

        return -1;
    }

    /**
     * Gets the total number of items in the PLM table.
     *
     * @return Item count (25)
     */
    public static int getItemCount() {
        return ITEM_COUNT;
    }

    /**
     * Gets the name of an item by its ID.
     *
     * @param itemId Item ID (0-24)
     * @return Item name, or "Unknown" if ID is invalid
     */
    public static String getItemName(int itemId) {
        if (itemId < 0 || itemId >= ITEM_COUNT) {
            return "Unknown";
        }
        return ITEM_NAMES[itemId];
    }

    /**
     * Gets the item ID from an item name.
     * Search is case-insensitive.
     *
     * @param itemName Item name
     * @return Item ID (0-24), or -1 if not found
     */
    public static int getItemId(String itemName) {
        if (itemName == null || itemName.isEmpty()) {
            return -1;
        }
        Integer id = ITEM_NAME_TO_ID.get(itemName.toLowerCase());
        return id != null ? id : -1;
    }

    /**
     * Gets the container offset value.
     * Each container type is separated by this many PLM entries.
     *
     * @return Container offset (84 = 0x54)
     */
    public static int getContainerOffset() {
        return CONTAINER_OFFSET;
    }

    /**
     * Gets the base PLM value (container 0, item 0).
     *
     * @return Base PLM value (0xEED7)
     */
    public static int getBasePlmValue() {
        return BASE_PLM_VALUE;
    }

    /**
     * Checks if a PLM value is valid (within expected range).
     *
     * @param plmType PLM type value to check
     * @return true if valid, false otherwise
     */
    public static boolean isValidPlmValue(int plmType) {
        return plmType >= BASE_PLM_VALUE && plmType <= 0xF0EE;
    }

    /**
     * Checks if an item ID is valid.
     *
     * @param itemId Item ID to check
     * @return true if valid (0-24), false otherwise
     */
    public static boolean isValidItemId(int itemId) {
        return itemId >= 0 && itemId < ITEM_COUNT;
    }

    /**
     * Checks if a container type is valid.
     *
     * @param containerType Container type to check
     * @return true if valid (0-2), false otherwise
     */
    public static boolean isValidContainerType(int containerType) {
        return containerType >= 0 && containerType < CONTAINER_COUNT;
    }
}
