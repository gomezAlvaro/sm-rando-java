package com.maprando.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for the map randomizer application.
 * Configures CORS settings for Vue.js frontend and static resource handling.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${spring.web.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Value("${spring.web.cors.allowed-methods}")
    private String[] allowedMethods;

    @Value("${spring.web.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${spring.web.cors.allow-credentials}")
    private Boolean allowCredentials;

    @Value("${spring.web.cors.max-age}")
    private Long maxAge;

    /**
     * Configures CORS (Cross-Origin Resource Sharing) settings for the API.
     * This allows the Vue.js frontend running on localhost:5173 to communicate
     * with the backend API on localhost:8080.
     *
     * @param registry CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .allowedHeaders(allowedHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    /**
     * Configures static resource handling for Vue.js frontend.
     * When the frontend is built, Vite outputs to src/main/resources/static,
     * which Spring Boot serves automatically.
     *
     * @param registry resource handler registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static resources from the built Vue.js app
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}