package com.github.semanticreleasekotlin

import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.types.shouldNotBeNull
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import java.io.File

/**
 * Testmap
 * - 2 version, all released
 * - 2 version, multiline, all released
 * - 2 version, multiline, unreleased changes (multi and single)
 */

class VersionTest : FeatureSpec() {
    override fun beforeTest(testCase: TestCase) {
        Changelog.overwriteCommand("./git.sh ")
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        Changelog.resetCommand()
    }

    init {
        feature("Versions should be read correctly") {
            scenario("Generate next version from git tree") {

                val log = Changelog.fromGit(dir = File("../test/git/2v_unreleased"))

                log.versions().size.shouldBe(2)
                log.hasUnreleasedChanges().shouldBeTrue()

                val newRelease: Version = log.newRelease()!!
                log.versions().size.shouldBe(3)

                newRelease.shouldNotBeNull()

                newRelease.major.shouldBe(0)
                newRelease.minor.shouldBe(3)
                newRelease.patch.shouldBe(0)

                newRelease.changes(Category.FEATURE).size.shouldBe(2)

                val changes = newRelease.changes(Category.BUGFIX)
                changes.size.shouldBe(2)

                val change = changes[0]
                change.category.shouldBe(Category.BUGFIX)
                change.description.shouldBe("second bugfix")
            }

            scenario("no unreleased changes should not lead to new version.") {
                val log = Changelog.fromGit(dir = File("../test/git/2v_released"))

                log.hasUnreleasedChanges().shouldBeFalse()

                log.versions().size.shouldBe(2)
            }


            scenario("Check actual git parsing works") {
                Changelog.resetCommand()
                val log = Changelog.fromGit(dir = File("."))

                log.versions().size.shouldBeGreaterThan(1)
            }
        }
    }
}