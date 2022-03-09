package chess.ui;

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import chess.DataBase.ChessDb;
import chess.DataBase.postMoves
import chess.model.GameChess
import chess.model.board.Board
import chess.model.board.Move;
import chess.model.Player
import chess.model.StatusGame
import chess.model.board.*
import kotlinx.coroutines.*

class GameViewStatus(private val chessDb: ChessDb, private val scope: CoroutineScope) {
    private var name: String="Unknown"
    var player by mutableStateOf<Player?>(null)
        private set
    var board by mutableStateOf<Board?>(null)
        private set
    private var moves: List<String> = emptyList()
    val waiting: Boolean get() = waitingJob!=null
    private var waitingJob: Job? = null

    /**
     * Opens a game with [gameName].
     * If [gameName] does not exist, creates new game with [gameName].
     */
    fun open(gameName: String) {
        stopWait()
        name = gameName
        player = Player.WHITE
        val game = getGame(gameName)
        this.board = if (game == null) {
            // inserts one document in the database so the other player can join the game
            postMoves(chessDb, gameName, "")
            this.moves = emptyList()
            Board()
        }
        else {
            this.moves = game.second
            game.first
        }
    }

    /**
     * Tries to open an existent game with [gameName].
     * If it doesn't exist, does nothing.
     */
    fun join(gameName :String) {
        stopWait()
        val (board, moves) = getGame(gameName) ?: return
        name = gameName
        player = Player.BLACK
        this.board = board
        this.moves = moves
    }

    /**
     * Tries to make [move].
     * If [move] is invalid, does nothing.
     */
    fun tryPlay(move: Move) {
        val board = this.board
        if (waiting || board == null) return
        val newBoard = board.makeMove(move)
        if (newBoard is Error) return
        if (newBoard is Success)
            this.board = newBoard.board
        waitForOther()
    }

    /**
     * Loads game moves, every 3 seconds, until there are more moves (when opponent plays).
     * This function starts a coroutine and suspends its execution.
     * The Job object allows canceling the wait, if necessary.
     */
    private fun waitForOther() = scope.launch { updateGame() }

    /**
     * Cancels the waiting for opponent's play.
     */
    private fun stopWait() {
        val job = waitingJob ?: return
        job.cancel()
        waitingJob = null
    }

    /**
     * Tries to refresh current game.
     * Stays in loop until game is updated.
     * @return if trying to restore game, detects that the game doesn't exist.
     */
    private suspend fun updateGame() {
        while (true) {
            delay(500)
            val (board, moves) = getGame(name) ?: return
            if (this.moves != moves) {
                this.moves = moves
                this.board = board
            }
        }
    }

    /**
     * Tries to restore the [gameName] game and if it can't, creates one with [gameName].
     * @return new Board with current game and respective moves list.
     */
    private fun getGame(gameName: String): Pair<Board, List<String>>? {
        val newBoard = Board()
        val moves = chess.DataBase.getMoves(chessDb, gameName)
        moves ?: return null
        if (moves.content == "")
            return newBoard to emptyList()
        val movesList = moves.content.trim().split(" ").toList()
        var statusGame = StatusGame(newBoard,movesList, null)
        movesList.forEach{ move: String ->
            val result = statusGame.board!!.makeMoveWithoutCheck(move)
            val board = result.board
            statusGame = statusGame.copy(board = board, lastMove = move)
        }
        return newBoard to movesList
    }
}