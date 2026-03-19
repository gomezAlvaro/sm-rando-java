package com.maprando;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Super Metroid Map Randomizer web interface.
 * This class serves as the entry point for the Spring Boot application and enables
 * component scanning for all Spring beans in the com.maprando package hierarchy.
 *
 * The application provides a REST API for seed generation and a Vue.js SPA frontend
 * for user interaction.
 */
@SpringBootApplication
public class MapRandomizerApplication {

    /**
     * Main method that starts the Spring Boot application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        SpringApplication.run(MapRandomizerApplication.class, args);
    }
}