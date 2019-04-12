package com.github.semanticreleasekotlin.tools

import java.io.OutputStream

class NoopOutputStream : OutputStream() {
    override fun write(b: Int) {
        // ignore that
    }

}