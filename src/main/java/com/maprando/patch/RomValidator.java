package com.maprando.patch;

import java.util.Arrays;

/**
 * Validator for Super Metroid ROM files.
 * Checks ROM size, header, and optional checksum validation.
 *
 * NOTE: This is a proof-of-concept implementation.
 * ROM addresses and validation logic are simplified for testing purposes.
 * Production deployment requires real ROM disassembly research.
 */
public class RomValidator {

    // Valid ROM sizes
    private static final int UNHEADERED_ROM_SIZE = 3145728;  // 3 MB (0x300000 bytes)
    private static final int HEADERED_ROM_SIZE = 3146240;    // 3 MB + 512 byte header

    // Expected ROM header string
    // Note: ROM may have "Super Metroid" (mixed case) or "SUPER METROID" (all caps)
    private static final String SUPER_METROID_HEADER = "Super Metroid";

    // Header location in ROM (SNES address $00FFC0, PC address $7FC0 for unheadered)
    private static final int HEADER_OFFSET_UNHEADERED = 0x7FC0;
    private static final int HEADER_OFFSET_HEADERED = 0x81C0;  // 0x7FC0 + 512 byte header

    /**
     * Validates if the given data is a valid Super Metroid ROM.
     * Checks size and header.
     *
     * @param data ROM data to validate
     * @return true if valid Super Metroid ROM, false otherwise
     */
    public static boolean isValidSuperMetroidRom(byte[] data) {
        if (data == null) {
            return false;
        }

        // Check size first
        if (!isValidSize(data.length)) {
            return false;
        }

        // Determine header offset based on size
        int headerOffset = (data.length == HEADERED_ROM_SIZE) ? HEADER_OFFSET_HEADERED : HEADER_OFFSET_UNHEADERED;

        // Check if we can read the header
        if (data.length < headerOffset + SUPER_METROID_HEADER.length()) {
            return false;
        }

        // Check for "Super Metroid" string in header
        // Use startsWith to allow for trailing spaces/padding
        // Case-insensitive comparison to handle different ROM versions
        byte[] headerBytes = new byte[SUPER_METROID_HEADER.length()];
        System.arraycopy(data, headerOffset, headerBytes, 0, SUPER_METROID_HEADER.length());
        String header = new String(headerBytes);

        return header.equalsIgnoreCase(SUPER_METROID_HEADER) ||
               new String(data, headerOffset, headerOffset + 32).toLowerCase().startsWith(SUPER_METROID_HEADER.toLowerCase());
    }

    /**
     * Validates if the given size is a valid Super Metroid ROM size.
     * Accepts both headered and unheadered ROM sizes.
     *
     * @param size size of ROM data in bytes
     * @return true if valid size, false otherwise
     */
    public static boolean isValidSize(int size) {
        return size == UNHEADERED_ROM_SIZE || size == HEADERED_ROM_SIZE;
    }

    /**
     * Validates ROM checksum.
     * NOTE: This is a simplified implementation for proof-of-concept.
     * Production would need actual checksum validation from ROM disassembly.
     *
     * @param data ROM data to validate
     * @return true for now (checksum validation not implemented)
     */
    public static boolean isValidChecksum(byte[] data) {
        // Placeholder - actual checksum validation requires ROM disassembly research
        // For proof-of-concept, we'll return true if size and header are valid
        return isValidSuperMetroidRom(data);
    }

    /**
     * Gets the expected size for an unheadered ROM.
     *
     * @return unheadered ROM size in bytes
     */
    public static int getUnheaderedSize() {
        return UNHEADERED_ROM_SIZE;
    }

    /**
     * Gets the expected size for a headered ROM.
     *
     * @return headered ROM size in bytes
     */
    public static int getHeaderedSize() {
        return HEADERED_ROM_SIZE;
    }

    /**
     * Checks if the given ROM data has a header.
     *
     * @param data ROM data to check
     * @return true if ROM has a header, false otherwise
     */
    public static boolean isHeadered(byte[] data) {
        if (data == null) {
            return false;
        }
        return data.length == HEADERED_ROM_SIZE;
    }

    /**
     * Removes header from headered ROM data.
     *
     * @param data ROM data (may be headered or unheadered)
     * @return unheadered ROM data, or original if already unheadered
     */
    public static byte[] unheaderRom(byte[] data) {
        if (data == null || !isHeadered(data)) {
            return data;
        }

        byte[] unheadered = new byte[UNHEADERED_ROM_SIZE];
        System.arraycopy(data, 512, unheadered, 0, UNHEADERED_ROM_SIZE);
        return unheadered;
    }
}
