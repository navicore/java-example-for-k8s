package com.example.probedemo;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservabilityConfig {

    @Bean
    public ObservationRegistryInspector observationRegistryInspector(ObservationRegistry observationRegistry) {
        return new ObservationRegistryInspector(observationRegistry);
    }
}
