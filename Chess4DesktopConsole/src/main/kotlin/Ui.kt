import Commands.Command
import Commands.Success
import Commands.buildMenuHandlers
import DataBase.FileDb
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import chess.model.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        val scope = rememberCoroutineScope()
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
                },
            )
            MainView(chess) { square ->
                chess = pressSquare(chess, square, menuHandlers)
            }
            scope.launch {
                val gameChess = refreshGame(menuHandlers, chess.gameChess)
                chess = chess.copy(gameChess = gameChess)
            }
        }
    }
}

/**
 * Tries to make a move if two pieces were selected or selects one.
 */
private fun pressSquare(chess: Chess, square: Square, menuHandlers: Map<String, Command>): Chess {
    val selected = chess.selected
    val board = chess.gameChess.status.board
    if (board != null) {
        // if it's not the players turn
        if (chess.gameChess.player !== chess.gameChess.status.currentPlayer) return chess
        // marks a piece
        if (selected == null) {
            return if (board[square] != null && board[square]!!.player === chess.gameChess.player)
                chess.copy(selected = square)
            else chess
        }
        else {
            // unmarc selected piece
            if (selected === square)
                return chess.copy(selected = null)
            val piece = board[square]
            // if the player presses one of its own pieces
            if (piece != null && piece.player === chess.gameChess.player)
                return chess.copy(selected = square)
            // tries to make a move
            else {
                val pieceType = board[selected]!!.type.toStr()
                val current = chess.selected.toString()
                val target = square.toString()
                val move = pieceType + current + target
                val gameChess = play(menuHandlers, chess.gameChess, move)
                if (gameChess != null)
                    return Chess(gameChess = gameChess)
            }
        }
    }
    return chess
}

fun createGame() =
    GameChess(/*Uses local database*/FileDb(), null, null, StatusGame(null,listOf(),null, null))

private fun play(menuHandlers: Map<String, Command>, gameChess: GameChess, move: String): GameChess? {
    val command = "PLAY"
    LineCommand(command, null)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, move)
    if (result is Success)
        return result.gameChess
    return null
}

private fun openGame(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess? {
    //print("GameName: ")
    //val gameName = readLine()
    val gameName = "gameTest"
    val command = "OPEN"
    LineCommand(command,gameName)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}

private fun joinGame(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess? {
    //print("GameName: ")
    //val gameName = readLine()
    val gameName = "gameTest"
    val command = "JOIN"
    LineCommand(command,gameName)
    val cmd: Command? = menuHandlers[command]
    val result =  cmd!!.action(gameChess, gameName)
    if (result is Success)
        return result.gameChess
    return null
}

private suspend fun refreshGame(menuHandlers: Map<String, Command>, gameChess: GameChess): GameChess {
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