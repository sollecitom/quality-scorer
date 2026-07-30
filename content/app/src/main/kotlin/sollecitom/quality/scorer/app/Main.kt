package sollecitom.quality.scorer.app

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.system.exitProcess

/**
 * CLI entry point: `quality-scorer --project <dir> [--coverage <kover.xml>] [--out <file>]`.
 * Prints (or writes) the reward JSON. Exit code 0 on success, 2 on a usage error.
 */
fun main(args: Array<String>) {
    val options = args.toList().zipWithNext().associate { it.first to it.second }
    val project = options["--project"] ?: run {
        System.err.println("usage: quality-scorer --project <dir> [--coverage <kover.xml>] [--out <file>]")
        exitProcess(2)
    }
    val coverage = options["--coverage"]?.let(::File)
    val report = runBlocking { Grader().grade(File(project), coverage) }
    val json = report.toJson()
    val out = options["--out"]
    if (out != null) File(out).writeText(json) else println(json)
}
