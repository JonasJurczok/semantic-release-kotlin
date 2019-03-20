#!/bin/bash


git branch

./mvnw -q clean verify
cat /github/workspace/semantic-release-kotlin-cli/target/surefire-reports

# TODO: coveralls support