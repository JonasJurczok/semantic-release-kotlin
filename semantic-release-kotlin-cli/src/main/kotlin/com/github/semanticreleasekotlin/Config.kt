package com.github.semanticreleasekotlin

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class Config(parser: ArgParser) {
    val from by parser.storing("--from", help = "The tag the version collection should start from. --from=0.1.0 excludes 0.1.0 and starts with the next commit.").default<String?>(null)

    val to by parser.storing("--to", help = "The tag the version collection should end at. --to=0.1.0 includes 0.1.0 but not the next commit.").default<String?>(null)

    val generateChangelog by parser.flagging("--generate-changelog", help = "Generate a changelog. Defaults to false").default(false)

    val ordering by parser.mapping(
            "--ascending" to Printer.Ordering.ASCENDING,
            "--descending" to Printer.Ordering.DESCENDING,
            help = "Ordering of the changelog entries").default(Printer.Ordering.ASCENDING)

    val verbose by parser.flagging("-v", "--verbose", help="Enable verbose output.")

    /*

    val output by parser.storing("--output", help = "Destination of the changelog file. Defaults to CHANGELOG.md").default("CHANGELOG.md")

    val format by parser.storing("--format", help = "The format used for the genrated changelog. Can be either an existing format (TODO Link), a path to a file containing a template string, or a string interpreted as a template (TODO link).").default("MARKDOWN")

    val append by parser.mapping("--append-top" to Append.TOP, "--append-bottom" to Append.BOTTOM, help = "Defines where the changelog should be appended if the output file already exists. Defaults to TOP").default(Append.TOP)
*/
    val directory by parser.positional("Path to the directory where the versions should be generated.")



    /*enum class Append {
        TOP,
        BOTTOM
    }*/
}