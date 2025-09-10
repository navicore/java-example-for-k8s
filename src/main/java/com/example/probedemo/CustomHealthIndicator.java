package com.example.probedemo;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class CustomHealthIndicator {

    private static final long START_TIME = System.currentTimeMillis();
    
    public Map<String, Object> health() {
        long uptime = System.currentTimeMillis() - START_TIME;
        
        // Simulate startup time - not ready for first 30 seconds
        if (uptime < 30000) {
            return Map.of(
                "status", "DOWN",
                "details", Map.of(
                    "startup", "starting",
                    "uptime", uptime + "ms",
                    "message", "Service is still starting up"
                )
            );
        }
        
        // Service is ready
        return Map.of(
            "status", "UP",
            "details", Map.of(
                "startup", "ready", 
                "uptime", uptime + "ms",
                "message", "Service is ready to serve traffic"
            )
        );
    }
}