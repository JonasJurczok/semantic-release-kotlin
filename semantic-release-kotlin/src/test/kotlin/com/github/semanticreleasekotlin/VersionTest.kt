package com.github.semanticreleasekotlin

import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import java.io.File

/**
 * Testmap
 * - 2 version, all released
 * - 2 version, multiline, all released
 * - 2 version, multiline, unreleased changes (multi and single)
 */

class VersionTest : FeatureSpec({
    feature("Versions should be read correctly") {
        scenario("Generate next version from git tree") {

            // Hack for testing
            Changelog.command = "./git.sh "

            val currentVersion = Version(0,1,0);
            val log = Changelog.fromGit(dir = File("src/test/git/2v_unreleased"), from = currentVersion);

            log.versions().size.shouldBe(2)
            log.hasUnreleasedChanges().shouldBeTrue()

            val optionalRelease = log.newRelease()
            log.versions.size.shouldBe(3)

            optionalRelease.isPresent.shouldBeTrue()

            val newVersion = optionalRelease.get()

            newVersion.major.shouldBe(0)
            newVersion.minor.shouldBe(2)
            newVersion.patch.shouldBe(1)

            newVersion.changes(Category.FEATURE).size.shouldBe(2)

            val changes = newVersion.changes(Category.BUGFIX);
            changes.size.shouldBe(2);


            val change = changes.get(0)
            change.category.shouldBe(Category.BUGFIX);
            change.description.shouldBe("second bugfix");
        }
    }
})