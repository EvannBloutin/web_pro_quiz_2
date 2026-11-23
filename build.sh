#!/usr/bin/env bash
# Compiles the Java sources into campus-library.jar (needs a JDK 17+).
set -e
cd "$(dirname "$0")"
rm -rf build && mkdir -p build/classes
javac --release 17 -encoding UTF-8 -cp "lib/*" -d build/classes $(find src/main/java -name '*.java')
cp src/main/resources/* build/classes/
jar cf campus-library.jar -C build/classes .
echo "Built campus-library.jar"
