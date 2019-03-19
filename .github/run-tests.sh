#!/bin/bash

set -eu

git branch

./mvnw clean verify

# TODO: coveralls support