import kotlin.math.max
import kotlin.math.min

fun main() {
    // "a,b" => Pair(a, b)
    fun parsePoint(pointDef: String): Pair<Int, Int> {
        val coordinates = pointDef.split(',')
        return Pair(coordinates[0].toInt(), coordinates[1].toInt())
    }
    // "a,b -> c,d" => Pair(Pair(a,b), Pair(c,d))
    fun parsePoints(pointsDef: String): Pair<Pair<Int, Int>, Pair<Int, Int>> {
        val points = pointsDef.split(" -> ")
        return Pair(parsePoint(points[0]), parsePoint(points[1]))
    }

    fun buildGrid(input: List<String>, withDiagonal: Boolean = false): HydrothermalVentGrid {
        var maxX = 0
        var maxY = 0

        val lines = input.map { parsePoints(it) }.map {
            val line = Line(it.first, it.second, withDiagonal)
            // Keep track of max X,Y to be able to build the grid later
            maxX = max(maxX, max(line.p1.first, line.p2.first))
            maxY = max(maxY, max(line.p1.second, line.p2.second))

            // Return the line
            line
        }

        return HydrothermalVentGrid(lines, Pair(maxX, maxY))
    }

    fun part1(input: List<String>): Int {
        val grid = buildGrid(input)
        return grid.getOverlappingPoints()
    }

    fun part2(input: List<String>): Int {
        val grid = buildGrid(input, true)
        return grid.getOverlappingPoints()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day05_test")
    check(part1(testInput) == 5)
    check(part2(testInput) == 12)

    val input = readInput("Day05")
    println(part1(input))
    println(part2(input))
}

class HydrothermalVentGrid(lines: List<Line>, maxPoint: Pair<Int, Int>) {
    private val grid = Array(maxPoint.first + 1) { Array(maxPoint.second + 1) { 0 } }

    init {
        lines.forEach { line ->
            line.points().forEach { point ->
                grid[point.first][point.second]++
            }
        }
    }

    fun printIt() {
        grid.forEach { row ->
            row.forEach {
                if (it == 0) print(".")
                else print(it)
            }
            println()
        }
    }

    fun getOverlappingPoints(): Int {
        return grid.fold(0) { sum, row ->
            sum + row.filter { it > 1 }.size
        }
    }
}

class Line(val p1: Pair<Int, Int>, val p2: Pair<Int, Int>, private val acceptDiagonal: Boolean = false) {
    fun points(): List<Pair<Int, Int>> {
        // Check if line is vertical or horizontal, then expand points in that direction
        return if (isVertical()) {
            getVerticalPoints()
        } else if (isHorizontal()) {
            getHorizontalPoints()
        } else {
            getDiagonalPoints()
        }
    }

    private fun isVertical(): Boolean {
        return p1.first == p2.first
    }

    private fun getVerticalPoints(): List<Pair<Int, Int>> {
        val x = p1.first
        val minY = min(p1.second, p2.second)
        val maxY = max(p1.second, p2.second)
        return (minY..maxY).map { Pair(x, it) }
    }

    private fun isHorizontal(): Boolean {
        return p1.second == p2.second
    }

    private fun getHorizontalPoints(): List<Pair<Int, Int>> {
        val y = p1.second
        val minX = min(p1.first, p2.first)
        val maxX = max(p1.first, p2.first)
        return (minX..maxX).map { Pair(it, y)}
    }

    private fun getDiagonalPoints(): List<Pair<Int, Int>> {
        if (acceptDiagonal) {
            var x = p1.first
            var y = p1.second
            val points: MutableList<Pair<Int, Int>> = ArrayList()

            if (p1.first > p2.first && p1.second > p2.second) { // descend at 45dg
                while (x >= p2.first && y >= p2.second) {
                    points.add(Pair(x, y))
                    x--
                    y--
                }
                return points
            }

            if (p1.first > p2.first && p1.second < p2.second) { // descend at 45dg
                while (x >= p2.first && y <= p2.second) {
                    points.add(Pair(x, y))
                    x--
                    y++
                }
                return points
            }

            if (p1.first < p2.first && p1.second > p2.second) { // descend at 45dg
                while (x <= p2.first && y >= p2.second) {
                    points.add(Pair(x, y))
                    x++
                    y--
                }
                return points
            }

            // p1.first < p2.first && p1.second < p2.second
            while (x <= p2.first && y <= p2.second) {
                points.add(Pair(x, y))
                x++
                y++
            }
            return points

        } else {
            return listOf()
        }
    }

    override fun toString(): String {
        return points().joinToString(",", prefix = "[", postfix = "]") { "(${it.first},${it.second})" }
    }
}