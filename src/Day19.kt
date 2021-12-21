import java.util.*
import kotlin.math.abs
import kotlin.math.max

fun main() {
    fun parse(input: List<String>): List<Set<SPoint>> {
        val coordinatesPerScanner = mutableListOf<Set<SPoint>>()
        var scannerList: MutableSet<SPoint>? = null

        for (row in input) {
            when(row.trim()) {
                "" -> coordinatesPerScanner.add(scannerList!!)
                else -> when(row.startsWith("---")) {
                    true -> scannerList = mutableSetOf()
                    false -> {
                        val tokens = row.split(",").map { it.toInt() }
                        scannerList!!.add(SPoint(tokens[0], tokens[1], tokens[2]))
                    }
                }
            }
        }

        // Add the last one
        coordinatesPerScanner.add(scannerList!!)

        return coordinatesPerScanner
    }

    fun part1(input: List<String>): Int {
        val scans = parse(input)
        val probeScanner = ProbeScanner(scans)

        val sol = probeScanner.getScannersAndProbes()
        return sol.second.size
    }

    fun part2(input: List<String>): Int {
        val scans = parse(input)
        val probeScanner = ProbeScanner(scans)

        val sol = probeScanner.getScannersAndProbes()
        val scanners = sol.first

        var maxDist = Int.MIN_VALUE
        for (s1 in scanners) {
            for (s2 in scanners) {
                maxDist = max(maxDist, s1.manhattanDistance(s2))
            }
        }

        return maxDist
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day19_test")
    check(part1(testInput) == 79)
    check(part2(testInput) == 3621)
    val input = readInput("Day19")
    println(part1(input))
    println(part2(input))
}

typealias SPoint = Triple<Int, Int, Int>

operator fun SPoint.minus(other: SPoint): SPoint {
    val (x1,y1,z1) = this
    val (x2,y2,z2) = other

    return SPoint(x1-x2, y1-y2, z1-z2)
}

operator fun SPoint.plus(other: SPoint): SPoint {
    val (x1,y1,z1) = this
    val (x2,y2,z2) = other

    return SPoint(x1+x2, y1+y2, z1+z2)
}

fun SPoint.manhattanDistance(other: SPoint): Int {
    val (x1,y1,z1) = this
    val (x2,y2,z2) = other

    return abs(x1-x2) + abs(y1-y2) + abs(z1-z2)
}

class ProbeScanner(private val scannersReadings: List<Set<SPoint>>) {
    // Represents all the possible transformations of a point in the 3D space

    private val POINT_TRANSFORMATIONS = listOf(
        // Face forward
        {p:SPoint -> SPoint(p.first, p.second, p.third)}, // (x, y, z)
        {p:SPoint -> SPoint(-p.second, p.first, p.third)}, // (-y, x, z)
        {p:SPoint -> SPoint(-p.first, -p.second, p.third)}, // (-x, -y, z)
        {p:SPoint -> SPoint(p.second, -p.first, p.third)}, // (y, -x, z)

        // Face left
        {p:SPoint -> SPoint(p.third, -p.first, -p.second)}, // (z, -x, -y)
        {p:SPoint -> SPoint(p.third, -p.second, p.first)}, // (z, -y, x)
        {p:SPoint -> SPoint(p.third, p.first, p.second)}, // (z, x, y)
        {p:SPoint -> SPoint(p.third, p.second, -p.first)}, // (z, y ,-x)

        // Face back
        {p:SPoint -> SPoint(-p.first, p.second, -p.third)}, // (-x, y, -z)
        {p:SPoint -> SPoint(-p.second, -p.first, -p.third)}, // (-y, -x, -z)
        {p:SPoint -> SPoint(p.first, -p.second, -p.third)}, // (x, -y, -z)
        {p:SPoint -> SPoint(p.second, p.first, -p.third)}, // (y, x, -z)

        // Face right
        {p:SPoint -> SPoint(-p.third, p.second, p.first)}, // (-z, y, x)
        {p:SPoint -> SPoint(-p.third, -p.first, p.second)}, // (-z, -x, y)
        {p:SPoint -> SPoint(-p.third, -p.second, -p.first)}, // (-z, -y, -x)
        {p:SPoint -> SPoint(-p.third, p.first, -p.second)}, // (-z, x, -y)

        // Face up
        {p:SPoint -> SPoint(p.first, p.third, -p.second)}, // (x, z, -y)
        {p:SPoint -> SPoint(-p.second, p.third, -p.first)}, // (-y, z, -x) -->
        {p:SPoint -> SPoint(-p.first, p.third, p.second)}, // (-x, z, y)
        {p:SPoint -> SPoint(p.second, p.third, p.first)}, // (y, z, x)

        // Face down
        {p:SPoint -> SPoint(p.first, -p.third, p.second)}, // (x, -z, y)
        {p:SPoint -> SPoint(p.second, -p.third, -p.first)}, // (y, -z, -x)
        {p:SPoint -> SPoint(-p.first, -p.third, -p.second)}, // (-x, -z, -y)
        {p:SPoint -> SPoint(-p.second, -p.third, p.first)}, // (-y, -z, x) -->
    )

    fun getScannersAndProbes(): Pair<Set<SPoint>, Set<SPoint>> {
        val knownScanners = mutableSetOf(SPoint(0,0,0)) // First scanner is at 0,0,0 location
        val knownProbes = scannersReadings.first().toMutableSet() // Assume points from first scanner is correct and known

        val queue = LinkedList<Set<SPoint>>() // All readings that need to be evaluated
        queue.addAll(scannersReadings.drop(1))

        while (queue.isNotEmpty()) {
            val nextReadings = queue.removeFirst()
            when (val scannerAndProbes = tryFindScannerAndProbes(knownProbes, nextReadings)) {
                null -> queue.add(nextReadings) // We did not find a correlation between the scanners evaluated yet
                else -> {
                    knownScanners.add(scannerAndProbes.first)
                    knownProbes.addAll(scannerAndProbes.second)
                }
            }
        }

        return knownScanners to knownProbes
    }

    // Try to find points that intersect with the known points
    // Compare the known points with all the possible transformations of the readings
    // Because the readings can be at any direction, only accepts when we found 12+ points in common
    // If found, then that direction is probably the right one.
    // Returns the translated points together if the position of the scanner that made the reading
    private fun tryFindScannerAndProbes(knownProbes: Set<SPoint>, readings: Set<SPoint>): Pair<SPoint, Set<SPoint>>? {
        val allTransformations = POINT_TRANSFORMATIONS.map { t -> readings.map { t(it) }.toSet() }

        return knownProbes.firstNotNullOfOrNull { knownPoint ->
            allTransformations.firstNotNullOfOrNull { transformedPoints ->
                transformedPoints.firstNotNullOfOrNull { transformedPoint ->
                    val diff = knownPoint - transformedPoint // This should be the position of the scanner if the direction is right
                    val translatedPoints = transformedPoints.map { it + diff }.toSet() // Translate all points using the difference: this should put all points in the same alignment
                    if (translatedPoints.intersect(knownProbes).size >= 12) { // We found scanners that overlap
                        diff to translatedPoints
                    } else {
                        null
                    }
                }
            }
        }
    }
}