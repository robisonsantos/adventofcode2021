import kotlin.math.max

fun main() {
    // "a,b" => Pair(a, b)
    fun parsePoint(pointDef: String): Point {
        val coordinates = pointDef.split(',')
        return Point(coordinates[0].toInt(), coordinates[1].toInt())
    }
    // "a,b -> c,d" => Pair(Pair(a,b), Pair(c,d))
    fun parsePoints(pointsDef: String): Pair<Point, Point> {
        val points = pointsDef.split(" -> ")
        return Pair(parsePoint(points[0]), parsePoint(points[1]))
    }

    fun part1(input: List<String>): Int {
        val lines = input.map { parsePoints(it) }.map { Line.newLine(it.first, it.second) }
        val grid = VentGrid(lines.filter { it is HLine || it is VLine }) // Ignore diagonals

        return grid.getOverlappingPoints()
    }

    fun part2(input: List<String>): Int {
        val lines = input.map { parsePoints(it) }.map { Line.newLine(it.first, it.second) }
        val grid = VentGrid(lines) // Use all lines

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

class VentGrid(lines: List<Line>) {
    private val grid: Array<Array<Int>>

    init {
        var maxX = 0
        var maxY = 0

        lines.forEach { line ->
            maxX = max(maxX, line.points().maxOf { it.x })
            maxY = max(maxY, line.points().maxOf { it.y })
        }

        grid = buildGrid(lines, maxX, maxY)
    }

    fun getOverlappingPoints(): Int {
        return grid.fold(0) { sum, row ->
            sum + row.filter { it > 1 }.size
        }
    }

    fun display() {
        grid.forEach { println(it.asList()) }
    }

    // X is the horizontal axis (or columns)
    // Y is the vertical axis (or rows)
    private fun buildGrid(lines: List<Line>, maxX: Int, maxY: Int): Array<Array<Int>> {
        val grid = Array(maxY + 1){ Array(maxX + 1) { 0 } }

        lines.forEach { line ->
            line.points().forEach { p ->
                grid[p.y][p.x]++
            }
        }

        return grid
    }
}

class Point(val x: Int, val y: Int): Comparable<Point> {
    override fun compareTo(other: Point): Int {
        if (this.x == other.x) {
            if (this.y == other.y) return 0
            if (this.y < other.y) return -1
            return 1
        }

        if (this.x < other.x) {
            if (this.y == other.y) return -1
            if (this.y < other.y) return -1
            return 1
        }

        // this.x > other.x
        if (this.y == other.y) return 1
        if (this.y > other.y) return 1
        return -1
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}

interface Line {
    fun points(): List<Point>

    companion object {
        fun newLine(p1: Point, p2: Point): Line {
            var tp1 = p1
            var tp2 = p2

            if (p1 > p2) {
                tp1 = p2
                tp2 = p1
            }

            if (tp1.compareTo(tp2) == 0) return HLine(tp1, tp2)
            if (tp1.y == tp2.y) return HLine(tp1, tp2)
            if (tp1.x == tp2.x) return VLine(tp1, tp2)
            if (tp1.x > tp2.x && tp1.y < tp2.y) return DLLine(tp1, tp2)
            return DRLine(tp1, tp2)
        }
    }
}

abstract class ALine(protected val p1: Point, protected val p2: Point): Line {
    private val linePoints = this.getPoints()

    override fun points() = linePoints
    override fun toString() = linePoints.toString()

    protected abstract fun getPoints(): List<Point>
}

class HLine(p1: Point, p2: Point): ALine(p1, p2) {
    override fun getPoints(): List<Point> {
        return (p1.x..p2.x).map { Point(it, p1.y) }
    }
}

class VLine(p1: Point, p2: Point): ALine(p1, p2) {
    override fun getPoints(): List<Point> {
        return (p1.y..p2.y).map { Point(p1.x, it) }
    }
}

class DRLine(p1: Point, p2: Point): ALine(p1, p2) {
    // 45 deg line from right to left
    override fun getPoints(): List<Point> {
        return (0..(p2.x - p1.x)).map { Point(p1.x + it, p1.y + it) }
    }
}

class DLLine(p1: Point, p2: Point): ALine(p1, p2) {
    // 45 deg line from left to right
    override fun getPoints(): List<Point> {
        return (0..(p1.x - p2.x)).map { Point(p1.x - it, p1.y + it)}
    }
}