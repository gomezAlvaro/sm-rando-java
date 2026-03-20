package com.maprando.patch;

import com.maprando.data.DataLoader;
import com.maprando.data.model.LocationData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Maps location IDs to ROM addresses where items are stored.
 * Handles SNES to PC address conversion for ROM patching.
 *
 * ROM addresses are stored in SNES format (e.g., "0x7E82F5") and
 * converted to PC addresses for ROM writing operations.
 */
public class LocationRomAddressMapper {

    private final Map<String, String> locationToRomAddress;
    private final Map<String, Integer> locationToPcAddress;

    /**
     * Creates a new mapper and loads ROM address mappings from data.
     *
     * @param dataLoader data loader containing location definitions
     */
    public LocationRomAddressMapper(DataLoader dataLoader) {
        this.locationToRomAddress = new HashMap<>();
        this.locationToPcAddress = new HashMap<>();
        loadRomAddresses(dataLoader);
    }

    /**
     * Loads ROM addresses from location definitions.
     *
     * @param dataLoader data loader containing location definitions
     */
    private void loadRomAddresses(DataLoader dataLoader) {
        LocationData locationData = dataLoader.getLocationData();
        if (locationData == null || locationData.getLocations() == null) {
            return;
        }

        for (LocationData.LocationDefinition loc : locationData.getLocations()) {
            String romAddress = loc.getRomAddress();
            if (romAddress != null && !romAddress.isEmpty() &&
                isValidRomAddressFormat(romAddress)) {
                locationToRomAddress.put(loc.getId(), romAddress);
                locationToPcAddress.put(loc.getId(), snesToPc(romAddress));
            }
        }
    }

    /**
     * Gets the ROM address for a location in SNES format.
     *
     * @param locationId location identifier
     * @return ROM address in SNES format (e.g., "0x7E82F5"), or null if not found
     */
    public String getRomAddress(String locationId) {
        if (locationId == null) {
            return null;
        }
        return locationToRomAddress.get(locationId);
    }

    /**
     * Checks if a location has a ROM address mapping.
     *
     * @param locationId location identifier
     * @return true if location has ROM address, false otherwise
     */
    public boolean hasRomAddress(String locationId) {
        if (locationId == null) {
            return false;
        }
        return locationToRomAddress.containsKey(locationId);
    }

    /**
     * Gets the PC address for a location.
     * PC addresses are used for actual ROM read/write operations.
     *
     * @param locationId location identifier
     * @return PC address, or -1 if location has no ROM address
     */
    public int getPcAddress(String locationId) {
        if (locationId == null) {
            return -1;
        }
        Integer pcAddr = locationToPcAddress.get(locationId);
        return pcAddr != null ? pcAddr : -1;
    }

    /**
     * Gets all locations that have ROM address mappings.
     *
     * @return set of location IDs with ROM addresses
     */
    public Set<String> getAllMappedLocations() {
        return new HashSet<>(locationToRomAddress.keySet());
    }

    /**
     * Converts a SNES ROM address to a PC address.
     * SNES addresses use the HiROM format.
     *
     * SNES address format: 0xABBBBB
     * - A: Bank (0x80-0xFF for HiROM)
     * - BBBBB: Offset within bank
     *
     * PC address calculation:
     * - If bank >= 0x80: pc = (bank - 0x80) * 0x10000 + offset
     * - For HiROM: pc = ((addr >> 1) & 0x3F8000) | (addr & 0x7FFF)
     *
     * @param snesAddress SNES address in hex format (e.g., "0x7E82F5")
     * @return PC address
     * @throws IllegalArgumentException if address format is invalid
     */
    public static int snesToPc(String snesAddress) {
        if (snesAddress == null || !snesAddress.startsWith("0x")) {
            throw new IllegalArgumentException("Invalid SNES address format: " + snesAddress);
        }

        try {
            int addr = Integer.parseInt(snesAddress.substring(2), 16);
            return Rom.snes2pc(addr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid SNES address: " + snesAddress, e);
        }
    }

    /**
     * Validates that a ROM address string is in valid SNES hex format.
     *
     * @param address address string to validate
     * @return true if valid format, false otherwise
     */
    public static boolean isValidRomAddressFormat(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }

        if (!address.startsWith("0x")) {
            return false;
        }

        String hexPart = address.substring(2);
        if (hexPart.isEmpty() || hexPart.length() > 6) {
            return false;
        }

        try {
            Integer.parseInt(hexPart, 16);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Gets the number of mapped locations.
     *
     * @return count of locations with ROM addresses
     */
    public int getMappedLocationCount() {
        return locationToRomAddress.size();
    }
}
