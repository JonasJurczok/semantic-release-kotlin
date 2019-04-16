# pull-requests
workflow "Pull Request" {
  on = "pull_request"
  resolves = ["Run PR tests"]
}

action "Check PR state" {
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
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
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
  args = "branch master"
}

action "Tests" {
  uses = "./.github/docker"
  args = ".github/run-tests.sh"
  needs = ["Check for master"]
}

action "Prepare release PR" {
  uses = "./.github/docker"
  needs = ["Tests"]
  secrets = ["GITHUB_TOKEN"]
  args = ".github/prepare-release.sh"
}


# releases
workflow "Releases" {
  resolves = ["Release"]
  on = "pull_request"
}

action "Check is merged" {
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
  args = "merged"
}

action "Check for release branch" {
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
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


