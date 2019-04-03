package com.github.semanticreleasekotlin

import io.kotlintest.shouldBe
import io.kotlintest.specs.FeatureSpec
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream

class CLITest : FeatureSpec( {
    feature("Version generation") {
        /** Testmap
         * - if all changes are released nothing should be created
         * - to should not generate new version
         */
        scenario("If all changes are released no new version should be generated.") {
            val dir = "../test/git/2v_released"

            val oldOut = System.out
            val oldErr = System.err

            val outStream = ByteArrayOutputStream()
            val out = outStream
            val errStream = ByteArrayOutputStream()
            val err = errStream
            System.setOut(PrintStream(outStream))
            System.setErr(PrintStream(errStream))

            main(arrayOf("--from=0.1.0", dir))

            out.toString().trim().shouldBe("")

            err.toString().contains("Nothing to do").shouldBe(true)
            System.setOut(oldOut)
            System.setErr(oldErr)
        }

        scenario("Check to is transported to library.") {
            val from = Version(0, 1, 0)
            val to = Version(0, 2, 0)
            val dir = File("Test")

            mockkObject(Changelog.Companion)

            every { Changelog.fromGit(dir, from = from, to = to) } returns mockk()

            main(arrayOf("--from=0.1.0", "--to=0.2.0", "Test"))

            verify(exactly = 1) { Changelog.fromGit(dir, from = from, to = to) }

            confirmVerified(Changelog)

            unmockkObject(Changelog.Companion)
        }

        scenario("Print new version to stdout") {

        }

    }

    feature("Changelog generation") {
        /**
         * no new commits -> no changelog
         * for each mode, verify changelog
         * changelog to custom location
         */


    }
})