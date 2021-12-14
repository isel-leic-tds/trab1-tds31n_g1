// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import Comands.buildMenuHandlers
import DataBase.MongoChessCommands
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.input.key.Key.Companion.Menu
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
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
                    onOpen = {

                    },
                    onExit = ::exitApplication
                )
            }
        }
    }
}

fun createGame(driver: MongoDriver) =
    GameChess(MongoChessCommands(driver), null, null, StatusGame(null,listOf(),null, null))
