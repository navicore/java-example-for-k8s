# Kubernetes Probes Demo - Java Spring Boot

A comprehensive example Spring Boot application designed to demonstrate all three Kubernetes probe types (Startup, Liveness, Readiness) with metrics and health endpoints.

## Features

- **Spring Boot 3.2.1** with Java 17
- **Spring Boot Actuator** for health endpoints
- **Micrometer with Prometheus** metrics
- **All three Kubernetes probe types** properly configured
- **Custom health indicators** for realistic startup simulation
- **Comprehensive monitoring endpoints**

## Probe Configuration

This application demonstrates all three probe types:

### 🚀 Startup Probe
- **Path**: `/actuator/health/liveness`
- **Port**: 8081 (management)
- **Purpose**: Ensures container starts properly before other probes begin
- **Timing**: 10s initial delay, 5s period, up to 30s total wait time

### 💓 Liveness Probe  
- **Path**: `/actuator/health/liveness`
- **Port**: 8081 (management)
- **Purpose**: Determines if container should be restarted
- **Timing**: 30s initial delay, 10s period, 5s timeout

### ✅ Readiness Probe
- **Path**: `/actuator/health/readiness`  
- **Port**: 8081 (management)
- **Purpose**: Determines if container should receive traffic
- **Timing**: 15s initial delay, 5s period, 3s timeout

## Application Endpoints

| Endpoint | Port | Description |
|----------|------|-------------|
| `/` | 8080 | Hello world with request counter + metrics |
| `/hello/{name}` | 8080 | Personalized hello with variable timing metrics |
| `/health-check` | 8080 | Simple health check |
| `/info` | 8080 | Service information and uptime |
| `/simulate-error` | 8080 | Error simulation for testing error metrics |
| `/actuator/health` | 8081 | Comprehensive health status |
| `/actuator/health/liveness` | 8081 | Liveness probe endpoint |
| `/actuator/health/readiness` | 8081 | Readiness probe endpoint |
| `/actuator/metrics` | 8081 | Application metrics |
| `/actuator/prometheus` | 8081 | **Prometheus-format metrics for navipod scraping** |
| `/actuator/info` | 8081 | Application info |

## 📊 Prometheus Metrics Available

The application exposes comprehensive Micrometer metrics in Prometheus format at `/actuator/prometheus` (port 8081). Perfect for navipod metrics scraping:

### Business Metrics
- `hello_requests_total{endpoint="root"}` - Total hello world requests
- `hello_named_requests_total{endpoint="named",name_length="short|long"}` - Named hello requests by name length
- `hello_errors_total{endpoint="root"}` - Total error count
- `health_check_requests_total{endpoint="health"}` - Health check requests
- `info_requests_total{endpoint="info"}` - Info endpoint requests

### Performance Metrics  
- `hello_request_duration_seconds{endpoint="root"}` - Request processing time histogram
- `hello_active_connections` - Current active connections gauge
- `hello_service_uptime_seconds` - Service uptime gauge

### System Metrics
- `jvm_*` - JVM memory, GC, threads (automatic via Micrometer)
- `process_*` - Process CPU, memory usage (automatic)
- `http_server_requests_*` - HTTP request metrics (automatic via Spring Boot)

## Quick Start

### Prerequisites
- Docker
- kubectl configured for your cluster
- kind cluster (if using the provided script)
- Maven (or use the Docker build)

### Option 1: Automated Build & Deploy
```bash
./build-and-deploy.sh
```

### Option 2: Manual Steps

1. **Build the application**:
   ```bash
   mvn clean package
   ```

2. **Build Docker image**:
   ```bash
   docker build -t probe-demo:latest .
   ```

3. **Load into kind cluster** (if using kind):
   ```bash
   kind load docker-image probe-demo:latest --name navipod-tests
   ```

4. **Deploy to Kubernetes**:
   ```bash
   kubectl apply -f k8s/
   ```

5. **Wait for deployment**:
   ```bash
   kubectl wait --for=condition=available --timeout=300s deployment/probe-demo
   ```

