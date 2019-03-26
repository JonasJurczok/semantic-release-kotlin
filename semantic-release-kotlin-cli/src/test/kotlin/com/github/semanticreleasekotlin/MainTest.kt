package com.github.semanticreleasekotlin

import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.specs.FeatureSpec

class Test : FeatureSpec( {
    feature("Test1") {
        scenario("Test") {
            "Test".shouldStartWith("Te")
        }
    }
})