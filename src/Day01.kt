fun main() {

    // Count how many items in the list is larger than the previous value
    fun countReadingOverBase(readings: List<Int>): Int {
        var base = readings.first()
        val result = readings.fold(0) { acc, reading ->
            val count = if (reading > base) acc + 1 else acc
            base = reading
            count
        }
        return result
    }

    fun part1(input: List<String>): Int {
        val readings = input.map { it.toInt() }
        return countReadingOverBase(readings)
    }

    fun part2(input: List<String>): Int {
        val windowSize = 3
        val readings = input.map { it.toInt() }

        // Group values as the sum of windowSize consecutive values
        // uses -1 when there's not enough elements to group
        val windowedReadings = (readings.indices).map { if (it + windowSize > readings.size) -1 else readings.subList(it, it + windowSize).sum() }
        return countReadingOverBase(windowedReadings)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day01_test")
    check(part1(testInput) == 7)

    val input = readInput("Day01")
    println(part1(input))
    println(part2(input))

}
