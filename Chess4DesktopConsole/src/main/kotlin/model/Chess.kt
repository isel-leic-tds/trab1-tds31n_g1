package model

import DataBase.ChessDb
import model.Board.Board


enum class Player {
    WHITE, BLACK;
    fun other() = if (this === WHITE) BLACK else WHITE
}

data class StatusGame(val board: Board?, val moves: List<String>, val currentPlayer: Player?, val lastMove: String?, val check: Boolean = false) {
    override fun toString(): String {
        return board.toString()
    }
}

data class GameChess(val chessDb: ChessDb, val gameId: String?, val player: Player?, val status: StatusGame)