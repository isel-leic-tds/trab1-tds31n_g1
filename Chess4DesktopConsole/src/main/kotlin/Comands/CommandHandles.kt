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
            if (commandResult is NewBoard)
                Success(gameChess.copy(player = Player.WHITE, status = commandResult.statusGame, gameId = gameId))
            // restoreGame should always produce a BoardSucess(). If not:
            else
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
            if (commandResult is NewBoard)
                Success(gameChess.copy(player = Player.BLACK, status = commandResult.statusGame, gameId = gameId))
            // restoreGame should always produce a BoardSucess(). If not:
            else
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
                when (commandResult) {
                    is NewBoard -> {
                        saveMove(gameChess.mongoChessCommands, gameChess.gameId, commandResult.statusGame.lastMove!!)
                        Success(gameChess.copy(status = commandResult.statusGame))
                    }
                    is CommandError -> CommandError1(commandResult)
                    else -> throw IllegalStateException()
                }
            }
            else
                GameNotIniciated()
        },
        show = { result: Result ->
            if (result is Success) {
                // dysplays board
                println(result)
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }
            if (result is Error)
                println(result)
        }
    ),
    "REFRESH" to Command(
        action = { gameChess: GameChess, gameId: String? ->
            if (gameChess.gameId == null)
                GameNotIniciated()
            val commandResult = restoreGame(gameChess.mongoChessCommands, gameChess.gameId)
            if (commandResult is NewBoard)
                Success(gameChess.copy(player = Player.WHITE, status = commandResult.statusGame, gameId = gameChess.gameId))
            // restoreGame should always produce a BoardSucess(). If not:
            else
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

abstract class Result
data class Success(val gameChess: GameChess): Result()
abstract class Error(val error: Any): Result() {
    override fun toString() = error.toString()
}
private data class MissingContent(val error_: String): Error(error_)
private data class GameNotIniciated(val error_: String = "Game not iniciated yet"): Error(error_)
data class CommandError1(val error_: CommandError): Error(error_) {
    override fun toString(): String {
        return error_.toString()
    }
}

object Terminate: Result()
