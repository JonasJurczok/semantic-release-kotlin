package com.github.semanticreleasekotlin

import com.github.semanticreleasekotlin.Changelog.Companion.ParserState.LAST_VERSION
import com.github.semanticreleasekotlin.Changelog.Companion.ParserState.NEW_VERSION
import com.github.semanticreleasekotlin.tools.OS
import org.slf4j.LoggerFactory
import java.io.File
import java.util.Optional


/**
 * Stores all changes in a given context.
 *
 * Changelog
 * - version 1
 *   - date
 *   - bugfixes
 *   - features
 * - version 2
 *   - date
 *   - bugfixes
 *   - features
 * - unreleased
 *   - bugfixes
 *   - features
 */
class Changelog {
    private val versions: MutableMap<String, Version> = HashMap()
    private val unreleased: MutableList<ChangelogEntry> = mutableListOf()
    private var latest: Optional<Version> = Optional.empty()

    fun hasUnreleasedChanges(): Boolean {
        return unreleased.size > 0
    }

    fun newRelease(): Optional<Version> {
        val changeType = unreleased.map { entry -> entry.category }
                .map { category -> category.changeType }
                .minBy { type -> type.ordinal }

        if (changeType == null) {
            logger.info("No unreleased changes found.")
            return Optional.empty()
        }

        lateinit var version: Version
        version = if (latest.isPresent) {
            incrementVersion(latest.get(), changeType)
        } else {
            logger.info("No previous version found. Starting with [0.1.0]")
            Version(0,1,0)
        }

        unreleased.forEach { entry ->
            logger.info("Adding entry [{}] to version [{}].", entry, version.asString())
            version.addChange(entry)
        }
        unreleased.clear()

        versions[version.asString()] = version

        return Optional.of(version)
    }

    private fun incrementVersion(version: Version, changeType: ChangeType): Version {
        var major = version.major
        var minor = version.minor
        var patch = version.patch

        when (changeType) {
            ChangeType.MAJOR -> major += 1
            ChangeType.MINOR -> minor += 1
            ChangeType.PATCH -> patch += 1
        }

        logger.info("Incremented version from [${version.asString()}] to [$major.$minor.$patch].")

        return Version(major, minor, patch)
    }

    fun versions(): Collection<Version> {
        return versions.values
    }

    fun addUnreleasedChange(change: ChangelogEntry) {
        unreleased.add(change)
    }

    private fun addVersion(version: Version) {
        versions[version.asString()] = version

        if (latest.isPresent) {
            if (latest.get() < version) {
                latest = Optional.of(version)
            }
        } else {
            latest = Optional.of(version)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Changelog::class.java)
        private val command = "git log --format=\"%d||%B\" --reverse "
        private var overwriteCommand: String? = null
        private var parserState: ParserState = LAST_VERSION

        private enum class ParserState {
            LAST_VERSION,
            NEW_VERSION
        }

        fun fromGit(dir: File, from: Version? = null, to: Version? = null): Changelog {
            /**
             * Get list of tags
             * find tag == version
             * iterate through tags until head reached
             */

            logger.info("Starting parsing git tree [{}] with version from [{}] to [{}].", dir, from, to)

            var command = overwriteCommand ?: this.command

            if (from != null) {
                command += "${from.asString()}.."
            }
            command += to?.asString() ?: "HEAD"

            val regex = Regex("(\\(tag: (.*)\\))?(\\|\\|)?(.*)")

            val changelog = Changelog()
            // iterate through the log in chronological orders
            val changes: MutableList<ChangelogEntry> = mutableListOf()

            OS.execute(dir, command) { sequence: Sequence<String> ->

                sequence.filter { line -> line.isNotBlank() }.forEach { line: String ->

                    val matchResult = regex.find(line)

                    val message = matchResult?.groups?.get(4)?.value ?: ""
                    val tag = matchResult?.groups?.get(2)?.value
                    val isNewCommit = matchResult?.groups?.get(3) != null
                    if (tag != null) {
                        parserState = LAST_VERSION
                    } else if (isNewCommit) {
                        parserState = NEW_VERSION
                    }
                    logger.debug("Processing message [{}] and tag [{}] for new commit [{}] and parser state [{}].", message, tag, isNewCommit, parserState)

                    val entry = ChangelogEntry.fromString(message)

                    entry.ifPresent {
                        when(parserState) {
                            LAST_VERSION -> {
                                val latest = changelog.latest
                                latest.ifPresentOrElse( {
                                    version -> version.addChange(entry.get())
                                }, {
                                    changes.add(entry.get())
                                })
                            }
                            NEW_VERSION -> changes.add(entry.get())
                        }
                    }

                    if (tag != null) {
                        // we have a tag, start a new version
                        logger.debug("Found tag [{}].", tag)

                        val version = Version.fromString(tag)

                        changes.forEach(version::addChange)

                        changes.clear()

                        changelog.addVersion(version)
                    }
                }
                changes.forEach(changelog::addUnreleasedChange)

            }

            return changelog
        }

        fun overwriteCommand(command: String) {
            overwriteCommand = command
        }

        fun resetCommand() {
            overwriteCommand = null
        }
    }

}
