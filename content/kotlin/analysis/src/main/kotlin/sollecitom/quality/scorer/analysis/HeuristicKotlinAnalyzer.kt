package sollecitom.quality.scorer.analysis

import sollecitom.quality.scorer.model.KotlinFunction
import sollecitom.quality.scorer.model.KotlinSourceFile
import sollecitom.quality.scorer.model.KotlinTypeDeclaration

/**
 * Extracts [KotlinSourceFile] facts by lightweight lexical analysis — no Kotlin compiler / PSI dependency, so
 * it runs fully offline. Lower fidelity than a PSI analyzer (unusual formatting can fool it), but adequate for
 * the MVP rules. Swappable via [KotlinSourceAnalyzer]: a PSI-backed implementation can replace it without
 * touching any rule.
 */
class HeuristicKotlinAnalyzer : KotlinSourceAnalyzer {

    override fun analyze(path: String, source: String, isTestSource: Boolean): KotlinSourceFile {
        val clean = blankCommentsAndStrings(source)
        val rawLines = source.split("\n")
        val functions = FUN_DECL.findAll(clean).map { toFunction(it, source, clean, rawLines) }.toList()
        val types = TYPE_DECL.findAll(clean).map { toType(it, source, clean, rawLines) }.toList()
        return KotlinSourceFile(path = path, functions = functions, types = types, isTestSource = isTestSource)
    }

    private fun toFunction(match: MatchResult, source: String, clean: String, rawLines: List<String>): KotlinFunction {
        val name = match.groupValues[1].trim('`')
        val start = match.range.first
        val declLine = source.take(start).count { it == '\n' }
        val parenOpen = match.range.last // the '(' the regex ends on
        val parenClose = matchBracket(clean, parenOpen, '(', ')')
        val (bodyStart, bodyEnd) = bodySpan(clean, parenClose)
        val lineCount = if (bodyEnd >= start) {
            source.substring(start, (bodyEnd + 1).coerceAtMost(source.length)).count { it == '\n' } + 1
        } else {
            1
        }
        val body = if (bodyStart in 0..bodyEnd) clean.substring(bodyStart, bodyEnd + 1) else ""
        val lineStart = clean.lastIndexOf('\n', start - 1) + 1
        val linePrefix = clean.substring(lineStart, start)
        val (hasKdoc, testAbove) = docAndTestAbove(rawLines, declLine)
        return KotlinFunction(
            name = name,
            lineCount = lineCount,
            cyclomaticComplexity = 1 + DECISION.findAll(body).count(),
            isPublic = !NON_PUBLIC.containsMatchIn(linePrefix),
            hasKdoc = hasKdoc,
            isTest = testAbove || TEST_ANNOTATION.containsMatchIn(linePrefix),
            assertionCount = ASSERTION.findAll(body).count(),
        )
    }

    private fun toType(match: MatchResult, source: String, clean: String, rawLines: List<String>): KotlinTypeDeclaration {
        val name = match.groupValues[2].trim('`')
        val start = match.range.first
        val declLine = source.take(start).count { it == '\n' }
        return KotlinTypeDeclaration(
            name = name,
            isPublic = isPublicAt(clean, start),
            hasKdoc = docAndTestAbove(rawLines, declLine).first,
        )
    }

    /** A declaration is public unless a private/protected/internal modifier precedes it on its own line. */
    private fun isPublicAt(clean: String, declStart: Int): Boolean {
        val lineStart = clean.lastIndexOf('\n', declStart - 1) + 1
        return !NON_PUBLIC.containsMatchIn(clean.substring(lineStart, declStart))
    }

    /** Looks at the lines above a declaration (skipping annotations and blanks) for KDoc and `@Test`. */
    private fun docAndTestAbove(rawLines: List<String>, declLine: Int): Pair<Boolean, Boolean> {
        var k = declLine - 1
        var isTest = false
        while (k >= 0) {
            val trimmed = rawLines[k].trim()
            when {
                trimmed.isEmpty() -> k--
                trimmed.startsWith("@") -> {
                    if (TEST_ANNOTATION.containsMatchIn(trimmed)) isTest = true
                    k--
                }
                else -> break
            }
        }
        return kdocEndsAt(rawLines, k) to isTest
    }

