import Commands.Command
import Commands.buildMenuHandlers
import DataBase.FileDb
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import chess.model.Square
import kotlinx.coroutines.launch
import model.Board.PieceType
import model.Board.toStr
import model.GameChess
import model.StatusGame
import mongoDb.MongoDriver
import ui.ChessMenuBar
import ui.DialogGameName
import ui.DialogPromotionPiece

data class Chess(val selected: Square? = null, val gameChess: GameChess)

fun main() = MongoDriver().use { driver ->
    application {
        val winState = WindowState(width = Dp.Unspecified, height = TOTAL_HEIGHT)
        Window(
            onCloseRequest = ::exitApplication,
            state = winState,
            title = "Jogo de Xadrez"
        ) { WindowContent(driver, ::exitApplication) }
    }
}