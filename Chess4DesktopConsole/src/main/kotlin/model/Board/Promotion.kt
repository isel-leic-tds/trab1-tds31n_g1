package model.Board

import chess.model.Row
import chess.model.Square
import model.Player

class Promotion: Board() {

    class MovePromotion(val square: Square, val pieceType: PieceType) : Move()

    /**
     * Chescks if its possible to make a promotion on given [square]
     */
    fun isPromotionPossible(square: Square, boardArr: Array<Array<Board.Piece?>>): Boolean {
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null && piece.type is Pawn) {
            if (piece.player === Player.WHITE && square.row === Row.EIGHT
                || piece.player === Player.BLACK && square.row === Row.ONE
            )
                return true
        }
        return false
    }

    /**
     * First, checks if it is possible to make a promotion on give[square] and if so, returns a MovePromotion object.
     */
    fun getMoveForPromotion(
        square: Square,
        pieceType: PieceType,
        boardArr: Array<Array<Board.Piece?>>
    ): MovePromotion? {
        return if (isPromotionPossible(square, boardArr))
            MovePromotion(square, pieceType)
        else null
    }

    /**
     * First, checks if it is possible to make a promotion with given [movePromotion] and if so, makes a promotion.
     */
    fun makePromotion(movePromotion: MovePromotion, board: Board): Board {
        if (!isPromotionPossible(movePromotion.square, board.boardArr)) return null
        // makes promotion
        val piece = board.boardArr[movePromotion.square.row.ordinal][movePromotion.square.column.ordinal]!!
        val newBoardArr = board.boardArr.clone()
        newBoardArr[movePromotion.square.row.ordinal][movePromotion.square.column.ordinal] =
            Board.Piece(movePromotion.pieceType, piece.player)
        return Board(board, newBoardArr)
    }
}