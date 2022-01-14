import Commands.Command
import Commands.Success
import Commands.buildMenuHandlers
import DataBase.FileDb
import DataBase.MongoDb
import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import chess.model.Square
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.Board.PieceType
import model.Board.toStr
import model.GameChess
import model.Player
import model.StatusGame
import mongoDb.MongoDriver
import ui.ChessMenuBar
import ui.DialogGameName
import ui.DialogPromotionPiece

// TODO a UI vai ter de perguntar à Board se é possível fazer promotion numa dada peça

data class Chess(val selected: Square? = null, val gameChess: GameChess)

fun main() = MongoDriver().use { driver ->
    application {
        val winState = WindowState(width = Dp.Unspecified, height = Dp.Unspecified)
        Window(
            onCloseRequest = ::exitApplication,
            state = winState,
            title = "Jogo de Xadrez"
        ) {
            val scope = rememberCoroutineScope()
            val menuHandlers = buildMenuHandlers()
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
                    },
                )
                MainView(chess) { square ->
                    val board = chess.gameChess.status.board
                    if (board != null) {
                        // decides if its to open promotion window
                        if (chess.selected != null && board.isPromotionPossible(chess.selected!!, square)) {
                            squareAux = square
                            openPromotion = true
                        }
                        else chess = pressSquare(chess, square, menuHandlers)
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
                                chess = pressSquare(chess, curSquareAux, menuHandlers, it)
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
    }
}

fun createGame(driver: MongoDriver) =
    GameChess(FileDb(), null, null, StatusGame(null,listOf(),null, null))


/**
 * Tries to make a move if two pieces were selected or selects one.
 */
private fun pressSquare(chess: Chess, square: Square, menuHandlers: Map<String, Command>, pieceForPormotion: PieceType? = null): Chess {

    val selected = chess.selected
    val board = chess.gameChess.status.board
    if (board != null) {
        // if it's not the players turn
        if (chess.gameChess.player !== chess.gameChess.status.currentPlayer) return chess
        // marks a piece
        if (selected == null) {
            return if (board[square] != null && board[square]!!.player === chess.gameChess.player)
                chess.copy(selected = square)
            else chess
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
                val pieceType = board[selected]!!.type.toStr()
                val current = chess.selected.toString()
                val target = square.toString()
                // tests promotion
                if (pieceForPormotion != null) {
                    val move = "$pieceType$current$target=${pieceForPormotion.toStr()}"
                    val gameChess = play(menuHandlers, chess.gameChess, move)
                    if (gameChess != null)
                        return Chess(gameChess = gameChess)
                }
                else {
                    val move = pieceType + current + target
                    val gameChess = play(menuHandlers, chess.gameChess, move)
                    if (gameChess != null)
                        return Chess(gameChess = gameChess)
                }
            }
        }
    }
    return chess
}
