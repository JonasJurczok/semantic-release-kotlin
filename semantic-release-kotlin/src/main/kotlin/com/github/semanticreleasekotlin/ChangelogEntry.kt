package com.github.semanticreleasekotlin

import org.slf4j.LoggerFactory
import java.util.Optional

/**
 * A single entry in a changelog version
 */
data class ChangelogEntry(val category: Category, val description: String) {
    companion object {

        private val logger = LoggerFactory.getLogger(ChangelogEntry::class.java)

        fun fromString(input: String): Optional<ChangelogEntry> {

            val keyword = input.trim().substringBefore(":")

            val category = Category.getLookupMap()[keyword]

            if (category == null) {
                logger.debug("Could not find category for input [$input].")
                return Optional.empty()
            }

            val description = input.substringAfter(":")
            val entry = ChangelogEntry(category, description.trim())

            logger.info("Created changelog entry [{}].", entry)

            return Optional.of(entry)
        }
    }

}
