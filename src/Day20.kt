
fun main() {
    fun part1(input: List<String>): Int {
        val imageProcessor = ImageProcessor(input)
        repeat(2) { imageProcessor.enhance() }

        return imageProcessor.countPixels().first
    }

    fun part2(input: List<String>): Int {
        val imageProcessor = ImageProcessor(input)
        repeat(50) { imageProcessor.enhance() }

        return imageProcessor.countPixels().first
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day20_test")
    check(part1(testInput) == 35)
    check(part2(testInput) == 3351)
    val input = readInput("Day20")
    println(part1(input))
    println(part2(input))
}

fun ImageProcessor(input: List<String>): ImageProcessor {
    val algo = input.first().chunked(1)
    val image = (2 until input.size).map { input[it].chunked(1) }

    return ImageProcessor(image, algo)
}

class ImageProcessor(private var image: List<List<String>>,
                     private val enhancementAlgo: List<String>,
                     private var default: String = ".") {

    fun enhance() {
        // Because the image is infinite and the algorithm is applied to the whole image
        // All "dark" pixels outside the area we are processing "flip" to the value of the algorithm
        // for the value "000000000" (the first element of the array) and flip back to value of
        // "1111111111" (last element of the array) on the next run.

       image =  (-1..image.size).map { i ->
            (-1 .. image.first().size).map { j ->
                val algoIndex = surroundings(i, j).map { (a,b) -> getElementOrDefault(a, b, default) }.joinToString("") { toBin(it) }.toInt(2)
                enhancementAlgo[algoIndex]
            }
        }

        // Update default value as all the "outside" pixels have changed due to the current default
        default = if (default == ".") enhancementAlgo.first() else enhancementAlgo.last()
    }

    fun countPixels(): Pair<Int, Int> {
        var dark = 0
        var light = 0

        image.forEach { r ->
            r.forEach { c ->
                if (c == "#") light += 1
                else dark += 1
            }
        }

        return light to dark
    }

    private fun surroundings(i: Int, j: Int): List<Pair<Int, Int>> {
        return listOf(
            Pair(i-1, j-1), Pair(i-1, j), Pair(i-1, j+1),
            Pair(i, j-1),   Pair(i, j),   Pair(i, j+1),
            Pair(i+1, j-1), Pair(i+1, j), Pair(i+1, j+1),
        )
    }

    private fun getElementOrDefault(i: Int, j: Int, default: String = "."): String {
        return if (i in image.indices && j in image.first().indices) image[i][j] else default
    }

    private fun toBin(c: String): String {
        return if (c == "#") "1" else "0"
    }

    fun display() {
        image.forEach { println(it) }
    }
}