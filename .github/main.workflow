# pull-requests
workflow "Pull Request" {
  on = "pull_request"
  resolves = ["Run PR tests"]
}

action "Check PR state" {
  uses = "actions/bin/filter@master"
  args = "not action closed"
}

action "Create merge result" {
  uses = "./.github/docker"
  args = ".github/create-merge-result.sh"
  needs = ["Check PR state"]
}

action "Run PR tests" {
  uses = "./.github/docker"
  needs = ["Create merge result"]
  args = ".github/run-tests.sh"
  secrets = ["COVERALLS_TOKEN"]
}

# pull-request cleanup
workflow "on pull request merge, delete the branch" {
  on = "pull_request"
  resolves = ["branch cleanup"]
}

action "branch cleanup" {
  uses = "jessfraz/branch-cleanup-action@master"
  secrets = ["GITHUB_TOKEN"]
}

# prepare release
workflow "Prepare release" {
  resolves = ["Prepare release PR"]
  on = "push"
}

action "Check for master" {
  uses = "actions/bin/filter@master"
  args = "branch master"
}

action "Prepare release PR" {
  uses = "./.github/docker"
  needs = ["Check for master"]
  secrets = ["GITHUB_TOKEN"]
  args = ".github/prepare-release.sh"
}


# releases
workflow "Releases" {
  resolves = ["Release"]
  on = "pull_request"
}

action "Check is merged" {
  uses = "actions/bin/filter@master"
  args = "merged true"
}

action "Check for release branch" {
  uses = "actions/bin/filter@master"
  args = "branch prepare-release"
  needs = ["Check is merged"]
}

action "Switch to master" {
  uses = "./.github/docker"
  args = ".github/switch-master.sh"
  needs = ["Check for release branch"]
}

action "Verify release build" {
  uses = "./.github/docker"
  args = ".github/run-tests.sh"
  secrets = ["COVERALLS_TOKEN"]
  needs = ["Switch to master"]
}

action "Release" {
  uses = "./.github/docker"
  needs = ["Verify release build"]
  args = ".github/release.sh"
  secrets = ["GITHUB_TOKEN"]
}


