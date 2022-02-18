package model.Board

import chess.model.Square
import model.Player

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

fun tryToMove(move: Move, board: Board): Boolean {
    val table = board.boardArr
    val piece = table[move.curSquare.row.ordinal][move.newSquare.column.ordinal]
    if (piece == null || piece.type.toStr() != move.piece.toStr()) return false
    when (piece.type) {
        is Pawn -> tryMovePawn(move.curSquare, move.newSquare, board)
        is Bishop -> tryMoveBishop(move, board)
        is Rook -> tryMoveRook(move, board)
        is Knight -> tryMoveKnight(move, board)
        is Queen -> tryMoveQueen(move, board)
        is King -> tryMoveKing(move, board)
    }
}

/**
 * Tries to move Pawn from [curSquare] to [newSquare].
 * @return false if there is no Pawn in [curSquare] or the Pawn cannot be moved to [newSquare]. Othewise, returns true.
 */
private fun tryMovePawn(curSquare: Square, newSquare: Square, board: Board): Boolean {
    val table = board.boardArr
    val piece = table[curSquare.row.ordinal][newSquare.column.ordinal]
    if (piece == null || piece.type !is Pawn) return false
    val player = piece.player
    val (rowDif, colDif) = getSquareDiff(curSquare, newSquare)
    // cannot move more than two steps vertically
    if (rowDif > 2) return false
    if (colDif == 0) {
        // doesnt move
        if (rowDif == 0) return false
        // moving foward
        if (rowDif < 0 && player === Player.WHITE || rowDif > 0 && player === Player.BLACK) {
            // cheks front Square
            if (hasEnemyPiece(player, curSquare.moveUp(player)!!, table)) return false
            // cheks second front Square
            if (rowDif == 2 && hasEnemyPiece(player, curSquare.moveUp(player)!!.moveUp(player)!!, table)) return false
            return true
        }
    }
}

private fun getRowDiff(curSquare: Square, newSquare: Square) = newSquare.row.ordinal - curSquare.row.ordinal
private fun getColDiff(curSquare: Square, newSquare: Square) = newSquare.column.ordinal - curSquare.column.ordinal
private fun getSquareDiff(curSquare: Square, newSquare: Square) =
    newSquare.row.ordinal - curSquare.row.ordinal to newSquare.column.ordinal - curSquare.column.ordinal

private fun hasEnemyPiece(player: Player, square: Square, table: Array<Array<Board.Piece?>>) =
    table[square.row.ordinal][square.column.ordinal]?.player !== player

private fun Square.moveUp(player: Player): Square? = if (player === Player.WHITE) this.incRow() else this.decRow()
private fun Square.moveDown(player: Player): Square? = if (player === Player.WHITE) this.decRow() else this.incRow()
private fun Square.moveLeft(player: Player): Square? = if (player === Player.WHITE) this.decColumn() else this.incColumn()
private fun Square.moveRight(player: Player): Square? = if (player === Player.WHITE) this.incColumn() else this.decColumn()
