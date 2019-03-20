#!/bin/bash

set -eu

git branch

./mvnw -q clean verify

# TODO: coveralls support