#!/usr/bin/env bash
# Starts Campus Library on http://localhost:8080 (needs a JDK/JRE 17+). Stop with Ctrl+C.
cd "$(dirname "$0")"
exec java -cp "campus-library.jar:lib/*" com.campuslibrary.Main
