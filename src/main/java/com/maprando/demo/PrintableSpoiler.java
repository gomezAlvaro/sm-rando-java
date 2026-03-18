package com.maprando.demo;

import com.maprando.randomize.RandomizationResult;

import java.util.Map;
import java.util.TreeMap;

/**
 * Generates a printable spoiler log for randomization results.
 */
public class PrintableSpoiler {

    private final RandomizationResult result;

    public PrintableSpoiler(RandomizationResult result) {
        this.result = result;
    }

    /**
     * Generate a complete spoiler log with header, placements, and summary.
     */
    public String generateSpoilerLog() {
        StringBuilder sb = new StringBuilder();

        // Header
        sb.append("╔═════════════════════════════════════════════════════════════════╗\n");
        sb.append("║           SUPER METROID RANDOMIZER - SPOILER LOG                 ║\n");
        sb.append("╚═════════════════════════════════════════════════════════════════╝\n");
        sb.append("\n");
        sb.append("Seed: ").append(result.getSeed()).append("\n");
        sb.append("Status: ").append(result.isSuccessful() ? "SUCCESS" : "FAILED").append("\n");
        sb.append("Items Placed: ").append(result.getPlacementCount()).append("\n");
        sb.append("\n");

        // Placements by region
        sb.append("╔═════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                      ITEM PLACEMENTS                            ║\n");
        sb.append("╚═════════════════════════════════════════════════════════════════╝\n");
        sb.append("\n");

        Map<String, StringBuilder> regionBuilders = new TreeMap<>();

        for (Map.Entry<String, String> entry : result.getPlacements().entrySet()) {
            String locationId = entry.getKey();
            String itemId = entry.getValue();

            // Extract region from location ID (before underscore)
            String region = locationId.split("_")[0];
            region = region.substring(0, 1).toUpperCase() + region.substring(1).toLowerCase();

            regionBuilders.computeIfAbsent(region, k -> new StringBuilder())
                    .append(String.format("  %-40s → %s\n", locationId, itemId));
        }

        // Print regions alphabetically
        for (Map.Entry<String, StringBuilder> entry : regionBuilders.entrySet()) {
            sb.append(entry.getKey()).append(":\n");
            sb.append(entry.getValue().toString());
            sb.append("\n");
        }

        // Summary
        sb.append("╔═════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                           SUMMARY                               ║\n");
        sb.append("╚═════════════════════════════════════════════════════════════════╝\n");

        Map<String, Integer> itemCounts = new TreeMap<>();
        for (String itemId : result.getPlacedItemIds()) {
            if (!itemId.endsWith("_TANK")) {
                itemCounts.merge(itemId, 1, Integer::sum);
            }
        }

        // Count tanks separately
        long energyTanks = result.getPlacedItemIds().stream()
                .filter(itemId -> "ENERGY_TANK".equals(itemId))
                .count();
        long missileTanks = result.getPlacedItemIds().stream()
                .filter(itemId -> "MISSILE_TANK".equals(itemId))
                .count();
        long superMissileTanks = result.getPlacedItemIds().stream()
                .filter(itemId -> "SUPER_MISSILE_TANK".equals(itemId))
                .count();
        long powerBombTanks = result.getPlacedItemIds().stream()
                .filter(itemId -> "POWER_BOMB_TANK".equals(itemId))
                .count();

        sb.append("\nMajor Items:\n");
        for (Map.Entry<String, Integer> entry : itemCounts.entrySet()) {
            sb.append(String.format("  %-35s %d\n", entry.getKey() + ":", entry.getValue()));
        }

        sb.append("\nResource Tanks:\n");
        sb.append(String.format("  %-35s %d\n", "Energy Tanks:", energyTanks));
        sb.append(String.format("  %-35s %d\n", "Missile Tanks:", missileTanks));
        sb.append(String.format("  %-35s %d\n", "Super Missile Tanks:", superMissileTanks));
        sb.append(String.format("  %-35s %d\n", "Power Bomb Tanks:", powerBombTanks));

        return sb.toString();
    }

    /**
     * Generate a compact spoiler log (placements only).
     */
    public String generateCompactLog() {
        StringBuilder sb = new StringBuilder();

        sb.append("Seed: ").append(result.getSeed()).append("\n");
        sb.append("Placements:\n");

        for (Map.Entry<String, String> entry : result.getPlacements().entrySet()) {
            sb.append(String.format("  %s → %s\n", entry.getKey(), entry.getValue()));
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return generateSpoilerLog();
    }
}
