package com.github.semanticreleasekotlin

import com.github.semanticreleasekotlin.tools.NoopOutputStream
import com.github.semanticreleasekotlin.tools.OS
import com.github.semanticreleasekotlin.tools.StoringAppender
import com.xenomachina.argparser.ArgParser
import io.kotlintest.TestCase
import io.kotlintest.TestCaseOrder
import io.kotlintest.TestResult
import io.kotlintest.matchers.file.shouldExist
import io.kotlintest.matchers.file.shouldNotExist
import io.kotlintest.matchers.numerics.shouldBeGreaterThan
import io.kotlintest.matchers.string.shouldNotBeBlank
import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.Configurator
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.file.Paths

class CLITest : FeatureSpec() {

    override fun testCaseOrder(): TestCaseOrder? = TestCaseOrder.Random

    override fun beforeTest(testCase: TestCase) {
        Changelog.overwriteCommand("./git.sh ")
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        // reset log level
        Configurator.setLevel("com.github.semanticreleasekotlin", Level.WARN)

        // reset appender
        val context = LogManager.getContext(false) as LoggerContext
        val appender = context.configuration.getAppender<StoringAppender>("StoringAppender")

        appender.clear()

        Changelog.resetCommand()

        // delete Changelog file
        File("CHANGELOG.md").delete()
    }

    init {
        feature("verbose output.") {
            scenario("-v flag should turn on verbose output") {

                val dir = "../test/git/2v_released"

                val args = arrayOf("--from=0.1.0", "-v", dir)
                val config: Config = ArgParser(args).parseInto(::Config)
                CLI(config).run(PrintStream(NoopOutputStream()))

                val context = LogManager.getContext(false) as LoggerContext
                val appender = context.configuration.getAppender<StoringAppender>("StoringAppender")
                appender.messages().count().shouldBeGreaterThan(0)
            }

            scenario("no -v flag should turn off verbose output") {
                val dir = "../test/git/2v_released"

                val args = arrayOf("--from=0.1.0", dir)
                val config: Config = ArgParser(args).parseInto(::Config)
                CLI(config).run(PrintStream(NoopOutputStream()))

                val context = LogManager.getContext(false) as LoggerContext
                val appender = context.configuration.getAppender<StoringAppender>("StoringAppender")
                appender.messages().count().shouldBe(0)
            }
        }

        feature("Version generation") {
            scenario("If all changes are released no new version should be generated.") {
                val dir = "../test/git/2v_released"

                val args = arrayOf("--from=0.1.0", "-v", dir)
                val config: Config = ArgParser(args).parseInto(::Config)

                val byteOutput = ByteArrayOutputStream()

                CLI(config).run(PrintStream(byteOutput))

                val reader = BufferedReader(InputStreamReader(ByteArrayInputStream(byteOutput.toByteArray())))
                reader.lines().count().shouldBe(0)

                val context = LogManager.getContext(false) as LoggerContext
                val appender = context.configuration.getAppender<StoringAppender>("StoringAppender")
                appender.messages().filter { line -> line.contains("Nothing to do") }.count().shouldBe(1)
            }

            scenario("New version should be printed to output.") {
                val dir = "../test/git/2v_unreleased"

                val args = arrayOf("--from=0.1.0", dir)
                val config: Config = ArgParser(args).parseInto(::Config)

                val byteOutput = ByteArrayOutputStream()

                CLI(config).run(PrintStream(byteOutput))

                val reader = BufferedReader(InputStreamReader(ByteArrayInputStream(byteOutput.toByteArray())))
                val line = reader.readLine()
                line.shouldNotBeBlank()
                reader.lines().count().shouldBe(0)
                line.shouldBe("0.3.0")

                val context = LogManager.getContext(false) as LoggerContext
                val appender = context.configuration.getAppender<StoringAppender>("StoringAppender")
                appender.messages().count().shouldBe(0)
            }
        }

        feature("Changelog generation") {
            /**
             * no new commits -> no changelog
             * for each mode, verify changelog
             * changelog to custom location
             */

            scenario("Without flag, no changelog is generated") {
                val dir = "../test/git/2v_released"

                val args = arrayOf(dir)
                val config: Config = ArgParser(args).parseInto(::Config)

                CLI(config).run(System.out)

                File("CHANGELOG.md").shouldNotExist()

            }

            scenario("Full changelog generated") {
                val dir = "../test/git/2v_released"

                val args = arrayOf("--generate-changelog", dir)
                val config: Config = ArgParser(args).parseInto(::Config)

                CLI(config).run(System.out)

                File("CHANGELOG.md").shouldExist()
            }

            scenario("Descending ordering") {
                // ascending is the default case and can be ignored
                val dir = "../test/git/2v_released"

                val args = arrayOf("--generate-changelog", dir)
                val config: Config = ArgParser(args).parseInto(::Config)

                CLI(config).run(System.out)
            }
        }
    }
}