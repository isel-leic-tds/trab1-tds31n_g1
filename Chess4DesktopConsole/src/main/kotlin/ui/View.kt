import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import chess.model.Square
import model.Board.*
import model.GameChess
import model.Player
import kotlin.math.sqrt

val BOARD_VIEW_PADDING = 30.dp
val LETTER_VIEW_PADDING_START = 50.dp
val LETTER_VIEW_PADDING_TOP = 20.dp
val NUMBER_VIEW_PADDING_START = 20.dp
val NUMBER_VIEW_PADDING_TOP = 20.dp
val PLAY_SIDE = 40.dp
val GRID_WIDTH = 5.dp
val GAME_DIM = sqrt(Square.values.size.toDouble()).toInt()
val MOVES_WITH = 300.dp
val LOG_HEIGHT = 80.dp
val MOVE_VIEW_BOX_PADDING = 20.dp
val MOVE_VIEW_ROW_PADDING = 10.dp
val LOG_VIEW_PADDING = 10.dp
val CHECK_VIEW = 50.dp
val CHECKMATE_VIEW = 50.dp

val TOTAL_HEIGHT =
    BOARD_VIEW_PADDING + LETTER_VIEW_PADDING_START + LETTER_VIEW_PADDING_TOP +
    NUMBER_VIEW_PADDING_START + NUMBER_VIEW_PADDING_TOP +
    (PLAY_SIDE + GRID_WIDTH) * GAME_DIM + LOG_HEIGHT + LOG_VIEW_PADDING + CHECK_VIEW +
    CHECKMATE_VIEW + MOVE_VIEW_BOX_PADDING + MOVE_VIEW_ROW_PADDING

@Composable
fun MainView(chess: Chess, showTargets: Boolean, onClick: (Square)->Unit) {
    Box(Modifier
        .background(Color(225, 172, 27))
        .height(TOTAL_HEIGHT)
    ) {
        Column {
            LettersView()
            Row {
                NumbersView()
                Column {
                    BoardView(chess, onClick, showTargets)
                    LogView(chess)
                    CheckView(chess.gameChess)
                    CheckmateView(chess.gameChess.status.board)
                    DrawView(chess.gameChess.status.draw)
                }
                MoveView(chess)
            }
        }
    }
}


@Composable
fun CheckView(gameChess: GameChess) {
    gameChess.status.board?:return
    if (gameChess.player === gameChess.status.board.currentPlayer && gameChess.status.board.check
        && !gameChess.status.board.checkmate) {
        Text(
            text = "CHECK",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .size(height = CHECK_VIEW, width = Dp.Unspecified),
        )
    }
}

@Composable
fun CheckmateView(board: Board?) {
    board?:return
    if (board.checkmate) {
        Text(
            text = "CHECKMATE",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .size(height = CHECKMATE_VIEW, width = Dp.Unspecified),
        )
    }
}

@Composable
fun DrawView(drawView: Boolean) {
    if (drawView) {
            Text(
            text = "DRAW",
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .size(height = PLAY_SIDE+GRID_WIDTH, width = Dp.Unspecified),
        )}
}

@Composable
fun LettersView() {
    Box(
        Modifier
            .padding(start = LETTER_VIEW_PADDING_START, top = LETTER_VIEW_PADDING_TOP)
            .size(height = Dp.Unspecified, width = PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1),)
    ) {
        Row {
            repeat(8) {
                Text(
                    text = "${('a'.code +it).toChar()}",
                    modifier = Modifier
                        .size(height = Dp.Unspecified, width = PLAY_SIDE+GRID_WIDTH),
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
            .padding(start = NUMBER_VIEW_PADDING_START, top = NUMBER_VIEW_PADDING_TOP)
            .size(height = PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1), width = Dp.Unspecified)
    ) {
        Column {
            repeat(8) {
                Text(
                    text = "${8 - it}",
                    modifier = Modifier
                        .size(height = PLAY_SIDE+GRID_WIDTH, width = Dp.Unspecified),
                )
            }
        }
    }
}

@Composable
fun PlayView(square: Square, board: Board?, selected: Boolean, possibleSquare: Boolean, onClick: () -> Unit) {
    paintSquare(square, onClick)
    var m = Modifier.size(PLAY_SIDE)
        .offset((PLAY_SIDE+GRID_WIDTH)*square.column.ordinal, (PLAY_SIDE+GRID_WIDTH)*square.row.ordinal)
    if (selected) {
        m = m.border(2.dp, Color.Red)
    }
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
                Image(painterResource("${img}W.png"), img, m)
            else
                Image(painterResource("${img}B.png"), img, m)
        }
        // draws circle
        if (possibleSquare)
            Box(modifier = m.clip(CircleShape).background(Color.Green.copy(alpha = 0.3f)))
    }
}

@Composable
private fun paintSquare(square: Square, onClick: () -> Unit) {
    if ((square.row.ordinal + square.column.ordinal) % 2 == 1)
        Box(
            Modifier
                .size(PLAY_SIDE)
                .offset((PLAY_SIDE + GRID_WIDTH) * square.column.ordinal, (PLAY_SIDE + GRID_WIDTH) * square.row.ordinal)
                .background(Color.Gray)
                .clickable {onClick()}
        )
    else
        Box(
            Modifier
                .size(PLAY_SIDE)
                .offset((PLAY_SIDE + GRID_WIDTH) * square.column.ordinal, (PLAY_SIDE + GRID_WIDTH) * square.row.ordinal)
                .background(Color.White)
                .clickable {onClick()}
        )
}

/**
 * Draws the board.
 */
@Composable
fun BoardView(chess: Chess, onClick: (Square)->Unit, showTargets: Boolean) {
    Box(Modifier
        .padding(BOARD_VIEW_PADDING)
        .background(Color.Black)
        .size(PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1))) {
        val possibleSquares =
            if (showTargets && chess.selected != null) chess.gameChess.status.board?.getPossibleSquaresToMove(chess.selected) else emptyList()
        Square.values.forEach { square ->
            PlayView(
                square,
                chess.gameChess.status.board,
                chess.selected === square,
                possibleSquares?.any {it == square} ?: false
            ) {onClick(square)}
        }
    }
}

@Composable
fun LogView(chess: Chess) {
    Box(Modifier
        .padding(LOG_VIEW_PADDING)
        .size(width = PLAY_SIDE* GAME_DIM+GRID_WIDTH*(GAME_DIM-1), height = LOG_HEIGHT)

    ) {
        val mod = Modifier.padding(5.dp)
        val gameId = chess.gameChess.gameId
        val currentPlayer = chess.gameChess.status.board?.currentPlayer
        if (gameId != null)
            Column {
                Row {
                    Text("Game: $gameId", fontWeight = FontWeight.Bold, modifier = mod)
                    Text(" | Turn: ${currentPlayer ?: "     "}", fontWeight = FontWeight.Bold, modifier = mod)
                }
                // TODO will display all error messages
                Text("Error: ", fontWeight = FontWeight.Bold, modifier = mod)
            }
    }
}

@Composable
fun MoveView(chess: Chess) {
    Box(Modifier
        .padding(MOVE_VIEW_BOX_PADDING)
        .size(width = MOVES_WITH, height = Dp.Unspecified)
        .background(Color.White)
    ) {
        Column {
            val moves = chess.gameChess.status.moves
            val size = moves.size
            moves.forEachIndexed { n, move ->
                if (n % 2 == 0) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(MOVE_VIEW_ROW_PADDING).fillMaxWidth()) {
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
