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
 * Checks if one of the Players has its King in check.
 * @return the player that has its King in check or null neather King is in check.
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
fun tryToMoveKing(kingSquare: Square, boardArr: Array<Array<Board.Piece?>>): Boolean {
    val king = boardArr[kingSquare.row.ordinal][kingSquare.column.ordinal]
    if (king == null || king.type !is King) return false
    val allMoves = Move.getAllMoves(kingSquare, King())
    val allvalidMoves = allMoves.filter { tryToMove(it, boardArr) }
    allvalidMoves.forEach { move ->
        val newBoard = boardArr.copy()
        newBoard[kingSquare.row.ordinal][kingSquare.column.ordinal] = null
        newBoard[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = king
        if (!isKingInCheck(newBoard, move.newSquare)) return true
    }
    return false
}

/**
 * Tries to move all pieces to protect the King.
 * @return true if there's at least one piece that can be moved to protect the King or false otherwise.
 */
fun isPieceThatProtectKing(kingSquare: Square, boardArr: Array<Array<Board.Piece?>>): Boolean {
    val king = boardArr[kingSquare.row.ordinal][kingSquare.column.ordinal]
    if (king == null || king.type !is King) return false
    val kingPlayer = king.player
    Square.values.forEach { square ->
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null && piece.player === kingPlayer && square !== kingSquare) {
            val allMoves = Move.getAllMoves(square, piece.type)
            val allValidMoves = allMoves.filter { tryToMove(it, boardArr) }
            allValidMoves.forEach { move ->
                val newBoard = boardArr.copy()
                newBoard[square.row.ordinal][square.column.ordinal] = null
                newBoard[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece
                if (!isKingInCheck(newBoard, kingSquare)) return true
            }
        }
    }
    return false
}