## Testing the Probes

### Using kubectl
```bash
# Check pod status and probe results
kubectl get pods -l app=probe-demo
kubectl describe pod -l app=probe-demo

# View logs
kubectl logs -l app=probe-demo -f

# Check services
kubectl get services -l app=probe-demo
```

### Using navipod
1. Run navipod from the parent directory
2. Navigate to the containers view
3. Select one of the `probe-demo` containers
4. View the **Health Probes** section in the details panel
5. You should see all three probe types with their configurations:
   - 🏥 **Liveness**: HTTP GET http://localhost:8081/actuator/health/liveness
   - 🏥 **Readiness**: HTTP GET http://localhost:8081/actuator/health/readiness  
   - 🏥 **Startup**: HTTP GET http://localhost:8081/actuator/health/liveness

### Direct Access (NodePort)
```bash
# Main application with metrics generation
curl http://localhost:30080/
curl http://localhost:30080/hello/navipod
curl http://localhost:30080/simulate-error

# Health endpoints
curl http://localhost:30081/actuator/health
curl http://localhost:30081/actuator/health/liveness
curl http://localhost:30081/actuator/health/readiness

# Prometheus metrics for navipod scraping
curl http://localhost:30081/actuator/prometheus
```

### 🔧 Testing Metrics for navipod Development

The application provides rich metrics data perfect for developing navipod's metrics scraping functionality:

1. **Generate traffic** to create interesting metrics:
   ```bash
   # Generate some requests
   for i in {1..10}; do curl http://localhost:30080/; done
   
   # Test named endpoints with different lengths
   curl http://localhost:30080/hello/short
   curl http://localhost:30080/hello/very-long-name-for-testing
   
   # Generate some errors
   curl http://localhost:30080/simulate-error
   ```

2. **View the Prometheus metrics** that navipod could scrape:
   ```bash
   curl -s http://localhost:30081/actuator/prometheus | grep hello_
   ```

3. **Kubernetes annotations** are already configured for metrics discovery:
   - `prometheus.io/scrape: "true"`  
   - `prometheus.io/path: "/actuator/prometheus"`
   - `prometheus.io/port: "8081"`
   - `navipod.io/metrics-enabled: "true"` (custom for navipod)

## Behavioral Simulation

The application includes realistic behavior:

- **Startup delay**: Service reports "DOWN" for the first 30 seconds to demonstrate startup probes
- **Request counting**: Each request increments counters visible in metrics
- **Custom health indicators**: Demonstrates complex health logic
- **Prometheus metrics**: Includes custom business metrics

## Probe Behavior Details

### Startup Phase (0-30 seconds)
- Startup probe: Will retry for up to 30 seconds
- Liveness probe: Doesn't start until startup succeeds
- Readiness probe: Reports DOWN until custom health indicator is ready
- Traffic: No traffic routed to pod

### Running Phase (30+ seconds)  
- Startup probe: Disabled after first success
- Liveness probe: Should always return UP (service restart if fails)
- Readiness probe: Should return UP (traffic removed if fails)
- Traffic: Pod receives requests

## Cleanup

```bash
kubectl delete -f k8s/
```

## Project Structure

```
java-example-for-k8s/
├── src/main/java/com/example/probedemo/
│   ├── ProbeDemoApplication.java      # Main Spring Boot class
│   ├── HelloController.java          # REST endpoints + metrics
│   └── CustomHealthIndicator.java    # Custom health logic
├── src/main/resources/
│   └── application.yml               # Spring Boot configuration
├── k8s/
│   ├── deployment.yaml              # Kubernetes deployment with probes
│   └── service.yaml                 # Service definitions
├── Dockerfile                       # Multi-stage Docker build
├── build-and-deploy.sh             # Automated build/deploy script
└── pom.xml                         # Maven dependencies
```

This example perfectly demonstrates how Kubernetes probes work in practice and provides a realistic service for testing probe visualization tools like navipod!