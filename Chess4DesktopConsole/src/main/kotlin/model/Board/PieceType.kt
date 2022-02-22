package model.Board

import chess.model.Row
import chess.model.Square
import model.Player
import kotlin.math.abs

fun PieceType.toStr() =
    when(this) {
        is Pawn -> "P"
        is Knight -> "N"
        is Bishop -> "B"
        is Rook -> "R"
        is Queen -> "Q"
        else -> "K"
    }

fun getPieceType(type: Char) =
    when(type) {
        'P' -> Pawn()
        'N' -> Knight()
        'B' -> Bishop()
        'R' -> Rook()
        'Q' -> Queen()
        'K' -> King()
        else -> null
    }

sealed class PieceType
class Pawn(val twoSteps:Boolean = false): PieceType()
class Knight: PieceType()
class Bishop: PieceType()
class Rook(val hasMoved:Boolean = false): PieceType()
class Queen: PieceType()
class King(val hasMoved:Boolean = false): PieceType()

fun tryToMove(move: Move, table: Array<Array<Board.Piece?>>): Boolean {
    val piece = table[move.curSquare.row.ordinal][move.curSquare.column.ordinal]
    if (piece == null || piece.type.toStr() != move.piece.toStr()) return false
    if (canEnPassant(move, table)) return true
    if (canCastle(move, table)) return true
    return when (piece.type) {
        is Pawn -> tryMovePawn(move.curSquare, move.newSquare, table)
        is Bishop -> tryMoveBishop(move.curSquare, move.newSquare, table)
        is Rook -> tryMoveRook(move.curSquare, move.newSquare, table)
        is Knight -> tryMoveKnight(move.curSquare, move.newSquare, table)
        is Queen -> tryMoveQueen(move.curSquare, move.newSquare, table)
        is King -> tryMoveKing(move.curSquare, move.newSquare, table)
    }
}

fun tryMoveKing(curSquare: Square, newSquare: Square, table: Array<Array<Board.Piece?>>): Boolean {
    val moveStats = getMoveStats(curSquare, newSquare, table) ?: return false
    val player = moveStats.player
    if (moveStats.pieceType !is King) return false
    val possibleSquares = listOfNotNull(
        curSquare.moveUp(player),
        curSquare.moveUp(player)?.moveLeft(player),
        curSquare.moveUp(player)?.moveRight(player),
        curSquare.moveLeft(player),
        curSquare.moveRight(player),
        curSquare.moveDown(player),
        curSquare.moveDown(player)?.moveLeft(player),
        curSquare.moveDown(player)?.moveRight(player)
    )
    return checkSquares(possibleSquares, player, newSquare, table)
}

fun tryMoveQueen(curSquare: Square, newSquare: Square, table: Array<Array<Board.Piece?>>): Boolean {
    val moveStats = getMoveStats(curSquare, newSquare, table) ?: return false
    val player = moveStats.player
    val (rowDif, colDif) = moveStats.diff
    if (checkDiagonals(rowDif, curSquare, colDif, table, newSquare, player)) return true
    return checkHorizontalsVerticals(rowDif, colDif, curSquare, table, newSquare, player)
}

fun tryMoveKnight(curSquare: Square, newSquare: Square, table: Array<Array<Board.Piece?>>): Boolean {
    val moveStats = getMoveStats(curSquare, newSquare, table) ?: return false
    val player = moveStats.player
    if (moveStats.pieceType !is Knight) return false
    val possibleSquares = listOfNotNull(
        curSquare.moveUp(player)?.moveUp(player)?.moveLeft(player),
        curSquare.moveUp(player)?.moveUp(player)?.moveRight(player),
        curSquare.moveLeft(player)?.moveLeft(player)?.moveDown(player),
        curSquare.moveLeft(player)?.moveLeft(player)?.moveUp(player),
        curSquare.moveRight(player)?.moveRight(player)?.moveUp(player),
        curSquare.moveLeft(player)?.moveLeft(player)?.moveDown(player),
        curSquare.moveRight(player)?.moveRight(player)?.moveDown(player),
        curSquare.moveDown(player)?.moveDown(player)?.moveLeft(player),
        curSquare.moveDown(player)?.moveDown(player)?.moveRight(player)
    )
    return checkSquares(possibleSquares, player, newSquare, table)
}

fun tryMoveRook(curSquare: Square, newSquare: Square, table: Array<Array<Board.Piece?>>): Boolean {
    val moveStats = getMoveStats(curSquare, newSquare, table) ?: return false
    val player = moveStats.player
    val (rowDif, colDif) = moveStats.diff
    if (moveStats.pieceType !is Rook) return false
    return checkHorizontalsVerticals(rowDif, colDif, curSquare, table, newSquare, player)
}

/**
 * Tries to move Bishop from [curSquare] to [newSquare].
 * @return false if there is no Bishop in [curSquare] or the Bishop cannot be moved to [newSquare]. Othewise, returns true.
 */
