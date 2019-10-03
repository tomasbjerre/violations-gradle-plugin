#!/bin/bash
./gradlew clean cE eclipse build install gitChangelogTask || exit 1
cd violations-gradle-plugin-example

./gradlew violations
