package com.maprando.patch;

import java.nio.charset.StandardCharsets;

/**
 * Patches seed metadata into ROM using Rust MapRandomizer format.
 * Compatible with the Rust project's seed storage system.
 *
 * Rust seed storage locations:
 * - 0x7FC0: ROM header "SUPERMETROID MAPRANDO" (21 bytes, null-terminated)
 * - 0xDFFEF0: Seed name (16 bytes, null-terminated, URL-safe ASCII)
 * - 0xDFFF00: Display seed (4 bytes, u32 little-endian)
 *
 * This format enables cross-platform seed compatibility between Java and Rust randomizers.
 *
 * Reference: rust/maprando/src/patch.rs: apply_seed_identifiers()
 */
public class RustSeedPatcher {

    // ROM address constants (matching Rust project)
    public static final int ROM_HEADER_ADDR = 0x7FC0;
    public static final int SEED_NAME_ADDR = 0xDFFEF0;
    public static final int DISPLAY_SEED_ADDR = 0xDFFF00;

    // Size limits
    public static final int MAX_ROM_HEADER_LENGTH = 21;
    public static final int MAX_SEED_NAME_LENGTH = 16;
    public static final int DISPLAY_SEED_SIZE = 4; // 4 bytes for u32

    // ROM header string (matches Rust)
    private static final String ROM_HEADER_STRING = "SUPERMETROID MAPRANDO";

    private final Rom rom;

    /**
     * Creates a new RustSeedPatcher.
     *
     * @param rom ROM to patch
     * @throws IllegalArgumentException if rom is null
     */
    public RustSeedPatcher(Rom rom) {
        if (rom == null) {
            throw new IllegalArgumentException("ROM cannot be null");
        }
        this.rom = rom;
    }

    /**
     * Patches the ROM header with Rust format.
     * Writes "SUPERMETROID MAPRANDO" to 0x7FC0.
     */
    public void patchRomHeader() {
        int pcAddr = Rom.snes2pc(ROM_HEADER_ADDR);
        byte[] headerBytes = ROM_HEADER_STRING.getBytes(StandardCharsets.UTF_8);

        // Write header string
        for (int i = 0; i < headerBytes.length; i++) {
            rom.writeU8(pcAddr + i, headerBytes[i] & 0xFF);
        }

        // Null-terminate
        rom.writeU8(pcAddr + headerBytes.length, 0);
    }

    /**
     * Patches the seed name to 0xDFFEF0.
     * Seed name must be URL-safe ASCII and max 16 characters.
     *
     * @param seedName seed identifier (max 16 characters, URL-safe ASCII)
     * @throws IllegalArgumentException if seedName is null, too long, or contains invalid characters
     */
    public void patchSeedName(String seedName) {
        if (seedName == null) {
            throw new IllegalArgumentException("Seed name cannot be null");
        }

        if (seedName.length() > MAX_SEED_NAME_LENGTH) {
            throw new IllegalArgumentException(
                "Seed name too long: max " + MAX_SEED_NAME_LENGTH +
                " characters, got " + seedName.length());
        }

        // Validate URL-safe ASCII characters only
        if (!isUrlSafe(seedName)) {
            throw new IllegalArgumentException(
                "Seed name must contain only URL-safe ASCII characters (a-z, A-Z, 0-9, -, _)");
        }

        int pcAddr = Rom.snes2pc(SEED_NAME_ADDR);
        byte[] nameBytes = seedName.getBytes(StandardCharsets.UTF_8);

        // Write seed name
        for (int i = 0; i < nameBytes.length; i++) {
            rom.writeU8(pcAddr + i, nameBytes[i] & 0xFF);
        }

        // Null-terminate
        rom.writeU8(pcAddr + nameBytes.length, 0);

        // Zero-pad remaining space
        for (int i = nameBytes.length + 1; i < MAX_SEED_NAME_LENGTH; i++) {
            rom.writeU8(pcAddr + i, 0);
        }
    }

    /**
     * Patches the display seed to 0xDFFF00.
     * Display seed is stored as a 4-byte little-endian u32.
     *
     * @param displaySeed display seed value (0 to 0xFFFFFFFF)
     * @throws IllegalArgumentException if displaySeed is out of u32 range
     */
    public void patchDisplaySeed(long displaySeed) {
        if (displaySeed < 0 || displaySeed > 0xFFFFFFFFL) {
            throw new IllegalArgumentException(
                "Display seed out of u32 range: must be 0 to 0xFFFFFFFF, got " + displaySeed);
        }

        int pcAddr = Rom.snes2pc(DISPLAY_SEED_ADDR);

        // Write as little-endian u32 (least significant byte first)
        rom.writeU8(pcAddr, (int) (displaySeed & 0xFF));
        rom.writeU8(pcAddr + 1, (int) ((displaySeed >> 8) & 0xFF));
        rom.writeU8(pcAddr + 2, (int) ((displaySeed >> 16) & 0xFF));
        rom.writeU8(pcAddr + 3, (int) ((displaySeed >> 24) & 0xFF));
    }

