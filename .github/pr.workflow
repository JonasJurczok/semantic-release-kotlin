# pull-requests
workflow "Pull Request" {
  on = "pull_request"
  resolves = ["Run Tests"]
}

action "Run Tests" {
  uses = "./.github/docker"
  args = ".github/run-tests.sh"
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