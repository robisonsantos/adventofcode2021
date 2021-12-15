import java.util.LinkedList

fun main() {
    fun part1(input: List<String>): Int {
        val polymer = Polymer(input)
        repeat(10) { polymer.executePairingStep() }

        val counterByElement = polymer.getCounterByElement()
        val counters = counterByElement.values.sorted()

        return counters.last() - counters.first()
    }

    fun part2(input: List<String>): ULong {
        val polymer = PolymerV2(input)
        repeat(40) { polymer.executePairingStep() }

        val counterByElement = polymer.getCounterByElement()
        val counters = counterByElement.values.sorted()

        return counters.last() - counters.first()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day14_test")
    check(part1(testInput) == 1588)
    check(part2(testInput) == 2188189693529UL)

    val input = readInput("Day14")
    println(part1(input))
    println(part2(input))
}

fun Polymer(input: List<String>): Polymer {
    val template = input.first()
    val rules = (2 until input.size).map { input[it] }

    return Polymer(template, rules)
}

fun PolymerV2(input: List<String>): PolymerV2 {
    val template = input.first()
    val rules = (2 until input.size).map { input[it] }

    return PolymerV2(template, rules)
}

// Allow solving via brute-force
class Polymer(template: String, pairInsertionRules: List<String>) {
    private val poly = LinkedList(template.chunked(1))
    private val insertionRules = pairInsertionRules.map { it.split(" -> ") }.associate { it[0] to it[1] }

    fun executePairingStep() {
        val startIdx = poly.size - 1
        val endIdx = 1

        for (i in startIdx downTo endIdx) {
            val key = key(poly[i-1], poly[i])
            if (insertionRules.contains(key)) {
                poly.add(i, insertionRules.getValue(key))
            }
        }
    }

    override fun toString(): String {
        return poly.joinToString("")
    }

    fun getCounterByElement(): Map<String, Int> {
        return poly.groupingBy { it }.eachCount()
    }

    private fun key(el1: String, el2: String): String {
        return "$el1$el2"
    }
}

/*
 * On every step, accumulate how many times a pair appears.
 * On the next step, each pair generates two new pairs 'n' times
 * To group each element per how many times they appear, just accumulate
 * the second element of each pair (as the string is formed by concatenating the pairs)
 * This means that the first element on the original template is counted 1X less (offBy1)
 */
class PolymerV2(private val template: String, pairInsertionRules: List<String>) {
    private var poly = mutableMapOf<String, ULong>()
    private val insertionRules = pairInsertionRules.map { it.split(" -> ") }.associate { it[0] to it[1] }

    init {
        val temp = template.chunked(1)
        for (i in (0 until temp.size - 1)) {
            val key = "${temp[i]}${temp[i+1]}"
            poly[key] = poly.getOrDefault(key, 0UL) + 1UL
        }
    }

    fun executePairingStep() {
        val newPoly = mutableMapOf<String, ULong>()

        poly.forEach { (pair, appearances) ->
            getNewPairs(pair).forEach {
                newPoly[it] = newPoly.getOrDefault(it, 0UL) + appearances
            }
        }

        poly = newPoly
    }

    fun getCounterByElement(): Map<String, ULong> {
        val entries = poly.keys.flatMap { it.chunked(1) }.toSet()
        val groups = mutableMapOf<String, ULong>()
        entries.forEach { groups[it] = 0UL }

        poly.forEach { (pair, appearances) ->
            val keys = pair.chunked(1)
            groups[keys[1]] = groups.getValue(keys[1]) + appearances
        }

        val offBy1 = template.chunked(1).first()
        groups[offBy1] = groups.getValue(offBy1) + 1UL

        return groups
    }

    private fun getNewPairs(pair: String): List<String> {
        if (insertionRules.contains(pair)) {
            val insertion = insertionRules.getValue(pair)
            val tokens = pair.chunked(1)

            return listOf("${tokens[0]}$insertion", "$insertion${tokens[1]}")
        }

        // No pair found in the mapping
        return listOf(pair)
    }
}
