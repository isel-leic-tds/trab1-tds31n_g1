import Commands.Command
import Commands.Success
import Commands.buildMenuHandlers
import DataBase.LocalDb
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import chess.model.Square
import model.GameChess
import model.StatusGame
import ui.ChessMenuBar

data class Chess(val selected: Square? = null, val gameChess: GameChess)

fun main() = application {
    val winState = WindowState(width = Dp.Unspecified, height = Dp.Unspecified)
    Window(
        onCloseRequest = ::exitApplication,
        state = winState,
        title = "Jogo de Xadrez"
    ) {
        // TODO The code cant acess remote database!!
        val menuHandlers = buildMenuHandlers()
        var chess by remember { mutableStateOf(Chess(gameChess = createGame())) }
        DesktopMaterialTheme {
            ChessMenuBar(
                onOpen = {
                    val result = openGame(menuHandlers, chess.gameChess)
                    if (result != null) chess = chess.copy(gameChess = result)
                },
                onJoin = {
                    val result = joinGame(menuHandlers, chess.gameChess)
                    if (result != null) chess = chess.copy(gameChess = result)
                }
            )
            Column {
                ChessView(chess) { square ->
                    chess = Chess(square, chess.gameChess)
                }
            }
        }
    }
}

fun createGame() =
    GameChess(LocalDb(), null, null, StatusGame(null,listOf(),null, null))

private fun openGame(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess? {
    print("GameName: ")
    //val gameName = readLine()
    val gameName = "test"
    val name = "OPEN"
    LineCommand(name,gameName)
    val cmd: Command? = menuHandlers[name]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}

private fun joinGame(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess? {
    print("GameName: ")
    val gameName = readLine()
    val name = "JOIN"
    LineCommand(name,gameName)
    val cmd: Command? = menuHandlers[name]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}

private fun play(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess? {
    print("GameName: ")
    val gameName = readLine()
    val name = "JOIN "
    LineCommand(name,gameName)
    val cmd: Command? = menuHandlers[name]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}