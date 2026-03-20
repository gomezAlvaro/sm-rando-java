package com.maprando.patch;

import com.maprando.data.DataLoader;

/**
 * PLM-based item patcher for Super Metroid ROM.
 *
 * Uses 16-bit PLM (Programmable Logic Module) type values instead of simple byte values.
 * Preserves item container type (pedestal, Chozo orb, hidden shot block) when patching.
 *
 * Based on the Rust MapRandomizer project's item placement system.
 *
 * Reference: rust/maprando/src/patch.rs: place_items(), item_to_plm_type()
 */
public class PlmItemPatcher {

    private final Rom rom;
    private final DataLoader dataLoader;

    /**
     * Creates a new PLM-based item patcher.
     *
     * @param rom ROM to patch
     * @param dataLoader Data loader for location mappings
     */
    public PlmItemPatcher(Rom rom, DataLoader dataLoader) {
        if (rom == null) {
            throw new IllegalArgumentException("ROM cannot be null");
        }
        if (dataLoader == null) {
            throw new IllegalArgumentException("DataLoader cannot be null");
        }
        this.rom = rom;
        this.dataLoader = dataLoader;
    }

    /**
     * Patches an item at a specific ROM address.
     * Detects the container type from the original PLM value and preserves it.
     *
     * @param plmAddress PC address where PLM is stored
     * @param itemName Name of item to place (case-insensitive)
     * @throws IllegalArgumentException if item name is invalid or original PLM is invalid
     */
    public void patchItem(int plmAddress, String itemName) {
        // Read original PLM type from ROM
        int originalPlm = rom.readU16(plmAddress);

        // Detect container type from original PLM
        int containerType = detectContainerType(originalPlm);
        if (containerType == -1) {
            throw new IllegalArgumentException(
                "Invalid original PLM value at 0x" + Integer.toHexString(plmAddress) +
                ": 0x" + Integer.toHexString(originalPlm));
        }

        // Get new PLM type for this item and container
        int newPlm = getPlmType(containerType, itemName);
        if (newPlm == -1) {
            throw new IllegalArgumentException("Invalid item name: " + itemName);
        }

        // Write new PLM type to ROM (16-bit write)
        rom.writeU16(plmAddress, newPlm);
    }

    /**
     * Detects the container type from a PLM value.
     * Uses range checks since PLM values are not strictly sequential.
     *
     * @param plmType PLM type value
     * @return Container type (0=pedestal, 1=chozo orb, 2=hidden block) or -1 if invalid
     */
    public int detectContainerType(int plmType) {
        return PlmTypeTable.detectContainerType(plmType);
    }

    /**
     * Gets the PLM type value for an item in a specific container.
     *
     * @param containerType Container type (0, 1, 2)
     * @param itemName Item name (case-insensitive)
     * @return PLM type value, or -1 if item not found
     */
    public int getPlmType(int containerType, String itemName) {
        return PlmTypeTable.getPlmType(containerType, itemName);
    }

    /**
     * Gets the ROM being patched.
     *
     * @return ROM instance
     */
    public Rom getRom() {
        return rom;
    }

    /**
     * Gets the data loader.
     *
     * @return DataLoader instance
     */
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    /**
     * Patches an item by location ID.
     * Looks up the PLM address from the location data and patches the item.
     *
     * @param locationId Location identifier
     * @param itemName Name of item to place
     * @throws IllegalArgumentException if location not found or has no ROM address
     */
    public void patchItemByLocation(String locationId, String itemName) {
        var locationData = dataLoader.getLocationData();

        // Find location by ID
        com.maprando.data.model.LocationData.LocationDefinition location = null;
        for (com.maprando.data.model.LocationData.LocationDefinition loc : locationData.getLocations()) {
            if (loc.getId().equals(locationId)) {
                location = loc;
                break;
            }
        }

        if (location == null) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }

        String romAddressStr = location.getRomAddress();
        if (romAddressStr == null || romAddressStr.isEmpty()) {
            throw new IllegalArgumentException("Location has no ROM address: " + locationId);
        }

        // Convert SNES address to PC address
        int snesAddress = LocationRomAddressMapper.snesToPc(romAddressStr);
        int pcAddress = Rom.snes2pc(snesAddress);

        patchItem(pcAddress, itemName);
    }

    /**
     * Gets the item ID for an item name.
     *
     * @param itemName Item name (case-insensitive)
     * @return Item ID (0-24), or -1 if not found
     */
    public int getItemId(String itemName) {
        return PlmTypeTable.getItemId(itemName);
    }

    /**
     * Gets the item name for an item ID.
     *
     * @param itemId Item ID (0-24)
     * @return Item name, or "Unknown" if invalid
     */
    public String getItemName(int itemId) {
        return PlmTypeTable.getItemName(itemId);
    }

    /**
     * Checks if an item name is valid.
     *
     * @param itemName Item name to check
     * @return true if valid, false otherwise
     */
    public boolean isValidItem(String itemName) {
        return PlmTypeTable.getItemId(itemName) != -1;
    }

    /**
     * Checks if a PLM value is valid.
     *
     * @param plmType PLM type value to check
     * @return true if valid, false otherwise
     */
    public boolean isValidPlmValue(int plmType) {
        return PlmTypeTable.isValidPlmValue(plmType);
    }
}
