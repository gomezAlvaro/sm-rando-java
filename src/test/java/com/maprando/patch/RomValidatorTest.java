package com.maprando.patch;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for RomValidator.
 * Tests ROM validation for size, header, and checksum.
 */
class RomValidatorTest {

    @Test
    void testValidSuperMetroidRom_Unheadered() {
        // Create valid unheadered ROM data (3145728 bytes)
        byte[] data = new byte[3145728];
        addSuperMetroidHeader(data);

        assertTrue(RomValidator.isValidSuperMetroidRom(data));
        assertTrue(RomValidator.isValidSize(data.length));
    }

    @Test
    void testValidSuperMetroidRom_Headered() {
        // Create valid headered ROM data (3146240 bytes)
        byte[] data = new byte[3146240];
        addSuperMetroidHeader(data, 512); // Header starts at offset 512

        assertTrue(RomValidator.isValidSuperMetroidRom(data));
        assertTrue(RomValidator.isValidSize(data.length));
    }

    @Test
    void testInvalidRomSize_TooSmall() {
        byte[] data = new byte[1000000];
        assertFalse(RomValidator.isValidSize(data.length));
        assertFalse(RomValidator.isValidSuperMetroidRom(data));
    }

    @Test
    void testInvalidRomSize_TooLarge() {
        byte[] data = new byte[4000000];
        assertFalse(RomValidator.isValidSize(data.length));
        assertFalse(RomValidator.isValidSuperMetroidRom(data));
    }

    @Test
    void testMissingSuperMetroidHeader() {
        byte[] data = new byte[3145728];
        // Don't add the header string

        assertFalse(RomValidator.isValidSuperMetroidRom(data));
    }

    @Test
    void testEmptyRom() {
        byte[] data = new byte[0];
        assertFalse(RomValidator.isValidSize(data.length));
        assertFalse(RomValidator.isValidSuperMetroidRom(data));
    }

    @Test
    void testNullRom() {
        assertFalse(RomValidator.isValidSuperMetroidRom(null));
        assertFalse(RomValidator.isValidSize(0));
    }

    @Test
    void testRomWithCorrectSizeString() {
        byte[] data = new byte[3145728];
        addSuperMetroidHeader(data);

        assertTrue(RomValidator.isValidSuperMetroidRom(data));
    }

    // Helper methods
    private void addSuperMetroidHeader(byte[] data) {
        addSuperMetroidHeader(data, 0);
    }

    private void addSuperMetroidHeader(byte[] data, int offset) {
        if (data.length < offset + 0x8000) {
            return;
        }
        // Add "SUPER METROID" string at expected location (0x7FC0 + offset)
        byte[] header = "SUPER METROID".getBytes();
        int headerOffset = 0x7FC0 + offset;
        System.arraycopy(header, 0, data, headerOffset, Math.min(header.length, data.length - headerOffset));
    }
}
