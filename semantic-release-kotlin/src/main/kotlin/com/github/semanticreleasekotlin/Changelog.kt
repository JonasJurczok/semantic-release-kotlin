package com.github.semanticreleasekotlin

import com.github.semanticreleasekotlin.tools.Logger
import com.github.semanticreleasekotlin.tools.OS
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
    val versions: MutableMap<String, Version> = HashMap()
    val unreleased: MutableList<ChangelogEntry> = mutableListOf()
    var latest: Optional<Version> = Optional.empty()

    fun hasUnreleasedChanges(): Boolean {
        return unreleased.size > 0
    }

    fun newRelease(): Optional<Version> {
        val changeType = unreleased.map { entry -> entry.category }
                .map { category -> category.changeType }
                .maxBy { type -> type.ordinal }

        if (changeType == null) {
            Logger.log("No unreleased changes found.")
            return Optional.empty()
        }

        lateinit var version: Version
        if (latest.isPresent) {
            version = incrementVersion(latest.get(), changeType)
        } else {
            Logger.log("No previous version found. Starting with [0.1.0]")
            version = Version(0,1,0)
        }

        unreleased.forEach { entry ->
            Logger.log("Adding entry [$entry] to version [${version.asString()}].")
            version.addChange(entry)
        }
        unreleased.clear()

        versions.put(version.asString(), version)

        return Optional.of(version)
    }

    private fun incrementVersion(version: Version, changeType: ChangeType): Version {
        var major = version.major
        var minor = version.minor
        var patch = version.patch

        if (changeType.equals(ChangeType.MAJOR)) {
            major += 1
        } else if (changeType.equals(ChangeType.MINOR)) {
            minor += 1
        } else if (changeType.equals(ChangeType.PATCH)) {
            patch += 1
        }

        Logger.log("Incremented version from [${version.asString()}] to [$major.$minor.$patch].")

        return Version(major, minor, patch)
    }

    fun versions(): Collection<Version> {
        return versions.values
    }

    fun addUnreleasedChange(change: ChangelogEntry) {
        unreleased.add(change)
    }

    private fun addVersion(version: Version) {
        versions.put(version.asString(), version);

        if (latest.isPresent()) {
            if (latest.get().compareTo(version) < 0) {
                latest = Optional.of(version)
            }
        } else {
            latest = Optional.of(version)
        }
    }

    companion object {
        internal var command = "git log --format=\"%d||%B\" --reverse "

        fun fromGit(dir: File, from: Version? = null, to: Version? = null): Changelog {
            /**
             * Get list of tags
             * find tag == version
             * iterate through tags until head reached
             */

            Logger.log("Starting parsing git tree ${dir} with version from $from to $to.")

            var command = this.command

            if (from != null) {
                command += "${from.asString()}.."
            }
            command += if (to != null) to.asString() else "HEAD"

            val regex = Regex("(\\(tag: (.*)\\))?(\\|\\|)(.*)")

            val changelog = Changelog()
            // iterate through the log in chronological orders
            val changes: MutableList<ChangelogEntry> = mutableListOf()

            OS.execute(dir, command) { sequence: Sequence<String> ->

                sequence.forEach { line: String ->

                    val matchResult = regex.find(line)

                    var message = line
                    var tag: String? = null
                    if (matchResult != null) {
                        tag = matchResult.groups[2]?.value
                        message = matchResult.groups[4]?.value ?: ""
                    }
                    Logger.log("Processing message [$message] and tag [$tag].")

                    val entry = ChangelogEntry.fromString(message)

                    entry.ifPresent { e -> changes.add(e) }

                    if (tag != null) {
                        // we have a tag, start a new version
                        Logger.log("Found tag ${tag}")

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
    }

}
