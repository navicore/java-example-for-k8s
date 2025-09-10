#!/bin/bash

set -e

echo "🔨 Building Spring Boot application..."
mvn clean package -DskipTests

echo "🐳 Building Docker image..."
docker build -t probe-demo:latest .

echo "📦 Loading image into kind cluster..."
kind load docker-image probe-demo:latest --name navipod-tests

echo "🚀 Deploying to Kubernetes..."
kubectl apply -f k8s/

echo "⏳ Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/probe-demo

echo "✅ Deployment complete!"
echo ""
echo "📊 Check status with:"
echo "  kubectl get pods -l app=probe-demo"
echo "  kubectl get services -l app=probe-demo"
echo ""
echo "🔍 View logs with:"
echo "  kubectl logs -l app=probe-demo -f"
echo ""
echo "🌐 Access the application:"
echo "  Main app: http://localhost:30080"
echo "  Health:   http://localhost:30081/actuator/health"
echo "  Metrics:  http://localhost:30081/actuator/prometheus"
echo ""
echo "🧪 Test probes in navipod to see the health configuration!"