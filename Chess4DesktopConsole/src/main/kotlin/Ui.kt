// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import Comands.buildMenuHandlers
import DataBase.MongoChessCommands
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import chess.model.Square
import model.Board.*
import mongoDb.MongoDriver

var curSquare: Square? = null

fun main() = application {
    val winState = WindowState(width = Dp.Unspecified, height = Dp.Unspecified)
    Window(
        onCloseRequest = ::exitApplication,
        state = winState,
        title = "Jogo de Xadrez"
    ) {
        MongoDriver().use { driver ->
            try {
                val mongoChessCommands = MongoChessCommands(driver)
                var board by remember { mutableStateOf(Board()) }
                val menuHandlers = buildMenuHandlers()
                DesktopMaterialTheme {
                    /*GaloMenuBar(
                        onNew = { game = Game() },
                        onExit = ::exitApplication
                    )
                     */
                    Column {
                        ChessView(board) { square ->
                            if (curSquare != null) {
                                val playerType = board.get(curSquare!!)!!.type
                                val str = "($playerType)$curSquare$square"
                                board.makeMove(str)
                                curSquare = null
                            } else
                                curSquare = square
                        }
                    }
                }
            } catch (ex: Exception) {
                println("Error: ${ex.message}.")
            }
        }
    }
}
