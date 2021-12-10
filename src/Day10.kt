import java.util.Stack

fun main() {

    fun part1(input: List<String>): Long {
        return input.map { evaluateSyntax(it) }
            .filterIsInstance<Corrupted>()
            .sumOf { it.unexpected.corruptedScore() }
    }

    fun part2(input: List<String>): Long {
        val incompletes = input.map { evaluateSyntax(it) }
            .filterIsInstance<Incomplete>()
            .map { autocomplete(it.remainingStack) }
            .map { computeAutocompleteScore(it) }
            .sorted()

        return incompletes[incompletes.size / 2]
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day10_test")
    check(part1(testInput) == 26397L)
    check(part2(testInput) == 288957L)

    val input = readInput("Day10")
    println(part1(input))
    println(part2(input))
}

sealed class EvaluationResul
object Success: EvaluationResul()
class Incomplete(val remainingStack: Stack<String>): EvaluationResul()
class Corrupted(val unexpected: String): EvaluationResul()

fun String.corruptedScore(): Long {
    return when (this) {
        ")" -> 3L
        "]" -> 57L
        "}" -> 1197L
        ">" -> 25137L
        else -> 0L
    }
}

fun String.autocompleteScore(): Long {
    return when (this) {
        ")" -> 1L
        "]" -> 2L
        "}" -> 3L
        ">" -> 4L
        else -> 0L
    }
}

fun String.isOpenToken(): Boolean {
    return setOf("(", "[", "<", "{").contains(this)
}

fun String.match(other: String): Boolean {
    return when (this) {
        "{" -> other == "}"
        "(" -> other == ")"
        "<" -> other == ">"
        "[" -> other == "]"
        else -> false
    }
}

fun String.getMatchToken(): String {
    return when (this) {
        "{" -> "}"
        "(" -> ")"
        "<" -> ">"
        "[" -> "]"
        else -> ""
    }
}

fun evaluateSyntax(input: String): EvaluationResul {
    val tokens = input.chunked(1)
    val stack = Stack<String>()

    for (token in tokens) {
        if (token.isOpenToken()) {
            stack.add(token)
        } else {
            val top = stack.peek()
            if (top != null && top.match(token)) {
                stack.pop()
            } else {
                return Corrupted(token)
            }
        }
    }

    return if (stack.isEmpty()) Success else Incomplete(stack)
}

fun autocomplete(stack: Stack<String>): List<String> {
    return stack.map { it.getMatchToken() }.reversed()
}

fun computeAutocompleteScore(autocomplete: List<String>): Long {
    return autocomplete.fold(0) { total, token -> (total * 5) + token.autocompleteScore() }
}

