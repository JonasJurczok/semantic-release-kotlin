package com.github.semanticreleasekotlin

import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.matchers.numerics.shouldBeLessThan
import io.kotlintest.matchers.string.shouldNotContain
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import com.github.semanticreleasekotlin.Printer.Ordering.ASCENDING
import com.github.semanticreleasekotlin.Printer.Ordering.DESCENDING
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import java.io.File

class ChangelogPrinterTest: FeatureSpec() {
    override fun beforeTest(testCase: TestCase) {
        Changelog.overwriteCommand("./git.sh ")
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        Changelog.resetCommand()
        File("CHANGELOG.md").delete()
    }

    /**
     * TESTMAP
     * - print changelog
     */
    init {
        feature("Print changelog") {
            scenario("Printing complete changelog descending should work.") {
                val log = Changelog.fromGit(dir = File("../test/git/2v_released"))

                Printer.printChangelog(log)

                val file = File("CHANGELOG.md")
                val content = file.readText()

                // Testing against fixed string to also cover formatting and blank lines.
                content.shouldBe("CHANGELOG\n" +
                        "=========\n" +
                        "\n" +
                        "### 0.1.0\n" +
                        "\n" +
                        "### 0.2.0\n" +
                        "\n" +
                        "#### FEATURE:\n" +
                        "* first feature\n" +
                        "\n" +
                        "#### BUGFIX:\n" +
                        "* first bugfix\n")
            }

            scenario("If output file exists it should be overwritten.") {
                val file = File("CHANGELOG.md")
                file.writeText("PrinterTest")

                val log = Changelog.fromGit(dir = File("../test/git/2v_released"))

                Printer.printChangelog(log)

                File("CHANGELOG.md").readText().shouldNotContain("PrinterTest")
            }
        }

        feature("Changelog ordering") {
            scenario("No ordering should mean ascending") {
                val log = Changelog.fromGit(dir = File("../test/git/2v_released"))

                Printer.printChangelog(log)

                val file = File("CHANGELOG.md")
                val content = file.readText()

                val first = content.indexOf("0.1.0")
                val second = content.indexOf("0.2.0")

                first.shouldBeLessThan(second)
            }

            scenario("Explicit ascending should work") {
                val log = Changelog.fromGit(dir = File("../test/git/2v_released"))

                Printer.printChangelog(log, ordering = ASCENDING)

                val file = File("CHANGELOG.md")
                val content = file.readText()

                val first = content.indexOf("0.1.0")
                val second = content.indexOf("0.2.0")

                first.shouldBeLessThan(second)
            }

            scenario("Explicit decending should work") {
                val log = Changelog.fromGit(dir = File("../test/git/2v_released"))

                Printer.printChangelog(log, ordering = DESCENDING)

                val file = File("CHANGELOG.md")
                val content = file.readText()

                val first = content.indexOf("0.1.0")
                val second = content.indexOf("0.2.0")

                first.shouldBeGreaterThan(second)
            }
        }
    }
}