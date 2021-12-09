import java.util.LinkedList
import java.util.Queue

fun main() {

    fun part1(input: List<String>): Int {
        val heightmap = Heightmap(input)

        return heightmap.getTotalRiskLevel()
    }

    // Find the three largest basins and multiply their sizes together
    fun part2(input: List<String>): Int {
        val heightmap = Heightmap(input)

        return heightmap.getBasinsSizes().sortedDescending().subList(0,3).fold(1) { acc, i -> acc * i }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day09_test")
    check(part1(testInput) == 15)
    check(part2(testInput) == 1134)

    val input = readInput("Day09")
    println(part1(input))
    println(part2(input))
}

fun Heightmap(data: List<String>): Heightmap {
    return Heightmap(data.map { it.split("".toRegex()).filter { v -> v.isNotEmpty() }.map { v -> v.toInt() } })
}

class Heightmap(private val heights: List<List<Int>>) {
    /**
     * The risk level of a low point is 1 plus its height
     */
    fun getTotalRiskLevel(): Int {
        return lowPointsLocation().sumOf { (i, j) -> heights[i][j] + 1 }
    }

    /**
     * A basin is all locations that eventually flow downward to a single low point. Therefore, every low point has a
     * basin, although some basins are very small. Locations of height 9 do not count as being in any basin, and all
     * other locations will always be part of exactly one basin
     */
    fun getBasinsSizes(): Iterable<Int> {
        return lowPointsLocation().map { getBasinSize(it) }
    }

    private fun lowPointsLocation(): Iterable<Pair<Int, Int>> {
        return heights.indices.flatMap { i ->
            heights.first().indices.filter { j -> isLowPoint(i, j) }.map { j -> i to j }
        }
    }

    private fun getBasinSize(position: Pair<Int, Int>): Int {
        var size = 0
        val visitedPositions = mutableSetOf<Pair<Int, Int>>()

        val queue: Queue<Pair<Int, Int>> = LinkedList()
        queue.add(position)

        while (queue.isNotEmpty()) {
            val pos = queue.remove()
            if (visitedPositions.contains(pos)) continue

            size++
            visitedPositions.add(pos)

            val neighbors = getNeighbors(pos.first, pos.second)
                             .filterNot { (i, j) -> heights[i][j] == 9 }
                             .filterNot { visitedPositions.contains(it) }
            queue.addAll(neighbors)
        }

        return size
    }

    /**
     * low points - the locations that are lower than any of its adjacent locations. Most locations have four adjacent
     * locations (up, down, left, and right); locations on the edge or corner of the map have three or two adjacent
     * locations, respectively. (Diagonal locations do not count as adjacent.)
     */
    private fun isLowPoint(i: Int, j: Int): Boolean {
        return heights[i][j] < getNeighbors(i, j).map { (i,j) -> heights[i][j] }.minOf { it }
    }

    private fun getNeighbors(i: Int, j: Int): List<Pair<Int, Int>> {
        val neighbors = mutableListOf<Pair<Int, Int>>()

        if (i - 1 >= 0) neighbors.add(i-1 to j)
        if (i + 1 < heights.size) neighbors.add(i+1 to j)
        if (j - 1 >= 0) neighbors.add(i to j-1)
        if (j + 1 < heights[i].size) neighbors.add(i to j+1)

        return neighbors
    }
}
