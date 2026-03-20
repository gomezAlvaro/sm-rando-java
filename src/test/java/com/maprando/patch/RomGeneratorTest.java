package com.maprando.patch;

import com.maprando.data.DataLoader;
import com.maprando.randomize.RandomizationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for RomGenerator.
 * Tests complete ROM generation from base ROM + placements.
 */
class RomGeneratorTest {

    @TempDir
    Path tempDir;

    private DataLoader dataLoader;
    private Path baseRomPath;
    private RomGenerator generator;

    @BeforeEach
    void setUp() throws IOException {
        dataLoader = new DataLoader();
        dataLoader.loadAllData();

        // Create a mock base ROM (3MB)
        baseRomPath = tempDir.resolve("base.smc");
        byte[] baseRomData = new byte[3145728];

        // Add "SUPER METROID" header for validation
        byte[] header = "SUPER METROID".getBytes();
        System.arraycopy(header, 0, baseRomData, 0x7FC0, header.length);

        Files.write(baseRomPath, baseRomData);

        generator = new RomGenerator(baseRomPath, dataLoader);
    }

    @Test
    void testGenerate_SimpleSeed() throws IOException {
        // Create a simple seed
        Map<String, String> placements = new HashMap<>();
        placements.put("brinstar_morph_ball_room", "MORPH_BALL");
        placements.put("brinstar_charge_beam_room", "CHARGE_BEAM");

        RandomizationResult result = RandomizationResult.builder()
            .seed("test123")
            .placements(placements)
            .timestamp(LocalDateTime.of(2026, 3, 19, 10, 0, 0))
            .algorithmUsed("basic")
            .build();

        // Generate ROM
        Rom patchedRom = generator.generate(result);

        assertNotNull(patchedRom);
        assertEquals(3145728, patchedRom.data.length);

        // Verify items were patched
        int addr1 = Rom.snes2pc(0x8282F5);
        int addr2 = Rom.snes2pc(0x8282F6);

        assertTrue(patchedRom.readU8(addr1) != 0);
        assertTrue(patchedRom.readU8(addr2) != 0);

        // Verify seed metadata was patched
        int seedDataAddr = Rom.snes2pc(0x82FF00);
        byte[] seedBytes = new byte[7];
        for (int i = 0; i < 7; i++) {
            seedBytes[i] = (byte) patchedRom.readU8(seedDataAddr + i);
        }
        String seedId = new String(seedBytes).trim();
        assertTrue(seedId.contains("test"));
    }

    @Test
    void testGenerateAndSave() throws IOException {
        Map<String, String> placements = new HashMap<>();
        placements.put("brinstar_morph_ball_room", "BOMB");

        RandomizationResult result = RandomizationResult.builder()
            .seed("savetest")
            .placements(placements)
            .timestamp(LocalDateTime.now())
            .algorithmUsed("foresight")
            .build();

        Path outputPath = tempDir.resolve("patched.smc");
        Path savedPath = generator.generateAndSave(result, outputPath);

        assertEquals(outputPath, savedPath);
        assertTrue(Files.exists(outputPath));

        // Verify saved ROM
        byte[] savedData = Files.readAllBytes(outputPath);
        assertEquals(3145728, savedData.length);
    }

    @Test
    void testGenerate_WithSeedMetadata() throws IOException {
        String seedId = "METATEST";
        LocalDateTime timestamp = LocalDateTime.of(2026, 3, 19, 15, 30, 45);
        String algorithm = "balanced";

        Map<String, String> placements = new HashMap<>();
        placements.put("brinstar_bomb_room", "ICE_BEAM");

        RandomizationResult result = RandomizationResult.builder()
            .seed(seedId)
            .placements(placements)
            .timestamp(timestamp)
            .algorithmUsed(algorithm)
            .build();

        Rom patchedRom = generator.generate(result);

        // Verify seed metadata
        SeedPatcher seedPatcher = new SeedPatcher(patchedRom);

        assertEquals(seedId, seedPatcher.readSeedId());
        assertEquals(timestamp, seedPatcher.readTimestamp());
        assertEquals(algorithm, seedPatcher.readAlgorithm());
    }

