import kotlin.math.max

fun main() {
    fun parse(input: String): Element {
        val numbers = (0..9).map { it.toString() }.toSet()
        var currentNode: PairElement? = null
        var parsedNode: PairElement? = null

        for (c in input.replace(",", "").chunked(1)) {
            if (c == "[") {
                val newNode = PairElement()
                newNode.parent = currentNode

                if (currentNode != null) {
                    if (currentNode.left == null)
                        currentNode.left = newNode
                    else
                        currentNode.right = newNode
                }
                currentNode = newNode
            } else if (numbers.contains(c)) {
                if (currentNode != null) {
                    if (currentNode.left == null)
                        currentNode.left = SingleElement(c.toInt())
                    else
                        currentNode.right = SingleElement(c.toInt())
                }
            } else if (c == "]") {
                parsedNode = currentNode
                currentNode = currentNode?.parent
            }
        }

        return parsedNode!!
    }

    fun part1(input: List<String>): Int {
        val sum = input.map { parse(it) }.reduce { a,b -> a + b}
        return sum.magnitude()
    }

    fun part2(input: List<String>): Int {
        val numbers = input.map { parse(it) }
        var maxMag = Int.MIN_VALUE

        for (i in numbers.indices) {
            for (j in numbers.indices) {
                if (i != j) {
                    maxMag = max(maxMag, (numbers[i] + numbers[j]).magnitude())
                }
            }
        }

        return maxMag
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day18_test")
    check(part1(testInput) == 4140)
    check(part2(testInput) == 3993)
    val input = readInput("Day18")
    println(part1(input))
    println(part2(input))
}

operator fun Element.plus(other: Element): Element {
    var total = PairElement()

    if (this is PairElement) this.parent = total
    if (other is PairElement) other.parent = total

    total.left = this
    total.right = other

    while (total.canReduce()) {
        total = if (total.canReducePair()) {
            total.reducePair().first as PairElement
        } else {
            total.reduceSingle().first as PairElement
        }
    }

    return total
}

sealed interface Element {
    fun magnitude(): Int

    fun reducePair(): Pair<Element, Pair<Int, Int>>
    fun reduceSingle(): Pair<Element, Pair<Int, Int>>

    fun canReduce() = canReducePair() || canReduceSingle()

    fun canReducePair(): Boolean
    fun canReduceSingle(): Boolean
}

data class SingleElement(val value: Int): Element {
    override fun magnitude() = value

    override fun reduceSingle(): Pair<Element, Pair<Int, Int>> {
        return if (canReduceSingle()) split() to Pair(0, 0) else this to Pair(0, 0)
    }

    override fun reducePair() = this to Pair(0, 0)

    override fun canReduceSingle(): Boolean {
        return (value > 9)
    }

    override fun canReducePair() = false

    private fun split(): Element {
        val half = value / 2
        val pair = PairElement()

        if (value % 2 == 0) {
            pair.left = SingleElement(half)
            pair.right = SingleElement(half)
        } else {
            pair.left = SingleElement(half)
            pair.right = SingleElement(half + 1)
        }

        return pair
    }

    override fun toString() = value.toString()
}

class PairElement: Element {
    constructor()

    constructor(a: PairElement, b: Int) {
        left = a
        right = SingleElement(b)
        a.parent = this
    }

    constructor(a: Int, b: PairElement) {
        left = SingleElement(a)
        right = b
        b.parent = this
    }

    constructor(a: Int, b: Int) {
        left = SingleElement(a)
        right = SingleElement(b)
    }

    constructor(a: Element, b: Element) {
        left = a
        right = b

        if (a is PairElement) a.parent = this
        if (b is PairElement) b.parent = this
    }

    var left: Element? = null
    var right: Element? = null
    var parent: PairElement? = null

    override fun magnitude(): Int {
        return 3 * left!!.magnitude() + 2 * right!!.magnitude()
    }

    override fun canReducePair(): Boolean {
        return countParent() >= 4 || left!!.canReducePair() || right!!.canReducePair()
    }

    override fun canReduceSingle(): Boolean {
        return left!!.canReduceSingle() || right!!.canReduceSingle()
    }

    override fun reducePair(): Pair<Element, Pair<Int, Int>> {
        if (canReducePair()) { // Exists a pair on left or right that can be reduced

            if (countParent() >= 4) {
                return SingleElement(0) to Pair(left!!.magnitude(), right!!.magnitude())
            }

            if (left!!.canReducePair()) {
                return doReductionToLeft { it.reducePair() }
            }

            if (right!!.canReducePair()) {
                return doReductionToRight { it.reducePair() }
            }
        }

        return this to Pair(0, 0)
    }

    override fun reduceSingle(): Pair<Element, Pair<Int, Int>> {
        if (canReduceSingle()) { // Exists a single on left or right that can be reduced
            if (left!!.canReduceSingle()) {
                return doReductionToLeft { it.reduceSingle() }
            }

            if (right!!.canReduceSingle()) {
                return doReductionToRight { it.reduceSingle() }
            }
        }

        return this to Pair(0, 0)
    }

    private fun doReductionToRight(reducer: (node: Element) -> Pair<Element, Pair<Int, Int>>): Pair<PairElement, Pair<Int, Int>> {
        val newPair = PairElement()
        val (newRight, values) = reducer(right!!)
        var (lv, rv) = values

        newPair.right = newRight
        newPair.left = left

        if (left is SingleElement) {
            newPair.left = SingleElement(left!!.magnitude() + lv)
            lv = 0
        } else if (lv > 0) {
            val (element, v) = (left as PairElement).extendReductionToLeft(lv)
            newPair.left = element
            lv = v
        }

        if (newPair.left is PairElement) (newPair.left as PairElement).parent = newPair
        if (newPair.right is PairElement) (newPair.right as PairElement).parent = newPair

        return newPair to Pair(lv, rv)
    }

    private fun doReductionToLeft(reducer: (node: Element) -> Pair<Element, Pair<Int, Int>>): Pair<PairElement, Pair<Int, Int>> {
        val newPair = PairElement()
        val (newLeft, values) = reducer(left!!)
        var (lv, rv) = values

        newPair.left = newLeft
        newPair.right = right

        if (right is SingleElement) {
            newPair.right = SingleElement(right!!.magnitude() + rv)
            rv = 0
        } else if (rv > 0) {
            val (element, v) = (right as PairElement).extendReductionToRight(rv)
            newPair.right = element
            rv = v
        }

        if (newPair.left is PairElement) (newPair.left as PairElement).parent = newPair
        if (newPair.right is PairElement) (newPair.right as PairElement).parent = newPair

        return newPair to Pair(lv, rv)
    }

    // Find the first single element to the left and update it with the value v
    // The fist element to the left is on the right ot the pair
    private fun extendReductionToLeft(v: Int): Pair<Element, Int> {
        return if (right is SingleElement) {
            PairElement(left!!, SingleElement((right as SingleElement).magnitude() + v)) to 0
        } else {
            val (element, remainder) = (right as PairElement).extendReductionToLeft(v)
            PairElement(left!!, element) to remainder
        }
    }

    // Find the first single element to the right and update it with the value v
    // The fist single element to the right is on the left of the pair
    private fun extendReductionToRight(v: Int): Pair<Element, Int> {
        return if (left is SingleElement) {
            PairElement(SingleElement((left as SingleElement).magnitude() + v), right!!) to 0
        } else {
            val (element, remainder) = (left as PairElement).extendReductionToRight(v)
            PairElement(element, right!!) to remainder
        }
    }

    private fun countParent(): Int {
        if (parent == null) return 0
        return 1 + (parent as PairElement).countParent()
    }

    override fun toString() = "[${left},${right}]"
}