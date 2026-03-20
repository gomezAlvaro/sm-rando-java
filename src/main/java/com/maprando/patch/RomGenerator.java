package com.maprando.patch;

import com.maprando.data.DataLoader;
import com.maprando.randomize.RandomizationResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Generates complete patched ROM files from randomization results.
 * Orchestrates the ROM patching process by combining:
 * - Base ROM loading
 * - Item placement patching
 * - Seed metadata injection
 *
 * Typical usage:
 * <pre>
 * RomGenerator generator = new RomGenerator(baseRomPath, dataLoader);
 * Rom patchedRom = generator.generate(randomizationResult);
 * patchedRom.save(outputPath);
 * </pre>
 *
 * NOTE: This is a proof-of-concept implementation.
 * ROM addresses and item byte values are placeholders for testing.
 * Production deployment requires real ROM disassembly research.
 */
public class RomGenerator {

    private final Path baseRomPath;
    private final DataLoader dataLoader;

    /**
     * Creates a new RomGenerator.
     *
     * @param baseRomPath path to base Super Metroid ROM
     * @param dataLoader data loader for location/item mappings
     * @throws IOException if base ROM cannot be loaded
     * @throws IllegalArgumentException if base ROM path is invalid
     */
    public RomGenerator(Path baseRomPath, DataLoader dataLoader) throws IOException {
        if (baseRomPath == null) {
            throw new IllegalArgumentException("Base ROM path cannot be null");
        }
        if (!Files.exists(baseRomPath)) {
            throw new IllegalArgumentException("Base ROM does not exist: " + baseRomPath);
        }
        if (dataLoader == null) {
            throw new IllegalArgumentException("DataLoader cannot be null");
        }

        this.baseRomPath = baseRomPath;
        this.dataLoader = dataLoader;

        // Validate base ROM
        byte[] baseRomData = Files.readAllBytes(baseRomPath);
        if (!RomValidator.isValidSuperMetroidRom(baseRomData)) {
            throw new IllegalArgumentException("Invalid Super Metroid ROM: " + baseRomPath);
        }
    }

    /**
     * Generates a complete patched ROM from a randomization result.
     *
     * Process:
     * 1. Load base ROM
     * 2. Clone ROM to avoid modifying original
     * 3. Patch all item placements
     * 4. Inject seed metadata
     * 5. Return patched ROM
     *
     * @param result randomization result with placements
     * @return patched ROM ready for saving
     * @throws IOException if ROM cannot be loaded
     * @throws IllegalArgumentException if result is null
     */
    public Rom generate(RandomizationResult result) throws IOException {
        if (result == null) {
            throw new IllegalArgumentException("RandomizationResult cannot be null");
        }

        // Load base ROM
        Rom baseRom = Rom.load(baseRomPath);

        // Clone ROM to avoid modifying original
        byte[] romData = new byte[baseRom.data.length];
        System.arraycopy(baseRom.data, 0, romData, 0, baseRom.data.length);
        Rom patchedRom = new Rom(romData);

        // Patch item placements
        ItemPatcher itemPatcher = new ItemPatcher(patchedRom, dataLoader);
        itemPatcher.patchAllPlacements(result);

        // Patch seed metadata
        SeedPatcher seedPatcher = new SeedPatcher(patchedRom);
        seedPatcher.patchAllMetadata(
            result.getSeed(),
            result.getTimestamp(),
            result.getAlgorithmUsed()
        );

        return patchedRom;
    }

    /**
     * Generates a patched ROM and saves it to a file.
     *
     * @param result randomization result with placements
     * @param outputPath path where ROM should be saved
     * @return path to saved ROM file
     * @throws IOException if ROM cannot be generated or saved
     */
    public Path generateAndSave(RandomizationResult result, Path outputPath) throws IOException {
        if (outputPath == null) {
            throw new IllegalArgumentException("Output path cannot be null");
        }

        // Create parent directories if needed
        Path parentDir = outputPath.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // Generate patched ROM
        Rom patchedRom = generate(result);

        // Save to file
        patchedRom.save(outputPath);

        return outputPath;
    }

    /**
     * Gets the base ROM path.
     *
     * @return path to base ROM
     */
    public Path getBaseRomPath() {
        return baseRomPath;
    }

    /**
     * Gets the data loader.
     *
     * @return data loader
     */
    public DataLoader getDataLoader() {
        return dataLoader;
    }

    /**
     * Validates that a randomization result can be patched.
     * Checks that all locations have ROM addresses and all items have mappings.
     *
     * @param result randomization result to validate
     * @return true if result can be patched, false otherwise
     */
    public boolean canPatch(RandomizationResult result) {
        if (result == null || result.getPlacements() == null) {
            return false;
        }

        LocationRomAddressMapper addressMapper = new LocationRomAddressMapper(dataLoader);
        ItemPatcher itemPatcher = new ItemPatcher(new Rom(new byte[3145728]), dataLoader);

        for (Map.Entry<String, String> entry : result.getPlacements().entrySet()) {
            String locationId = entry.getKey();
            String itemId = entry.getValue();

            // Check location has ROM address
            if (!addressMapper.hasRomAddress(locationId)) {
                return false;
            }

            // Check item has mapping
            if (!itemPatcher.hasItemMapping(itemId)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets statistics about a randomization result for ROM generation.
     *
     * @param result randomization result
     * @return statistics string
     */
    public String getGenerationStats(RandomizationResult result) {
        if (result == null) {
            return "No result provided";
        }

        int totalPlacements = result.getPlacementCount();
        int mappedLocations = 0;
        int mappedItems = 0;

        LocationRomAddressMapper addressMapper = new LocationRomAddressMapper(dataLoader);
        ItemPatcher itemPatcher = new ItemPatcher(new Rom(new byte[3145728]), dataLoader);

        for (Map.Entry<String, String> entry : result.getPlacements().entrySet()) {
            String locationId = entry.getKey();
            String itemId = entry.getValue();

            if (addressMapper.hasRomAddress(locationId)) {
                mappedLocations++;
            }
            if (itemPatcher.hasItemMapping(itemId)) {
                mappedItems++;
            }
        }

        return String.format("Placements: %d, Mappable locations: %d, Mappable items: %d",
            totalPlacements, mappedLocations, mappedItems);
    }
}