    @Test
    void testGenerate_EmptyPlacements() throws IOException {
        Map<String, String> placements = new HashMap<>();

        RandomizationResult result = RandomizationResult.builder()
            .seed("empty")
            .placements(placements)
            .timestamp(LocalDateTime.now())
            .algorithmUsed("basic")
            .build();

        Rom patchedRom = generator.generate(result);

        assertNotNull(patchedRom);
        assertEquals(3145728, patchedRom.data.length);
    }

    @Test
    void testGenerate_NullResult() {
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(null);
        });
    }

    @Test
    void testGenerate_InvalidBaseRomPath() {
        Path invalidPath = tempDir.resolve("nonexistent.smc");

        assertThrows(IllegalArgumentException.class, () -> {
            new RomGenerator(invalidPath, dataLoader);
        });
    }

    @Test
    void testGenerateAndSave_CreateParentDirectories() throws IOException {
        Map<String, String> placements = new HashMap<>();
        placements.put("brinstar_morph_ball_room", "MORPH_BALL");

        RandomizationResult result = RandomizationResult.builder()
            .seed("mkdirtest")
            .placements(placements)
            .timestamp(LocalDateTime.now())
            .algorithmUsed("basic")
            .build();

        Path outputPath = tempDir.resolve("subdir/subdir2/patched.smc");
        Path savedPath = generator.generateAndSave(result, outputPath);

        assertTrue(Files.exists(outputPath));
        assertTrue(Files.exists(savedPath));
    }

    @Test
    void testGenerate_PreservesBaseRomData() throws IOException {
        // Write some data to base ROM
        byte[] baseRomData = Files.readAllBytes(baseRomPath);
        baseRomData[0x100] = (byte) 0x42;
        baseRomData[0x200] = (byte) 0x99;
        Files.write(baseRomPath, baseRomData);

        Map<String, String> placements = new HashMap<>();
        placements.put("brinstar_morph_ball_room", "BOMB");

        RandomizationResult result = RandomizationResult.builder()
            .seed("preservetest")
            .placements(placements)
            .timestamp(LocalDateTime.now())
            .algorithmUsed("basic")
            .build();

        Rom patchedRom = generator.generate(result);

        // Verify base ROM data is preserved
        assertEquals(0x42, patchedRom.readU8(0x100));
        assertEquals(0x99, patchedRom.readU8(0x200));
    }

    @Test
    void testGetBaseRomPath() {
        assertEquals(baseRomPath, generator.getBaseRomPath());
    }

    @Test
    void testGetDataLoader() {
        assertEquals(dataLoader, generator.getDataLoader());
    }

    @Test
    void testGenerate_MultiplePlacements() throws IOException {
        Map<String, String> placements = new HashMap<>();
        placements.put("brinstar_morph_ball_room", "MORPH_BALL");
        placements.put("brinstar_charge_beam_room", "CHARGE_BEAM");
        placements.put("brinstar_bomb_room", "BOMB");
        placements.put("brinstar_xray_room", "ICE_BEAM");
        placements.put("norfair_ice_beam_room", "WAVE_BEAM");

        RandomizationResult result = RandomizationResult.builder()
            .seed("multi")
            .placements(placements)
            .timestamp(LocalDateTime.now())
            .algorithmUsed("foresight")
            .build();

        Rom patchedRom = generator.generate(result);

        // Verify all placements
        int addr1 = Rom.snes2pc(0x8282F5);
        int addr2 = Rom.snes2pc(0x8282F6);
        int addr3 = Rom.snes2pc(0x8282F7);
        int addr4 = Rom.snes2pc(0x8282F8);
        int addr5 = Rom.snes2pc(0x8282F9);

        assertTrue(patchedRom.readU8(addr1) != 0);
        assertTrue(patchedRom.readU8(addr2) != 0);
        assertTrue(patchedRom.readU8(addr3) != 0);
        assertTrue(patchedRom.readU8(addr4) != 0);
        assertTrue(patchedRom.readU8(addr5) != 0);
    }
}
