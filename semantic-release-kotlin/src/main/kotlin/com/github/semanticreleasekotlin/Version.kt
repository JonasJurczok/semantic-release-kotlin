package com.github.semanticreleasekotlin

data class Version (val major: Int = 0, val minor: Int = 0, val patch: Int = 0) : Comparable<Version> {
    private val changes: MutableMap<Category, MutableList<ChangelogEntry>> = mutableMapOf()

    companion object {

        fun fromString(version: String): Version {

            // we assume semantic versioning
            val parts = version.split(".")

            if (parts.size != 3) {
                throw IllegalArgumentException("Could not identify semantic version from [$version]. Three components expected")
            }

            return Version(parts.get(0).toInt(),parts.get(1).toInt(),parts.get(2).toInt())
        }
    }
    fun changes(category: Category): List<ChangelogEntry> {
        return changes.get(category)?.toList() ?: emptyList();
    }

    fun changes(): Map<Category, List<ChangelogEntry>> {
        TODO("implement")
    }

    fun addChange(change: ChangelogEntry) {
        changes.computeIfAbsent(change.category, { mutableListOf()})
                .add(change)
    }

    override fun compareTo(other: Version): Int {
        if (major != other.major) {
            return major - other.major
        }

        if (minor != other.minor) {
            return minor - other.minor
        }

        if (patch != other.patch) {
            return patch - other.patch
        }

        return 0
    }

    fun asString(): String {
        return "$major.$minor.$patch"
    }
}
