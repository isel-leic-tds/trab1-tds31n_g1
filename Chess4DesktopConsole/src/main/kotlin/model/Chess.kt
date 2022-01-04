package model

import DataBase.ChessDb
import model.Board.Board
import model.Board.globalCheck

enum class Player {
    WHITE, BLACK;
    fun other() = if (this === WHITE) BLACK else WHITE
}

data class StatusGame(val board: Board?, val moves: List<String>, val currentPlayer: Player?, val lastMove: String?, val check: Boolean = globalCheck) {
    override fun toString(): String {
        return board.toString()
    }
}

data class GameChess(val chessDb: ChessDb, val gameId: String?, val player: Player?, val status: StatusGame)