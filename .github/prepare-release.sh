#!/bin/bash

set -eu

# Ensure that the GITHUB_TOKEN secret is included
if [[ -z "$GITHUB_TOKEN" ]]; then
  echo "Set the GITHUB_TOKEN env variable."
  exit 1
fi

if [[ -z "$GITHUB_REPOSITORY" ]]; then
	echo "Set the GITHUB_REPOSITORY env variable."
	exit 1
fi
echo "Repository $GITHUB_REPOSITORY"

# Prepare the headers
API_VERSION=v3
AUTH_HEADER="Authorization: token ${GITHUB_TOKEN}"
HEADER="Accept: application/vnd.github.${API_VERSION}+json application/vnd.github.shadow-cat-preview"
HEADER="${HEADER}; application/vnd.github.antiope-preview+json"

BASE=https://api.github.com
REPO_URL="${BASE}/repos/${GITHUB_REPOSITORY}"
PULLS_URL=$REPO_URL/pulls
METHOD=POST


# We assume we are on the branch releases are created from.
# We also assume that we are not already on a tag

GIT_DESCRIBE=$(git describe --tags)
GIT_TAG=$(echo "$GIT_DESCRIBE" | cut -d"-" -f1)
GIT_COMMIT_COUNT=$(echo "$GIT_DESCRIBE" | cut -d"-" -f2)

echo "Found tag $GIT_TAG with $GIT_COMMIT_COUNT commits since."

if [[ "$GIT_COMMIT_COUNT" == "0" ]]; then
  echo "No commits since last release. Aborting".
  exit 78
fi

SOURCE="prepare-release"
TARGET="master"


git checkout -b "$SOURCE"

# determine next version

DATE=$(date +"%Y%m%d%H%M%S")
VERSION="0.1.${DATE}"

# version bump
export MAVEN_OPTS=-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
./mvnw versions:set versions:commit -B -DnewVersion="${VERSION}"

# TODO: generate changelog

git config --global user.email "${GITHUB_ACTOR}@github-actions.com"
git config --global user.name "$GITHUB_ACTOR"

git commit -am"prepare release ${VERSION}"
git tag "${VERSION}"

git push --set-upstream origin "$SOURCE" --force
git push --set-upstream origin --tags --force

# Check if the branch already has a pull request open

DATA="{\"base\":\"${TARGET}\", \"head\":\"${SOURCE}\"}"
RESPONSE=$(curl -sSL -H "${AUTH_HEADER}" -H "${HEADER}" --user "${GITHUB_ACTOR}" -X GET --data "${DATA}" ${PULLS_URL})
PR=$(echo "${RESPONSE}" | jq --raw-output '.[] | .head.ref')
echo "Response ref: ${PR}"

# Option 1: The pull request is already open
if [[ "${PR}" == "${SOURCE}" ]]; then
  echo "Pull request from ${SOURCE} to ${TARGET} is already open. Updating..."

  PR_ID=$(echo "${RESPONSE}" | jq --raw-output '.[] | .number')
  PULLS_URL="${PULLS_URL}/${PR_ID}"

  METHOD=PATCH
fi

TITLE="Prepare release ${VERSION}"
BODY="This is an automated pull request to prepare release ${VERSION}"

# Post the pull request
DATA="{\"title\":\"${TITLE}\", \"body\": \"${BODY}\", \"base\":\"${TARGET}\", \"head\":\"${SOURCE}\", \"draft\": true}"
echo "curl --user ${GITHUB_ACTOR} -X ${METHOD} --data ${DATA} ${PULLS_URL}"
curl -sSL -H "${AUTH_HEADER}" -H "${HEADER}" --user "${GITHUB_ACTOR}" -X "${METHOD}" --data "${DATA}" "${PULLS_URL}"
echo $?