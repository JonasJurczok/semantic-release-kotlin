package com.github.semanticreleasekotlin.tools

import java.io.File
import java.util.concurrent.TimeUnit

object OS {
    fun execute(dir: File, command: String, callback: ((Sequence<String>) -> Unit)) {
        Logger.log("Executing command [$command] in directory [$dir].")
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
