fun main() {
    fun part1(input: List<String>): Int {
        val consortium = Consortium(input)
        return (1..100).sumOf { consortium.executeStep() }
    }

    fun part2(input: List<String>): Int {
        val expectedStableOctopuses = input.size * input.first().length
        val consortium = Consortium(input)
        var step = 0

        // Find when all octopuses flash
        // Flashes eventually stabilizes and all octopuses are stable at the same time
        while (consortium.countStableOctopuses() < expectedStableOctopuses) {
            consortium.executeStep()
            step++
        }

        return step
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day11_test")
    check(part1(testInput) == 1656)
    check(part2(testInput) == 195)

    val input = readInput("Day11")
    println(part1(input))
    println(part2(input))
}

fun Consortium(input: List<String>): Consortium {
    val octopuses = input.map { it.chunked(1).map { c -> Octopus(c.toInt()) } }
    return Consortium(octopuses)
}

// A single octopus
class Octopus(private var energyLevel: Int) {
    fun updateEnergyLevel() {
        energyLevel++
    }

    private fun canFlash(): Boolean {
        return energyLevel > 9
    }

    fun tryFlash(): Boolean {
        if (canFlash()) {
            energyLevel = 0
            return true
        }

        return false
    }

    fun isStable(): Boolean {
        return energyLevel == 0
    }

    fun getEnergyLevel(): Int {
        return energyLevel
    }
}

// A group of Octopuses
class Consortium(private val octopuses: List<List<Octopus>>) {
    // Return how many octopuses have flashed on this step
    fun executeStep(): Int {
        val flashedOctopuses = mutableSetOf<Pair<Int, Int>>()

        // First, the energy level of each octopus increases by 1.
        octopuses.forEach { it.forEach { o -> o.updateEnergyLevel() }}

        // Then, any octopus with an energy level greater than 9 flashes. This increases the energy level of all
        // adjacent octopuses by 1, including octopuses that are diagonally adjacent. If this causes an octopus to
        // have an energy level greater than 9, it also flashes. This process continues as long as new octopuses
        // keep having their energy level increased beyond 9. (An octopus can only flash at most once per step.)
        for (i in octopuses.indices) {
            for (j in octopuses[i].indices) {
                val octopus = octopuses[i][j]
                if (octopus.tryFlash()) {
                    flashedOctopuses.add(Pair(i, j))
                    shareEnergy(getAdjacentOctopusesPos(i, j), flashedOctopuses)
                }
            }
        }

        return flashedOctopuses.size
    }

    fun display() {
        octopuses.forEach { println(it.map { o -> o.getEnergyLevel() }) }
    }

    private fun shareEnergy(friends: List<Pair<Int, Int>>, flashedOctopuses: MutableSet<Pair<Int, Int>>) {
        if (friends.isEmpty()) return

        for (pos in friends) {
            if (flashedOctopuses.contains(pos)) continue
            val (i, j) = pos
            val octopus = octopuses[i][j]

            octopus.updateEnergyLevel()
            if (octopus.tryFlash()) {
                flashedOctopuses.add(pos)
                val newFriends = getAdjacentOctopusesPos(i, j).filterNot { flashedOctopuses.contains(it) }
                shareEnergy(newFriends, flashedOctopuses)
            }
        }
    }

    private fun getAdjacentOctopusesPos(i: Int, j: Int): List<Pair<Int,Int>> {
        val adj = mutableListOf<Pair<Int, Int>>()

        for (x in (i - 1)..(i + 1)) {
            if (x < 0 || x >= octopuses.size) continue

            for (y in (j - 1)..(j + 1)) {
                if (y < 0 || y >= octopuses[x].size) continue

                adj.add(Pair(x, y))
            }
        }
        return adj
    }

    fun countStableOctopuses(): Int {
        return octopuses.sumOf { it.count { o -> o.isStable() } }
    }
}