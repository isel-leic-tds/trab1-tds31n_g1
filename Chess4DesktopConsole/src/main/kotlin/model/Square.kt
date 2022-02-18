package chess.model

class Square(val column: Column, val row: Row) {
    companion object {
        private var lineCount = 1

        /**
         * Returns all the possible Squares in a list of Squares.
         */
        val values = Array<Square>(Column.values().size * Row.values().size) { it ->
            val square = Square(Column.values[if (it < Column.values.size-1) it else it % Column.values.size],Row.values[lineCount-1])
            // when arrives to the final Column, increments the counter
            if (it >= Column.values().size-1 && it % Column.values.size == Column.values.size-1) ++lineCount
            square
        }
    }
    override fun toString() = "" + this.column.letter + this.row.digit

    fun incColumn(): Square? {
        val nextColumn = this.column.nextColumn() ?: return null
        return Square(nextColumn, this.row)
    }
    fun decColumn(): Square? {
        val prevColumn = this.column.previousColumn() ?: return null
        return Square(prevColumn, this.row)
    }
    fun incRow(): Square? {
        val nextRow = this.row.nextRow() ?: return null
        return Square(nextRow, this.row)
    }
    fun decRow(): Square? {
        val prevRow = this.row.previousRow() ?: return null
        return Square(prevRow, this.row)
    }
}

fun String.toSquareOrNull(): Square? {
    if (this.length > 2) return null
    val col = this[0].toColumnOrNull()
    val row = this[1].toRowOrNull()
    return if (col != null && row != null) Square(col, row) else null
}