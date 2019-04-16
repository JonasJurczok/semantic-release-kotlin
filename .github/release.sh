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
AUTH_HEADER="Authorization: token ${GITHUB_TOKEN}"
API_VERSION=v3
API_HEADER="Accept: application/vnd.github.${API_VERSION}+json"

# Tag is checked before

# build release
# TODO: upload to maven central
VERSION=$(./mvnw org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev "Download" -E  | grep -Ev "INFO")
./mvnw clean verify

git config --global user.email "${GITHUB_ACTOR}@github-actions.com"
git config --global user.name "$GITHUB_ACTOR"

git tag "$VERSION"
git push --set-upstream origin --tags

# create release
URL="https://api.github.com/repos/${GITHUB_REPOSITORY}/releases"
RESPONSE=$(curl -sSL -XPOST -H "$AUTH_HEADER" -H "$API_HEADER" "$URL" -d "{ \"tag_name\": \"${VERSION}\"}")

RELEASE_TAG=$(jq --raw-output '.tag_name' <<< "$RESPONSE")

if [[ "$VERSION" != "$RELEASE_TAG" ]]; then
    echo "Creating release failed."
    exit -1
fi

echo "Release created"

RELEASE_ID=$(jq --raw-output '.id' <<< "$RESPONSE")

echo "Found release id $RELEASE_ID"

# For each matching file
for file in $(find . -name sem*.jar); do

    echo "Processing file ${file}"

    FILENAME=$(basename ${file})
    UPLOAD_URL="https://uploads.github.com/repos/${GITHUB_REPOSITORY}/releases/${RELEASE_ID}/assets?name=${FILENAME}"
    echo "$UPLOAD_URL"

    # Upload the file
    curl \
        -sSL \
        -XPOST \
        -H "${AUTH_HEADER}" \
        --upload-file "${file}" \
        --header "Content-Type:application/octet-stream" \
        "${UPLOAD_URL}"
done