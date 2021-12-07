
fun main() {
    fun simulate(input: List<String>, days: Int): ULong {
        val fishes = input.first().split(",").map { it.toInt() }.map { LanternFish(it) }.toMutableList()

        repeat((1..days).count()) {
            val newFishes = fishes.mapNotNull { it.liveForADay() }
            fishes.addAll(newFishes)
        }

        return fishes.size.toULong()
    }

    fun part1(input: List<String>): ULong {
        return simulate(input, 80)
    }

    fun simulate2(input: List<String>, days: Int): ULong {
        val fishes = input.first().split(",").map { it.toInt() }
        val parents: Array<ULong> = Array(7){ 0UL }
        val children: Array<ULong> = Array(9){ 0UL }

        // Set the initial state of the parents... each
        // slot represents the day they will spawn
        for (f in fishes) {
            parents[f]++
        }

        for (day in (0 until days)) {
            val di = day % 7 // Which day slot are we considering
            if (di == 0) {
                // This is a new set of days, so merge all children from past week
                // with current parents, as they now can start to spawn
                for (i in (parents.indices)) {
                    parents[i] += children[i]
                    children[i] = 0UL
                }

                // Children spawned at the end of the week, will only start spawning on the next week
                children[0] = children[7]
                children[1] = children[8]
                children[7] = 0UL
                children[8] = 0UL
            }

            // Parent spawns new children that will start spawning on the next cycle
            // on the 9th day (so, two days after the parent spawned it)
            children[di + 2] = parents[di]
        }

        return parents.sum() + children.sum()
    }

    fun part2(input: List<String>): ULong {
        return simulate2(input, 256)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day06_test")
    check(part1(testInput) == 5934UL)
    check(part2(testInput) == 26984457539UL)

    val input = readInput("Day06")
    println(part1(input))
    println(part2(input))
}

class LanternFish(private var timeToSpawn: Int = 8) {

    fun liveForADay(): LanternFish? {
        if (shouldSpawnNow()) {
            resetTimeToSpawn()
            return LanternFish()
        }

        timeToSpawn--
        return null
    }

    private fun shouldSpawnNow(): Boolean {
        return timeToSpawn == 0
    }

    private fun resetTimeToSpawn() {
        timeToSpawn = 6
    }
}
