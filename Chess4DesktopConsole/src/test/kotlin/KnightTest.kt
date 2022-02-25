import chess.model.Board.Board
import chess.model.Board.Success
import org.junit.Test
import kotlin.test.assertEquals


class KnightTest {
    @Test
    fun `Moves Knight`() {
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Nb1c3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Nb8a6")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Nc3d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Na6c5")!!) as Success
        assertEquals(
            "r bqkbnr"+
                    "pppppppp"+
                    "        "+
                    "  nN    "+
                    "        "+
                    "        "+
                    "PPPPPPPP"+
                    "R BQKBNR", sut.board.toStringTest() )
    }
}

