#!/bin/bash

set -eu

AUTH_HEADER="Authorization: token ${GITHUB_TOKEN}"
API_VERSION=v3
API_HEADER="Accept: application/vnd.github.${API_VERSION}+json"
HASH=$(git rev-parse HEAD)
URL="https://api.github.com/repos/${GITHUB_REPOSITORY}/statuses/${HASH}"

report_status() {
  curl -sSL -XPOST -H "$AUTH_HEADER" -H "$API_HEADER" "$URL" -d "{ \"state\": \"$1\", \"description\": \"Github Actions CI\", \"context\": \"Github Actions CI\"}"
}

trap 'report_status "failure"' ERR

BRANCH=$(git branch | grep "*")
echo "Working on branch ${BRANCH}"

# send status to commit (test pending)
report_status "pending"

export MAVEN_OPTS=-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
./mvnw -B clean test jacoco:report

if [[ -n "${COVERALLS_TOKEN:-}" ]]; then
    echo "Found coveralls token. Sending coverage reports"
     ./mvnw -B coveralls:report -DrepoToken="${COVERALLS_TOKEN}"
fi

# send status to commit (test complete)
report_status "success"
