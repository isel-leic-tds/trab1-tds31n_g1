import Commands.Command
import Commands.Option
import Commands.buildMenuHandlers
import DataBase.FileDb
import DataBase.MongoDb
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.FrameWindowScope
import chess.model.Square
import kotlinx.coroutines.launch
import model.Board.Board
import model.Board.Move
import model.GameChess
import model.StatusGame
import mongoDb.MongoDriver
import ui.ChessMenuBar
import ui.DialogGameName
import ui.DialogPromotionPiece

@Composable
fun FrameWindowScope.WindowContent(driver: MongoDriver, onExit: ()->Unit ) {
    val scope = rememberCoroutineScope()
    val menuHandlers = buildMenuHandlers()
    // model
    var chess by remember { mutableStateOf(Chess(gameChess = createGame(driver))) }
    var startGame by remember { mutableStateOf<((gameName: String) -> Chess)?>(null) }
    var openPromotion by remember { mutableStateOf(false) }
    var moveForPromotion by remember { mutableStateOf<Move?>(null) }
    DesktopMaterialTheme {
        ChessMenuBar(
            onOpen = {
                startGame = {
                    val gameChess = openGame(menuHandlers, chess.gameChess, it)
                    if (gameChess != null)
                        chess = chess.copy(gameChess = gameChess)
                    chess
                }
            },
            onJoin = {
                startGame = {
                    val gameChess = joinGame(menuHandlers, chess.gameChess, it)
                    if (gameChess != null) {
                        chess = chess.copy(gameChess = gameChess)
                        scope.launch {
                            val gameChess = refreshGame(menuHandlers, chess.gameChess)
                            chess = chess.copy(gameChess = gameChess)
                        }
                    }
                    chess
                }
            }
        )
        MainView(chess) { square ->
            val result = onSquarePressed(chess, square, menuHandlers)
            if (result is Success) {
                chess = result.chess
                scope.launch {
                    val gameChess = refreshGame(menuHandlers, chess.gameChess)
                    chess = chess.copy(gameChess = gameChess)
                }
            }
            else if (result is PromotionNecessary) {
                val board = chess.gameChess.status.board
                if (board != null) {
                    val move = board.toMoveOrNull(chess.selected!!, square)
                    if (board.needsPromotion(move!!)) {
                        moveForPromotion = move
                        openPromotion = true
                    }
                }
            }
        }
    }
    if (openPromotion) {
        val moveForPromotionAux = moveForPromotion
        DialogPromotionPiece(
            onOk = {
                val board = chess.gameChess.status.board
                if (board != null && moveForPromotionAux != null) {
                    val moveWithPromotion = board.toPromotionMoveOrNull(moveForPromotionAux, it)
                    if (moveWithPromotion != null) {
                        val result = play(menuHandlers, chess.gameChess, moveWithPromotion)
                        if (result != null) chess = chess.copy(gameChess = result)
                    }
                }
                openPromotion = false
            }
        )
    }
    val currStartGame = startGame
    if (currStartGame != null)
        DialogGameName(
            onOk = { chess = currStartGame(it); startGame = null },
            onCancel = { startGame = null }
        )
}

fun createGame(driver: MongoDriver) =
    GameChess(MongoDb(driver), null, null, StatusGame(null,listOf(),null))

abstract class Result()
object PromotionNecessary: Result()
class Success(val chess: Chess): Result()

/**
 * Tries to make a move if two pieces were selected or selects one.
 */
private fun onSquarePressed(chess: Chess, square: Square, menuHandlers: Map<Option, Command>): Result {
    val selected = chess.selected
    // checks if its player turn
    if (!chess.gameChess.isPlayerTurn()) return Success(chess)
    val board = chess.gameChess.status.board
    if (board != null) {
        // if the player didn't yet choose a square marks a piece
        return if (selected == null)
            noSquareSelected(board, square, chess)
        else
            squareSelected(selected, board, square, chess, menuHandlers)
    }
    return Success(chess)
}

private fun noSquareSelected(board: Board, square: Square, chess: Chess): Result {
    val result = board.isFromPlayer(square, chess.gameChess.player!!)
    // if there's no piece in that square or contains another player's piece
    if (result == null || !result)
        return Success(chess)
    return Success(chess.copy(selected = square))
}

private fun squareSelected(selected: Square, board: Board, square: Square, chess: Chess, menuHandlers: Map<Option, Command>): Result {
    // unmarcs selected piece
    if (selected === square)
        return Success(chess.copy(selected = null))
    // if the player presses one of its own pieces
    val result = board.isFromPlayer(square, chess.gameChess.player!!)
    if (result == true)
        return Success(chess.copy(selected = square))
    // tries to make a move
    else {
        // will never retun null because we already check if the selected square was null
        val move = board.toMoveOrNull(selected, square) ?: return Success(chess.copy(selected = null))
        if (board.needsPromotion(move))
            return PromotionNecessary
        // makes a move
        val gameChess = play(menuHandlers, chess.gameChess, move)
        if (gameChess != null)
            return Success(Chess(gameChess = gameChess, selected = null))
        return Success(chess)
    }
}
