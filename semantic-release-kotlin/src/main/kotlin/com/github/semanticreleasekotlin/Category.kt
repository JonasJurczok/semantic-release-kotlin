package com.github.semanticreleasekotlin

import com.github.semanticreleasekotlin.ChangeType.MINOR
import com.github.semanticreleasekotlin.ChangeType.PATCH

enum class Category(val changeType: ChangeType, val keywords: List<String>) {
    FEATURE(MINOR, listOf("Feature", "Features")),
    BUGFIX(PATCH, listOf("Bugfix", "Bugfixes")),
    DOCS(PATCH, listOf("Doc", "Docs")),
    STYLE(PATCH, listOf("Style", "Styles")),
    REFACTOR(PATCH, listOf("Refactor", "Refactoring")),
    PERFORMANCE(PATCH, listOf("Performance", "Performances", "Perf")),
    TEST(PATCH, listOf("Test", "Tests")),
    CHORE(PATCH, listOf("Chore", "Chores"));


    companion object {
        private lateinit var lookup: MutableMap<String, Category>

        fun getLookupMap(): Map<String, Category> {

            if (::lookup.isInitialized) {
                return lookup
            }

            lookup = mutableMapOf()

            for (value in values()) {
                for (keyword in value.keywords) {
                    lookup.put(keyword, value)
                }
            }

            return lookup
        }
    }
}