    /**
     * Patches all seed metadata at once.
     * This includes ROM header, seed name, and display seed.
     *
     * @param seedName seed identifier (max 16 characters)
     * @param displaySeed display seed value (u32)
     */
    public void patchAllMetadata(String seedName, long displaySeed) {
        patchRomHeader();
        patchSeedName(seedName);
        patchDisplaySeed(displaySeed);
    }

    /**
     * Reads the seed name from ROM.
     *
     * @return seed name, or empty string if not found
     */
    public String readSeedName() {
        int pcAddr = Rom.snes2pc(SEED_NAME_ADDR);

        // Read until null terminator or max length
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_SEED_NAME_LENGTH; i++) {
            int b = rom.readU8(pcAddr + i);
            if (b == 0) break;
            sb.append((char) b);
        }

        return sb.toString();
    }

    /**
     * Reads the display seed from ROM.
     *
     * @return display seed value, or 0 if not found
     */
    public long readDisplaySeed() {
        int pcAddr = Rom.snes2pc(DISPLAY_SEED_ADDR);

        // Read as little-endian u32
        int byte0 = rom.readU8(pcAddr);
        int byte1 = rom.readU8(pcAddr + 1);
        int byte2 = rom.readU8(pcAddr + 2);
        int byte3 = rom.readU8(pcAddr + 3);

        return ((long) byte3 << 24) | ((long) byte2 << 16) | ((long) byte1 << 8) | byte0;
    }

    /**
     * Reads the ROM header from ROM.
     *
     * @return ROM header string, or empty string if not found
     */
    public String readRomHeader() {
        int pcAddr = Rom.snes2pc(ROM_HEADER_ADDR);

        // Read until null terminator or max length
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MAX_ROM_HEADER_LENGTH; i++) {
            int b = rom.readU8(pcAddr + i);
            if (b == 0) break;
            sb.append((char) b);
        }

        return sb.toString();
    }

    /**
     * Validates if a string is URL-safe ASCII.
     * URL-safe characters: a-z, A-Z, 0-9, hyphen, underscore
     *
     * @param s string to validate
     * @return true if URL-safe, false otherwise
     */
    private static boolean isUrlSafe(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }

        for (char c : s.toCharArray()) {
            boolean isValid =
                (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                (c >= '0' && c <= '9') ||
                c == '-' || c == '_';

            if (!isValid) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the total size of seed data in bytes.
     * Does not include ROM header (which is separate).
     *
     * @return total size (seed name + display seed)
     */
    public static int getSeedDataSize() {
        return MAX_SEED_NAME_LENGTH + DISPLAY_SEED_SIZE;
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
     * Checks if the ROM has been patched with Rust seed metadata.
     *
     * @return true if ROM header contains "SUPERMETROID MAPRANDO", false otherwise
     */
    public boolean isRustFormatted() {
        String header = readRomHeader();
        return ROM_HEADER_STRING.equals(header);
    }

    /**
     * Clears all seed metadata from ROM.
     * Resets seed name to empty and display seed to 0.
     * ROM header is preserved.
     */
    public void clearMetadata() {
        // Clear seed name
        int nameAddr = Rom.snes2pc(SEED_NAME_ADDR);
        for (int i = 0; i < MAX_SEED_NAME_LENGTH; i++) {
            rom.writeU8(nameAddr + i, 0);
        }

        // Clear display seed
        int seedAddr = Rom.snes2pc(DISPLAY_SEED_ADDR);
        for (int i = 0; i < DISPLAY_SEED_SIZE; i++) {
            rom.writeU8(seedAddr + i, 0);
        }
    }

    /**
     * Gets the ROM header address constant.
     *
     * @return ROM header SNES address
     */
    public static int getRomHeaderAddr() {
        return ROM_HEADER_ADDR;
    }

    /**
     * Gets the seed name address constant.
     *
     * @return Seed name SNES address
     */
    public static int getSeedNameAddr() {
        return SEED_NAME_ADDR;
    }

    /**
     * Gets the display seed address constant.
     *
     * @return Display seed SNES address
     */
    public static int getDisplaySeedAddr() {
        return DISPLAY_SEED_ADDR;
    }
}
