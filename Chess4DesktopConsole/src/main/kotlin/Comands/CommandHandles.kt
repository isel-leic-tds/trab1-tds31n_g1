package Comands

import model.GameChess
import model.Player
import java.lang.IllegalStateException

/**
 * Type of each command handler.
 * Explicitly separates the action of the command and the presentation of the result.
 * It is possible to do tests on the action, without having tests on the presentation.
 * @property action The action function.
 * @property show The show function.
 */
data class Command(

    /**
     * Performs the action of the command returning type Result witch contains the result of the operation:
     * Success if the operation went well, otherwise Error. Both are a subtype of Result containing its own information.
     * Receive the parameter given on command line.
     * May throws exception in case of failure with appropriate error message.
     */
    val action: (GameChess, String?) -> Result,

    /**
     * Displays the result returned by the action.
     * Receive the result information to show.
     */
    val show: (Result) -> Unit = { }
)

/**
 * Build the associative map of command handlers to initiate/exit games.
 * @return The handlers map of all commands.
 */
fun buildMenuHandlers() = mapOf(
    "OPEN" to Command(
        // returns a new Board (restored or new)
        action = { gameChess: GameChess, gameId: String? ->
            if (gameId == null)
                MissingContent("GameId at fault")
            val commandResult = restoreGame(gameChess.mongoChessCommands, gameId)
            if (commandResult is BoardSuccess)
                Success(gameChess.copy(player = Player.WHITE, status = commandResult.statusGame, gameId = gameId))
            // restoreGame should always produce a BoardSucess(). If not:
            throw IllegalStateException()
        },
        show = { result: Result ->
            if (result is Error)
               println(result)
            if (result is Success) {
                val gameChess = result.gameChess
                // displays Board
                println(result)
                println("Game "+gameChess.gameId+" opened. Play with white pieces")
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }
        }
    ),
    "JOIN" to Command(
        action = { gameChess: GameChess, gameId: String? ->
            if (gameId == null)
                MissingContent("GameId at fault")
            val commandResult = joinGame(gameChess.mongoChessCommands, gameId)
            if (commandResult is BoardSuccess)
                Success(gameChess.copy(player = Player.WHITE, status = commandResult.statusGame, gameId = gameId))
            // restoreGame should always produce a BoardSucess(). If not:
            throw IllegalStateException()
        },
        show = { result: Result ->
            if (result is Error)
                println(result)
            if (result is Success) {
                val gameChess = result.gameChess
                // displays Board
                println(result)
                println("Game "+gameChess.gameId+" opened. Play with black pieces")
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }
        }
    ),
    "EXIT" to Command( { _,_ -> Terminate }),
    "PLAY" to Command(
        action = { gameChess: GameChess, move: String? ->
            if (gameChess.gameId != null) {
                if (move == null)
                    MissingContent("GameId at fault")
                val commandResult = makeMove(gameChess.status, move, gameChess.player!!)
                if (commandResult is BoardSuccess) {
                    saveMove(gameChess.mongoChessCommands, gameChess.gameId, commandResult.lastMove!!)
                    Success(gameChess.copy(status = commandResult.statusGame))
                }
                CommandError1(commandResult)
            }
            else
                GameNotIniciated()
        },
        show = { result: Result ->
            if (result is Success) {
                println(result)
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }
            if (result is Error)
                println(result)
               /* when(result.type) {
                    ErrorType.GAME_NOT_INITIATED ->
                        println("Can't play without a game: try open or join commands.")
                    ErrorType.NOT_YOUR_TURN ->
                        println("Wait for your turn: try refresh command")
                    ErrorType.MISSING_CONTENT ->
                        println("Missing move.")
                    ErrorType.INVALID_MOVE ->
                        println("Invalid move.")
                }
                */
        }
    ),
    "REFRESH" to Command(
        action = { gameChess: GameChess, gameId: String? ->
            if (gameChess.gameId == null)
                GameNotIniciated()
            val commandResult = restoreGame(gameChess.mongoChessCommands, gameChess.gameId)
            if (commandResult is BoardSuccess)
                Success(gameChess.copy(player = Player.WHITE, status = commandResult.statusGame, gameId = gameChess.gameId))
            // restoreGame should always produce a BoardSucess(). If not:
            throw IllegalStateException()
        },
        show = {result ->
            if (result is Error)
                println(result)
            if (result is Success) {
                // dysplays board
                println(result)
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }
        }
    ),
    "MOVES" to Command(
        action = { gameChess: GameChess, gameId: String? ->
            if (gameChess.gameId != null)
                Success(gameChess)
            else
               GameNotIniciated()
        },
        show = { result ->
            if (result is Error)
               println(result)
            if (result is Success) {
                println(result.gameChess.status.list.toString())
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }

        },
    )
)

/*
TODO
Maybe its a good idea to transform the ErrorType into many objects and override toString() to print every Error.
Also the errors coming from the Board are not being displayed
 */

abstract class Result

abstract class Error(): Result() {
    override fun toString(): String {
        return super.toString()
    }
}
private data class InvalidMove(val error: String ="Invalid move"): Error()
private data class MissingContent(val msg: String): Error()
private data class GameNotIniciated(val error: String = "Game not iniciated yet"): Error()
//private data class InvalidMove(): Error("Invalid move")
class CommandError1(val error: Any): Error()

object Terminate: Result()

/**
 * Used to represent the possible Errors witch could have occured in the commands above.
 */
private enum class ErrorType() {
    INVALID_MOVE, MISSING_CONTENT, GAME_NOT_INITIATED, NOT_YOUR_TURN
}


class Success(val gameChess: GameChess): Result()