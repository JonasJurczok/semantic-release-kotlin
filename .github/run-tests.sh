#!/bin/bash

set -eu

BRANCH=$(git branch | grep "*")
echo "Working on branch ${BRANCH}"

export MAVEN_OPTS=-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

./mvnw -B clean test jacoco:report coveralls:report -DrepoToken="${COVERALLS_TOKEN}"


./mvnw versions:set versions:commit -B -DnewVersion="2"


git config --global user.email "${GITHUB_ACTOR}@github-actions.com"
git config --global user.name "$GITHUB_ACTOR"

git commit -am"prepare release 2"
git tag 2

git push --set-upstream origin fix-workflow
git push --set-upstream origin --tags
