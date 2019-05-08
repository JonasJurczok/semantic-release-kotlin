package com.github.semanticreleasekotlin

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.slf4j.LoggerFactory
import java.io.File
import java.io.PrintStream

class CLI(private val config: Config) {

    fun run(outputStream: PrintStream) {
        if (config.verbose) {
            Configurator.setLevel("com.github.semanticreleasekotlin", Level.TRACE)
        }

        logger.trace("Starting processing in CLI.")
        logger.trace("Provided config is [{}].", config)
        val dir = File(config.directory)

        val from = if (config.from != null) Version.fromString(config.from!!) else null
        val to = if (config.to != null) Version.fromString(config.to!!) else null

        val log = Changelog.fromGit(dir, from, to)

        if (log.hasUnreleasedChanges()) {
            val release = log.newRelease()
            release?.let {
                outputStream.println(release.asString())
            }
        } else {
            logger.info("No new version to create. Nothing to do.")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CLI::class.java)
    }
}