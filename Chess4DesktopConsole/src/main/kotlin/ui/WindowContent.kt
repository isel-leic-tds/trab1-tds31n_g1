import Commands.Command
import Commands.buildMenuHandlers
import DataBase.FileDb
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.FrameWindowScope
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

fun FrameWindowScope.WindowContent(driver: MongoDriver, onExit: ()->Unit ) {
    val scope = rememberCoroutineScope()
    val menuHandlers = buildMenuHandlers()
    // model
    var chess by remember { mutableStateOf(Chess(gameChess = createGame(driver))) }
    var startGame by remember { mutableStateOf<((gameName: String) -> Chess)?>(null) }
    var openPromotion by remember { mutableStateOf(false) }
    var squareAux by remember { mutableStateOf<Square?>(null) }
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
                    if (gameChess != null)
                        chess = chess.copy(gameChess = gameChess)
                    chess
                }
            }
        )
        MainView(chess) { square ->
            val board = chess.gameChess.status.board
            if (board != null) {
                // decides if its to open promotion window
                if (chess.selected != null && board.isPromotionPossible(chess.selected!!, square)) {
                    squareAux = square
                    openPromotion = true
                }
                else chess = onSquarePressed(chess, square, menuHandlers)
            }
        }
        val currStartGame = startGame
        if (currStartGame != null)
            DialogGameName(
                onOk = { chess = currStartGame(it); startGame = null },
                onCancel = { startGame = null }
            )
        if (openPromotion) {
            val curSquareAux = squareAux
            DialogPromotionPiece(
                onOk = {
                    if (curSquareAux != null)
                        chess = onSquarePressed(chess, curSquareAux, menuHandlers, it)
                    openPromotion = false
                }
            )
        }
        scope.launch {
            val gameChess = refreshGame(menuHandlers, chess.gameChess)
            chess = chess.copy(gameChess = gameChess)
        }
    }
}


fun createGame(driver: MongoDriver) =
    GameChess(FileDb(), null, null, StatusGame(null,listOf(),null, null))


/**
 * Tries to make a move if two pieces were selected or selects one.
 */
private fun onSquarePressed(chess: Chess, square: Square, menuHandlers: Map<String, Command>, pieceForPormotion: PieceType? = null): Chess {
    val selected = chess.selected
    // checks if its player turn
    if (!chess.gameChess.isPlayerTurn()) return chess
    val board = chess.gameChess.status.board
    if (board != null) {
        // if the player didnt choose yet a square marks a piece
        if (selected == null) {
            val result = board.isFromPlayer(square, chess.gameChess.player)
            // if there's no piece in that square or contains another player's piece
            if (result == null || !result)
                return chess
            return chess.copy(selected = square)
        }
        else {
            // unmarc selected piece
            if (selected === square)
                return chess.copy(selected = null)
            val piece = board[square]
            // if the player presses one of its own pieces
            if (piece != null && piece.player === chess.gameChess.player)
                return chess.copy(selected = square)
            // tries to make a move
            else {
                val move1 = board.toMoveOrNull(selected, square)
                val gameChess = play(menuHandlers, chess.gameChess, move1)
                if (gameChess != null) {
                    val move2 = gameChess.status.board.isPromotionPossible(move1)
                    if (move2 != null)
                        // TODO open promotion window
                }
                if (gameChess != null)
                    return Chess(gameChess = gameChess)
            }
        }
    }
    return chess
}
