#!/bin/bash
./gradlew publishToMavenLocal -Pversion=latest-SNAPSHOT || exit 1
cd violations-gradle-plugin-example

./gradlew violations
