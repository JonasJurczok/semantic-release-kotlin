# pull-requests
workflow "Pull Request" {
  on = "pull_request"
  resolves = ["Run PR tests"]
}

action "Create merge result" {
  uses = "./.github/docker"
  args = ".github/create-merge-result.sh"
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
  resolves = ["push"]
  on = "push"
}

action "Check for master" {
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
  args = "branch master"
}

action "Check is not tag" {
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
  args = "not tag"
  needs = ["Check for master"]
}

action "Tests" {
  uses = "./.github/docker"
  args = ".github/run-tests.sh"
  secrets = ["COVERALLS_TOKEN"]
  needs = ["Check is not tag"]
}

action "Prepare release PR" {
  uses = "./.github/docker"
  needs = ["Tests"]
  secrets = ["GITHUB_TOKEN"]
  args = ".github/prepare-release.sh"
}

action "push" {
  uses = "ludeeus/action-push@master"
  env = {
    ACTION_MAIL = "octocat@octocat.org"
    ACTION_NAME = "octocat"
    ACTION_BRANCH = "prepare-release"
    ACTION_MESSAGE = "Action commit"
  }
  needs = ["Prepare release PR"]
  secrets = ["GITHUB_TOKEN"]
}

# releases
workflow "Releases" {
  resolves = ["Release"]
  on = "push"
}

action "Check is tag" {
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
  needs = ["Check for master"]
  args = "tag"
}

action "Verify release build" {
  uses = "./.github/docker"
  args = ".github/run-tests.sh"
  secrets = ["COVERALLS_TOKEN"]
  needs = ["Check is tag"]
}

action "Release" {
  uses = "./.github/docker"
  needs = ["Verify release build"]
  args = ".github/release.sh"
  secrets = ["GITHUB_TOKEN"]
}


