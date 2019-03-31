package com.github.semanticreleasekotlin

import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.specs.FeatureSpec

class Test : FeatureSpec( {
    feature("Version generation") {
        // Testmap
        // - from should be transported
        // - to should not generate new version



    }

    feature("Changelog generation") {
        // no new commits -> no changelog
        // for each mode, verify changelog
        // changelog to custom location


    }
})