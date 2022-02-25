package chess.model

import chess.DataBase.ChessDb
import chess.model.Board.Board


enum class Player {
    WHITE, BLACK;
    fun other() = if (this === WHITE) BLACK else WHITE
    override fun toString() = if (this == WHITE) "Branco" else "Preto"
}



data class StatusGame(
    val board: Board?,
    val moves: List<String>,
    val lastMove: String?,
    val draw: Boolean = false
) {
    override fun toString(): String {
        return board.toString()
    }
}

data class GameChess(val chessDb: ChessDb, val gameId: String?, val player: Player?, val status: StatusGame) {
    fun isBoardNull() = status.board == null
    fun isPlayerTurn(): Boolean {
        status.board ?: return false
        return player === status.board.currentPlayer
    }
}