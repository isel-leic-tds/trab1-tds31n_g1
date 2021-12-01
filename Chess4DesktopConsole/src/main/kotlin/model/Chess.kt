package model

import DataBase.MongoChessCommands
import model.Board.Board

enum class Player {
    WHITE, BLACK;
    fun advance() = if (this === WHITE) BLACK else WHITE
}

data class StatusGame(val board: Board?, val list: List<String>, val currentPlayer: Player?)

data class GameChess(val mongoChessCommands: MongoChessCommands, val gameId: String?, val player: Player?, val status: StatusGame)