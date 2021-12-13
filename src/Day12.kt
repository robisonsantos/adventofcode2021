
fun main() {
    fun part1(input: List<String>): Int {
        val pathFinder = PathFinder(cavePairs(input))
        val paths = pathFinder.getPaths(Cave("start"))
        return paths.size
    }

    fun part2(input: List<String>): Int {
        val pathFinder = PathFinder(cavePairs(input))
        val paths = pathFinder.getPathsWithLenience(Cave("start"))
        return paths.size
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day12_test")
    check(part1(testInput) == 226)
    check(part2(testInput) == 3509)

    val input = readInput("Day12")
    println(part1(input))
    println(part2(input))
}

fun cavePairs(input: List<String>): List<Pair<Cave, Cave>> {
    return input.flatMap {
        val tokens = it.split("-")
        listOf(
            Cave(tokens[0]) to Cave(tokens[1]),
            Cave(tokens[1]) to Cave(tokens[0])
        ).filterNot { cp -> cp.first.id == "end" } // end node can only be entered
         .filterNot { cp -> cp.second.id == "start" } // start node can only be exited

    }
}

data class Cave(val id: String) {
    fun isSmall(): Boolean {
        return id.lowercase() == id
    }
}

class PathFinder(caves: List<Pair<Cave, Cave>>) {
    private val connections: Map<Cave, List<Cave>> = caves.groupBy(keySelector = { it.first }, valueTransform = { it.second })

    fun getPaths(startingCave: Cave): Set<String> {
        val paths = mutableSetOf<String>()

        getPaths(startingCave, "", paths, setOf())

        return paths
    }

    fun getPathsWithLenience(startingCave: Cave): Set<String> {
        val paths = mutableSetOf<String>()

        getPaths(startingCave, "", paths, setOf(), lenience = null)

        return paths
    }

    private fun getPaths(cave: Cave, currentPath: String, paths: MutableSet<String>, blocked: Set<Cave>) {
        if (cave.id == "end") {
            paths.add(currentPath + cave.id)
            return
        }

        for (nextCave in connections.getOrDefault(cave, listOf())) {
            if (blocked.contains(nextCave)) continue
            if (cave.isSmall()) {
                getPaths(nextCave, currentPath + cave, paths, blocked + cave)
            } else {
                getPaths(nextCave, currentPath + cave, paths, blocked)
            }
        }
    }

    // Caves in the lenience list can be visited at most twice
    private fun getPaths(cave: Cave, currentPath: String, paths: MutableSet<String>, blocked: Set<Cave>, lenience: Cave?) {
        if (cave.id == "end") {
            paths.add(currentPath + cave.id)
            return
        }

        for (nextCave in connections.getOrDefault(cave, listOf())) {
            if (blocked.contains(nextCave)) continue
            if (cave.isSmall()) {
                if (lenience == null) {
                    // Give lenience for the current small cave once
                    getPaths(nextCave, currentPath + cave, paths, blocked, cave)
                }
                getPaths(nextCave, currentPath + cave, paths, blocked + cave, lenience)
            } else {
                getPaths(nextCave, currentPath + cave, paths, blocked, lenience)
            }
        }
    }
}