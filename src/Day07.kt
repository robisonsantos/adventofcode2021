import kotlin.math.abs

fun main() {

    fun part1(input: List<String>): Int {
        val sortedInput = input.first().split(",").map { it.toInt() }.sorted()
        val middle = sortedInput.size / 2.0
        val median = if (middle - middle.toInt() == 0.0) {
            sortedInput[middle.toInt()]
        } else {
            (sortedInput[middle.toInt()] + sortedInput[middle.toInt() + 1]) / 2
        }

        return sortedInput.sumOf { abs(it - median) }
    }

    fun part2(input: List<String>): Int {
        // Brute force
        val sortedInput = input.first().split(",").map { it.toInt() }.sorted()
        val maxValue = sortedInput.last()

//        var minIndex = 0
        var minSum = Int.MAX_VALUE

        for (i in (0..maxValue)) {
            // Sum the arithmetic progression for each entry based on each index
            val currentSum = sortedInput.sumOf { (abs(it - i) * (1 + abs(it - i))) / 2 }
            if (currentSum < minSum) {
                minSum = currentSum
//                minIndex = i
            }
        }

//        println(">> Index: $minIndex")
        return minSum
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day07_test")
    check(part1(testInput) == 37)
    check(part2(testInput) == 168)

    val input = readInput("Day07")
    println(part1(input))
    println(part2(input))
}
