#!/bin/bash

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Create bin directory if it doesn't exist
mkdir -p "$SCRIPT_DIR/bin"

# Compile the Lox interpreter if not already compiled
if [ ! -f "$SCRIPT_DIR/../lox/lox/lox/Lox.class" ]; then
    echo "Compiling Lox interpreter..."
    cd "$SCRIPT_DIR/../lox/lox"
    javac $(find . -name "*.java")
    cd "$SCRIPT_DIR"
fi

# Compile the test framework
echo "Compiling test framework..."
javac -d "$SCRIPT_DIR/bin" "$SCRIPT_DIR/src/main/java/lox"/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed"
    exit 1
fi

# Run tests (uses relative paths by default)
echo "Running tests..."
cd "$SCRIPT_DIR"
java -cp bin lox.LoxTestFramework "${1:-./../testFiles}" "${2:-./../lox/lox}"
