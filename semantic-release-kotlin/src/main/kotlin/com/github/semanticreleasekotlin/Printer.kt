package com.github.semanticreleasekotlin

import java.io.File

class Printer {
    companion object {
        fun printChangelog(log: Changelog) {
            val changelog = File("CHANGELOG.md")

            changelog.writeText("CHANGELOG")
            changelog.appendText("\n")
            changelog.appendText("=========")
            changelog.appendText("\n")

            log.versions().sorted().forEach {

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