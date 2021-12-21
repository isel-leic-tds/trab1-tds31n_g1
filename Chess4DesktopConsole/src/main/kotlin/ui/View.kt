import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import chess.model.Square
import model.Board.*
import model.Player
import org.jetbrains.skija.FontStyle
import org.litote.kmongo.text
import kotlin.math.sqrt

val PLAY_SIDE = 60.dp
val GRID_WIDTH = 5.dp
val GAME_DIM = sqrt(Square.values.size.toDouble()).toInt()
val BOARD_SIDE = PLAY_SIDE * GAME_DIM + GRID_WIDTH *(GAME_DIM -1)
val HISTORY_DIM = 300.dp

@Composable
fun PlayView(square: Square, board: Board?, selected: Boolean, onClick: () -> Unit) {
    paintSquare(square)
    var m = Modifier.size(PLAY_SIDE)
        .offset((PLAY_SIDE+GRID_WIDTH)*square.column.ordinal, (PLAY_SIDE+GRID_WIDTH)*square.row.ordinal)
        .clickable { onClick() }
    if (selected) m = m.border(2.dp, Color.Red)
    Box(m) {
        if (board != null) {
            val place = board[square]
            if (place != null) {
                val player = place.player
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
                    Image(painterResource("${img}W.png"), img)
                else
                    Image(painterResource("${img}B.png"), img)
            }
        }
    }
}

/**
 * Draws the board.
 */
@Composable
fun BoardView(chess: Chess, onClick: (Square)->Unit ) {
    Box(Modifier
        .background(Color(225, 172, 27))
        .padding(20.dp).background(Color.Black)
        .size(PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1))) {
        Square.values.forEach { square ->
            PlayView(square, chess.gameChess.status.board, chess.selected === square) { onClick(square) }
        }
    }
}

@Composable
fun MoveView(chess: Chess) {
    Box(Modifier
        .background(Color(225, 172, 27))
        .padding(20.dp)
        .size(HISTORY_DIM, PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1))
        .background(Color.White)) {
        Column {
            val moves = chess.gameChess.status.moves
            val size = moves.size
            moves.forEachIndexed { n, move ->
                if (n % 2 == 0) {
                    Row {
                        Text("${n/2+1} $move", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        if(size > n+1)
                            Text(" - ${moves[n + 1]}", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun paintSquare(square: Square) {
    if ((square.row.ordinal + square.column.ordinal) % 2 == 1)
        Box(
            Modifier
                .size(PLAY_SIDE)
                .offset((PLAY_SIDE + GRID_WIDTH) * square.column.ordinal, (PLAY_SIDE + GRID_WIDTH) * square.row.ordinal)
                .background(Color.White)
        )
    else
        Box(
            Modifier
                .size(PLAY_SIDE)
                .offset((PLAY_SIDE + GRID_WIDTH) * square.column.ordinal, (PLAY_SIDE + GRID_WIDTH) * square.row.ordinal)
                .background(Color.Gray)
        )

}
