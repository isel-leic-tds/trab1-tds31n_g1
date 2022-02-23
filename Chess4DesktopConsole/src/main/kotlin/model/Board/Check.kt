package model.Board

import chess.model.Square

/**
 * Checks if the current board is in checkmate
 */
fun isInCheckmate(boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square): Boolean {
    val whitePlayer = isKingInCheck(boardArr, whiteKingPosition)
    val blackPlayer = isKingInCheck(boardArr, blackKingPosition)
    // if none of the players have its King in checkMate
    if (!whitePlayer && !blackPlayer) return false
    val kingInDangerous = if (whitePlayer) whiteKingPosition else blackKingPosition
    if (tryToMoveKing(kingInDangerous, boardArr)) return false
    if (isPieceThatProtectKing(kingInDangerous, boardArr)) return false
    return true
}

/**
 * Checks if one of the players has its King in check.
 * @return
 */
fun isKingInCheck(boardArr: Array<Array<Board.Piece?>>, kingSquare: Square): Boolean {
    val king = boardArr[kingSquare.row.ordinal][kingSquare.column.ordinal]
    if (king == null || king.type !is King) return false
    Square.values.forEach { square ->
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null && tryToMove(Move(piece.type, square, kingSquare), boardArr))
            return true
    }
    return false
}

/**
 * Tries to move King out of danger.
 * @return true if it is possible to move him out of danger or false otherwise.
 */
fun tryToMoveKing(kingSquare: Square, table: Array<Array<Board.Piece?>>): Boolean {
    val king = table[kingSquare.row.ordinal][kingSquare.column.ordinal]
    if (king == null || king.type !is King) return false
    val allMoves = Move.getAllMoves(kingSquare, King())
    val allValidMoves = allMoves.filter { tryToMove(it, table) }
    allValidMoves.forEach { move ->
        val newBoard = table.copy()
        newBoard[kingSquare.row.ordinal][kingSquare.column.ordinal] = null
        // updates King
        newBoard[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = Board.Piece(King(true), king.player)
        if (!isKingInCheck(newBoard, move.newSquare)) return true
    }
    return false
}

/**
 * Tries to move all pieces to protect the King.
 * @return true if there's at least one piece that can be moved to protect the King or false otherwise.
 */
fun isPieceThatProtectKing(kingSquare: Square, table: Array<Array<Board.Piece?>>): Boolean {
    val king = table[kingSquare.row.ordinal][kingSquare.column.ordinal]
    if (king == null || king.type !is King) return false
    val kingPlayer = king.player
    Square.values.forEach { square ->
        val piece = table[square.row.ordinal][square.column.ordinal]
        if (piece != null && piece.player === kingPlayer && square !== kingSquare) {
            val allMoves = Move.getAllMoves(square, piece.type)
            val allValidMoves = allMoves.filter { tryToMove(it, table) }
            allValidMoves.forEach { move ->
                val newBoard = table.copy()
                newBoard[square.row.ordinal][square.column.ordinal] = null
                newBoard[move.newSquare.row.ordinal][move.newSquare.column.ordinal] =
                    // updates piece if is Rook or Pawn
                    when (piece.type) {
                        is Rook -> Board.Piece(Rook(true), piece.player)
                        is Pawn -> Board.Piece(Pawn(true), piece.player)
                        else -> piece
                    }
                if (!isKingInCheck(newBoard, kingSquare)) return true
            }
        }
    }
    return false
}
