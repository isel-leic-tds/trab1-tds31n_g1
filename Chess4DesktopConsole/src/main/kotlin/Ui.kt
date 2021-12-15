import Comands.Command
import Comands.Success
import Comands.buildMenuHandlers
import DataBase.MongoChessCommands
import androidx.compose.desktop.ComposeWindow
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import chess.model.Square
import model.Board.*
import model.GameChess
import model.StatusGame
import mongoDb.MongoDriver
import ui.ChessMenuBar
import kotlin.Result

var curSquare: Square? = null

fun main() = application {
    val winState = WindowState(width = Dp.Unspecified, height = Dp.Unspecified)
    Window(
        onCloseRequest = ::exitApplication,
        state = winState,
        title = "Jogo de Xadrez"
    ) {
        // TODO connection is etablished but the code cant acess database!!
        MongoDriver().use { driver ->
            val menuHandlers = buildMenuHandlers()
            var gameChess by remember { mutableStateOf(createGame(driver)) }
            DesktopMaterialTheme {
                ChessMenuBar(
                    onOpen = {
                        val result = openGame(menuHandlers, gameChess)
                        if (result != null) gameChess = result
                    },
                    onJoin = {
                        val result = joinGame(menuHandlers, gameChess)
                        if (result != null) gameChess = result
                    }
                )
                Column {
                    ChessView(Board()) { square ->

                    }
                    /*
                    ChessView(gameChess.status.board) { square ->
                        if (curSquare != null) {
                            val playerType = board.get(curSquare!!)!!.type
                            val str = "($playerType)$curSquare$square"
                            board.makeMove(str)
                            curSquare = null
                        } else
                            curSquare = square
                    }
                     */
                }
            }
        }
    }
}

fun createGame(driver: MongoDriver) =
    GameChess(MongoChessCommands(driver), null, null, StatusGame(null,listOf(),null, null))

private fun openGame(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess? {
    print("GameName: ")
    val gameName = readLine()
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