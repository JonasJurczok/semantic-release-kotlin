#!/bin/bash

set -eu

./mvnw dependency:resolve

git branch

./mvnw clean verify

# TODO: coveralls support