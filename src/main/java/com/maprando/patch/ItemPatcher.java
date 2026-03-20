package com.maprando.patch;

import com.maprando.data.DataLoader;
import com.maprando.randomize.RandomizationResult;

import java.util.Map;

/**
 * Patches items into ROM at specific addresses.
 * Converts item IDs to ROM byte values and writes them to the correct locations.
 *
 * NOTE: This is a proof-of-concept implementation.
 * Item to ROM byte mapping uses placeholder values for testing.
 * Production deployment requires real ROM disassembly research to determine
 * actual item byte values used by Super Metroid.
 */
public class ItemPatcher {

    private final Rom rom;
    private final LocationRomAddressMapper addressMapper;

    /**
     * Item ID to ROM byte value mapping.
     * NOTE: These are placeholder values for proof-of-concept testing.
     * Real ROM byte values must be determined from ROM disassembly.
     *
     * Common Super Metroid item byte values (approximate):
     * - $E5: Empty pedestal / Energy Tank
     * - $E6: Missile tank
     * - $E7: Super Missile tank
     * - $E8: Power Bomb tank
     * - $01-$E4: Major items (actual mapping needs research)
     */
    private static final Map<String, Byte> ITEM_ROM_BYTES = Map.ofEntries(
        // Major items - placeholder values ($01-$0F range)
        Map.entry("MORPH_BALL", (byte) 0x01),
        Map.entry("CHARGE_BEAM", (byte) 0x02),
        Map.entry("ICE_BEAM", (byte) 0x03),
        Map.entry("WAVE_BEAM", (byte) 0x04),
        Map.entry("SPAZER_BEAM", (byte) 0x05),
        Map.entry("PLASMA_BEAM", (byte) 0x06),
        Map.entry("LONG_BEAM", (byte) 0x07),
        Map.entry("BOMB", (byte) 0x08),
        Map.entry("SPRING_BALL", (byte) 0x09),
        Map.entry("SCREW_ATTACK", (byte) 0x0A),
        Map.entry("VARIA_SUIT", (byte) 0x0B),
        Map.entry("GRAVITY_SUIT", (byte) 0x0C),
        Map.entry("MORPH_BALL_BALL", (byte) 0x0D),
        Map.entry("HI_JUMP_BOOTS", (byte) 0x0E),
        Map.entry("SPEED_BOOSTER", (byte) 0x0F),
        Map.entry("SPACE_JUMP", (byte) 0x10),
        Map.entry("GRAPPLING_BEAM", (byte) 0x11),
        Map.entry("XRAY_SCOPE", (byte) 0x12),

        // Tanks - actual ROM values
        Map.entry("MISSILE_TANK", (byte) 0xE6),
        Map.entry("SUPER_MISSILE_TANK", (byte) 0xE7),
        Map.entry("POWER_BOMB_TANK", (byte) 0xE8),
        Map.entry("ENERGY_TANK", (byte) 0xE5),

        // Special
        Map.entry("NOTHING", (byte) 0xE5),  // Empty pedestal
        Map.entry("RESERVE_TANK", (byte) 0xE9)
    );

    /**
     * Creates a new ItemPatcher.
     *
     * @param rom ROM to patch
     * @param dataLoader data loader for location address mappings
     */
    public ItemPatcher(Rom rom, DataLoader dataLoader) {
        this.rom = rom;
        this.addressMapper = new LocationRomAddressMapper(dataLoader);
    }

    /**
     * Creates a new ItemPatcher with a custom address mapper.
     *
     * @param rom ROM to patch
     * @param addressMapper custom address mapper
     */
    public ItemPatcher(Rom rom, LocationRomAddressMapper addressMapper) {
        this.rom = rom;
        this.addressMapper = addressMapper;
    }

    /**
     * Patches a single item to ROM at the specified location.
     *
     * @param locationId location identifier
     * @param itemId item identifier to place
     * @throws IllegalArgumentException if location has no ROM address or item is unknown
     */
    public void patchItem(String locationId, String itemId) {
        if (locationId == null || itemId == null) {
            throw new IllegalArgumentException("Location ID and Item ID cannot be null");
        }

        // Get PC address for location
        int pcAddr = addressMapper.getPcAddress(locationId);
        if (pcAddr == -1) {
            throw new IllegalArgumentException("Location has no ROM address: " + locationId);
        }

        // Get ROM byte value for item
        byte itemByte = getItemByteValue(itemId);

        // Write item to ROM
        rom.writeU8(pcAddr, itemByte & 0xFF);
    }

    /**
     * Patches all placements from a randomization result to ROM.
     *
     * @param result randomization result with placements
     */
    public void patchAllPlacements(RandomizationResult result) {
        if (result == null || result.getPlacements() == null) {
            return;
        }

        for (Map.Entry<String, String> entry : result.getPlacements().entrySet()) {
            String locationId = entry.getKey();
            String itemId = entry.getValue();

            try {
                patchItem(locationId, itemId);
            } catch (IllegalArgumentException e) {
                // Log warning but continue with other placements
                System.err.println("Warning: Could not patch " + locationId + ": " + e.getMessage());
            }
        }
    }

    /**
     * Gets the ROM byte value for an item ID.
     *
     * @param itemId item identifier
     * @return ROM byte value
     * @throws IllegalArgumentException if item ID is unknown
     */
    public byte getItemByteValue(String itemId) {
        if (itemId == null) {
            throw new IllegalArgumentException("Item ID cannot be null");
        }

        Byte itemByte = ITEM_ROM_BYTES.get(itemId);
        if (itemByte == null) {
            throw new IllegalArgumentException("Unknown item ID: " + itemId);
        }

        return itemByte;
    }

    /**
     * Checks if an item ID has a known ROM byte mapping.
     *
     * @param itemId item identifier
     * @return true if item has mapping, false otherwise
     */
    public boolean hasItemMapping(String itemId) {
        if (itemId == null) {
            return false;
        }
        return ITEM_ROM_BYTES.containsKey(itemId);
    }

    /**
     * Gets the number of items with ROM byte mappings.
     *
     * @return count of mapped items
     */
    public int getMappedItemCount() {
        return ITEM_ROM_BYTES.size();
    }

    /**
     * Gets the address mapper used by this patcher.
     *
     * @return address mapper
     */
    public LocationRomAddressMapper getAddressMapper() {
        return addressMapper;
    }

    /**
     * Gets the ROM being patched.
     *
     * @return ROM
     */
    public Rom getRom() {
        return rom;
    }
}
