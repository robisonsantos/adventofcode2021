import kotlin.math.max

fun main() {
    fun setupPlayers(input: List<String>, winningScore: Int): Triple<Player, Player, Die> {
        val pos1 = input.first().split(":")[1].trim().toInt()
        val pos2 = input.last().split(":")[1].trim().toInt()

        val die = DeterministicDie(100)
        return Triple(Player(die, pos1, 10, winningScore), Player(die, pos2, 10, winningScore), die)
    }
    
    fun part1(input: List<String>): Int {
        val (player1, player2, die) = setupPlayers(input, 1000)

        while (true) {
            player1.play()
            if (player1.isWinner()) break

            player2.play()
            if (player2.isWinner()) break
        }

        return if (player1.isWinner()) player2.getScore() * die.getNRolls() else player1.getScore() * die.getNRolls()
    }

    fun part2(input: List<String>): ULong {
        // I could not solve this one during my time box. Took inspiration from other solutions
        val pos1 = input.first().split(":")[1].trim().toInt()
        val pos2 = input.last().split(":")[1].trim().toInt()

        val diracGame = DiracGame()
        val wins = diracGame.countWins(Input(PlayerId.PLAYER_1, pos1, 0, pos2, 0))

        return max(wins.first, wins.second)
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day21_test")
    check(part1(testInput) == 739785)
    check(part2(testInput) == 444356092776315UL)

    val input = readInput("Day21")
    println(part1(input))
    println(part2(input))
}

sealed interface Die {
    fun roll(): Int
    fun getNRolls(): Int
}
class DeterministicDie(private val size: Int): Die {
    private var rolls = 0

    override fun roll(): Int {
        val value = rolls % size + 1
        rolls += 1

        return value
    }

    override fun getNRolls() = rolls
}

class Player(private val die: Die, initialPosition: Int, private val boardSize: Int, private val winningScore: Int) {
    private var score = 0
    private var position = initialPosition

    fun play() {
        val moveBy = (1..3).sumOf { die.roll() }
        position = (position + moveBy - 1) % boardSize + 1
        score += position
    }

    fun getScore(): Int {
        return score
    }

    fun isWinner() = score >= winningScore
}

enum class PlayerId { PLAYER_1, PLAYER_2 }

// Used to cache the results
data class Input(val player: PlayerId, val pos1: Int, val score1: Int, val pos2: Int, val score2: Int)

class DiracGame {
    private val mem = mutableMapOf<Input, Pair<ULong, ULong>>()
    private val combinations = mutableListOf<List<Int>>()

    init {
        (1..3).forEach { i ->
            (1..3).forEach { j ->
                (1..3).forEach { k ->
                    combinations.add(listOf(i, j, k))
                }
            }
        }
    }

    fun countWins(input: Input): Pair<ULong, ULong> {
        if (mem.contains(input)) {
            return mem[input]!!
        }

        if (input.score1 >= 21) {
            mem[input] = 1UL to 0UL
            return mem[input]!!
        }

        if (input.score2 >= 21) {
            mem[input] = 0UL to 1UL
            return mem[input]!!
        }

        var wins = 0UL to 0UL

        for (roll in combinations) {
            val (w0, w1) = if(input.player == PlayerId.PLAYER_1) {
                val (newP1Pos, newP1Score) = move(input.pos1, input.score1, roll.sum())
                countWins(Input(PlayerId.PLAYER_2, newP1Pos, newP1Score, input.pos2, input.score2))
            } else {
                val (newP2Pos, newP2Score) = move(input.pos2, input.score2, roll.sum())
                countWins(Input(PlayerId.PLAYER_1, input.pos1, input.score1, newP2Pos, newP2Score))
            }

            wins = wins.first + w0 to wins.second + w1
        }

        mem[input] = wins
        return mem[input]!!
    }

    private fun move(pos: Int, score: Int, roll: Int): Pair<Int, Int> {
        val newPos = (pos + roll - 1) % 10 + 1
        val newScore = score + newPos
        return newPos to newScore
    }
}
