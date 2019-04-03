package com.github.semanticreleasekotlin

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody

fun main(args: Array<String>) = mainBody {

    // semver --from=0.1.0 --to=0.5.0 --generate-changelog --output=./changelog.md --format=[markdown, factorio, ...] --append=[top, bottom] -v <path>
    // 0.5.1

    // format: either predefined type, else if file exists it will be treated as template, else it will be treated as format string

    val config: Config = ArgParser(args).parseInto(::Config)

    CLI(config).run(System.out)
}