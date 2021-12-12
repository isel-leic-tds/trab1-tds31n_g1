import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import chess.model.Square
import model.Board.*
import model.GameChess
import model.Player

val PLAY_SIDE = 150.dp
val GRID_WIDTH = 5.dp
const val GAME_DIM = 8
val BOARD_SIDE = PLAY_SIDE * GAME_DIM + GRID_WIDTH *(GAME_DIM -1)

@Composable
fun PlayView(square: Square, board: Board, onClick: () -> Unit) {
    Box(
        Modifier
            .size(PLAY_SIDE)
            .offset((PLAY_SIDE+GRID_WIDTH)*square.column.ordinal, (PLAY_SIDE+GRID_WIDTH)*square.row.ordinal)
            .background(Color.White)
            .clickable { onClick() }
    ) {
        val player = board[square]!!.player
        val img =
            when (board[square]!!.type) {
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
fun ChessView(board: Board, onClick: (Square)->Unit ) {
    Box(Modifier.background(Color.Black).size(PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1))) {
        Square.values.forEach { square ->
            PlayView(square, board) { onClick(square) }
        }
    }
}
