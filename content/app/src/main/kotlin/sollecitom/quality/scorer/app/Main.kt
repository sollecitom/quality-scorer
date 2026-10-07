package sollecitom.quality.scorer.app

import kotlinx.coroutines.runBlocking
import sollecitom.quality.scorer.model.InvalidInputException
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
    val projectDirectory = File(project)
    if (!projectDirectory.isDirectory) {
        System.err.println("--project is not a directory: $project")
        exitProcess(2)
    }
    val coverage = options["--coverage"]?.let(::File)
    val report = try {
        runBlocking { Grader().grade(projectDirectory, coverage) }
    } catch (error: InvalidInputException) {
        System.err.println(error.message)
        exitProcess(2)
    }
    val json = report.toJson()
    val out = options["--out"]
    if (out != null) File(out).writeText(json) else println(json)
}
