import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.window.FrameWindowScope
import chess.DataBase.FileDb
import chess.model.Square
import chess.model.board.Move
import chess.model.GameChess
import chess.model.StatusGame
import chess.mongoDb.MongoDriver
import chess.ui.ChessMenuBar
import chess.ui.DialogGameName
import chess.ui.DialogPromotionPiece
import chess.ui.GameViewStatus

data class Chess(val selected: Square? = null, val status: GameViewStatus)

@Composable
fun FrameWindowScope.WindowContent(driver: MongoDriver, onExit: ()->Unit ) {
    val scope = rememberCoroutineScope()

    // View Model
    var chess by remember { mutableStateOf(Chess(status = GameViewStatus(FileDb(), scope))) }
    var startGame by remember { mutableStateOf<((gameName: String) -> Unit)?>(null) }
    var openPromotion by remember { mutableStateOf(false) }
    var moveForPromotion by remember { mutableStateOf<Move?>(null) }

    data class Options(val targets: Boolean, val singlePlayer: Boolean)
    var options by remember { mutableStateOf(Options(targets = true, singlePlayer = false)) }

    DesktopMaterialTheme {
        ChessMenuBar(
            onOpen = {
                startGame = { gameName ->
                    chess.status.open(gameName)
                }
            },
            onJoin = {
                startGame = { gameName ->
                    chess.status.join(gameName)
                }
            },
            onTargets = { options = options.copy(targets = !options.targets) },
            onSinglePlayer = { options = options.copy(singlePlayer = !options.singlePlayer) },
        )
        MainView(chess, options.targets) { square ->
            squarePressed(chess, square,
                onSelectedChange = {
                    chess = chess.copy(selected = square)
                },
                onPromotionNecessary = {
                    val board = chess.status.board
                    if (board != null) {
                        val move = board.toMoveOrNull(chess.selected!!, square)
                        if (board.needsPromotion(move!!)) {
                            moveForPromotion = move
                            openPromotion = true
                        }
                    }
                },
                onMoveMade = { chess = chess.copy(selected = null) }
            )
        }
    }
    if (openPromotion) {
        val moveForPromotionAux = moveForPromotion
        DialogPromotionPiece(
            onOk = {
                val board = chess.status.board
                if (board != null && moveForPromotionAux != null) {
                    val moveWithPromotion = board.toPromotionMoveOrNull(moveForPromotionAux, it)
                    if (moveWithPromotion != null)
                        chess.status.tryPlay(moveWithPromotion)
                }
                openPromotion = false
            }
        )
    }
    val currStartGame = startGame
    if (currStartGame != null)
        DialogGameName(
            onOk = { gameName ->
                currStartGame(gameName)
                startGame = null
            },
            onCancel = { startGame = null }
        )

}

fun createGame(driver: MongoDriver) =
    GameChess(FileDb(), null, null, StatusGame(null,listOf(),null))

/**
 * Calls [onSelectedChange] if [square] is valid to be selected.
 * Calls [onPromotionNecessary] if it's necessary to make a Promotion move.
 */
private fun squarePressed(chess: Chess, square: Square,
                          onSelectedChange: () -> Unit, onPromotionNecessary: () -> Unit, onMoveMade: () -> Unit) {
    // doesn't allow to select a piece if it's not the players turn.
    val board = chess.status.board ?: return
    if (chess.status.player != board.currentPlayer) return
    val selected = chess.selected
    // if the player didn't yet choose a square marks a piece
    if (selected == null)
        noSquareSelected(square, chess, onSelectedChange)
    else
        squareSelected(selected, square, chess, onSelectedChange, onPromotionNecessary, onMoveMade)
}

/**
 * Calls [onSelectedChange] if [square] is valid to be selected.
 */
private fun noSquareSelected(square: Square, chess: Chess, onSelectedChange: () -> Unit) {
    val board = chess.status.board ?: return
    val player = chess.status.player ?: return
    board[square] ?: onSelectedChange()
    val result = board.isFromPlayer(square, player)
    // if there's no piece in that square or contains another player's piece
    if (result == null || !result) return
    onSelectedChange()
}

/**
 * Calls [onSelectedChange] if [newSquare] is valid to be selected.
 * Calls [onPromotionNecessary] if it's necessary to make a Promotion move.
 * Calls [onMoveMade] if tried to make a move.
 */
private fun squareSelected(selected: Square, newSquare: Square, chess: Chess,
                           onSelectedChange: () -> Unit, onPromotionNecessary: () -> Unit, onMoveMade: () -> Unit) {
    val board = chess.status.board ?: return
    val player = chess.status.player ?: return
    // unmark selected piece
    if (selected === newSquare)
        onSelectedChange()
    // if the player presses one of its own pieces
    val result = board.isFromPlayer(newSquare, player)
    if (result == true)
        onSelectedChange()
    // tries to make a move
    else {
        // will never return null because we already checked if the selected square was null
        val move = board.toMoveOrNull(selected, newSquare) ?: return onMoveMade()
        if (board.needsPromotion(move))
            onPromotionNecessary()
        // makes a move
        chess.status.tryPlay(move)
        onMoveMade()
    }
}