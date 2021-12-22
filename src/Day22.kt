import kotlin.math.abs

fun main() {
    fun parseActions(input: List<String>): List<RebootStep> {
        return input.map {
            val value = it.startsWith("on")
            val ranges = it.split(" ")[1]
                            .split(",")
                            .map { t -> t.split("=")[1] }
                            .map { r ->
                                val boundaries = r.split("..").map { b -> b.toLong() }.sorted()
                                boundaries[0]..boundaries[1]
                            }
            RebootStep(ranges[0], ranges[1], ranges[2], value)
        }
    }

    fun part1(input: List<String>): Long {
        val space = parseActions(input).filter {
            val(xRange, yRange, zRange, _) = it
            xRange.first >= -50 && xRange.last <= 50 &&
            yRange.first >= -50 && yRange.last <= 50 &&
            zRange.first >= -50 && zRange.last <= 50
        }.map {
            val(xRange, yRange, zRange, value) = it
            Cube(Triple(xRange.first, yRange.first, zRange.first), Triple(xRange.last, yRange.last, zRange.last)) to value
        }.map {
            Space(listOf(it.first)) to it.second
        }.fold(Space(listOf())) { space, action ->
            if (action.second) {
                space + action.first
            } else {
                space - action.first
            }
        }

        return space.volume()
    }

    fun part2(input: List<String>): Long {
        val space = parseActions(input).map {
            val(xRange, yRange, zRange, value) = it
            Cube(Triple(xRange.first, yRange.first, zRange.first), Triple(xRange.last, yRange.last, zRange.last)) to value
        }.map {
            Space(listOf(it.first)) to it.second
        }.fold(Space(listOf())) { space, action ->
            if (action.second) {
                space + action.first
            } else {
                space - action.first
            }
        }

        return space.volume()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day22_test")
    check(part1(testInput) == 474140L)
    check(part2(testInput) == 2758514936282235L)

    val input = readInput("Day22")
    println(part1(input))
    println(part2(input))
}

data class RebootStep(val xRange: LongRange, val yRange: LongRange, val zRange: LongRange, val value: Boolean)

// Cubes with negative sign will generate negative volumes. This means that in a space, they are hollow (or not present)
class Cube(private val p1: Triple<Long, Long, Long>, private val p2: Triple<Long, Long, Long>, private val sign: Int = 1) {
    fun intersection(other: Cube): Cube? {

        val newSign = if (this.sign < 0 || other.sign < 0) -1 else 1

        val (xa1, ya1, za1) = this.p1
        val (xa2, ya2, za2) = this.p2

        val (xb1, yb1, zb1) = other.p1
        val (xb2, yb2, zb2) = other.p2

        // Case1: they do not intersect
        if (xa2 < xb1 || xb2 < xa1 || ya2 < yb1 || yb2 < ya1 || za2 < zb1 || zb2 < za1) {
            return null
        }

        //---------------------------------------
        // Case2: they intersect totally
        // Either cube it totally inside the other
        //---------------------------------------

        // Check this cube is inside the other cube
        if ((xa1 >= xb1 && xa2 <= xb2) && (ya1 >= yb1 && ya2 <= yb2) && (za1 >= zb1 && za2 <= zb2)) {
            return Cube(p1, p2, newSign)
        }

        // Check the other cube is inside this one
        if ((xb1 >= xa1 && xb2 <= xa2) && (yb1 >= ya1 && yb2 <= ya2) && (zb1 >= za1 && zb2 <= za2)) {
            return Cube(other.p1, other.p2, newSign)
        }

        //-------------------------------------
        // Case3: they intersect partially
        // Either cube is partially inside the other one
        //------------------------------------

        val newX1: Long
        val newX2: Long

        val newY1: Long
        val newY2: Long

        val newZ1: Long
        val newZ2: Long

        if (xa1 < xb1) {
            newX1 = xb1
            newX2 = if (xa2 > xb2) xb2 else xa2
        } else {
            newX1 = xa1
            newX2 = if (xa2 > xb2) xb2 else xa2
        }

        if (ya1 < yb1) {
            newY1 = yb1
            newY2 = if (ya2 > yb2) yb2 else ya2
        } else {
            newY1 = ya1
            newY2 = if (ya2 > yb2) yb2 else ya2
        }

        if (za1 < zb1) {
            newZ1 = zb1
            newZ2 = if (za2 > zb2) zb2 else za2
        } else {
            newZ1 = za1
            newZ2 = if (za2 > zb2) zb2 else za2
        }

        return Cube(Triple(newX1, newY1, newZ1), Triple(newX2, newY2, newZ2), newSign)
    }

    fun volume(): Long {
        val (x1, y1, z1) = p1
        val (x2, y2, z2) = p2

        // Ranges are inclusive
        val dx = abs(x2-x1) + 1
        val dy = abs(y2-y1) + 1
        val dz = abs(z2-z1) + 1

        return sign * ( dx  * dy * dz )
    }

    fun reverse() = Cube(p1, p2, sign * -1)
}

class Space(private val cubes: List<Cube>) {
    operator fun plus(other: Space): Space {
        val intersections = this.cubes.flatMap { c1 -> other.cubes.map { c2 -> c1.intersection(c2) } }.filterNotNull().map { it.reverse() }
        val newCubes = mutableListOf<Cube>()

        newCubes += this.cubes
        newCubes += other.cubes
        newCubes += intersections

        return Space(newCubes)
    }

    operator fun minus(other: Space): Space {
        val intersections = this.cubes.flatMap { c1 -> other.cubes.map { c2 -> c1.intersection(c2) } }.filterNotNull().map { it.reverse() }

        val newCubes = mutableListOf<Cube>()
        newCubes += this.cubes
        newCubes += intersections

        return Space(newCubes)
    }

    fun volume(): Long {
        return cubes.sumOf { it.volume() }
    }
}

