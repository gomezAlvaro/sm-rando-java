package com.maprando.web.config;

import com.maprando.data.DataLoader;
import com.maprando.randomize.advanced.QualityMetricsCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * Spring configuration for randomizer beans and services.
 * This class provides the necessary beans for seed generation,
 * including DataLoader and QualityMetricsCalculator.
 */
@Configuration
public class RandomizerConfig {

    /**
     * Creates a DataLoader bean that loads JSON game data at startup.
     * The DataLoader reads items, locations, requirements, and difficulties
     * from the resources/data directory.
     *
     * @return configured DataLoader instance
     * @throws RuntimeException if data loading fails
     */
    @Bean
    public DataLoader dataLoader() {
        try {
            DataLoader dataLoader = new DataLoader();
            dataLoader.loadAllData();
            return dataLoader;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load game data", e);
        }
    }

    /**
     * Creates a QualityMetricsCalculator bean for analyzing seed quality.
     * This calculator provides metrics like reachability percentage,
     * difficulty assessment, and overall quality ratings.
     *
     * @param dataLoader the data loader for game data
     * @return configured QualityMetricsCalculator instance
     */
    @Bean
    public QualityMetricsCalculator qualityMetricsCalculator(DataLoader dataLoader) {
        return new QualityMetricsCalculator(dataLoader);
    }
}