private fun tryMoveBishop(curSquare: Square, newSquare: Square, table: Array<Array<Board.Piece?>>): Boolean {
    val moveStats = getMoveStats(curSquare, newSquare, table) ?: return false
    val player = moveStats.player
    val (rowDif, colDif) = moveStats.diff
    if (moveStats.pieceType !is Bishop) return false
    return checkDiagonals(rowDif, curSquare, colDif, table, newSquare, player)
}

/**
 * Tries to move Pawn from [curSquare] to [newSquare].
 * @return false if there is no Pawn in [curSquare] or the Pawn cannot be moved to [newSquare]. Othewise, returns true.
 */
private fun tryMovePawn(curSquare: Square, newSquare: Square, table: Array<Array<Board.Piece?>>): Boolean {
    val moveStats = getMoveStats(curSquare, newSquare, table) ?: return false
    val player = moveStats.player
    val (rowDif, colDif) = moveStats.diff
    // cannot move more than two steps vertically
    if (moveStats.pieceType !is Pawn || abs(rowDif) > 2) return false
    if (colDif == 0) {
        // doesnt move
        if (rowDif == 0) return false
        // moving foward
        if (rowDif < 0 && player === Player.WHITE || rowDif > 0 && player === Player.BLACK) {
            // cheks front Square
            if (hasPiece(curSquare.moveUp(player)!!, table)) return false
            if (abs(rowDif) == 1) return true
            // trying to move two squares
            if (
                abs(rowDif) == 2 &&
                player === Player.WHITE && curSquare.row === Row.TWO || player === Player.BLACK && curSquare.row === Row.SEVEN &&
                !hasPiece(curSquare.moveUp(player)!!.moveUp(player)!!, table)
            )
                        return true
            return false
        }
    }
    // trying to eat
    if (abs(colDif) == 1 && abs(rowDif) == 1) {
       val squareFrontLeft = curSquare.moveUp(player)?.moveLeft(player)
        val squareFrontRight = curSquare.moveUp(player)?.moveRight(player)
        if (newSquare == squareFrontLeft && hasPiece(squareFrontLeft, table) && hasEnemyPiece(player, squareFrontLeft, table)
                || newSquare == squareFrontRight && hasPiece(squareFrontRight, table) && hasEnemyPiece(player, squareFrontRight, table)
        ) return true
    }
    return false

}

/**
 * Checks if it is possible to make enPassant with given parameters.
 */
fun canEnPassant(move: Move, table: Array<Array<Board.Piece?>>): Boolean {
    val diffCol = move.newSquare.column.ordinal - move.curSquare.column.ordinal
    // playerPiece will never be null because we called isValidSquare()
    val piece = table[move.curSquare.row.ordinal][move.curSquare.column.ordinal] ?: return false
    if (piece.type !is Pawn) return false
    if (diffCol == -1) { //Left
        val advPawn = table[move.curSquare.row.ordinal][move.curSquare.column.ordinal - 1]
        if (advPawn != null && piece.player != advPawn.player)
            if (advPawn.type is Pawn && advPawn.type.twoSteps)
                return true
    } else if (diffCol == 1) { //Right
        val advPawn = table[move.curSquare.row.ordinal][move.curSquare.column.ordinal + 1]
        if (advPawn != null && piece.player != advPawn.player)
            if (advPawn.type is Pawn && advPawn.type.twoSteps)
                return true
    }
    return false
}

/**
 * Checks if it is possible to make a castling move.
 */
// TODO -> fix!
fun canCastle(move: Move, table: Array<Array<Board.Piece?>>): Boolean {
    val moveStats = getMoveStats(move.curSquare, move.newSquare, table) ?: return false
    val player = moveStats.player
    if (moveStats.pieceType !is King || moveStats.pieceType.hasMoved) return false
    // checks row dif
    if (moveStats.diff.first != 0) return false
    if (moveStats.diff.second == 2) { // to right
        val piece = table[move.newSquare.row.ordinal][move.newSquare.column.ordinal+1]
        if (piece != null && piece.type is Rook && !piece.type.hasMoved) {
            val square1 = move.curSquare.moveRight(player) ?: return false
            val square2 = square1.moveRight(player) ?: return false
            // checks if there is at least one piece between King and Rook
            return !listOf(square1, square2).map { table[it.row.ordinal][it.column.ordinal] }
                .any{it != null}
        }
    } else if (moveStats.diff.second == -2) { // to left
        val piece = table[move.newSquare.row.ordinal][move.newSquare.column.ordinal-2]
        if (piece != null && piece.type is Rook && !piece.type.hasMoved) {
            val square1 = move.curSquare.moveLeft(player) ?: return false
            val square2 = square1.moveLeft(player) ?: return false
            val square3 = square2.moveLeft(player) ?: return false
            // checks if there is at least one piece between King and Rook
            return !listOf(square1, square2, square3).map { table[it.row.ordinal][it.column.ordinal] }
                .any { it != null }
        }
    }
    return false
}


