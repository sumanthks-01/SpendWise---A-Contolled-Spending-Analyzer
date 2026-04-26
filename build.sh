#!/usr/bin/env bash
# Build script for Render deployment

set -e

echo "🔨 Building SpendWise..."

# Clean and build the project
mvn clean package -DskipTests

echo "✅ Build completed successfully!"
