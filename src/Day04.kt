fun main() {

    fun part1(input: List<String>): Int {
        val drawnNumbers = input.first().split(",").map { it.toInt() }
        val boards = (1 until input.size step 6).map { BingoBoard(input.subList(it + 1, it + 6)) } // skip the empty line

        drawnNumbers.forEach { number ->
            // Apply drawn number to all boards, and get the first one that wins
            val winningBoard = boards.map { Pair(it.verifyNumber(number), it) }.find { it.first }?.second

            if (winningBoard != null) {
                return winningBoard.computeBingoScore(number)
            }
        }

        return -1
    }

    fun part2(input: List<String>): Int {
        val drawnNumbers = input.first().split(",").map { it.toInt() }
        val boards = (1 until input.size step 6).map {
            BingoBoard(input.subList(it + 1, it + 6))
        }.toMutableList() // skip the empty line

        var lastWinningScore = 0

        // Find all winning boards in order,
        // store the last score
        drawnNumbers.apply {
            forEach { number ->
                if (boards.isEmpty()) return@apply

                // Apply the drawn number to all boards, and check if there was any winner
                val allWinning = boards.map { Pair(it.verifyNumber(number), it) }.filter { it.first }

                // Remove all winners from boards, so we don't over-count
                allWinning.map { it.second }.forEach { boards.remove(it) }

                // Get the score of the last winner
                if (allWinning.isNotEmpty()) {
                    lastWinningScore = allWinning.last().second.computeBingoScore(number)
                }
            }
        }
        return lastWinningScore
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day04_test")
    check(part1(testInput) == 4512)
    check(part2(testInput) == 1924)

    val input = readInput("Day04")
    println(part1(input))
    println(part2(input))
}

class BingoBoard(layout: List<String>) {
    private val board = layout.map {
        it.trim().split("\\s+".toRegex()).map { n -> BingoNode(n.toInt()) }
    }.toMutableList()

    /** Verify if the number is present on the board
     * If present, mark as checked and validate
     * its col/row. If bingo, return true, else false
     */
    fun verifyNumber(number: Int): Boolean {
        board.indices.forEach { i ->
            board[i].indices.forEach { j ->
                val node = board[i][j]
                if (node.value == number) {
                    node.checked  = true
                    return checkBingo(i, j)
                }
            }
        }

        return false
    }

    /**
     * Start by finding the sum of all unmarked numbers on that board.
     * Then, multiply that sum by the number that was just called when the board won
     */
    fun computeBingoScore(winningNumber: Int): Int {
        val sumUnmarked = board.fold(0) { sum, row ->
            sum + row.filter { !it.checked }.sumOf { it.value }
        }

        return sumUnmarked * winningNumber
    }

    private fun checkBingo(i: Int, j: Int): Boolean {
        return checkBingoOnRow(i) || checkBingoOnColumn(j)
    }

    private fun checkBingoOnRow(i: Int): Boolean {
        val row = board[i]
        return row.all { it.checked }
    }

    private fun checkBingoOnColumn(j: Int): Boolean {
        val col = board.map { it[j] }
        return col.all { it.checked }
    }

    inner class BingoNode(val value: Int, var checked: Boolean = false)
}