private fun getSquareDiff(curSquare: Square, newSquare: Square) =
    newSquare.row.ordinal - curSquare.row.ordinal to newSquare.column.ordinal - curSquare.column.ordinal

/**
 * @throws
 */
private fun hasEnemyPiece(player: Player, square: Square, table: Array<Array<Board.Piece?>>): Boolean {
    require( hasPiece(square, table) )
    return table[square.row.ordinal][square.column.ordinal]?.player !== player
}

private fun hasFriendlyPiece(player: Player, square: Square, table: Array<Array<Board.Piece?>>): Boolean {
    require( hasPiece(square, table) )
    return table[square.row.ordinal][square.column.ordinal]?.player === player
}
private fun hasPiece(square: Square, table: Array<Array<Board.Piece?>>) =
    table[square.row.ordinal][square.column.ordinal]?.player !== null
private fun Square.moveUp(player: Player) = if (player === Player.WHITE) this.decRow() else this.incRow()

private fun Square.moveDown(player: Player) = if (player === Player.WHITE) this.incRow() else this.decRow()
private fun Square.moveLeft(player: Player) = if (player === Player.WHITE) this.decColumn() else this.incColumn()
private fun Square.moveRight(player: Player) = if (player === Player.WHITE) this.incColumn() else this.decColumn()
private data class MoveStats(val pieceType: PieceType, val player: Player, val diff: Pair<Int, Int>)

/**
 * @return MoveStats object that contains the player and a Pair contaning the rowDif and colDif.
 * @return null if theres no piece in [curSquare]
 */
private fun getMoveStats(curSquare: Square, newSquare: Square, table: Array<Array<Board.Piece?>>): MoveStats? {
    val piece = table[curSquare.row.ordinal][curSquare.column.ordinal] ?: return null
    val player = piece.player
    val (rowDif, colDif) = getSquareDiff(curSquare, newSquare)
    return MoveStats(piece.type, player, Pair(rowDif, colDif))
}

/**
 * Checks if diagonal squares given by [rowDif] and [colDif] have some piece.
 * @return false if there are no diagonal squares to check.
 */
private fun checkDiagonals(
    rowDif: Int,
    curSquare: Square,
    colDif: Int,
    table: Array<Array<Board.Piece?>>,
    newSquare: Square,
    player: Player
): Boolean {
    if (abs(rowDif) != abs(colDif) || rowDif == 0) return false
    // hold the direction to iterate in the Board
    var n = 0
    repeat(abs(rowDif) - 1) { i -> // TODO -> i is not being incremented
        val nextSquare = Square(
            curSquare.column.ordinal + if (colDif > 0) n + 1 else -n - 1,
            curSquare.row.ordinal + if (rowDif > 0) n + 1 else -n - 1
        ) ?: return false
        if (hasPiece(nextSquare, table) && nextSquare != newSquare)
            return false
        ++n
    }
    if (hasPiece(newSquare, table) && hasFriendlyPiece(player, newSquare, table))
        return false
    return true
}

/**
 * Checks if horizontal or vertical squares given by [rowDif] and [colDif] have some piece.
 * @return false if there are no horizontal or vertical squares to check.
 */
private fun checkHorizontalsVerticals(
    rowDif: Int,
    colDif: Int,
    curSquare: Square,
    table: Array<Array<Board.Piece?>>,
    newSquare: Square,
    player: Player
): Boolean {
    if (rowDif != 0 && colDif != 0 || rowDif == 0 && colDif == 0) return false
    var n = 0
    // number of squares to iterate in following loop
    val times = if (rowDif == 0) abs(colDif) - 1 else abs(rowDif) - 1
    repeat(times) { i -> // TODO -> i is not being incremented
        val nextSquare = Square(
            curSquare.column.ordinal + if (colDif == 0) 0 else if (colDif < 0) -n - 1 else n + 1,
            curSquare.row.ordinal + if (rowDif == 0) 0 else if (rowDif < 0) -n - 1 else n + 1
        ) ?: return false
        if (hasPiece(nextSquare, table) && nextSquare != newSquare)
            return false
        ++n
    }
    if (hasPiece(newSquare, table) && hasFriendlyPiece(player, newSquare, table))
        return false
    return true
}

private fun checkSquares(list: List<Square>, player: Player, newSquare: Square, table: Array<Array<Board.Piece?>>): Boolean {
    return list.map{ square -> // checks if any of the squares matches the newSquare and if is suiteble to be moved to
        newSquare == square && (hasPiece(square, table) && hasEnemyPiece(player, square, table) || !hasPiece(square, table))
    }.filter { it }.isNotEmpty()
}
