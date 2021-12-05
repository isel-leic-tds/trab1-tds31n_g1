package Comands

import DataBase.*
import Moves
import model.Board.Board
import model.Board.Error
import model.Board.Result
import model.Player
import model.StatusGame
import java.lang.IllegalStateException

abstract class CommandResult()
abstract class CommandError(val error: Any): CommandResult() {
    override fun toString() = super.toString()
}
class CommandSucess(val result: Any): CommandResult()
class EmptyMove(): CommandError("Move command is empty")
class WaitForOtherPlayer(): CommandError("Wait for your turn!")
class InvalidGameId(): CommandError("Invalid or emprty gameId")
class GameDoesNotExist(): CommandError("Game not created yet")
data class BoardError(val error: model.Board.Error): CommandError(error)
// TODO should exist a BoardRestored() where lastMove didnt exist
data class BoardSuccess(val statusGame: StatusGame, val lastMove: String?): CommandResult() {
    override fun toString(): String {
        return statusGame.board.toString()
    }
}

/**
 * Checks in the database if the gameId exists.
 * If so, returns a new Board with all saved moves executed.
 * Otherwise, will create a new document with empty list of moves. Then returns a new Board,
 */
fun restoreGame(mongoChessCommands: MongoChessCommands, gameId: String?): CommandResult {
    if (gameId == null) return InvalidGameId()
    val newBoard = Board()
    val moves = DataBase.getMoves(mongoChessCommands,gameId)
    if (moves == null) {
        // inserts one document in the database so the other player can join the game
        postMoves(mongoChessCommands,gameId,"")
        return CommandSucess(BoardSuccess(StatusGame(newBoard,listOf(), Player.WHITE,), null))
    }
    if (moves.content == "") return CommandSucess(BoardSuccess(StatusGame(newBoard,listOf(), Player.WHITE), null))
    val list = moves.content.trim().split(" ").toList()
    var statusGame = StatusGame(newBoard,list,Player.WHITE)
    list.forEach{ move: String -> statusGame = statusGame.copy(board = statusGame.board!!.makeMoveWithCorrectString(move),
                                                                currentPlayer = statusGame.currentPlayer!!.advance())}
    return CommandSucess(BoardSuccess(statusGame, list[list.size-1]))
}

/**
 * Checks in the database if the gameId exists.
 * If so, returns a new Board with all saved moves executed.
 * Otherwise, will report an error.
 */
fun joinGame(mongoChessCommands: MongoChessCommands, gameId: String?): CommandResult {
    if (gameId == null) return InvalidGameId()
    val newBoard = Board()
    val moves = DataBase.getMoves(mongoChessCommands,gameId) ?: return GameDoesNotExist()
    if (moves.content == "") return CommandSucess(BoardSuccess(StatusGame(newBoard,listOf(), Player.BLACK), null))
    val list = moves.content.trim().split(" ").toList()
    var statusGame = StatusGame(newBoard,list,Player.WHITE)
    list.forEach{ move: String -> statusGame = statusGame.copy(board = statusGame.board!!.makeMoveWithCorrectString(move),
                                                                currentPlayer = statusGame.currentPlayer!!.advance())}
    return CommandSucess(BoardSuccess(statusGame, list[list.size-1]))
}

/**
 * Appends a given [move] to the database with the [gameId] game identifier.
 */
fun saveMove(mongoChessCommands: MongoChessCommands, gameId: String, move: String): CommandResult {
    val moves: Moves? = DataBase.getMoves(mongoChessCommands,gameId)
    if (moves == null)
        // adds a new document in the collection to hold the moves for the new chess game
        postMoves(mongoChessCommands,gameId,move)
    else
        // replace the first document (which) contains the saved moves for the current chess game
        replaceMoves(mongoChessCommands, gameId, moves.content+" "+move)
    return CommandSucess(true)
}
/**
 * Makes a given [move] to the [statusGame] board.
 * Return the new Status Game if the make move went well of null.
 */
fun makeMove(statusGame: StatusGame, move: String?, player: Player): CommandResult {
    if (move == null) return EmptyMove()
    if (statusGame.currentPlayer != player) return WaitForOtherPlayer()
    val result = statusGame.board!!.makeMove(move, statusGame.currentPlayer)
    if (result is model.Board.Error)
        return BoardError(result)
    if (result is model.Board.Success)
        return CommandSucess(BoardSuccess(StatusGame(result.board, statusGame.list + result.str, player.advance()), result.str))
    // if the result is something else other than model.Board.Sucess or model.Board.Error
    throw IllegalStateException()
}

/**
 * Given a [gameId], returns all saved moves in the dataBase.
 */
fun getMoves(mongoChessCommands: MongoChessCommands, gameId: String): CommandResult? {
    val moves = DataBase.getMoves(mongoChessCommands,gameId) ?: return null
    return CommandSucess(moves.content.trim().split(" "))
}