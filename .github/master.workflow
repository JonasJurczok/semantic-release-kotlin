# releases
workflow "Releases" {
  resolves = ["Release"]
  on = "push"
}

action "Check for master" {
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
  args = "branch master"
}

action "Check not tag" {
  uses = "actions/bin/filter@d820d56839906464fb7a57d1b4e1741cf5183efa"
  args = "not tag"
  needs = ["Check for master"]
}

action "Tests" {
  uses = "./.github/docker"
  args = ".github/run-tests.sh"
  needs = ["Check not tag"]
}

action "Release" {
  uses = "./.github/docker"
  args = ".github/release.sh"
  needs = ["Tests"]
  secrets = ["GITHUB_TOKEN"]
}