package com.maprando.web.service;

import com.maprando.data.DataLoader;
import com.maprando.web.dto.SpoilerData;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for parsing spoiler logs into structured data.
 */
@Service
public class SpoilerParserService {

    private final DataLoader dataLoader;

    public SpoilerParserService(DataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    /**
     * Parse spoiler log file into structured data.
     */
    public SpoilerData parseSpoilerLog(String seedId, Path spoilerPath) throws IOException {
        String content = Files.readString(spoilerPath);
        return parseSpoilerContent(seedId, content);
    }

    /**
     * Parse spoiler log content into structured data.
     */
    public SpoilerData parseSpoilerContent(String seedId, String content) {
        // Extract seed information
        String seed = extractField(content, "Seed:");
        String status = extractField(content, "Status:");
        int itemsPlaced = parseIntField(extractField(content, "Items Placed:"));

        // Parse item placements by region
        Map<String, List<SpoilerData.ItemPlacement>> placementsByRegion = parsePlacements(content);

        // Parse item summaries
        SpoilerData.ItemSummary majorItems = parseItemSummary(content, "Major Items:");
        SpoilerData.ItemSummary resourceTanks = parseItemSummary(content, "Resource Tanks:");

        return new SpoilerData(
                seedId,
                seed,
                status,
                itemsPlaced,
                placementsByRegion,
                majorItems,
                resourceTanks
        );
    }

    /**
     * Parse item placements grouped by region.
     */
    private Map<String, List<SpoilerData.ItemPlacement>> parsePlacements(String content) {
        Map<String, List<SpoilerData.ItemPlacement>> placements = new HashMap<>();

        // Find the item placements section
        String placementsSection = extractSection(content, "ITEM PLACEMENTS", "SUMMARY");

        if (placementsSection == null) {
            return placements;
        }

        // Split by region
        String[] lines = placementsSection.split("\n");
        String currentRegion = null;
        Pattern locationPattern = Pattern.compile("^\\s*(\\w+_\\w+)\\s+→\\s+(\\w+)$");

        for (String line : lines) {
            line = line.trim();

            // Check if this is a region header
            if (line.endsWith(":") && !line.contains("→")) {
                currentRegion = line.substring(0, line.length() - 1);
                placements.putIfAbsent(currentRegion, new ArrayList<>());
                continue;
            }

            // Parse item placement
            Matcher matcher = locationPattern.matcher(line);
            if (matcher.matches() && currentRegion != null) {
                String locationId = matcher.group(1);
                String itemId = matcher.group(2);

                // Get location metadata from data loader
                String locationName = locationId; // fallback
                String region = currentRegion;
                boolean isProgression = isProgressionItem(itemId);

                try {
                    var locationDef = dataLoader.getLocationDefinition(locationId);
                    if (locationDef != null) {
                        locationName = locationDef.getName();
                        region = locationDef.getRegion();
                    }
                } catch (Exception e) {
                    // Use fallback values
                }

                String itemName = formatItemName(itemId);

                SpoilerData.ItemPlacement placement = new SpoilerData.ItemPlacement(
                        locationId,
                        locationName,
                        itemId,
                        itemName,
                        region,
                        isProgression
                );

                placements.get(currentRegion).add(placement);
            }
        }

        return placements;
    }

    /**
     * Parse item summary section.
     */
    private SpoilerData.ItemSummary parseItemSummary(String content, String sectionHeader) {
        Map<String, Integer> items = new LinkedHashMap<>();

        String summarySection = extractSection(content, sectionHeader, null);
        if (summarySection == null) {
            return new SpoilerData.ItemSummary(items);
        }

        Pattern itemPattern = Pattern.compile("^\\s*(\\w+)\\s*:\\s*(\\d+)$");

        for (String line : summarySection.split("\n")) {
            Matcher matcher = itemPattern.matcher(line.trim());
            if (matcher.matches()) {
                String itemName = matcher.group(1);
                int count = Integer.parseInt(matcher.group(2));
                items.put(itemName, count);
            }
        }

        return new SpoilerData.ItemSummary(items);
    }

    /**
     * Extract a section between two headers.
     */
    private String extractSection(String content, String startHeader, String endHeader) {
        int startIndex = content.indexOf(startHeader);
        if (startIndex == -1) {
            return null;
        }

        startIndex = content.indexOf("\n", startIndex);
        if (startIndex == -1) {
            return null;
        }
        startIndex++; // Skip the newline

        if (endHeader != null) {
            int endIndex = content.indexOf(endHeader, startIndex);
            if (endIndex == -1) {
                return null;
            }
            return content.substring(startIndex, endIndex).trim();
        } else {
            // Go to end of content
            return content.substring(startIndex).trim();
        }
    }

    /**
     * Extract a field value from content.
     */
    private String extractField(String content, String fieldName) {
        Pattern pattern = Pattern.compile(Pattern.quote(fieldName) + "\\s*(\\S+)");
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Parse an integer field.
     */
    private int parseIntField(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Check if an item is a progression item.
     */
    private boolean isProgressionItem(String itemId) {
        // Tanks are not progression items
        if (itemId.endsWith("_TANK")) {
            return false;
        }

        // These are progression items
        Set<String> progressionItems = Set.of(
                "MORPH_BALL", "BOMB", "SPRING_BALL", "POWER_BOMB",
                "CHARGE_BEAM", "ICE_BEAM", "WAVE_BEAM", "SPAZER_BEAM", "PLASMA_BEAM",
                "VARIA_SUIT", "GRAVITY_SUIT",
                "HI_JUMP_BOOTS", "SPEED_BOOSTER", "SPACE_JUMP", "SCREW_ATTACK",
                "GRAPPLE_BEAM", "XRAY_SCOPE"
        );

        return progressionItems.contains(itemId);
    }

    /**
     * Format item ID for display.
     */
    private String formatItemName(String itemId) {
        String formatted = itemId.replace("_", " ").toLowerCase();

        // Capitalize first letter of each word
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : formatted.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
