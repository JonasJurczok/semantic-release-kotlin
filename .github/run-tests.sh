#!/bin/bash

set -eu

git branch

./mvnw -B clean verify -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

./mvnw coveralls:report -DrepoToken="${COVERALLS_TOKEN}"

# TODO: coveralls support