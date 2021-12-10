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

fun evaluateSyntax(input: String): EvaluationResul {
    val tokens = input.split("".toRegex()).filter { it.isNotEmpty() }
    val stack = Stack<String>()

    for (token in tokens) {
        if (isOpen(token)) {
            stack.add(token)
        } else {
            val top = stack.peek()
            if (top != null && isMatch(top, token)) {
                stack.pop()
            } else {
                return Corrupted(token)
            }
        }
    }

    return if (stack.isEmpty()) Success else Incomplete(stack)
}

fun isOpen(token: String): Boolean {
    return setOf("(", "[", "<", "{").contains(token)
}

fun isMatch(open: String, close: String): Boolean {
    return when (open) {
        "{" -> close == "}"
        "(" -> close == ")"
        "<" -> close == ">"
        "[" -> close == "]"
        else -> false
    }
}

fun getMatch(open: String): String {
    return when (open) {
        "{" -> "}"
        "(" -> ")"
        "<" -> ">"
        "[" -> "]"
        else -> ""
    }
}

fun autocomplete(stack: Stack<String>): List<String> {
    return stack.map { getMatch(it) }.reversed()
}

fun computeAutocompleteScore(autocomplete: List<String>): Long {
    return autocomplete.fold(0) { total, token -> (total * 5) + token.autocompleteScore() }
}

