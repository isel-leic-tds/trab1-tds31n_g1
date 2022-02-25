package chess.Commands

import chess.DataBase.*
import Moves
import chess.model.Board.Board
import chess.model.Board.Move
import chess.model.Player
import chess.model.StatusGame
import java.lang.IllegalStateException

abstract class CommandResult()
abstract class CommandSucess(): CommandResult()
class MoveSaved(): CommandSucess()
abstract class CommandError(private val error: Any): CommandResult() {
    override fun toString() = error.toString()
}
class EmptyMove(): CommandError("Move command is empty")
class WaitForOtherPlayer(): CommandError("Wait for your turn!")
class InvalidGameId(): CommandError("Invalid or emprty gameId")
class GameDoesNotExist(): CommandError("Game not created yet")
data class BoardError(val boardError: chess.model.Board.Error): CommandError(boardError) {
    override fun toString(): String {
        return boardError.toString()
    }
}
class NewBoard(val statusGame: StatusGame): CommandSucess()

/**
 * Checks in the database if the gameId exists.
 * If so, returns a new Board with all saved moves executed.
 * Otherwise, will create a new document with empty list of moves. Then returns a new Board,
 */
fun restoreGame(chessDb: ChessDb, gameId: String?): CommandResult {
    if (gameId == null) return InvalidGameId()
    val newBoard = Board()
    val moves = chess.DataBase.getMoves(chessDb,gameId)
    if (moves == null) {
        // inserts one document in the database so the other player can join the game
        postMoves(chessDb,gameId,"")
        return NewBoard(StatusGame(newBoard,listOf(),null))
    }
    if (moves.content == "") return NewBoard(StatusGame(newBoard,listOf(), null),)
    val list = moves.content.trim().split(" ").toList()
    var statusGame = StatusGame(newBoard,list, null)
    list.forEach{ move: String ->
        val result = statusGame.board!!.makeMoveWithoutCheck(move)
        val board = result.board
        statusGame = statusGame.copy(board = board, lastMove = move)
    }
    return NewBoard(statusGame)
}

/**
 * Checks in the database if the gameId exists.
 * If so, returns a new Board with all saved moves executed.
 * Otherwise, will report an error.
 */
fun joinGame(chessDb: ChessDb, gameId: String?): CommandResult {
    if (gameId == null) return InvalidGameId()
    val newBoard = Board()
    val moves = chess.DataBase.getMoves(chessDb,gameId) ?: return GameDoesNotExist()
    if (moves.content == "") return NewBoard(StatusGame(newBoard,listOf(), null))
    val list = moves.content.trim().split(" ").toList()
    var statusGame = StatusGame(newBoard,list, null)
    list.forEach{ move: String -> val result = statusGame.board!!.makeMoveWithoutCheck(move)
        val board = result.board
        val check = result.check
        statusGame = statusGame.copy(board = board, lastMove = move)
    }
    return NewBoard(statusGame)
}

/**
 * Appends a given [move] to the database with the [gameId] game identifier.
 */
fun saveMove(chessDb: ChessDb, gameId: String, move: String): CommandResult {
    val moves: Moves? = chess.DataBase.getMoves(chessDb,gameId)
    if (moves == null)
        // adds a new document in the collection to hold the moves for the new chess game
        postMoves(chessDb,gameId,move)
    else
        // replace the first document (which) contains the saved moves for the current chess game
        replaceMoves(chessDb, gameId, moves.content+" "+move)
    return MoveSaved()
}
/**
 * Makes a given [move] to the [statusGame] board.
 * Return the new Status Game if the make move went well of null.
 */
fun makeMove(statusGame: StatusGame, move: Move, player: Player): CommandResult {
    statusGame.board ?: return GameDoesNotExist()
    if (statusGame.board.currentPlayer != player) return WaitForOtherPlayer()
    val result = statusGame.board!!.makeMove(move)
    if (result is chess.model.Board.Error)
        return BoardError(result)
    if (result is chess.model.Board.Success)
        return NewBoard(StatusGame(result.board, statusGame.moves + move.toString(), move.toString()))
    // if the result is something else other than model.Board.Sucess or model.Board.Error
    throw IllegalStateException()
}
