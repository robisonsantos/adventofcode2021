import kotlin.math.max
import kotlin.math.sqrt

fun main() {
    fun part1(input: List<String>): Int {
        val probeSolver = ProbeSolver(input.first().trim())
        probeSolver.solve()

        return probeSolver.maxHeight
    }

    fun part2(input: List<String>): Int {
        val probeSolver = ProbeSolver(input.first().trim())
        probeSolver.solve()

        return probeSolver.possibleVelocities.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day17_test")
    check(part1(testInput) == 45)
    check(part2(testInput) == 112)

    val input = readInput("Day17")
    println(part1(input))
    println(part2(input))
}

class ProbeSolver(targetAreaDef: String) {
    private val targetArea = targetArea(targetAreaDef)
    var maxHeight = Int.MIN_VALUE
    val possibleVelocities = mutableListOf<Pair<Int, Int>>()

    fun solve() {
        for (vx in xRange(targetArea[0])) {
            for (vy in yRange(targetArea[1])) {
                val maybeMaxHeight = tryReachTarget(vx, vy)
                if (maybeMaxHeight != null) {
                    possibleVelocities.add(vx to vy)
                    maxHeight = max(maxHeight, maybeMaxHeight)
                }
            }
        }
    }

    private fun targetArea(def: String): Array<IntArray> {
        val ranges = def.replace("target area: ", "").split(",")
        val xRange = ranges[0].trim().replace("x=", "").split("..").map { it.toInt() }
        val yRange = ranges[1].trim().replace("y=", "").split("..").map { it.toInt() }
        return arrayOf(xRange.toIntArray(), yRange.toIntArray())
    }

    private fun xRange(xArea: IntArray): IntRange {
        val x1 = (-1 + sqrt(8.0 * xArea[0] + 1)) / 2
        val x2 = (-1 - sqrt(8.0 * xArea[0] + 1)) / 2

        val minX = listOf(x1.toInt(), x2.toInt()).filter { it > 0 }.minOf { it }
        val maxX = xArea[1]

        return minX..maxX
    }

    private fun yRange(yArea: IntArray): IntRange {
        return yArea[0]..100 // Guessing a max for y :(
    }

    private fun tryReachTarget(vx: Int, vy: Int): Int? {
        // Position of the probe
        var x = 0
        var y = 0
        var maxH = Int.MIN_VALUE

        // local copies, so we can modify them
        var lVX = vx
        var lVY = vy

        while(true) {
            x += lVX
            y += lVY

            maxH = max(maxH, y)

            if (lVX > 0) lVX -= 1
            if (lVX < 0) lVX += 1
            lVY -= 1

            if (x > targetArea[0][1] || y < targetArea[1][0]) return null // outside the target area
            if (x >= targetArea[0][0] && y <= targetArea[1][1]) return maxH // within the target area
        }
    }
}