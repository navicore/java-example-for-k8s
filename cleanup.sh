#!/bin/bash

echo "🧹 Cleaning up probe-demo deployment..."

kubectl delete -f k8s/ --ignore-not-found=true

echo "⏳ Waiting for resources to be removed..."
kubectl wait --for=delete pod -l app=probe-demo --timeout=60s 2>/dev/null || true

echo "✅ Cleanup complete!"

echo ""
echo "🗑️  Optionally remove Docker image:"
echo "  docker rmi probe-demo:latest"