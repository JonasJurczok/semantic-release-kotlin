package com.github.semanticreleasekotlin

import com.github.semanticreleasekotlin.tools.Logger
import java.util.Optional

/**
 * A single entry in a changelog version
 */
data class ChangelogEntry(val category: Category, val description: String) {
    companion object {
        fun fromString(input: String): Optional<ChangelogEntry> {

            val keyword = input.trim().substringBefore(":")

            val category = Category.getLookupMap()[keyword]

            if (category == null) {
                Logger.log("Could not find category for input [$input].")
                return Optional.empty()
            }

            val description = input.substringAfter(":")
            val entry = ChangelogEntry(category, description.trim())

            Logger.log("Created changelogentry $entry")

            return Optional.of(entry)
        }
    }

}
