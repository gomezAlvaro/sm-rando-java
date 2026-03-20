package com.maprando.demo;

import com.maprando.data.DataLoader;
import com.maprando.patch.Rom;
import com.maprando.patch.RomGenerator;
import com.maprando.patch.RomValidator;
import com.maprando.randomize.ItemPool;
import com.maprando.randomize.RandomizationResult;
import com.maprando.randomize.BasicRandomizer;
import com.maprando.randomize.Location;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Demo program to test ROM patching with the real ROM file.
 * Generates a randomized seed and creates a patched .smc ROM file.
 */
public class RomPatcherDemo {

    public static void main(String[] args) {
        System.out.println("=== Super Metroid ROM Patcher Demo ===\n");

        try {
            // Load game data
            System.out.println("Loading game data...");
            DataLoader dataLoader = new DataLoader();
            dataLoader.loadAllData();
            System.out.println("✓ Loaded " + dataLoader.getLocationData().getLocations().size() + " locations");
            System.out.println("✓ Loaded " + dataLoader.getItemData().getItems().size() + " items\n");

            // Validate base ROM
            Path baseRomPath = Paths.get("Super Metroid (JU) [!].smc");
            System.out.println("Validating base ROM: " + baseRomPath);

            if (!Files.exists(baseRomPath)) {
                System.err.println("✗ Base ROM not found: " + baseRomPath);
                System.err.println("Please place a vanilla Super Metroid ROM at this location.");
                return;
            }

            byte[] romData = java.nio.file.Files.readAllBytes(baseRomPath);
            boolean isValid = RomValidator.isValidSuperMetroidRom(romData);
            System.out.println("✓ ROM is valid: " + isValid);
            System.out.println("✓ ROM size: " + romData.length + " bytes\n");

            // Create randomizer and generate seed
            System.out.println("Generating random seed...");
            BasicRandomizer randomizer = new BasicRandomizer("demo-seed-" + System.currentTimeMillis());

            // Create item pool
            ItemPool itemPool = ItemPool.createStandardPool();
            randomizer.setItemPool(itemPool);

            // Create locations from data
            List<Location> locations = createLocationsFromData(dataLoader);
            for (Location location : locations) {
                randomizer.addLocation(location);
            }
            System.out.println("✓ Added " + locations.size() + " locations\n");

            // Generate randomization
            System.out.println("Running randomization...");
            RandomizationResult result = randomizer.randomize();

            System.out.println("✓ Seed generated: " + result.getSeed());
            System.out.println("✓ Algorithm: " + result.getAlgorithmUsed());
            System.out.println("✓ Items placed: " + result.getPlacementCount());
            System.out.println("✓ Successful: " + result.isSuccessful());

            if (!result.getWarnings().isEmpty()) {
                System.out.println("✓ Warnings: " + result.getWarnings());
            }
            System.out.println();

            // Create ROM generator
            System.out.println("Creating ROM generator...");
            RomGenerator romGenerator = new RomGenerator(baseRomPath, dataLoader);
            System.out.println("✓ ROM generator initialized\n");

            // Patch ROM
            System.out.println("Patching ROM...");
            Rom patchedRom = romGenerator.generate(result);
            System.out.println("✓ ROM patched successfully\n");

            // Save patched ROM
            Path outputPath = Paths.get("patched-" + result.getSeed() + ".smc");
            System.out.println("Saving patched ROM to: " + outputPath);
            patchedRom.save(outputPath);
            System.out.println("✓ ROM saved successfully\n");

            // Display some placements
            System.out.println("=== Sample Item Placements ===");
            int count = 0;
            for (var entry : result.getPlacements().entrySet()) {
                if (count >= 5) break;
                String locationId = entry.getKey();
                String itemId = entry.getValue();
                String locationName = result.getLocationName(locationId);
                System.out.println("  " + locationName + " → " + itemId);
                count++;
            }
            if (result.getPlacementCount() > 5) {
                System.out.println("  ... and " + (result.getPlacementCount() - 5) + " more");
            }
            System.out.println();

            // Verify seed metadata was written
            System.out.println("=== Seed Metadata Verification ===");
            int seedDataAddr = com.maprando.patch.Rom.snes2pc(0x82FF00);
            StringBuilder seedIdBuilder = new StringBuilder();
            for (int i = 0; i < 32; i++) {
                int b = patchedRom.readU8(seedDataAddr + i);
                if (b == 0) break;
                seedIdBuilder.append((char) b);
            }
            String writtenSeedId = seedIdBuilder.toString();
            System.out.println("✓ Seed ID in ROM: " + writtenSeedId);

            // Read timestamp
            int yearAddr = seedDataAddr + 32;
            int year = patchedRom.readU16(yearAddr);
            System.out.println("✓ Timestamp in ROM: year=" + year);

            System.out.println("\n=== ROM Patching Complete! ===");
            System.out.println("Output file: " + outputPath.toAbsolutePath());
            System.out.println("\nYou can now play this ROM in an emulator (snes9x, bsnes, etc.)");
            System.out.println("\nNOTE: This is a proof-of-concept implementation.");
            System.out.println("ROM addresses are placeholders for testing.");
            System.out.println("Item placements may not work correctly until real");
            System.out.println("ROM addresses from disassembly are implemented.");

        } catch (IOException e) {
            System.err.println("✗ Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("✗ Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates Location objects from loaded data.
     */
    private static List<Location> createLocationsFromData(DataLoader dataLoader) {
        List<Location> locations = new ArrayList<>();
        var locationData = dataLoader.getLocationData();

        for (var locDef : locationData.getLocations()) {
            // Create location with id, name, region
            Location location = new Location(
                locDef.getId(),
                locDef.getName(),
                locDef.getRegion()
            );
            locations.add(location);
        }

        return locations;
    }
}
