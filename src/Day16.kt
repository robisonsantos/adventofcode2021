import kotlin.IllegalArgumentException

fun main() {
    fun charToBin(hex: String): String {
        return when(hex) {
            "0" -> "0000"
            "1" -> "0001"
            "2" -> "0010"
            "3" -> "0011"
            "4" -> "0100"
            "5" -> "0101"
            "6" -> "0110"
            "7" -> "0111"
            "8" -> "1000"
            "9" -> "1001"
            "A" -> "1010"
            "B" -> "1011"
            "C" -> "1100"
            "D" -> "1101"
            "E" -> "1110"
            "F" -> "1111"
            else -> throw IllegalArgumentException("Not a valid hex char $hex")
        }
    }

    fun hexToBin(hex: String): String {
        return hex.chunked(1).joinToString("") { charToBin(it) }
    }

    fun sumVersions(packet: Packet): Int {
        return when(packet) {
            is LiteralValuePacket -> packet.version
            is OperatorPacket -> packet.version + packet.getSubPackets().sumOf { sumVersions(it) }
            else -> throw IllegalArgumentException("Unknown packet type")
        }
    }

    fun part1(input: List<String>): Int {
        val p = Parser(hexToBin(input.first()))
        val (packet, _) = p.parsePacket()

        return sumVersions(packet)
    }

    fun part2(input: List<String>): ULong {
        val p = Parser(hexToBin(input.first()))
        val (packet, _) = p.parsePacket()

        return packet.getValue()
    }

    // test if implementation meets criteria from the description, like:
    val testInput = readInput("Day16_test")
    check(part1(testInput) == 31)
    check(part2(testInput) == 54UL)

    val input = readInput("Day16")
    println(part1(input))
    println(part2(input))
}


class Parser(private val binBody: String) {
    private var currentPos = 0

    fun parsePacket(): Pair<Packet, String> {
        val (version, type) = parseHeader()

        return when (type) {
            is PacketType.LiteralValue -> LiteralValuePacket(version, this) to this.binBody.substring(currentPos)
            is PacketType.LengthBasedOperator -> LengthBasedOperatorPacket(version, OperationType.fromInt(type.id),this) to this.binBody.substring(currentPos)
            is PacketType.QuantityBasedOperator -> QuantityBasedOperatorPacket(version, OperationType.fromInt(type.id),this) to this.binBody.substring(currentPos)
        }
    }

    fun parseValue(): ULong {
        var binValue = ""

        for ((cont, bin) in sequence { while(true) yield(getValueChunk()) }) {
            binValue += bin
            if (cont == 0) break
        }

        return binValue.toULong(2)
    }

    fun parseLengthTypeId(): Int {
        return getChunk(1).toInt()
    }

    fun parseSubPacketsByLength(): List<Packet> {
        val length = getChunk(15).toInt(2)
        val subPackets = getChunk(length)

        val subPacketParser = Parser(subPackets)

        val subPacketsList = mutableListOf<Packet>()
        for ((packet, remainingPackets) in sequence { while(true) yield(subPacketParser.parsePacket()) }) {
            subPacketsList.add(packet)
            if (remainingPackets.isEmpty()) break
        }

        return subPacketsList
    }

    fun parseSubPacketsByQuantity(): List<Packet> {
        val quantity = getChunk(11).toInt(2)
        return (1..quantity).map { parsePacket().first }
    }

    private fun parseHeader(): Pair<Int, PacketType> {
        val version = getChunk(3).toInt(2)
        val type =  PacketType.fromTypeId(getChunk(3).toInt(2), this)

        return version to type
    }

    private fun getChunk(size: Int): String {
        val chunk = this.binBody.substring(currentPos until currentPos + size)
        currentPos += size
        return chunk
    }

    private fun getValueChunk(): Pair<Int, String> {
        val chunk = getChunk(5)
        val cont = chunk[0] - '0'
        val bin = chunk.substring(1)

        return cont to bin
    }
}

sealed class PacketType {
    object LiteralValue: PacketType()
    class LengthBasedOperator(val id: Int): PacketType()
    class QuantityBasedOperator(val id: Int): PacketType()

    companion object {
        fun fromTypeId(i: Int, parser: Parser): PacketType {
            return when (i) {
                4 -> LiteralValue
                else -> {
                    when (parser.parseLengthTypeId()) {
                        0 -> LengthBasedOperator(i)
                        1 -> QuantityBasedOperator(i)
                        else -> throw IllegalArgumentException("Unsupported value $i")
                    }
                }
            }
        }
    }
}

enum class OperationType {
    SUM,
    PRODUCT,
    MINIMUM,
    MAXIMUM,
    GREATER_THAN,
    LESS_THAN,
    EQUAL_TO;

    companion object {
        fun fromInt(id: Int): OperationType {
            return when(id) {
                0 -> SUM
                1 -> PRODUCT
                2 -> MINIMUM
                3 -> MAXIMUM
                5 -> GREATER_THAN
                6 -> LESS_THAN
                7 -> EQUAL_TO
                else -> throw IllegalArgumentException("Invalid operation type id $id")
            }
        }
    }
}

abstract class Packet(val version: Int) {
    abstract fun getValue(): ULong
}

abstract class OperatorPacket(version: Int, private val type: OperationType ): Packet(version) {
    abstract fun getSubPackets(): List<Packet>

    override fun getValue(): ULong {
       return when(type) {
            OperationType.SUM -> getSubPackets().sumOf { it.getValue() }
            OperationType.PRODUCT -> getSubPackets().fold(1UL) { acc, sp -> acc * sp.getValue()}
            OperationType.MINIMUM -> getSubPackets().minOf { it.getValue() }
            OperationType.MAXIMUM -> getSubPackets().maxOf { it.getValue() }
            OperationType.GREATER_THAN -> if (getSubPackets().first().getValue() > getSubPackets().last().getValue()) 1UL else 0UL
            OperationType.LESS_THAN -> if (getSubPackets().first().getValue() < getSubPackets().last().getValue()) 1UL else 0UL
            OperationType.EQUAL_TO -> if (getSubPackets().first().getValue() == getSubPackets().last().getValue()) 1UL else 0UL
        }
    }
}

class LiteralValuePacket(version: Int, parser: Parser): Packet(version) {
    private val value = parser.parseValue()

    override fun getValue() = value
}

class LengthBasedOperatorPacket(version: Int, type: OperationType, parser: Parser): OperatorPacket(version, type) {
    private val subPackages = parser.parseSubPacketsByLength()

    override fun getSubPackets(): List<Packet> {
        return subPackages
    }
}

class QuantityBasedOperatorPacket(version: Int, type: OperationType, parser: Parser): OperatorPacket(version, type) {
    private val subPackages = parser.parseSubPacketsByQuantity()

    override fun getSubPackets(): List<Packet> {
        return subPackages
    }
}