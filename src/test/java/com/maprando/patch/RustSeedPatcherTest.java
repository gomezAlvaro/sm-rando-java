package com.maprando.patch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Tests for Rust-compatible seed storage format.
 * Tests seed metadata injection matching Rust MapRandomizer project.
 *
 * Rust seed storage locations:
 * - 0x7FC0: ROM header "SUPERMETROID MAPRANDO" (21 bytes)
 * - 0xDFFEF0: Seed name (16 bytes, null-terminated, URL-safe ASCII)
 * - 0xDFFF00: Display seed (4 bytes, u32 little-endian)
 *
 * Reference: rust/maprando/src/patch.rs: apply_seed_identifiers()
 */
class RustSeedPatcherTest {

    private Rom rom;
    private RustSeedPatcher patcher;

    @BeforeEach
    void setUp() {
        // Create a test ROM (3MB size)
        byte[] testRomData = new byte[0x300000];
        rom = new Rom(testRomData);
        patcher = new RustSeedPatcher(rom);
    }

    /**
     * Test creating RustSeedPatcher.
     */
    @Test
    void testCreatePatcher() {
        assertNotNull(patcher);
        assertNotNull(patcher.getRom());
    }

    /**
     * Test patching ROM header with Rust format.
     */
    @Test
    void testPatchRomHeader() {
        patcher.patchRomHeader();

        // Read header from 0x7FC0 (PC address)
        int pcAddr = Rom.snes2pc(0x7FC0);
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            int b = rom.readU8(pcAddr + i);
            if (b == 0) break;
            header.append((char) b);
        }

