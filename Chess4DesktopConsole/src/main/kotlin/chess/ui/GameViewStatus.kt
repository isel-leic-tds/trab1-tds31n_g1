package chess.ui;

import Moves
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import chess.DataBase.ChessDb;
import chess.DataBase.postMoves
import chess.DataBase.replaceMoves
import chess.model.board.Board
import chess.model.board.Move;
import chess.model.Player
import chess.model.board.*
import kotlinx.coroutines.*

class GameViewStatus(private val chessDb: ChessDb, private val scope: CoroutineScope) {
    var name: String="Unknown"
    var player by mutableStateOf<Player?>(null)
        private set
    var board by mutableStateOf<Board?>(null)
        private set
    var moves: List<String> = emptyList()
    private val waiting: Boolean get() = waitingJob!=null
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
        waitForOther()
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
        waitForOther()
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
        if (newBoard is Success) {
            this.board = newBoard.board
            saveMove(move.toString())
        }
        waitForOther()
    }

    /**
     * Loads game moves, every 3 seconds, until there are more moves (when opponent plays).
     * This function starts a coroutine and suspends its execution.
     * The Job object allows canceling the wait, if necessary.
     */
    private fun waitForOther() {
        val board = this.board ?: return
        // if it's the players turn
        if (board.currentPlayer === player) return
        waitingJob = scope.launch { updateGame() }
    }

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
        val currentBoard = this.board ?: return
        while (true) {
            delay(500)
            val (board, moves) = getGame(name) ?: return
            if (currentBoard.toString() != board.toString()) {
                this.moves = moves
                this.board = board
                return
            }
        }
    }

    /**
     * Tries to restore the [gameName] game and if it can't, creates one with [gameName].
     * @return new Board with current game and respective moves list.
     */
    private fun getGame(gameName: String): Pair<Board, List<String>>? {
        var newBoard = Board()
        val moves = chess.DataBase.getMoves(chessDb, gameName)
        moves ?: return null
        if (moves.content == "")
            return newBoard to emptyList()
        val movesList = moves.content.trim().split(" ").toList()
        movesList.forEach{ move: String ->
            newBoard = newBoard.makeMoveWithoutCheck(move).board
        }
        return newBoard to movesList
    }

    /**
     * Appends a given [move] to the database with the [gameId] game identifier.
     */
    fun saveMove(move: String) {
        val moves: Moves? = chess.DataBase.getMoves(chessDb, name)
        if (moves == null)
            // adds a new document in the collection to hold the moves for the new chess game
            postMoves(chessDb, name, move)
        else
            // replace the first document (which) contains the saved moves for the current chess game
            replaceMoves(chessDb, name, moves.content+" "+move)
    }
}