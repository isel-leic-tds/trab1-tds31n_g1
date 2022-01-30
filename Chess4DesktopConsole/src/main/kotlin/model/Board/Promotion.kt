package model.Board

import chess.model.Row
import model.Player

/**
 * When we need to know if that piece can make a promotion
 */
fun isPromotionPossible(move: Move, boardArr: Array<Array<Board.Piece?>>): Boolean {
    val piece = boardArr[move.curSquare.row.ordinal][move.newSquare.column.ordinal]
    if (piece != null && piece.type is Pawn) {
        if (piece.player === Player.WHITE && move.newSquare.row === Row.EIGHT
            || piece.player === Player.BLACK && move.newSquare.row === Row.ONE
        )
            return true
    }
    return false
}

fun getMoveForPromotion(move: Move, pieceType: PieceType, boardArr: Array<Array<Board.Piece?>>): Move? {
    val piece = boardArr[move.curSquare.row.ordinal][move.newSquare.column.ordinal]
    if (piece != null && piece.type is Pawn) {
        if (piece.player === Player.WHITE && move.newSquare.row === Row.EIGHT
            || piece.player === Player.BLACK && move.newSquare.row === Row.ONE
        )
            return move.copy(type = Promotion(pieceType))
    }
    return null
}

/*
        var newBoard = ((result) as ISuccess).content as Board
        val inCheck = (result).check
        val inCheckmate = (result).checkmate

        // promotion
        if (checkPromotion(move.newSquare)) {
            if (!(move.type is Promotion))
                return MakePromotion()
            result = doPromotion(newBoard.boardArr, move.newSquare, move)
            if (result is Error) return result
            newBoard = ((result) as ISuccess).content as Board
        }
        else if (move.type is Promotion)
            return BadPromotion()

        return Success(newBoard, move.toString(), inCheck, inCheckmate)
     */

/*
/**
 * Checks if the PieceType for Promotion is valid.
 * Otherwise, returns an Error
 */
private fun getPieceForPromotion(move: Move): Result {
    val newPiece = (move.type as Promotion).newPiece
    if (newPiece is King || newPiece is Pawn)
        return PromotionNotValid()
    return ISuccess(newPiece)
}

/**
 * Does explicitly a Promotion move given in [square] with [newPiece].
 * @Throws IllegalCallerException if the [square] is empty.
 */
private fun doPromotion(board: Array<Array<Piece?>>, square: Square, move: Move): Result {
    val result = getPieceForPromotion(move)
    if (result is ISuccess) {
        val newPiece = result.content as PieceType
        val newBoardArr = board.clone()
        val piece = newBoardArr[square.row.ordinal][square.column.ordinal] ?: throw IllegalCallerException()
        newBoardArr[square.row.ordinal][square.column.ordinal] = Piece(newPiece, piece.player)
        return ISuccess(Board(this, newBoardArr))
    }
    return result
}
 */