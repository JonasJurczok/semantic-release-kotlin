#!/bin/bash

set -eu

BRANCH=$(git branch | grep "*")
echo "Working on branch ${BRANCH}"

export MAVEN_OPTS=-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
./mvnw -B clean test jacoco:report

if [[ -n "${COVERALLS_TOKEN:-}" ]]; then
    echo "Found coveralls token. Sending coverage reports"
     ./mvnw -B coveralls:report -DrepoToken="${COVERALLS_TOKEN}"
fi
