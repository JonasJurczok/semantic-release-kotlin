#!/bin/bash


git branch

./mvnw -B clean verify -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
cat /github/workspace/semantic-release-kotlin-cli/target/surefire-reports/*

# TODO: coveralls support