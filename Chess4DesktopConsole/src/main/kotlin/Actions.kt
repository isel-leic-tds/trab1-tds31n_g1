import Commands.Command
import Commands.Success
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import model.GameChess

fun play(menuHandlers: Map<String, Command>, gameChess: GameChess, move: String): GameChess? {
    val command = "PLAY"
    LineCommand(command, null)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, move)
    if (result is Success)
        return result.gameChess
    return null
}

fun openGame(menuHandlers: Map<String, Command>, gameChess: GameChess, gameName: String): GameChess? {
    //val gameName = "test"
    val command = "OPEN"
    LineCommand(command,gameName)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}

fun joinGame(menuHandlers: Map<String, Command>, gameChess: GameChess, gameName: String): GameChess? {
    //val gameName = "test"
    val command = "JOIN"
    LineCommand(command,gameName)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}

suspend fun refreshGame(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess {
    if (gameChess.status.currentPlayer === gameChess.player) return gameChess
    return withContext(Dispatchers.IO) {
        val gameName = "gameTest"
        val command = "REFRESH"
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