    private fun kdocEndsAt(rawLines: List<String>, line: Int): Boolean {
        if (line < 0 || !rawLines[line].trimEnd().endsWith("*/")) return false
        var j = line
        while (j >= 0 && !rawLines[j].contains("/*")) j--
        return j >= 0 && rawLines[j].contains("/**")
    }

    /** Index range of the function body: `{ ... }` (block) or `= ...` (expression). (-1, -2) if none. */
    private fun bodySpan(clean: String, parenClose: Int): Pair<Int, Int> {
        val cap = NEXT_DECL.find(clean, parenClose + 1)?.range?.first ?: clean.length
        var i = parenClose + 1
        while (i < clean.length && i < cap) {
            when (clean[i]) {
                '{' -> return i to matchBracket(clean, i, '{', '}')
                '=' -> return i to expressionEnd(clean, i + 1)
            }
            i++
        }
        return -1 to -2
    }

    private fun matchBracket(text: String, openIndex: Int, open: Char, close: Char): Int {
        var depth = 0
        var i = openIndex
        while (i < text.length) {
            when (text[i]) {
                open -> depth++
                close -> if (--depth == 0) return i
            }
            i++
        }
        return text.length - 1
    }

    private fun expressionEnd(clean: String, start: Int): Int {
        var i = start
        var depth = 0
        while (i < clean.length) {
            when (clean[i]) {
                '(', '[', '{' -> depth++
                ')', ']', '}' -> depth--
                '\n' -> if (depth <= 0) return (i - 1).coerceAtLeast(start)
            }
            i++
        }
        return clean.length - 1
    }

    /** Same-length copy of the source with comment bodies and string literals blanked (newlines preserved), so
     *  braces and keywords inside them never fool the extractors. */
    private fun blankCommentsAndStrings(src: String): String {
        val out = CharArray(src.length) { ' ' }
        var i = 0
        val n = src.length
        while (i < n) {
            val c = src[i]
            when {
                c == '/' && i + 1 < n && src[i + 1] == '/' -> {
                    while (i < n && src[i] != '\n') i++
                }
                c == '/' && i + 1 < n && src[i + 1] == '*' -> {
                    i += 2
                    while (i < n && !(src[i] == '*' && i + 1 < n && src[i + 1] == '/')) {
                        if (src[i] == '\n') out[i] = '\n'
                        i++
                    }
                    i = (i + 2).coerceAtMost(n)
                }
                c == '"' && i + 2 < n && src[i + 1] == '"' && src[i + 2] == '"' -> {
                    i += 3
                    while (i < n && !(src[i] == '"' && i + 1 < n && src[i + 1] == '"' && i + 2 < n && src[i + 2] == '"')) {
                        if (src[i] == '\n') out[i] = '\n'
                        i++
                    }
                    i = (i + 3).coerceAtMost(n)
                }
                c == '"' -> {
                    i++
                    while (i < n && src[i] != '"' && src[i] != '\n') {
                        if (src[i] == '\\') i++
                        i++
                    }
                    if (i < n && src[i] == '"') i++
                }
                c == '\'' -> {
                    i++
                    while (i < n && src[i] != '\'' && src[i] != '\n') {
                        if (src[i] == '\\') i++
                        i++
                    }
                    if (i < n && src[i] == '\'') i++
                }
                else -> {
                    out[i] = c
                    i++
                }
            }
        }
        return String(out)
    }

    private companion object {
        val FUN_DECL = Regex("""\bfun\b\s+(?:<[^>]*>\s*)?(?:[A-Za-z_][\w.]*\.)?(`[^`]+`|[A-Za-z_]\w*)\s*\(""")
        val TYPE_DECL = Regex("""\b(class|interface|object)\b\s+(`[^`]+`|[A-Za-z_]\w*)""")
        val NEXT_DECL = Regex("""\b(fun|class|interface|object)\b""")
        val ASSERTION = Regex("""\bassert\w*\b|\bshouldBe\b|\bshouldNotBe\b|\bverify\s*\(|\bexpectThat\b|\bfail\s*\(""")
        val DECISION = Regex("""\bif\b|\bwhen\b|\bfor\b|\bwhile\b|\bcatch\b|&&|\|\||\?:""")
        val NON_PUBLIC = Regex("""\b(private|protected|internal)\b""")
        val TEST_ANNOTATION = Regex("""@\w*Test\b""")
    }
}
