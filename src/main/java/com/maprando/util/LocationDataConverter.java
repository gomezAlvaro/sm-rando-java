package com.maprando.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Utility to convert Rust room_geometry.json to our locations.json format.
 *
 * Run this once to generate the initial locations.json file with real ROM addresses.
 * Usage: java LocationDataConverter <path_to_room_geometry.json> <output_path>
 */
public class LocationDataConverter {

    private static final String[] AREA_NAMES = {
        "Crateria",
        "Brinstar",
        "Norfair",
        "Wrecked Ship",
        "Maridia",
        "Tourian"
    };

    private static final Map<String, String> AREA_REGION_MAP = new HashMap<>();
    static {
        AREA_REGION_MAP.put("Crateria", "Crateria");
        AREA_REGION_MAP.put("Brinstar", "Brinstar");
        AREA_REGION_MAP.put("Norfair", "Norfair");
        AREA_REGION_MAP.put("Wrecked Ship", "Wrecked Ship");
        AREA_REGION_MAP.put("Maridia", "Maridia");
        AREA_REGION_MAP.put("Tourian", "Tourian");
    }

    private final ObjectMapper objectMapper;
    private final Path inputPath;
    private final Path outputPath;

    public LocationDataConverter(String inputPath, String outputPath) {
        this.objectMapper = new ObjectMapper();
        this.inputPath = Paths.get(inputPath);
        this.outputPath = Paths.get(outputPath);
    }

    public void convert() throws IOException {
        System.out.println("Reading room_geometry.json from: " + inputPath);

        // Read the room_geometry.json
        JsonNode rooms = objectMapper.readTree(inputPath.toFile());

        System.out.println("Found " + rooms.size() + " rooms");

        // Build locations list
        ArrayNode locations = objectMapper.createArrayNode();
        int locationCount = 0;
        int roomWithItemsCount = 0;

        for (JsonNode room : rooms) {
            int roomId = room.get("room_id").asInt();
            String roomName = room.get("name").asText();
            int areaIndex = room.get("area").asInt();
            JsonNode items = room.get("items");

            if (items != null && items.isArray() && items.size() > 0) {
                roomWithItemsCount++;
                String areaName = getAreaName(areaIndex);
                String regionName = AREA_REGION_MAP.getOrDefault(areaName, areaName);

                // Create a location for each item in the room
                for (int i = 0; i < items.size(); i++) {
                    JsonNode item = items.get(i);
                    int itemAddr = item.get("addr").asInt();

                    // Convert PC address to SNES address format (0xXXXXXX)
                    String snesAddress = pcToSnesAddress(itemAddr);

                    // Create location ID
                    String locationId = createLocationId(roomName, areaName, i, roomId);

                    ObjectNode location = objectMapper.createObjectNode();
                    location.put("id", locationId);
                    location.put("name", roomName + (items.size() > 1 ? " (" + (i + 1) + ")" : ""));
                    location.put("region", regionName);
                    location.put("area", areaName);
                    location.put("romAddress", snesAddress);
                    location.put("pcAddress", itemAddr); // Store PC address for reference
                    location.put("roomId", roomId); // Store room ID for reference
                    location.put("roomName", roomName); // Store room name for reference
                    location.putArray("requirements"); // Empty requirements for now
                    location.put("isEarlyGame", isEarlyGameRoom(roomId, areaName));

                    locations.add(location);
                    locationCount++;
                }
            }
        }

        // Create the output structure
        ObjectNode output = objectMapper.createObjectNode();
        output.set("locations", locations);

        // Write to output file
        System.out.println("Writing " + locationCount + " locations to: " + outputPath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), output);

        System.out.println("\nConversion complete!");
        System.out.println("- Total rooms: " + rooms.size());
        System.out.println("- Rooms with items: " + roomWithItemsCount);
        System.out.println("- Total locations: " + locationCount);
    }

    private String getAreaName(int areaIndex) {
        if (areaIndex >= 0 && areaIndex < AREA_NAMES.length) {
            return AREA_NAMES[areaIndex];
        }
        return "Unknown";
    }

    private String pcToSnesAddress(int pcAddr) {
        // Convert PC address to SNES address using correct HiROM formula
        // Matches Rom.pc2snes(): ((addr << 1) & 0xFF0000) | (addr & 0xFFFF) | 0x808000
        int snesAddr = ((pcAddr << 1) & 0xFF0000) | (pcAddr & 0xFFFF) | 0x808000;
        return String.format("0x%06X", snesAddr);
    }

    private String createLocationId(String roomName, String areaName, int itemIndex, int roomId) {
        // Create a unique, readable location ID
        // Format: area_roomname_index or area_roomname (if single item)
        String normalizedRoomName = roomName.toLowerCase()
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_|_$", "");
        String normalizedAreaName = areaName.toLowerCase()
            .replaceAll("[^a-z0-9]+", "_")
            .replaceAll("^_|_$", "");

        if (itemIndex == 0) {
            return normalizedAreaName + "_" + normalizedRoomName;
        } else {
            return normalizedAreaName + "_" + normalizedRoomName + "_" + (itemIndex + 1);
        }
    }

    private boolean isEarlyGameRoom(int roomId, String areaName) {
        // Mark some rooms as early game accessible
        // This is a heuristic - would need refinement based on actual game logic

        // Crateria early game rooms
        if (roomId <= 20) return true; // Landing Site area

        // Brinstar early game rooms
        if (areaName.equals("Brinstar") && roomId < 100) return true;

        return false;
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java LocationDataConverter <room_geometry.json> <output_locations.json>");
            System.err.println("Example: java LocationDataConverter C:/Users/agr_b/MapRandomizer/room_geometry.json ./locations_new.json");
            System.exit(1);
        }

        try {
            LocationDataConverter converter = new LocationDataConverter(args[0], args[1]);
            converter.convert();
        } catch (IOException e) {
            System.err.println("Error converting location data: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
