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
import model.Board.toStr
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
                    val gameChess = openGame(menuHandlers, chess.gameChess)
                    if (gameChess != null) chess = chess.copy(gameChess = gameChess)
                },
                onJoin = {
                    val gameChess = joinGame(menuHandlers, chess.gameChess)
                    if (gameChess != null) chess = chess.copy(gameChess = gameChess)
                }
            )
            Column {
                ChessView(chess) { square ->
                    val selected = chess.selected
                    if (selected == null)
                        chess = Chess(square, chess.gameChess)
                    else {
                        val board = chess.gameChess.status.board
                        if (board != null) {
                            val pieceType = board[selected]!!.type.toStr()
                            val current = chess.selected.toString()
                            val target = square.toString()
                            val move = pieceType + current + target
                            val gameChess = makeMove(menuHandlers, chess.gameChess, move)
                            if (gameChess != null)
                                chess = Chess(gameChess = gameChess)
                        }
                    }
                }
            }
        }
    }
}

fun createGame() =
    GameChess(LocalDb(), null, null, StatusGame(null,listOf(),null, null))

private fun makeMove(menuHandlers: Map<String, Command>, gameChess: GameChess, move: String): GameChess? {
    val command = "PLAY"
    LineCommand(command, null)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, move)
    if (result is Success)
        return result.gameChess
    return null
}

private fun openGame(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess? {
    print("GameName: ")
    //val gameName = readLine()
    val gameName = "test"
    val command = "OPEN"
    LineCommand(command,gameName)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}

private fun joinGame(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess? {
    print("GameName: ")
    val gameName = readLine()
    val command = "JOIN"
    LineCommand(command,gameName)
    val cmd: Command? = menuHandlers[command]
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