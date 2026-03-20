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

/**
 * Utility to add requirements to locations.json based on game logic.
 * This enables the ReachabilityAnalysis and SeedVerifier tests.
 */
public class RequirementsUpdater {

    private final ObjectMapper objectMapper;
    private final Path locationsPath;
    private final Path outputPath;

    public RequirementsUpdater(String locationsPath, String outputPath) {
        this.objectMapper = new ObjectMapper();
        this.locationsPath = Paths.get(locationsPath);
        this.outputPath = Paths.get(outputPath);
    }

    public void updateRequirements() throws IOException {
        System.out.println("Reading locations.json from: " + locationsPath);

        // Read the locations.json
        JsonNode locationsJson = objectMapper.readTree(locationsPath.toFile());
        ArrayNode locations = (ArrayNode) locationsJson.get("locations");

        System.out.println("Found " + locations.size() + " locations");

        int updatedCount = 0;
        for (int i = 0; i < locations.size(); i++) {
            ObjectNode location = (ObjectNode) locations.get(i);
            String locationId = location.get("id").asText();
            String area = location.get("area").asText();

            // Add requirements based on location
            ArrayNode requirements = getRequirementsForLocation(locationId, area);
            location.set("requirements", requirements);

            updatedCount++;
        }

        // Write to output file
        System.out.println("Writing " + updatedCount + " updated locations to: " + outputPath);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputPath.toFile(), locationsJson);

        System.out.println("\nRequirements update complete!");
    }

    private ArrayNode getRequirementsForLocation(String locationId, String area) {
        ArrayNode requirements = objectMapper.createArrayNode();

        // Add requirements based on location patterns
        if (locationId.contains("brinstar")) {
            addBrinstarRequirements(locationId, requirements);
        } else if (locationId.contains("norfair") || locationId.contains("norfair")) {
            addNorfairRequirements(locationId, requirements);
        } else if (locationId.contains("maridia")) {
            addMaridiaRequirements(locationId, requirements);
        } else if (locationId.contains("wrecked") || locationId.contains("wrecked_ship")) {
            addWreckedShipRequirements(locationId, requirements);
        } else if (locationId.contains("tourian")) {
            addTourianRequirements(locationId, requirements);
        } else if (locationId.contains("lower_norfair")) {
            addLowerNorfairRequirements(locationId, requirements);
        }

        return requirements;
    }

    private void addBrinstarRequirements(String locationId, ArrayNode requirements) {
        // Brinstar locations that require morph
        if (locationId.contains("morph") || locationId.contains("bomb")) {
            requirements.add("can_morph");
        }

        // Locations that require bombs
        if (locationId.contains("bomb") && !locationId.contains("torizo")) {
            requirements.add("can_place_bombs");
        }

        // Red tower area requires speed or hi-jump
        if (locationId.contains("red") || locationId.contains("tower")) {
            requirements.add("can_hijump");
        }
    }

    private void addNorfairRequirements(String locationId, ArrayNode requirements) {
        // Most Norfair requires heat protection
        if (!locationId.contains("early") && !locationId.contains("elevator")) {
            requirements.add("can_survive_heat");
        }

        // Locations requiring grapple
        if (locationId.contains("grapple") || locationId.contains("wave")) {
            requirements.add("has_grapple");
        }

        // Speed booster room
        if (locationId.contains("speed")) {
            requirements.add("can_survive_heat");
            requirements.add("can_speed_booster");
        }
    }

    private void addMaridiaRequirements(String locationId, ArrayNode requirements) {
        // Maridia requires gravity suit for water
        if (!locationId.contains("early") && !locationId.contains("outer")) {
            requirements.add("can_swim_water");
        }

        // Watering hole requires grapple
        if (locationId.contains("water") || locationId.contains("hole")) {
            requirements.add("has_grapple");
        }

        // Botwoon area
        if (locationId.contains("botwoon")) {
            requirements.add("can_swim_water");
            requirements.add("has_grapple");
        }
    }

    private void addWreckedShipRequirements(String locationId, ArrayNode requirements) {
        // Early wrecked ship is accessible
        if (locationId.contains("main") || locationId.contains("bowling")) {
            // No requirements for early areas
        }

        // Gravity suit area
        if (locationId.contains("gravity")) {
            requirements.add("has_grapple");
        }
    }

    private void addTourianRequirements(String locationId, ArrayNode requirements) {
        // All Tourian requires ice beam
        requirements.add("has_ice_beam");

        // Metroids area requires more
        if (locationId.contains("metroid")) {
            requirements.add("can_morph");
            requirements.add("has_ice_beam");
        }

        // Mother Brain requires everything
        if (locationId.contains("mother") || locationId.contains("brain")) {
            requirements.add("can_morph");
            requirements.add("has_ice_beam");
            requirements.add("can_spacejump");
            requirements.add("has_grapple");
            requirements.add("can_survive_heat");
        }
    }

    private void addLowerNorfairRequirements(String locationId, ArrayNode requirements) {
        // Lower Norfair requires heat protection and lava survival
        requirements.add("can_survive_heat");
        requirements.add("can_survive_lava");

        // Ridley requires special preparation
        if (locationId.contains("ridley")) {
            requirements.add("can_survive_heat");
            requirements.add("can_surf_lava");
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java RequirementsUpdater <locations.json> <output_locations.json>");
            System.err.println("Example: java RequirementsUpdater ./locations.json ./locations_with_requirements.json");
            System.exit(1);
        }

        try {
            RequirementsUpdater updater = new RequirementsUpdater(args[0], args[1]);
            updater.updateRequirements();
        } catch (IOException e) {
            System.err.println("Error updating requirements: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
