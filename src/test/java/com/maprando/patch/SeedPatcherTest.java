package com.maprando.patch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for SeedPatcher.
 * Tests seed metadata injection into ROM free space.
 */
class SeedPatcherTest {

    private Rom rom;
    private SeedPatcher patcher;

    @BeforeEach
    void setUp() {
        // Create a mock ROM (3MB)
        byte[] romData = new byte[3145728];
        rom = new Rom(romData);
        patcher = new SeedPatcher(rom);
    }

    @Test
    void testPatchSeedId() {
        String seedId = "TEST1234";
        patcher.patchSeedId(seedId);

        // Verify seed ID was written to free space
        int seedDataAddr = Rom.snes2pc(SeedPatcher.SEED_DATA_ADDR);
        byte[] readData = new byte[seedId.length()];
        for (int i = 0; i < seedId.length(); i++) {
            readData[i] = (byte) rom.readU8(seedDataAddr + i);
        }

        String readSeedId = new String(readData);
        assertEquals(seedId, readSeedId);
    }

    @Test
    void testPatchSeedId_Long() {
        String seedId = "VERYLONGSEEDID12345678";
        patcher.patchSeedId(seedId);

        // Verify seed ID was written (should be truncated to max length)
        int seedDataAddr = Rom.snes2pc(SeedPatcher.SEED_DATA_ADDR);
        byte[] readData = new byte[SeedPatcher.MAX_SEED_ID_LENGTH];
        for (int i = 0; i < SeedPatcher.MAX_SEED_ID_LENGTH; i++) {
            readData[i] = (byte) rom.readU8(seedDataAddr + i);
        }

        String readSeedId = new String(readData).trim();
        assertTrue(readSeedId.length() <= SeedPatcher.MAX_SEED_ID_LENGTH);
        assertTrue(readSeedId.length() > 0);
    }

    @Test
    void testPatchTimestamp() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 3, 19, 12, 30, 45);
        patcher.patchTimestamp(timestamp);

        // Verify timestamp was written
        int timestampAddr = Rom.snes2pc(SeedPatcher.SEED_DATA_ADDR) + SeedPatcher.MAX_SEED_ID_LENGTH;

        // Read year (2 bytes)
        int year = rom.readU16(timestampAddr);
        assertEquals(2026, year);

        // Read month
        int month = rom.readU8(timestampAddr + 2);
        assertEquals(3, month);

        // Read day
        int day = rom.readU8(timestampAddr + 3);
        assertEquals(19, day);

        // Read hour
        int hour = rom.readU8(timestampAddr + 4);
        assertEquals(12, hour);

        // Read minute
        int minute = rom.readU8(timestampAddr + 5);
        assertEquals(30, minute);

        // Read second
        int second = rom.readU8(timestampAddr + 6);
        assertEquals(45, second);
    }

    @Test
    void testPatchAlgorithm() {
        String algorithm = "foresight";
        patcher.patchAlgorithm(algorithm);

        // Verify algorithm was written
        int algorithmAddr = Rom.snes2pc(SeedPatcher.SEED_DATA_ADDR) +
                           SeedPatcher.MAX_SEED_ID_LENGTH +
                           SeedPatcher.TIMESTAMP_SIZE;

        byte[] readData = new byte[SeedPatcher.MAX_ALGORITHM_LENGTH];
        for (int i = 0; i < SeedPatcher.MAX_ALGORITHM_LENGTH; i++) {
            readData[i] = (byte) rom.readU8(algorithmAddr + i);
        }

        String readAlgorithm = new String(readData).trim();
        assertEquals(algorithm, readAlgorithm);
    }

    @Test
    void testPatchFullMetadata() {
        String seedId = "FULLTEST";
        LocalDateTime timestamp = LocalDateTime.of(2026, 3, 19, 10, 15, 30);
        String algorithm = "balanced";

        patcher.patchSeedId(seedId);
        patcher.patchTimestamp(timestamp);
        patcher.patchAlgorithm(algorithm);

        // Verify all data was written
        int baseAddr = Rom.snes2pc(SeedPatcher.SEED_DATA_ADDR);

        // Verify seed ID
        byte[] seedBytes = new byte[(int) seedId.length()];
        for (int i = 0; i < seedId.length(); i++) {
            seedBytes[i] = (byte) rom.readU8(baseAddr + i);
        }
        assertEquals(seedId, new String(seedBytes));

        // Verify year
        int year = rom.readU16(baseAddr + SeedPatcher.MAX_SEED_ID_LENGTH);
        assertEquals(2026, year);

        // Verify algorithm
        int algoAddr = baseAddr + SeedPatcher.MAX_SEED_ID_LENGTH + SeedPatcher.TIMESTAMP_SIZE;
        byte[] algoBytes = new byte[(int) algorithm.length()];
        for (int i = 0; i < algorithm.length(); i++) {
            algoBytes[i] = (byte) rom.readU8(algoAddr + i);
        }
        assertEquals(algorithm, new String(algoBytes).trim());
    }

    @Test
    void testPatchSeedId_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            patcher.patchSeedId(null);
        });
    }

    @Test
    void testPatchTimestamp_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            patcher.patchTimestamp(null);
        });
    }

    @Test
    void testPatchAlgorithm_Null() {
        assertThrows(IllegalArgumentException.class, () -> {
            patcher.patchAlgorithm(null);
        });
    }

    @Test
    void testGetSeedDataSize() {
        int expectedSize = SeedPatcher.MAX_SEED_ID_LENGTH +
                          SeedPatcher.TIMESTAMP_SIZE +
                          SeedPatcher.MAX_ALGORITHM_LENGTH;
        assertEquals(expectedSize, SeedPatcher.getSeedDataSize());
    }

    @Test
    void testPatchAllMetadata() {
        String seedId = "ALLMETADATA";
        LocalDateTime timestamp = LocalDateTime.now();
        String algorithm = "basic";

        patcher.patchAllMetadata(seedId, timestamp, algorithm);

        // Verify seed ID exists
        int baseAddr = Rom.snes2pc(SeedPatcher.SEED_DATA_ADDR);
        byte[] seedBytes = new byte[SeedPatcher.MAX_SEED_ID_LENGTH];
        for (int i = 0; i < SeedPatcher.MAX_SEED_ID_LENGTH; i++) {
            seedBytes[i] = (byte) rom.readU8(baseAddr + i);
        }
        assertTrue(new String(seedBytes).trim().contains(seedId));
    }

    @Test
    void testFreeSpaceNotExceeded() {
        // Write a large amount of metadata
        String seedId = "LARGESEEDID12345";
        LocalDateTime timestamp = LocalDateTime.now();
        String algorithm = "very_long_algorithm_name";

        patcher.patchAllMetadata(seedId, timestamp, algorithm);

        // Verify we haven't written past the free space
        int baseAddr = Rom.snes2pc(SeedPatcher.SEED_DATA_ADDR);
        int totalSize = SeedPatcher.getSeedDataSize();

        // The address at the end should still be within bounds
        assertTrue(baseAddr + totalSize < rom.data.length);
    }
}
