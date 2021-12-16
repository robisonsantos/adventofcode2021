import kotlin.math.min

fun main() {
    fun part1(input: List<String>): Int {
        val riskMap = RiskMap(input)
        return riskMap.getLowestTotalRisk()
    }

    fun part2(input: List<String>): Int {
        val template = input.map { it.chunked(1).map { r -> r.toInt() } }
        val expandedMap = expandMap(template, 5)

        val riskMap = RiskMap(expandedMap)
        return riskMap.getLowestTotalRisk()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day15_test")
    check(part1(testInput) == 40)
    check(part2(testInput) == 315)

    val input = readInput("Day15")
    println(part1(input))
    println(part2(input))
}

/*
The entire cave is actually five times larger in both dimensions than you thought; the area you originally scanned is
just one tile in a 5x5 tile area that forms the full map. Your original map tile repeats to the right and downward;
 each time the tile repeats to the right or downward, all of its risk levels are 1 higher than the tile immediately
 up or left of it. However, risk levels above 9 wrap back around to 1.
 */
fun expandMap(template: List<List<Int>>, xFactor: Int): List<List<Int>> {
    val height = template.size
    val width = template.first().size

    val newMap = mutableListOf<MutableList<Int>>()

    for (i in 0 until height * xFactor) {
        newMap.add(mutableListOf())
        for (j in 0 until width * xFactor) {
            val addFactor = i/height + j/height
            var value = template[i%height][j%width] + addFactor

            if (value > 9) {
                value %= 9
            }

            newMap[i].add(value)
        }
    }

    return newMap
}

fun RiskMap(input: List<String>): RiskMap {
    val riskLevels = input.map { it.chunked(1).map { r -> r.toInt() }}
    return RiskMap(riskLevels)
}

class RiskMap(riskLevels: List<List<Int>>) {
    private data class Node(val riskLevel: Int, val posOnGrid: Pair<Int, Int>) {
        var accumulatedRisk: Int = Int.MAX_VALUE

        fun updateAccumulatedRisk(parentNode: Node) {
            accumulatedRisk = min(accumulatedRisk, riskLevel + parentNode.accumulatedRisk)
        }
    }

    private val map = riskLevels.mapIndexed { i, row -> row.mapIndexed { j, risk -> Node(risk, i to j) }}

    fun getLowestTotalRisk(): Int {
        val unvisited = map.flatten().toMutableSet()
        val updated = mutableSetOf<Node>()
        val startNode = map.first().first()
        val endNode = map.last().last()

        startNode.accumulatedRisk = 0
        updated.add(startNode)

        while (unvisited.isNotEmpty()) {
            val currentNode = updated.minByOrNull { it.accumulatedRisk }!! // To speed things up without using min-heap (updated is always shorter than unvisited)
            unvisited.remove(currentNode)
            updated.remove(currentNode)

            if (currentNode == endNode) break

            neighbors(currentNode).filter { unvisited.contains(it) }.forEach {
                it.updateAccumulatedRisk(currentNode)
                updated.add(it)
            }
        }

        return endNode.accumulatedRisk
    }

    private fun neighbors(node: Node): List<Node> {
        val neighbors = mutableListOf<Node>()
        val (i, j) = node.posOnGrid

        if (i - 1 >= 0) neighbors.add(map[i-1][j])
        if (i + 1 < map.size) neighbors.add(map[i+1][j])
        if (j - 1 >= 0) neighbors.add(map[i][j-1])
        if (j + 1 < map[i].size) neighbors.add(map[i][j+1])

        return neighbors
    }

    fun display() {
        for (l in map) {
            for (n in l) {
                val risk = n.riskLevel
                var acc = n.accumulatedRisk
                var colorStart = "\u001B[31m"
                var colorRes = "\u001B[0m"

                if (acc == Int.MAX_VALUE) {
                    colorRes = ""
                    colorStart = ""
                    acc = 0
                }

                val fmtRisk = String.format("%03d", risk)
                val fmtAcc = String.format("%03d", acc)
                print("${colorStart}[${fmtRisk},${fmtAcc}]${colorRes}")
            }
            println()
        }
        println()
    }
}