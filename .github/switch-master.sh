#!/bin/bash

set -eu

BRANCH=$(git branch | grep "*")
echo "Working on branch ${BRANCH}"

git checkout master