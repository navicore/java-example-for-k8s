package com.example.probedemo;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Component;

@Component
public class ObservationRegistryInspector {

    private final ObservationRegistry observationRegistry;

    public ObservationRegistryInspector(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
        inspectRegistry();
    }

    public void inspectRegistry() {
        // Breakpoint here to inspect ObservationRegistry
        System.out.println("ObservationRegistry class: " + observationRegistry.getClass().getName());
        System.out.println("ObservationRegistry: " + observationRegistry);
    }

    public ObservationRegistry getObservationRegistry() {
        return observationRegistry;
    }
}
