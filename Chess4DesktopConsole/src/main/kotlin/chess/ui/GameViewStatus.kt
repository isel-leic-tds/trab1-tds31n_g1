package chess.ui;

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import chess.DataBase.ChessDb;
import chess.model.Board.Board
import chess.model.Board.Move;
import chess.model.Player
import chess.model.StatusGame
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.launch

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
     * Create a new game where the player is the cross (first player).
     * @param gameName Name of the game to create.
     */
    fun new(gameName: String) {
        stopWait()
        name = gameName
        player = Player.WHITE
        game = Game()
        moves = emptyList()
        scope.launch { opers.save(name, emptyList()) }
    }
    /**
     * Open the game where the player is the circle (second player).
     * @param gameName Name of the game to load.
     */
    fun join(gameName :String) {
        stopWait()
        name = gameName
        player = Player.BLACK
        scope.launch {
            moves = opers.load(name)
            val g = Game(moves)
            game = g
            if (player != g.turn) waitForOther()
        }
    }
    /**
     * Try to make a move in the indicated [position].
     * @param position Where to make the move
     */
    fun tryPlay(position: Position) {
        val g = game ?: return
        if (g.turn!=player) return
        val g2 = g.tryPlay(position)
        if (g2 === g) return
        moves = moves + Move(position,g.turn)
        game = g2
        scope.launch {
            opers.save(name, moves)
            waitForOther()
        }
    }
    /**
     * Loads game moves, every 3 seconds, until there are more moves (when opponent plays).
     * This function starts a coroutine and suspends its execution.
     * The Job object allows canceling the wait, if necessary.
     */
    private fun waitForOther() {
        val g = game
        if (g==null || g.isOver) return
        waitingJob = scope.launch {
            do {
                delay(3000)
                moves = opers.load(name)
            } while (moves.size==g.numberOfPlays)
            game = Game(moves)
            waitingJob = null
        }
    }

    /**
     * Cancels the waiting for opponent's play.
     */
    private fun stopWait() {
        val job = waitingJob ?: return
        job.cancel()
        waitingJob = null
    }
}