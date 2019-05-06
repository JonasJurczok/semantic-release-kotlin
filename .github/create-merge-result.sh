#!/bin/bash

set -eu

ACTION=$(jq --raw-output .action "$GITHUB_EVENT_PATH")
MERGED=$(jq --raw-output .pull_request.merged "$GITHUB_EVENT_PATH")

echo "Found action $ACTION and merged $MERGED"

if [[ "$ACTION" != "closed" ]] && [[ "$MERGED" == "false" ]]; then
  HEAD=$(jq --raw-output .pull_request.head.ref "$GITHUB_EVENT_PATH")
  BASE=$(jq --raw-output .pull_request.base.ref "$GITHUB_EVENT_PATH")

  echo "Creating merge result for head $HEAD and base $BASE"

  echo "event content:"
  cat "$GITHUB_EVENT_PATH"

  echo "git remotes"
  git remote -v

  git checkout "$BASE"
  git checkout "$HEAD"
  git merge "$BASE"
fi
