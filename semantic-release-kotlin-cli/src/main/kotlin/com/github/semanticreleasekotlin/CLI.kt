package com.github.semanticreleasekotlin

import org.slf4j.LoggerFactory
import java.io.File
import java.io.PrintStream

class CLI(val config: Config) {

    fun run(outputStream: PrintStream) {
        logger.trace("Starting processing in CLI.")
        logger.trace("Provided config is [{}].", config)
        val dir = File(config.directory)

        val from = if (config.from != null) Version.fromString(config.from!!) else null
        val to = if (config.to != null) Version.fromString(config.to!!) else null

        Changelog.fromGit(dir, from, to)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CLI::class.java)
    }
}