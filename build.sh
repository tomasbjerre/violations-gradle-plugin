#!/bin/bash
./gradlew clean cE eclipse build publishToMavenLocal || exit 1
cd violations-gradle-plugin-example

./gradlew violations
