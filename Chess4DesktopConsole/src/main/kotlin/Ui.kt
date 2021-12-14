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

var curSquare: Square? = null

fun main() = application {
    val winState = WindowState(width = Dp.Unspecified, height = Dp.Unspecified)
    Window(
        onCloseRequest = ::exitApplication,
        state = winState,
        title = "Jogo de Xadrez"
    ) {
        MongoDriver().use { driver ->
            var gameChess by remember { mutableStateOf(createGame(driver)) }
            DesktopMaterialTheme {
                ChessMenuBar(
                    onOpen = {},
                    onExit = ::exitApplication
                )
            }
        }
    }
}

fun createGame(driver: MongoDriver) =
    GameChess(MongoChessCommands(driver), null, null, StatusGame(null,listOf(),null, null))