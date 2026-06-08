package com.example.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // e.g. "http://localhost:5173, https://example.com" -> two origins
        String[] parts = allowedOrigins.split(",");
        List<String> originList = new ArrayList<>();
        for (String part : parts) {
            String origin = part.trim();
            if (!origin.isEmpty()) {
                originList.add(origin);
            }
        }
        String[] origins = originList.toArray(new String[0]);

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
