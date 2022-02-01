package Commands

import model.Board.MovePos
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
    val action: (GameChess, Any) -> Result,

    /**
     * Displays the result returned by the action.
     * Receive the result information to show.
     */
    val show: (Result) -> Unit = { }
)

enum class Option{OPEN, JOIN, PLAY, REFRESH, MOVES, EXIT}

/**
 * Build the associative map of command handlers to initiate/exit games.
 * @return The handlers map of all commands.
 */
fun buildMenuHandlers() = mapOf(
    Option.OPEN to Command(
        // returns a new Board (restored or new)
        action = { gameChess: GameChess, gameId: Any ->
            if (gameId is String) {
                if (gameId.length == 0)
                    MissingContent(gameChess, "GameId at fault")
                val commandResult = restoreGame(gameChess.chessDb, gameId)
                if (commandResult is NewBoard)
                    Success(gameChess.copy(player = Player.WHITE, status = commandResult.statusGame, gameId = gameId))
                // restoreGame should always produce a BoardSucess(). If not:
                else throw IllegalStateException()
            }
            else MissingContent(gameChess, "GameId needs to be a String")
        },
        show = { result: Result ->
            if (result is Error)
               println(result)
            if (result is Success) {
                val gameChess = result.gameChess
                // displays Board
                println(result.gameChess.status)
                println("Game "+gameChess.gameId+" opened. Play with white pieces")
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }
        }
    ),
    Option.JOIN to Command(
        action = { gameChess: GameChess, gameId: Any ->
            if (gameId is String) {
                if (gameId.length == 0)
                    MissingContent(gameChess, "GameId at fault")
                val commandResult = joinGame(gameChess.chessDb, gameId)
                if (commandResult is NewBoard)
                    Success(gameChess.copy(player = Player.BLACK, status = commandResult.statusGame, gameId = gameId))
                else
                    GameNotIniciated(gameChess)
            }
            else MissingContent(gameChess, "GameId at fault")
        },
        show = { result: Result ->
            if (result is Error)
                println(result)
            if (result is Success) {
                val gameChess = result.gameChess
                // displays Board
                println(result.gameChess.status)
                println("Game "+gameChess.gameId+" opened. Play with black pieces")
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }
        }
    ),
    Option.EXIT to Command( { _, _ -> Terminate }),
    Option.PLAY to Command(
        action = { gameChess: GameChess, move: Any ->
            if (gameChess.gameId != null) {
                if (move !is MovePos)
                    MissingContent(gameChess, "Move needs to be a Move object")
                else {
                    val commandResult = makeMove(gameChess.status, move, gameChess.player!!)
                    when (commandResult) {
                        is NewBoard -> {
                            saveMove(gameChess.chessDb, gameChess.gameId, commandResult.statusGame.lastMove!!)
                            Success(gameChess.copy(status = commandResult.statusGame))
                        }
                        is CommandError -> CommandError1(gameChess, commandResult)
                        else -> throw IllegalStateException()
                    }
                }
            }
            else GameNotIniciated(gameChess)
        },
        show = { result: Result ->
            if (result is Success) {
                // dysplays board
                println(result.gameChess.status)
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }
            if (result is Error) {
                println(result)
                println(result.gameChess.gameId + ':' + result.gameChess.status.currentPlayer + '>')
            }
        }
    ),
    /**
     * If the refresh results in the same list of moves, the Result object returned conatins the same gameChess.
     */
    Option.REFRESH to Command(
        action = { gameChess: GameChess, gameId: Any ->
            if (gameChess.gameId != null) {
                GameNotIniciated(gameChess)
                val commandResult = restoreGame(gameChess.chessDb, gameChess.gameId)
                if (commandResult is NewBoard)
                    // if the refresh returns a similar board
                    if (gameChess.status.moves == commandResult.statusGame.moves)
                        Success(gameChess)
                    else
                        Success(gameChess.copy(status = commandResult.statusGame))
                // restoreGame should always produce a BoardSucess(). If not:
                else
                    throw IllegalStateException()
            }
            else MissingContent(gameChess, "GameId at fault")
        },
        show = {result ->
            if (result is Error)
                println(result)
            if (result is Success) {
                // dysplays board
                println(result.gameChess.status)
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }
        }
    ),
    Option.MOVES to Command(
        action = { gameChess: GameChess, gameId: Any ->
            if (gameChess.gameId != null)
                Success(gameChess)
            else
               GameNotIniciated(gameChess)
        },
        show = { result ->
            if (result is Error)
               println(result)
            if (result is Success) {
                println(result.gameChess.status.moves.toString())
                println(result.gameChess.gameId+':'+result.gameChess.status.currentPlayer+'>')
            }

        },
    )
)

abstract class Result
data class Success(val gameChess: GameChess): Result()
abstract class Error(open val gameChess: GameChess, open val error: Any): Result() {
    override fun toString() = error.toString()
}
private class MissingContent(override val gameChess: GameChess, override val error: String): Error(gameChess, error)
private class GameNotIniciated(override val gameChess: GameChess, override val error: String = "Game not iniciated yet"): Error(gameChess, error)
data class CommandError1(override val gameChess: GameChess, override val error: CommandError): Error(gameChess, error) {
    override fun toString(): String {
        return error.toString()
    }
}

object Terminate: Result()
