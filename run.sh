#!/bin/bash
# Compile and run the Library Management System (Linux / macOS).
# Usage:  ./run.sh

set -e
cd "$(dirname "$0")"

echo "Compiling..."
mkdir -p bin
javac -d bin --release 17 $(find src -name "*.java")

echo "Starting application..."
echo ""
java -cp bin com.library.main.Main
