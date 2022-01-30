import Commands.Command
import Commands.Option
import Commands.buildMenuHandlers
import DataBase.FileDb
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.FrameWindowScope
import chess.model.Square
import kotlinx.coroutines.launch
import model.Board.Board
import model.Board.Move
import model.Board.PieceType
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
    var moveForPromotion by remember { mutableStateOf<Move>(null) }
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
            val chessAux = onSquarePressed(chess, square, menuHandlers)
            // if there was a move
            if (chessAux.gameChess !== chess.gameChess) {
                chess = chessAux
                val board = chess.gameChess.status.board
                if (board != null) {
                    val move = board.toMoveOrNull(chess.selected!!, square)
                    if (board.isPromotionPossible(move!!)) {
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
                if (board != null) {
                    val moveWithPromotion = board.getMoveForPromotion(moveForPromotionAux, it)
                    chess = board.makePromotion(moveForPromotion)
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
    scope.launch {
        val gameChess = refreshGame(menuHandlers, chess.gameChess)
        chess = chess.copy(gameChess = gameChess)
    }
}


fun createGame(driver: MongoDriver) =
    GameChess(FileDb(), null, null, StatusGame(null,listOf(),null, null))


/**
 * Tries to make a move if two pieces were selected or selects one.
 */
private fun onSquarePressed(chess: Chess, square: Square, menuHandlers: Map<Option, Command>, pieceForPormotion: PieceType? = null): Chess {
    val selected = chess.selected
    // checks if its player turn
    if (!chess.gameChess.isPlayerTurn()) return chess
    val board = chess.gameChess.status.board
    if (board != null) {
        // if the player didn't choose yet a square marks a piece
        if (selected == null)
            return noSquareSelected(board, square, chess)
        else
            return squareSelected(selected, board, square, chess, menuHandlers)
    }
    return chess
}

private fun noSquareSelected(board: Board, square: Square, chess: Chess): Chess {
    val result = board.isFromPlayer(square, chess.gameChess.player!!)
    // if there's no piece in that square or contains another player's piece
    if (result == null || !result)
        return chess
    return chess.copy(selected = square)
}


private fun squareSelected(selected: Square, board: Board, square: Square, chess: Chess, menuHandlers: Map<Option, Command>): Chess {
    // unmarcs selected piece
    if (selected === square)
        return chess.copy(selected = null)
    // if the player presses one of its own pieces
    val result = board.isFromPlayer(square, chess.gameChess.player!!)
    if (result == true)
        return chess.copy(selected = square)
    // tries to make a move
    else {
        // will never retun null because we already check if the selected square was null (TODO fix later (remove double bunn))
        val move = board.toMoveOrNull(selected, square)!! ?: return chess.copy(selected = null)
        // makes a move
        val gameChess = play(menuHandlers, chess.gameChess, move)
        if (gameChess != null) {
            return Chess(gameChess = gameChess)
        }
    }
}


private fun checkPromotionWindow(openPromotion: Boolean, square: Square, move: Move, pieceForPormotion: PieceType, board: Board) {
    if (openPromotion) {
        val curSquareAux = square
        DialogPromotionPiece(
            onOk = {
                if (curSquareAux != null) {
                    val moveWithPromotion = board.getMoveForPromotion(move, pieceForPormotion)
                    chess = board.makePromotion(move)
                }
                openPromotion = false
            }
        )
    }
}