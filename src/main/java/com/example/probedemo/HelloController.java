package com.example.probedemo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class HelloController {

    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final Counter helloCounter;
    private final Counter errorCounter; 
    private final Timer requestTimer;
    private final Gauge activeConnections;
    private final AtomicInteger activeConnectionsCount = new AtomicInteger(0);

    public HelloController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Business metrics
        this.helloCounter = Counter.builder("hello_requests_total")
                .description("Total number of hello requests")
                .tag("endpoint", "root")
                .register(meterRegistry);

        this.errorCounter = Counter.builder("hello_errors_total")
                .description("Total number of hello request errors")
                .tag("endpoint", "root")
                .register(meterRegistry);

        // Performance metrics
        this.requestTimer = Timer.builder("hello_request_duration_seconds")
                .description("Request processing time")
                .tag("endpoint", "root")
                .register(meterRegistry);

        // System metrics
        this.activeConnections = Gauge.builder("hello_active_connections", this, HelloController::getActiveConnections)
                .description("Number of active connections")
                .register(meterRegistry);

        // Custom business gauge
        Gauge.builder("hello_service_uptime_seconds", this, HelloController::getUptimeSeconds)
                .description("Service uptime in seconds")
                .register(meterRegistry);
    }

    @GetMapping("/")
    public String hello() {
        Timer.Sample sample = Timer.start(meterRegistry);
        activeConnectionsCount.incrementAndGet();
        try {
            // Simulate some processing time
            Thread.sleep(ThreadLocalRandom.current().nextInt(50, 200));
            
            helloCounter.increment();
            requestCount.incrementAndGet();
            
            return String.format("Hello World! Request #%d at %s", 
                                requestCount.get(), LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorCounter.increment();
            throw new RuntimeException("Request interrupted", e);
        } finally {
            activeConnectionsCount.decrementAndGet();
            sample.stop(requestTimer);
        }
    }

    @GetMapping("/hello/{name}")
    public String helloName(@PathVariable String name) {
        Timer.Sample sample = Timer.start(meterRegistry);
        activeConnectionsCount.incrementAndGet();
        try {
            // Simulate variable processing time based on name length
            Thread.sleep(name.length() * 10);
            
            Counter.builder("hello_named_requests_total")
                    .description("Total number of named hello requests")
                    .tag("endpoint", "named")
                    .tag("name_length", String.valueOf(name.length() > 10 ? "long" : "short"))
                    .register(meterRegistry)
                    .increment();
            
            requestCount.incrementAndGet();
            
            return String.format("Hello %s! Request #%d at %s", 
                                name, requestCount.get(), LocalDateTime.now());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            errorCounter.increment();
            throw new RuntimeException("Request interrupted", e);
        } finally {
            activeConnectionsCount.decrementAndGet();
            sample.stop(requestTimer);
        }
    }

    @GetMapping("/health-check")
    public String healthCheck() {
        Counter.builder("health_check_requests_total")
                .description("Total number of health check requests")
                .tag("endpoint", "health")
                .register(meterRegistry)
                .increment();
                
        return "Service is healthy!";
    }

    @GetMapping("/info")
    public String info() {
        Counter.builder("info_requests_total")
                .description("Total number of info requests") 
                .tag("endpoint", "info")
                .register(meterRegistry)
                .increment();
                
        return String.format("Probe Demo Service - Uptime: %s, Total Requests: %d, Active: %d", 
                           java.time.Duration.ofMillis(System.currentTimeMillis() - startTime),
                           requestCount.get(),
                           activeConnectionsCount.get());
    }

    @GetMapping("/simulate-error")
    public String simulateError() {
        errorCounter.increment();
        
        if (ThreadLocalRandom.current().nextBoolean()) {
            throw new RuntimeException("Simulated error for testing!");
        }
        
        return "Error simulation - this time it worked!";
    }

    // Gauge callback methods
    public double getActiveConnections() {
        return activeConnectionsCount.get();
    }

    public double getUptimeSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000.0;
    }

    private static final long startTime = System.currentTimeMillis();
    private final MeterRegistry meterRegistry;
}