package com.github.semanticreleasekotlin

import com.github.semanticreleasekotlin.Printer.Ordering.ASCENDING
import com.github.semanticreleasekotlin.Printer.Ordering.DESCENDING
import java.io.File

class Printer {

    enum class Ordering {
        ASCENDING,
        DESCENDING
    }

    companion object {
        fun printChangelog(log: Changelog, ordering: Ordering = ASCENDING) {
            val changelog = File("CHANGELOG.md")

            changelog.writeText("CHANGELOG")
            changelog.appendText("\n")
            changelog.appendText("=========")
            changelog.appendText("\n")

            // TODO: is this idiomatic?
            with(
                    when(ordering) {
                        ASCENDING -> log.versions().sorted()
                        DESCENDING -> log.versions().sortedDescending()
                    }
            ) {
                this.forEach {

                    changelog.appendText("\n")
                    changelog.appendText("### ${it.asString()}")
                    changelog.appendText("\n")

                    it.changes().toSortedMap().forEach { category, changes ->
                        changelog.appendText("\n")
                        changelog.appendText("#### $category:")
                        changelog.appendText("\n")

                        changes.forEach {
                            entry -> changelog.appendText("* ${entry.description}\n")
                        }
                    }
                }
            }


        }
    }
}