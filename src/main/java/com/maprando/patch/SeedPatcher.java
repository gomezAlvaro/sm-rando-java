package com.maprando.patch;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

/**
 * Patches seed metadata into ROM free space.
 * Stores seed ID, timestamp, and algorithm for verification.
 *
 * Uses free space at the end of the ROM (0x82FF00 for 3MB ROM).
 * This area is typically unused in the vanilla ROM.
 *
 * Layout:
 * - Seed ID: 32 bytes (fixed size, null-terminated string)
 * - Timestamp: 7 bytes (year: 2 bytes, month: 1 byte, day: 1 byte, hour: 1 byte, minute: 1 byte, second: 1 byte)
 * - Algorithm: 32 bytes (fixed size, null-terminated string)
 * - Total: 71 bytes
 *
 * NOTE: This is a proof-of-concept implementation.
 * In production, seed data should be stored in a more secure location
 * and possibly encrypted to prevent tampering.
 */
public class SeedPatcher {

    // Free space address for seed data (end of 3MB ROM)
    // 0x82FF00 SNES = 0x02FF00 PC, which is within the 3MB ROM
    public static final int SEED_DATA_ADDR = 0x82FF00;

    // Size limits
    public static final int MAX_SEED_ID_LENGTH = 32;
    public static final int TIMESTAMP_SIZE = 7;  // year(2) + month(1) + day(1) + hour(1) + minute(1) + second(1)
    public static final int MAX_ALGORITHM_LENGTH = 32;

    private final Rom rom;

    /**
     * Creates a new SeedPatcher.
     *
     * @param rom ROM to patch
     */
    public SeedPatcher(Rom rom) {
        if (rom == null) {
            throw new IllegalArgumentException("ROM cannot be null");
        }
        this.rom = rom;
    }

    /**
     * Patches the seed ID into ROM.
     * Seed ID is stored as a null-terminated string.
     *
     * @param seedId seed identifier (max 32 characters)
     * @throws IllegalArgumentException if seedId is null or too long
     */
    public void patchSeedId(String seedId) {
        if (seedId == null) {
            throw new IllegalArgumentException("Seed ID cannot be null");
        }

        if (seedId.length() > MAX_SEED_ID_LENGTH) {
            throw new IllegalArgumentException("Seed ID too long: max " +
                MAX_SEED_ID_LENGTH + " characters, got " + seedId.length());
        }

        int pcAddr = Rom.snes2pc(SEED_DATA_ADDR);
        byte[] seedBytes = seedId.getBytes(StandardCharsets.UTF_8);

        // Write seed ID
        for (int i = 0; i < seedBytes.length; i++) {
            rom.writeU8(pcAddr + i, seedBytes[i] & 0xFF);
        }

        // Null-terminate
        rom.writeU8(pcAddr + seedBytes.length, 0);

        // Pad remaining space with zeros
        for (int i = seedBytes.length + 1; i < MAX_SEED_ID_LENGTH; i++) {
            rom.writeU8(pcAddr + i, 0);
        }
    }

