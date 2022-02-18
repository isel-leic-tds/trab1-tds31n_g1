package chess.model

enum class Column(val letter: Char) {
    A('a'), B('b'), C('c'), D('d'), E('e'), F('f'), G('g'), H('h');
    companion object {
        val values = Array<Column>(Column.values().size) { it ->
            Column.values()[it]
        }
    }

    /**
     * @return the next Column or null if there are no more.
     */
    fun nextColumn() = if (this.ordinal == values().size) null else values[this.ordinal+1]
    /**
     * @return the previous Column or null if it's already the first one.
     */
    fun previousColumn() = if (this.ordinal == 0) null else values[this.ordinal-1]
}

fun Char.toColumnOrNull(): Column? {
    val cols: Array<Column> = Column.values()
    val col = cols.filter { column -> column.letter == this}
    if (col.isEmpty()) return null
    return col[0]
}

fun Int.toColumn() = Column.values()[this]

fun main() {
    print(Column.valueOf('j'.toString()))
}