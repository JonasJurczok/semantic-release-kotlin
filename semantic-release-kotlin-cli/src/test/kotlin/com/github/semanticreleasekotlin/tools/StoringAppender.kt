package com.github.semanticreleasekotlin.tools

import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.Core
import org.apache.logging.log4j.core.Filter
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.config.plugins.PluginAttribute
import org.apache.logging.log4j.core.config.plugins.PluginElement
import org.apache.logging.log4j.core.config.plugins.PluginFactory


@Plugin(name = "StoringAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
class StoringAppender protected constructor(name: String, filter: Filter?) : AbstractAppender(name, filter, null, false, null) {

    private val messages = mutableListOf<String>()

    override fun append(event: LogEvent) {
        messages.add(event.message.formattedMessage)
    }

    fun messages(): Sequence<String> {
        return messages.asSequence()
    }

    fun clear() {
        messages.clear()
    }

    companion object {
        @JvmStatic
        @PluginFactory
        fun createAppender(
                @PluginAttribute("name") name: String,
                @PluginElement("Filter") filter: Filter?): StoringAppender {
            return StoringAppender(name, filter)
        }
    }
}