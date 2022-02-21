import Commands.Command
import Commands.Option
import Commands.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import model.Board.Move
import model.GameChess

fun play(menuHandlers: Map<Option, Command>, gameChess: GameChess, move: Move): GameChess? {
    val command = Option.PLAY
    LineCommand(command, null)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, move)
    if (result is Success)
        return result.gameChess
    return null
}

fun openGame(menuHandlers: Map<Option, Command>, gameChess: GameChess, gameName: String): GameChess? {
    val gameName = "test"
    val command = Option.OPEN
    LineCommand(command,gameName)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}

fun joinGame(menuHandlers: Map<Option, Command>, gameChess: GameChess, gameName: String): GameChess? {
    val gameName = "test"
    val command = Option.JOIN
    LineCommand(command,gameName)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}

suspend fun refreshGame(menuHandlers: Map<Option, Command>, gameChess: GameChess): GameChess {
    gameChess.status.board ?: return gameChess
    if (gameChess.status.board.currentPlayer === gameChess.player) return gameChess
    return withContext(Dispatchers.IO) {
        val gameName = "gameTest"
        val command = Option.REFRESH
        LineCommand(command, gameName)
        val cmd: Command? = menuHandlers[command]
        var result = cmd!!.action(gameChess, gameName)
        do {
            delay(2000)
            result = cmd!!.action(gameChess, gameName)
        } while (result !is Success || result.gameChess.status.board === gameChess.status.board)
        result.gameChess
    }
}

/**
 * Command line after is parsed.
 * first: name of command in uppercase.
 * second: optional parameter (one or more words)
 */
typealias LineCommand = Pair<Option, String?>