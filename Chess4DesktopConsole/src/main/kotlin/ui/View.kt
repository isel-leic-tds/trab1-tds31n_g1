import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.VerticalAlignmentLine
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chess.model.Square
import model.Board.*
import model.Player
import kotlin.math.sqrt

val PLAY_SIDE = 60.dp
val GRID_WIDTH = 5.dp
val GAME_DIM = sqrt(Square.values.size.toDouble()).toInt()
val BOARD_SIDE = PLAY_SIDE * GAME_DIM + GRID_WIDTH *(GAME_DIM -1)
val MOVES_DIM = 300.dp

@Composable
fun MainView(chess: Chess, onClick: (Square)->Unit ) {
    Box(Modifier
        .background(Color(225, 172, 27))
    ) {
        Column {
            LettersView()
            Row {
                NumbersView()
                Column {
                    BoardView(chess, onClick)
                    LogView(chess)
                }
                MoveView(chess)
            }
        }
    }
}


@Composable
fun LettersView() {
    Box(
        Modifier
            .padding(start = 50.dp, top = 20.dp)
            .size(height = Dp.Unspecified, width = PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1),)
        //.background(Color.Red)
    ) {
        Row {
            repeat(8) {
                Text(
                    text = "${('a'.code +it).toChar()}",
                    modifier = Modifier
                        .size(height = Dp.Unspecified, width = PLAY_SIDE+GRID_WIDTH),
                        //.background(Color.White)
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun NumbersView() {
    Box(
        Modifier
            .padding(start = 20.dp, top = 20.dp)
            .size(height = PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1), width = Dp.Unspecified)
            //.background(Color.Red)
    ) {
        Column {
            repeat(8) {
                Text(
                    text = "${8 - it}",
                    modifier = Modifier
                        .size(height = PLAY_SIDE+GRID_WIDTH, width = Dp.Unspecified),
                        //.background(Color.White)
                )
            }
        }
    }
}

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
        .padding(20.dp)
        .background(Color.Black)
        .size(PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1))) {
        Square.values.forEach { square ->
            PlayView(square, chess.gameChess.status.board, chess.selected === square) { onClick(square) }
        }
    }
}

@Composable
fun LogView(chess: Chess) {
    Box(Modifier
        .padding(10.dp)
        .size(width = PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1), height = Dp.Unspecified)

    ) {
        val mod = Modifier.padding(5.dp)
        val gameId = chess.gameChess.gameId
        val currentPlayer = chess.gameChess.status.currentPlayer
        if (gameId != null)
            Column {
                Row {
                    Text("Game: $gameId", fontWeight = FontWeight.Bold, modifier = mod.background(Color.Red))
                    Text(" | Turn: $currentPlayer", fontWeight = FontWeight.Bold, modifier = mod.background(Color.Blue))
                }
                // TODO will display all error messages
                Text("Error: ", fontWeight = FontWeight.Bold, modifier = mod.background(Color.Yellow))
            }
    }
}

@Composable
fun MoveView(chess: Chess) {
    Box(Modifier
        .padding(20.dp)
        .size(width = MOVES_DIM, height = Dp.Unspecified)
        .background(Color.White)
    ) {
        Column {
            val moves = chess.gameChess.status.moves
            val size = moves.size
            moves.forEachIndexed { n, move ->
                if (n % 2 == 0) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(10.dp).fillMaxWidth()) {
                        Text(
                            "${n/2+1}. $move",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                        )
                        if(size > n+1) {
                            Text(
                                " - ",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                moves[n + 1],
                                fontFamily = FontFamily.Monospace,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

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
                .background(Color.Gray)
        )
    else
        Box(
            Modifier
                .size(PLAY_SIDE)
                .offset((PLAY_SIDE + GRID_WIDTH) * square.column.ordinal, (PLAY_SIDE + GRID_WIDTH) * square.row.ordinal)
                .background(Color.White)
        )

}