    /**
     * Patches the timestamp into ROM.
     * Timestamp is stored as binary values for compactness.
     *
     * @param timestamp timestamp to store
     * @throws IllegalArgumentException if timestamp is null
     */
    public void patchTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }

        int pcAddr = Rom.snes2pc(SEED_DATA_ADDR) + MAX_SEED_ID_LENGTH;

        // Write year (2 bytes, big-endian)
        rom.writeU16(pcAddr, timestamp.getYear());

        // Write month
        rom.writeU8(pcAddr + 2, timestamp.getMonthValue());

        // Write day
        rom.writeU8(pcAddr + 3, timestamp.getDayOfMonth());

        // Write hour
        rom.writeU8(pcAddr + 4, timestamp.getHour());

        // Write minute
        rom.writeU8(pcAddr + 5, timestamp.getMinute());

        // Write second
        rom.writeU8(pcAddr + 6, timestamp.getSecond());
    }

    /**
     * Patches the algorithm name into ROM.
     * Algorithm is stored as a null-terminated string.
     *
     * @param algorithm algorithm name (max 32 characters)
     * @throws IllegalArgumentException if algorithm is null or too long
     */
    public void patchAlgorithm(String algorithm) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }

        if (algorithm.length() > MAX_ALGORITHM_LENGTH) {
            throw new IllegalArgumentException("Algorithm name too long: max " +
                MAX_ALGORITHM_LENGTH + " characters, got " + algorithm.length());
        }

        int pcAddr = Rom.snes2pc(SEED_DATA_ADDR) + MAX_SEED_ID_LENGTH + TIMESTAMP_SIZE;
        byte[] algoBytes = algorithm.getBytes(StandardCharsets.UTF_8);

        // Write algorithm name
        for (int i = 0; i < algoBytes.length; i++) {
            rom.writeU8(pcAddr + i, algoBytes[i] & 0xFF);
        }

        // Null-terminate
        rom.writeU8(pcAddr + algoBytes.length, 0);

        // Pad remaining space with zeros
        for (int i = algoBytes.length + 1; i < MAX_ALGORITHM_LENGTH; i++) {
            rom.writeU8(pcAddr + i, 0);
        }
    }

    /**
     * Patches all seed metadata at once.
     *
     * @param seedId seed identifier
     * @param timestamp timestamp
     * @param algorithm algorithm name
     */
    public void patchAllMetadata(String seedId, LocalDateTime timestamp, String algorithm) {
        patchSeedId(seedId);
        patchTimestamp(timestamp);
        patchAlgorithm(algorithm);
    }

    /**
     * Gets the total size of seed data in bytes.
     *
     * @return total size
     */
    public static int getSeedDataSize() {
        return MAX_SEED_ID_LENGTH + TIMESTAMP_SIZE + MAX_ALGORITHM_LENGTH;
    }

    /**
     * Gets the ROM being patched.
     *
     * @return ROM
     */
    public Rom getRom() {
        return rom;
    }

    /**
     * Reads the seed ID from ROM.
     *
     * @return seed ID, or empty string if not found
     */
    public String readSeedId() {
        int pcAddr = Rom.snes2pc(SEED_DATA_ADDR);

        // Read until null terminator or max length
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_SEED_ID_LENGTH; i++) {
            int b = rom.readU8(pcAddr + i);
            if (b == 0) break;
            sb.append((char) b);
        }

        return sb.toString();
    }

    /**
     * Reads the timestamp from ROM.
     *
     * @return timestamp, or null if not found
     */
    public LocalDateTime readTimestamp() {
        int pcAddr = Rom.snes2pc(SEED_DATA_ADDR) + MAX_SEED_ID_LENGTH;

        try {
            int year = rom.readU16(pcAddr);
            int month = rom.readU8(pcAddr + 2);
            int day = rom.readU8(pcAddr + 3);
            int hour = rom.readU8(pcAddr + 4);
            int minute = rom.readU8(pcAddr + 5);
            int second = rom.readU8(pcAddr + 6);

            if (year == 0) {
                return null;  // No timestamp written
            }

            return LocalDateTime.of(year, month, day, hour, minute, second);
        } catch (Exception e) {
            return null;  // Invalid timestamp
        }
    }

    /**
     * Reads the algorithm name from ROM.
     *
     * @return algorithm name, or empty string if not found
     */
    public String readAlgorithm() {
        int pcAddr = Rom.snes2pc(SEED_DATA_ADDR) + MAX_SEED_ID_LENGTH + TIMESTAMP_SIZE;

        // Read until null terminator or max length
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_ALGORITHM_LENGTH; i++) {
            int b = rom.readU8(pcAddr + i);
            if (b == 0) break;
            sb.append((char) b);
        }

        return sb.toString();
    }
}
