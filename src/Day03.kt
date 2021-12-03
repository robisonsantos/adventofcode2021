fun main() {
    fun part1(input: List<String>): Int {
        val reportManipulator = ReportManipulator(input)
        return reportManipulator.gammaRate * reportManipulator.epsilonRate
    }

    fun part2(input: List<String>): Int {
        val reportManipulator = ReportManipulator(input)
        return reportManipulator.o2GenRating * reportManipulator.co2ScrRating
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day03_test")
    check(part1(testInput) == 198)
    check(part2(testInput) == 230)

    val input = readInput("Day03")
    println(part1(input))
    println(part2(input))
}

class ReportManipulator(private val report: List<String>) {
    private val commonBits: Array<Int>
    private val inputBitSize: Int = report.first().length

    val gammaRate: Int
    val epsilonRate: Int

    val o2GenRating: Int
    val co2ScrRating: Int

    init {
        commonBits = findCommonBits(report)

        gammaRate = calculateGammaRate()
        epsilonRate = calculateEpsilonRate()
        o2GenRating = calculateO2GenRating()
        co2ScrRating = calculateCO2ScrRating()
    }

    private fun findCommonBits(report: List<String>): Array<Int> {
        val bitCounters = Array(inputBitSize) { 0 }
        val totalEntries = report.size

        report.forEach { data ->
            for (i in 0 until inputBitSize) {
                if (data[i] == '1') {
                    bitCounters[i]++
                }
            }
        }

        val commonBits = Array(inputBitSize) { 0 }
        for (i in bitCounters.indices) {
            commonBits[i] = if (bitCounters[i] >= totalEntries / 2.0) {
                1
            } else {
                0
            }
        }

        return commonBits
    }

    /**
     * Each bit in the gamma rate can be determined by finding the most common bit in the corresponding position of all numbers in the diagnostic report.
     */
    private fun calculateGammaRate(): Int {
        return calculateRate(inversionFactor = 0)
    }

    /**
     * The epsilon rate is calculated in a similar way; rather than use the most common bit, the least common bit from each position is used.
     */
    private fun calculateEpsilonRate(): Int {
        return calculateRate(inversionFactor = 1)
    }

    private fun calculateRate(inversionFactor: Int = 0): Int {
        var rateStr = ""
        for (i in 0 until inputBitSize) {
            rateStr += commonBits[i] xor inversionFactor
        }

        return rateStr.toInt(2)
    }

    /**
     * To find oxygen generator rating, determine the most common value (0 or 1) in the current bit position, and keep only numbers with that bit in that position.
     * If 0 and 1 are equally common, keep values with a 1 in the position being considered.
     */
    private fun calculateO2GenRating(): Int {
        return calculateRating(inversionFactor = 0)
    }

    /**
     * To find CO2 scrubber rating, determine the least common value (0 or 1) in the current bit position, and keep only numbers with that bit in that position.
     * If 0 and 1 are equally common, keep values with a 0 in the position being considered
     */
    private fun calculateCO2ScrRating(): Int {
        return calculateRating(inversionFactor = 1)
    }

    private fun calculateRating(inversionFactor: Int = 0): Int {
        var reportToFilter: List<String> = ArrayList(report)
        var commonBits = commonBits
        val indices = commonBits.indices

        for (i in indices) {
            reportToFilter = reportToFilter.filter { it[i].digitToInt() == (commonBits[i] xor inversionFactor) }
            commonBits = findCommonBits(reportToFilter)
            if (reportToFilter.size == 1) {
                return reportToFilter.first().toInt(2)
            }
        }
        return -1
    }
}
