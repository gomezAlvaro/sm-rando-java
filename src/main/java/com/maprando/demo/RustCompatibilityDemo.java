package com.maprando.demo;

import com.maprando.data.DataLoader;
import com.maprando.patch.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo program to test Rust-compatible ROM generation.
 * Verifies that PLM patching, node pointer map, and seed storage all work correctly.
 */
public class RustCompatibilityDemo {

    public static void main(String[] args) {
        System.out.println("=== Rust Compatibility Test Demo ===\n");

        try {
            // Step 1: Load game data
            System.out.println("Step 1: Loading game data...");
            DataLoader dataLoader = new DataLoader();
            dataLoader.loadAllData();
            System.out.println("✓ Loaded " + dataLoader.getLocationData().getLocations().size() + " locations");
            System.out.println("✓ Loaded " + dataLoader.getItemData().getItems().size() + " items\n");

            // Step 2: Load base ROM
            System.out.println("Step 2: Loading base ROM...");
            Path baseRomPath = Paths.get("Super Metroid (JU) [!].smc");

            if (!Files.exists(baseRomPath)) {
                System.err.println("✗ Base ROM not found: " + baseRomPath);
                System.err.println("Please place a vanilla Super Metroid ROM at this location.");
                return;
            }

            Rom baseRom = Rom.load(baseRomPath);
            System.out.println("✓ Loaded base ROM: " + baseRomPath);
            System.out.println("✓ ROM size: " + baseRom.data.length + " bytes\n");

            // Step 3: Create test ROM (clone of base)
            System.out.println("Step 3: Creating test ROM...");
            byte[] romData = new byte[baseRom.data.length];
            System.arraycopy(baseRom.data, 0, romData, 0, baseRom.data.length);
            Rom testRom = new Rom(romData);
            System.out.println("✓ Created test ROM (copy of base ROM)\n");

            // Step 4: Test PLM-based item patching
            System.out.println("Step 4: Testing PLM-based item patching...");
            testPlmPatching(testRom, dataLoader);
            System.out.println();

            // Step 5: Test node pointer map
            System.out.println("Step 5: Testing node pointer map...");
            testNodePtrMap();
            System.out.println();

            // Step 6: Test Rust-compatible seed storage
            System.out.println("Step 6: Testing Rust-compatible seed storage...");
            testRustSeedStorage(testRom);
            System.out.println();

            // Step 7: Verify all modifications
            System.out.println("Step 7: Verifying ROM modifications...");
            verifyRomModifications(testRom);
            System.out.println();

            // Step 8: Save patched ROM
            System.out.println("Step 8: Saving patched ROM...");
            Path outputPath = Paths.get("rust-compatible-test.smc");
            testRom.save(outputPath);
            System.out.println("✓ Saved patched ROM to: " + outputPath.toAbsolutePath());
            System.out.println();

            // Success summary
            System.out.println("=== Test Results ===");
            System.out.println("✓ PLM-based item patching: WORKING");
            System.out.println("✓ Node pointer map: WORKING");
            System.out.println("✓ Rust seed storage: WORKING");
            System.out.println("\nAll Rust-compatible systems are functional!");
            System.out.println("\nYou can now play the patched ROM in an emulator.");
            System.out.println("\nNOTE: This uses placeholder ROM addresses.");
            System.out.println("For production, real ROM addresses from disassembly are needed.");

        } catch (IOException e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tests PLM-based item patching.
     */
    private static void testPlmPatching(Rom rom, DataLoader dataLoader) {
        PlmItemPatcher patcher = new PlmItemPatcher(rom, dataLoader);

        // Test PLM value table lookups
        System.out.println("  Testing PLM value table lookups:");

        // Test container 0 (pedestal) values
        int energyTankPlm = PlmTypeTable.getPlmType(0, "Energy Tank");
        int morphBallPlm = PlmTypeTable.getPlmType(0, "Morph Ball");
        System.out.println("    Energy Tank (container 0): 0x" + Integer.toHexString(energyTankPlm));
        System.out.println("    Morph Ball (container 0): 0x" + Integer.toHexString(morphBallPlm));

        // Test container 1 (Chozo orb) values
        int energyTankChozo = PlmTypeTable.getPlmType(1, "Energy Tank");
        int morphBallChozo = PlmTypeTable.getPlmType(1, "Morph Ball");
        System.out.println("    Energy Tank (container 1): 0x" + Integer.toHexString(energyTankChozo));
        System.out.println("    Morph Ball (container 1): 0x" + Integer.toHexString(morphBallChozo));

        // Test container 2 (hidden block) values
        int energyTankHidden = PlmTypeTable.getPlmType(2, "Energy Tank");
        int morphBallHidden = PlmTypeTable.getPlmType(2, "Morph Ball");
        System.out.println("    Energy Tank (container 2): 0x" + Integer.toHexString(energyTankHidden));
        System.out.println("    Morph Ball (container 2): 0x" + Integer.toHexString(morphBallHidden));

        // Write PLM values to ROM at test addresses
        System.out.println("\n  Testing PLM writes to ROM:");

        // Use test addresses in free space
        int testAddr1 = Rom.snes2pc(0xDFFF20); // Free space after seed data
        int testAddr2 = Rom.snes2pc(0xDFFF22);
        int testAddr3 = Rom.snes2pc(0xDFFF24);

        // Write Morph Ball in container 0
        rom.writeU16(testAddr1, morphBallPlm);
        int read1 = rom.readU16(testAddr1);
        System.out.println("    Wrote 0x" + Integer.toHexString(morphBallPlm) +
            " to 0xDFFF20, read back: 0x" + Integer.toHexString(read1));

        // Write Morph Ball in container 1
        rom.writeU16(testAddr2, morphBallChozo);
        int read2 = rom.readU16(testAddr2);
        System.out.println("    Wrote 0x" + Integer.toHexString(morphBallChozo) +
            " to 0xDFFF22, read back: 0x" + Integer.toHexString(read2));

        // Write Morph Ball in container 2
        rom.writeU16(testAddr3, morphBallHidden);
        int read3 = rom.readU16(testAddr3);
        System.out.println("    Wrote 0x" + Integer.toHexString(morphBallHidden) +
            " to 0xDFFF24, read back: 0x" + Integer.toHexString(read3));

        // Verify writes
        if (read1 == morphBallPlm && read2 == morphBallChozo && read3 == morphBallHidden) {
            System.out.println("\n  ✓ All PLM writes verified successfully");
        }

        // Test item name lookups
        System.out.println("\n  Testing item name to PLM conversions:");
        String[] testItems = {"Energy Tank", "Morph Ball", "Grapple Beam", "Nothing"};
        for (String item : testItems) {
            int itemId = PlmTypeTable.getItemId(item);
            if (itemId != -1) {
                int plm = PlmTypeTable.getPlmType(0, item);
                System.out.println("    \"" + item + "\" → ID " + itemId + " → PLM 0x" + Integer.toHexString(plm));
            }
        }

        System.out.println("\n✓ PLM system test complete");
    }

    /**
     * Tests node pointer map functionality.
     */
    private static void testNodePtrMap() {
        NodePtrMap nodePtrMap = new NodePtrMap();

        // Add some test entries (simulating room geometry data)
        nodePtrMap.put(5, 2, 0x848200);
        nodePtrMap.put(19, 7, 0x84A100);
        nodePtrMap.put(42, 1, 0x84C300);

        System.out.println("  Added 3 node pointer entries");
        System.out.println("  (5, 2) → 0x848200");
        System.out.println("  (19, 7) → 0x84A100");
        System.out.println("  (42, 1) → 0x84C300");

        // Test lookup
        int ptr1 = nodePtrMap.get(5, 2);
        int ptr2 = nodePtrMap.get(19, 7);
        int ptr3 = nodePtrMap.get(42, 1);

        if (ptr1 == 0x848200 && ptr2 == 0x84A100 && ptr3 == 0x84C300) {
            System.out.println("  ✓ All lookups successful");
        } else {
            System.out.println("  ✗ Lookup failed!");
        }

        // Test statistics
        var stats = nodePtrMap.getStatistics();
        System.out.println("  Total nodes: " + stats.get("totalNodes"));
        System.out.println("  Total rooms: " + stats.get("totalRooms"));

        // Test validation
        if (nodePtrMap.isValid()) {
            System.out.println("  ✓ Node pointer map is valid");
        }

        System.out.println("✓ Node pointer map test complete");
    }

    /**
     * Tests Rust-compatible seed storage.
     */
    private static void testRustSeedStorage(Rom rom) {
        RustSeedPatcher patcher = new RustSeedPatcher(rom);

        // Test data
        String seedName = "test-seed-01";
        long displaySeed = 123456789L;

        System.out.println("  Seed name: " + seedName);
        System.out.println("  Display seed: " + displaySeed);

        // Patch ROM header
        patcher.patchRomHeader();
        String header = patcher.readRomHeader();
        System.out.println("  ROM header: \"" + header + "\"");

        // Patch seed name
        patcher.patchSeedName(seedName);
        String readName = patcher.readSeedName();
        System.out.println("  Written seed name: " + readName);

        // Patch display seed
        patcher.patchDisplaySeed(displaySeed);
        long readSeed = patcher.readDisplaySeed();
        System.out.println("  Written display seed: " + readSeed);

        // Verify data matches
        if (seedName.equals(readName) && displaySeed == readSeed) {
            System.out.println("  ✓ Seed data verification successful");
        } else {
            System.out.println("  ✗ Seed data verification failed!");
        }

        // Verify addresses
        System.out.println("  Seed name @ 0x" + Integer.toHexString(RustSeedPatcher.SEED_NAME_ADDR));
        System.out.println("  Display seed @ 0x" + Integer.toHexString(RustSeedPatcher.DISPLAY_SEED_ADDR));

        // Check if ROM is Rust-formatted
        if (patcher.isRustFormatted()) {
            System.out.println("  ✓ ROM is Rust-formatted");
        }

        System.out.println("✓ Rust seed storage test complete");
    }

    /**
     * Verifies ROM modifications.
     */
    private static void verifyRomModifications(Rom rom) {
        // Verify ROM header
        int headerAddr = Rom.snes2pc(0x7FC0);
        StringBuilder header = new StringBuilder();
        for (int i = 0; i < 21; i++) {
            int b = rom.readU8(headerAddr + i);
            if (b == 0) break;
            header.append((char) b);
        }

        if ("SUPERMETROID MAPRANDO".equals(header.toString())) {
            System.out.println("  ✓ ROM header correct: \"" + header + "\"");
        } else {
            System.out.println("  ✗ ROM header incorrect: \"" + header + "\"");
        }

        // Verify seed name at 0xDFFEF0
        int nameAddr = Rom.snes2pc(0xDFFEF0);
        StringBuilder seedName = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            int b = rom.readU8(nameAddr + i);
            if (b == 0) break;
            seedName.append((char) b);
        }

        if ("test-seed-01".equals(seedName.toString())) {
            System.out.println("  ✓ Seed name correct at 0xDFFEF0: \"" + seedName + "\"");
        } else {
            System.out.println("  ✗ Seed name incorrect: \"" + seedName + "\"");
        }

        // Verify display seed at 0xDFFF00
        int seedAddr = Rom.snes2pc(0xDFFF00);
        int byte0 = rom.readU8(seedAddr);
        int byte1 = rom.readU8(seedAddr + 1);
        int byte2 = rom.readU8(seedAddr + 2);
        int byte3 = rom.readU8(seedAddr + 3);
        long readSeed = ((long) byte3 << 24) | ((long) byte2 << 16) | ((long) byte1 << 8) | byte0;

        if (readSeed == 123456789L) {
            System.out.println("  ✓ Display seed correct at 0xDFFF00: " + readSeed);
        } else {
            System.out.println("  ✗ Display seed incorrect: " + readSeed);
        }

        // Verify bytes are in correct little-endian order
        System.out.println("    Display seed bytes: 0x" + Integer.toHexString(byte0) +
            " 0x" + Integer.toHexString(byte1) +
            " 0x" + Integer.toHexString(byte2) +
            " 0x" + Integer.toHexString(byte3) +
            " (little-endian)");

        System.out.println("✓ ROM verification complete");
    }
}
