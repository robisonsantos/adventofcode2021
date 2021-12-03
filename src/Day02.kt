fun main() {
    fun part1(input: List<String>): Int {
        val submarine = SimpleSubmarine()
        val plan = input.map { it.split(" ") }.map { Pair(it[0], it[1].toInt()) }

        submarine.maneuver(plan)
        return submarine.depthPos * submarine.horizontalPos
    }

    fun part2(input: List<String>): Int {
        val submarine = AimedSubmarine()
        val plan = input.map { it.split(" ") }.map { Pair(it[0], it[1].toInt()) }

        submarine.maneuver(plan)
        return submarine.depthPos * submarine.horizontalPos
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day02_test")
    check(part1(testInput) == 150)

    val input = readInput("Day02")
    println(part1(input))
    println(part2(input))
}

abstract class Submarine {
    var horizontalPos = 0
    var depthPos = 0

    fun maneuver(plan: List<Pair<String, Int>>) {
        plan.forEach { applyManeuver(it) }
    }

    abstract fun applyManeuver(maneuver: Pair<String, Int>)
}

class SimpleSubmarine: Submarine() {
    override fun applyManeuver(maneuver: Pair<String, Int>) {
        val direction = maneuver.first
        val amount = maneuver.second

        // forward X increases the horizontal position by X units.
        // down X increases the depth by X units.
        // up X decreases the depth by X units.
        when(direction) {
            "forward" -> horizontalPos += amount
            "down" -> depthPos += amount
            "up" -> depthPos -= amount
        }
    }
}

class AimedSubmarine: Submarine() {
    var aim = 0

    override fun applyManeuver(maneuver: Pair<String, Int>) {
        val direction = maneuver.first
        val amount = maneuver.second

        // down X increases your aim by X units.
        // up X decreases your aim by X units.
        // forward X does two things:
        //    It increases your horizontal position by X units.
        //    It increases your depth by your aim multiplied by X.
        when(direction) {
            "down" -> aim += amount
            "up" -> aim -= amount
            "forward" -> {
                horizontalPos += amount
                depthPos += aim * amount
            }
        }
    }
}