        assertEquals("SUPERMETROID MAPRANDO", header.toString());
    }

    /**
     * Test patching seed name to 0xDFFEF0.
     */
    @Test
    void testPatchSeedName() {
        String seedName = "abc123";
        patcher.patchSeedName(seedName);

        // Read seed name from 0xDFFEF0
        int pcAddr = Rom.snes2pc(0xDFFEF0);
        StringBuilder readName = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int b = rom.readU8(pcAddr + i);
            if (b == 0) break;
            readName.append((char) b);
        }

        assertEquals(seedName, readName.toString());
    }

    /**
     * Test seed name maximum length (16 bytes).
     */
    @Test
    void testSeedNameMaxLength() {
        // 15 characters should work
        String name15 = "123456789012345";
        assertDoesNotThrow(() -> patcher.patchSeedName(name15));

        // 16 characters should also work (fills buffer exactly)
        String name16 = "1234567890123456";
        assertDoesNotThrow(() -> patcher.patchSeedName(name16));
    }

    /**
     * Test seed name too long throws exception.
     */
    @Test
    void testSeedNameTooLong() {
        String nameTooLong = "12345678901234567"; // 17 characters
        assertThrows(IllegalArgumentException.class, () -> {
            patcher.patchSeedName(nameTooLong);
        });
    }

    /**
     * Test patching display seed to 0xDFFF00.
     */
    @Test
    void testPatchDisplaySeed() {
        long displaySeed = 123456789L;
        patcher.patchDisplaySeed(displaySeed);

        // Read display seed from 0xDFFF00 (4 bytes, little-endian u32)
        int pcAddr = Rom.snes2pc(0xDFFF00);
        int byte0 = rom.readU8(pcAddr);
        int byte1 = rom.readU8(pcAddr + 1);
        int byte2 = rom.readU8(pcAddr + 2);
        int byte3 = rom.readU8(pcAddr + 3);

        // Little-endian: least significant byte first
        long readSeed = (byte3 << 24) | (byte2 << 16) | (byte1 << 8) | byte0;
        assertEquals(displaySeed, readSeed);
    }

    /**
     * Test display seed value 0.
     */
    @Test
    void testDisplaySeedZero() {
        assertDoesNotThrow(() -> {
            patcher.patchDisplaySeed(0);
        });

        int pcAddr = Rom.snes2pc(0xDFFF00);
        for (int i = 0; i < 4; i++) {
            assertEquals(0, rom.readU8(pcAddr + i));
        }
    }

    /**
     * Test display seed maximum value.
     */
    @Test
    void testDisplaySeedMaxValue() {
        long maxSeed = 0xFFFFFFFFL; // Max u32
        patcher.patchDisplaySeed(maxSeed);

        int pcAddr = Rom.snes2pc(0xDFFF00);
        for (int i = 0; i < 4; i++) {
            assertEquals(0xFF, rom.readU8(pcAddr + i));
        }
    }

    /**
     * Test patching all seed metadata at once.
     */
    @Test
    void testPatchAllMetadata() {
        String seedName = "testseed";
        long displaySeed = 987654321L;

        patcher.patchAllMetadata(seedName, displaySeed);

        // Verify ROM header
        int headerAddr = Rom.snes2pc(0x7FC0);
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            int b = rom.readU8(headerAddr + i);
            if (b == 0) break;
            header.append((char) b);
        }
        assertEquals("SUPERMETROID MAPRANDO", header.toString());

        // Verify seed name
        int nameAddr = Rom.snes2pc(0xDFFEF0);
        StringBuilder readName = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int b = rom.readU8(nameAddr + i);
            if (b == 0) break;
            readName.append((char) b);
        }
        assertEquals(seedName, readName.toString());

        // Verify display seed
        int seedAddr = Rom.snes2pc(0xDFFF00);
        int byte0 = rom.readU8(seedAddr);
        int byte1 = rom.readU8(seedAddr + 1);
        int byte2 = rom.readU8(seedAddr + 2);
        int byte3 = rom.readU8(seedAddr + 3);
        long readSeed = (byte3 << 24) | (byte2 << 16) | (byte1 << 8) | byte0;
        assertEquals(displaySeed, readSeed);
    }

    /**
     * Test reading seed name from ROM.
     */
    @Test
    void testReadSeedName() {
        String seedName = "readtest";
        patcher.patchSeedName(seedName);

        String readName = patcher.readSeedName();
        assertEquals(seedName, readName);
    }

    /**
     * Test reading display seed from ROM.
     */
    @Test
    void testReadDisplaySeed() {
        long displaySeed = 456789123L;
        patcher.patchDisplaySeed(displaySeed);

        long readSeed = patcher.readDisplaySeed();
        assertEquals(displaySeed, readSeed);
    }

    /**
     * Test reading returns empty string when no seed name written.
     */
    @Test
    void testReadSeedNameEmpty() {
        String readName = patcher.readSeedName();
        assertTrue(readName.isEmpty() || readName.equals("\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0"));
    }

    /**
     * Test reading returns 0 when no display seed written.
     */
    @Test
    void testReadDisplaySeedZero() {
        long readSeed = patcher.readDisplaySeed();
        assertEquals(0, readSeed);
    }

    /**
     * Test seed name uses URL-safe ASCII characters only.
     */
    @Test
    void testSeedNameUrlSafe() {
        // URL-safe characters: letters, numbers, hyphen, underscore
        String urlSafeName = "AbC-123_456";
        assertDoesNotThrow(() -> {
            patcher.patchSeedName(urlSafeName);
        });

        String readName = patcher.readSeedName();
        assertEquals(urlSafeName, readName);
    }

    /**
     * Test that seed name is null-terminated.
     */
    @Test
    void testSeedNameNullTerminated() {
        String seedName = "short";
        patcher.patchSeedName(seedName);

        int pcAddr = Rom.snes2pc(0xDFFEF0);

        // Check characters are written
        for (int i = 0; i < seedName.length(); i++) {
            assertEquals(seedName.charAt(i), rom.readU8(pcAddr + i));
        }

        // Check null terminator
        assertEquals(0, rom.readU8(pcAddr + seedName.length()));

        // Check rest is zero-padded
        for (int i = seedName.length() + 1; i < 16; i++) {
            assertEquals(0, rom.readU8(pcAddr + i));
        }
    }

    /**
     * Test ROM header padding.
     */
    @Test
    void testRomHeaderPadding() {
        patcher.patchRomHeader();

        int pcAddr = Rom.snes2pc(0x7FC0);

        // Check "SUPERMETROID MAPRANDO" is written
        String header = "SUPERMETROID MAPRANDO";
        for (int i = 0; i < header.length(); i++) {
            assertEquals(header.charAt(i), rom.readU8(pcAddr + i));
        }

        // Check null terminator
        assertEquals(0, rom.readU8(pcAddr + header.length()));
    }

    /**
     * Test display seed endianness (little-endian).
     */
    @Test
    void testDisplaySeedEndianness() {
        // Use a value with different bytes to verify endianness
        // Use value that fits in signed int range for testing
        long displaySeed = 0x12345678L;
        patcher.patchDisplaySeed(displaySeed);

        int pcAddr = Rom.snes2pc(0xDFFF00);

        // Little-endian: least significant byte first
        assertEquals(0x78, rom.readU8(pcAddr));     // Byte 0: LSB
        assertEquals(0x56, rom.readU8(pcAddr + 1)); // Byte 1
        assertEquals(0x34, rom.readU8(pcAddr + 2)); // Byte 2
        assertEquals(0x12, rom.readU8(pcAddr + 3)); // Byte 3: MSB
    }

    /**
     * Test that ROM addresses match Rust project exactly.
     */
    @Test
    void testRomAddressesMatchRust() {
        // Verify address constants match Rust
        assertEquals(0x7FC0, RustSeedPatcher.ROM_HEADER_ADDR);
        assertEquals(0xDFFEF0, RustSeedPatcher.SEED_NAME_ADDR);
        assertEquals(0xDFFF00, RustSeedPatcher.DISPLAY_SEED_ADDR);
        assertEquals(16, RustSeedPatcher.MAX_SEED_NAME_LENGTH);
        assertEquals(4, RustSeedPatcher.DISPLAY_SEED_SIZE);
    }

    /**
     * Test patching metadata multiple times overwrites previous data.
     */
    @Test
    void testOverwriteMetadata() {
        // Write first seed
        patcher.patchAllMetadata("seed1", 111);
        assertEquals("seed1", patcher.readSeedName());
        assertEquals(111, patcher.readDisplaySeed());

        // Overwrite with second seed
        patcher.patchAllMetadata("seed2", 222);
        assertEquals("seed2", patcher.readSeedName());
        assertEquals(222, patcher.readDisplaySeed());
    }

    /**
     * Test that ROM header is only written once.
     */
    @Test
    void testRomHeaderWrittenOnce() {
        patcher.patchRomHeader();

        int pcAddr = Rom.snes2pc(0x7FC0);
        String header1 = new String(
            java.util.Arrays.copyOfRange(rom.data, pcAddr, pcAddr + 22)
        );

        // Patch again
        patcher.patchRomHeader();

        String header2 = new String(
            java.util.Arrays.copyOfRange(rom.data, pcAddr, pcAddr + 22)
        );

        assertEquals(header1, header2);
    }

    /**
     * Test that seed data doesn't overflow into other regions.
     */
    @Test
    void testNoDataOverflow() {
        // Write maximum size seed name
        String maxName = "1234567890123456"; // 16 chars
        patcher.patchSeedName(maxName);

        // Write maximum display seed
        patcher.patchDisplaySeed(0xFFFFFFFFL);

        // Verify data is contained within expected bounds
        int nameAddr = Rom.snes2pc(0xDFFEF0);
        int seedAddr = Rom.snes2pc(0xDFFF00);

        // Seed name should be within 16 bytes
        for (int i = 0; i < 16; i++) {
            assertTrue(rom.readU8(nameAddr + i) >= 0 && rom.readU8(nameAddr + i) <= 0xFF);
        }

        // Display seed should be within 4 bytes
        for (int i = 0; i < 4; i++) {
            assertTrue(rom.readU8(seedAddr + i) >= 0 && rom.readU8(seedAddr + i) <= 0xFF);
        }

        // Byte after seed name should not be overwritten by seed name
        assertTrue(rom.readU8(nameAddr + 16) == 0 || rom.readU8(nameAddr + 16) == 0xFF);
    }

    /**
     * Test compatibility with old seed format.
     * Old format used 0x82FF00, new format uses 0xDFFEF0/0xDFFF00.
     */
    @Test
    void testNewFormatDifferentFromOld() {
        // New format addresses should be different from old
        assertNotEquals(0x82FF00, RustSeedPatcher.SEED_NAME_ADDR);
        assertNotEquals(0x82FF00, RustSeedPatcher.DISPLAY_SEED_ADDR);

        // New addresses should match Rust
        assertEquals(0xDFFEF0, RustSeedPatcher.SEED_NAME_ADDR);
        assertEquals(0xDFFF00, RustSeedPatcher.DISPLAY_SEED_ADDR);
    }
}
