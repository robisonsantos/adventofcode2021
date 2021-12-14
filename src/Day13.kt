fun main() {
    fun parseInput(input: List<String>, points: MutableList<Pair<Int, Int>>, actions: MutableList<Action>) {
        input.map { it.trim() }.filterNot { it.isEmpty() }.forEach {
            if (it.startsWith("fold along y")) {
                val tokens = it.split("=")
                actions.add(FoldY(tokens[1].toInt()))
            } else if (it.startsWith("fold along x")) {
                val tokens = it.split("=")
                actions.add(FoldX(tokens[1].toInt()))
            } else {
                val tokens = it.split(",")
                points.add(tokens[0].toInt() to tokens[1].toInt())
            }
        }
    }

    fun part1(input: List<String>): Int {
        val points = mutableListOf<Pair<Int, Int>>()
        val actions = mutableListOf<Action>()
        parseInput(input, points, actions)

        val width = points.maxOf { it.first } + 1
        val height = points.maxOf { it.second } + 1

        val paper = Paper(points, height, width)

        when (val action = actions.first()) {
            is FoldX -> paper.foldLeft(action.axis)
            is FoldY -> paper.foldUp(action.axis)
        }

        return paper.countMarkers()
    }

    fun part2(input: List<String>) {
        val points = mutableListOf<Pair<Int, Int>>()
        val actions = mutableListOf<Action>()
        parseInput(input, points, actions)

        val width = points.maxOf { it.first } + 1
        val height = points.maxOf { it.second } + 1

        val paper = Paper(points, height, width)

        actions.forEach { action ->
            when (action) {
                is FoldX -> paper.foldLeft(action.axis)
                is FoldY -> paper.foldUp(action.axis)
            }
        }

        paper.display()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day13_test")
    check(part1(testInput) == 17)

    val input = readInput("Day13")
    println(part1(input))
    println(part2(input))
}

sealed interface Action
data class FoldY(val axis: Int): Action
data class FoldX(val axis: Int): Action

class Paper(markerPoints: List<Pair<Int, Int>>, private var height: Int, private var width: Int) {
    private var grid = Array(height) { Array(width) { "." } }

    init {
        // Pair<Int, Int> => (x, y)
        // x -> increases to right
        // y -> increases to bottom
        markerPoints.forEach { grid[it.second][it.first] = "#" }
    }

    fun foldUp(axis: Int) {
        val newHeight = height - (height - axis)
        val newGrid = Array(newHeight) { y -> Array(width) { x -> grid[y][x] } }

        for (delta in (1 until height - axis)) {
            for (x in 0 until width) {
                val marker = grid[axis + delta][x]
                if (marker == "#") {
                    newGrid[axis - delta][x] = marker
                }
            }
        }

        // Update internal state
        grid = newGrid
        height = newHeight
    }

    fun foldLeft(axis: Int) {
        val newWidth = width - (width - axis)
        val newGrid = Array(height) { y -> Array(newWidth) { x -> grid[y][x] } }

        for (y in (0 until height)) {
            for (delta in 1 until width - axis) {
                val marker = grid[y][axis + delta]
                if (marker == "#") {
                    newGrid[y][axis - delta] = marker
                }
            }
        }

        // Update internal state
        grid = newGrid
        width = newWidth
    }

    fun countMarkers(): Int {
        return grid.sumOf { it.filter { v -> v == "#"}.size }
    }

    fun display() {
        grid.forEach { println(it.toList().joinToString { v -> if (v == "#") "\u001b[41m#[0m" else v }) }
    }
}