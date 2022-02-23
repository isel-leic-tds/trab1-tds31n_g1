package chess.model

enum class Row(val digit: Char) {
    EIGHT('8'), SEVEN('7'), SIX('6'), FIVE('5'), FOUR('4'), THREE('3'), TWO('2'), ONE('1');
    companion object {
        val values = Array<Row>(Column.values().size) { it ->
            Row.values()[it]
        }
        operator fun invoke(n: Int) = if (n in values.indices) values[n] else null
    }
    /**
     * @return the next Column or null if there are no more.
     */
    fun nextRow() = if (this.ordinal == Column.values().size-1) null else values[this.ordinal+1]
    /**
     * @return the previous Row or null if it's already the first one.
     */
    fun previousRow() = if (this.ordinal == 0) null else values[this.ordinal-1]
}

fun Char.toRowOrNull(): Row? {
    val rows: Array<Row> = Row.values()
    val row = rows.filter { row -> row.digit == this}
    if (row.isEmpty()) return null
    return row[0]
}

fun Int.toRow() = Row.values()[this]