package chess

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import chess.model.Square
import chess.model.GameChess
import chess.mongoDb.MongoDriver
import TOTAL_HEIGHT
import WindowContent

//var variable = "MONGO_CONNECTION=mongodb+srv://miguel:RGJ#rc74JhB3V.f@cluster0.pwqp1.mongodb.net/TDS?retryWrites=true&w=majority"
//val var2 = "MONGO_DB_NAME=TDS"

/*
to run JAR:
$echo $MONGO_DB_NAME
$set $MONGO_DB_NAME=TDS
$java -jar chess-windows-x64-1.0.0.jar
 */


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