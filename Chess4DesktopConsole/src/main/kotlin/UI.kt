// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import chess.model.Square
import model.Board.*
import model.GameChess
import model.Player


val PLAY_SIDE = 150.dp
val GRID_WIDTH = 5.dp
const val GAME_DIM = 8

@Composable
fun PlayView(square: Square, board: Board, onClick: () -> Unit) {
    Box(
        Modifier
        .size(PLAY_SIDE)
        .offset((PLAY_SIDE+GRID_WIDTH)*square.column.ordinal, (PLAY_SIDE+GRID_WIDTH)*square.row.ordinal)
        .background(Color.White)
        .clickable { onClick() }
    ) {
        val player = board[square].player
        val img =
            when (board[square].type) {
                is Pawn -> "pawn"
                is Rook -> "rook"
                is Bishop -> "bishop"
                is King -> "king"
                is Queen -> "queen"
                else -> "knight"
            }
        if (player === Player.WHITE)
            Image(painterResource("($img)W.png"), img)
        else
            Image(painterResource("($img)B.png"), img)
    }
}

@Composable
fun ChessView(gameChess: GameChess, onClick: (Square)->Unit ) {
    Box(Modifier.background(Color.Black).size(PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1))) {
        Square.values.forEach { square ->
            PlayView(square, gameChess.status.board) { onClick(square) }
        }
    }
}

fun main() = application {
    val winState = WindowState(width = Dp.Unspecified, height = Dp.Unspecified)
    Window(
        onCloseRequest = ::exitApplication,
        state = winState,
        title = "Jogo de Xadrez"
    ) {
        var game by remember { mutableStateOf(GameChess()) }
        DesktopMaterialTheme {
            ChessView(game) { pos ->
                if (game.canPlay(pos)) game = game.play(pos)
            }
        }
    }
}
