import kotlin.math.max
import kotlin.streams.toList

fun main() {
    operator fun String.minus(b:String): String {
        return (this.split("".toRegex()).toSet() - b.split("".toRegex()).toSet()).joinToString("")
    }

    // Just count how many entries on the "output" part are decodable
    fun part1(input: List<String>): Int {
        // The simplest way is to just count how many entries
        // in the output display contains "unique" sizes that map to single "digit"

        val identifiableSizes = setOf(2,4,3,7) // representing numbers 1, 4, 7, 8
        return input.map { it.split("|") }
                    .flatMap { it[1].trim().split(" ") }
                    .map { it.length }
                    .filter { identifiableSizes.contains(it) }
                    .size
    }

    // Apply some rules to the group of strings to
    // figure out the mapping:
    // - size 2 => 1
    // - size 4 => 4
    // - size 3 => 7
    // - size 7 => 8
    //
    // - a string with size 6 - '4'
    //   - if size == 2 => 9
    // - a string with size 5, where '9' - 5 - '1'
    //   - if size == 0 => 5
    // - a string with size 5 - '1'
    //   - if size == 3 => 3
    //   - if size == 4 => 2
    // - '9' - '5' == signal 'c'
    // - a string with size 6 - 'c'
    //   - if size == 5 => 0
    //   - else => 6
    fun getMapping(codes: List<String>): Map<List<Int>, Int> {
        val stringToInt = mutableMapOf<List<Int>, Int>()
        val codesBySize = codes.groupByTo(mutableMapOf()) { it.length }

        // - size 2 => 1
        val one = codesBySize[2]!!.removeFirst()
        stringToInt[one.chars().sorted().toList()] = 1

        // - size 4 => 4
        val four = codesBySize[4]!!.removeFirst()
        stringToInt[four.chars().sorted().toList()] = 4

        // - size 3 => 7
        stringToInt[codesBySize[3]!!.removeFirst().chars().sorted().toList()] = 7

        // - size 7 => 8
        stringToInt[codesBySize[7]!!.removeFirst().chars().sorted().toList()] = 8

        // - a string with size 6 - '4'
        //   - if size == 2 => 9
        val nine = codesBySize[6]!!.first { (it - four).length == 2 }
        stringToInt[nine.chars().sorted().toList()] = 9
        codesBySize[6]!!.remove(nine)

        // - a string with size 5, where '9' - 5 - '1'
        //   - if size == 0 => 5
        val five = codesBySize[5]!!.first { (nine - it - one).isEmpty() }
        stringToInt[five.chars().sorted().toList()] = 5
        codesBySize[5]!!.remove(five)

        // - a string with size 5 - '1'
        //   - if size == 3 => 3
        //   - if size == 4 => 2
        val three = codesBySize[5]!!.first { (it - one).length == 3 }
        stringToInt[three.chars().sorted().toList()] = 3
        codesBySize[5]!!.remove(three)

        val two = codesBySize[5]!!.first { (it - one).length == 4 }
        stringToInt[two.chars().sorted().toList()] = 2
        codesBySize[5]!!.remove(two)

        // - '9' - '5' == signal 'c'
        val signalC = nine - five

        // - a string with size 6 - 'c'
        //   - if size == 5 => 0
        //   - else => 6
        val zero = codesBySize[6]!!.first { (it - signalC).length == 5 }
        stringToInt[zero.chars().sorted().toList()] = 0
        codesBySize[6]!!.remove(zero)

        val six = codesBySize[6]!!.first()
        stringToInt[six.chars().sorted().toList()] = 6
        codesBySize[6]!!.remove(six)

        return stringToInt
    }

     fun decode(encoded: String): List<Int> {
         val tokens = encoded.split("|")
         val codes = tokens[0].trim().split(" ")
         val outputs = tokens[1].trim().split(" ")

         val mapping = getMapping(codes)
         return outputs.map { mapping[it.chars().sorted().toList()]!! }
     }

    fun toNumber(digits: List<Int>): Int {
        return digits.joinToString("") { it.toString() }.toInt()
    }

    fun part2(input: List<String>): Int {
        return input.sumOf { toNumber(decode(it)) }
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day08_test")
    check(part1(testInput) == 26)
    check(part2(testInput) == 61229)

    val input = readInput("Day08")
    println(part1(input))
    println(part2(input))
}
