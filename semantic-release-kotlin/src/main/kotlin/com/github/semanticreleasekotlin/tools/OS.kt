package com.github.semanticreleasekotlin.tools

import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit

object OS {
    private val logger = LoggerFactory.getLogger(OS::class.java)

    fun execute(dir: File, command: String, callback: ((Sequence<String>) -> Unit)) {
        logger.debug("Executing command [$command] in directory [$dir].")
        val arguments = command.split(' ').toTypedArray();
        val process = ProcessBuilder(*arguments)
                .directory(dir)
                .start()
                .also { it.waitFor(10, TimeUnit.SECONDS) }

        if (process.exitValue() != 0) {
            throw Exception(process.errorStream.bufferedReader().readText())
        }

        process.inputStream.bufferedReader().useLines(callback)
    }

}
