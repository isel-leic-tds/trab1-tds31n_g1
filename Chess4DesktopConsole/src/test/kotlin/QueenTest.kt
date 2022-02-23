import model.Board.Board
import model.Board.Success
import model.Player
import org.junit.Test
import kotlin.test.assertEquals

class QueenTest {
    @Test
    fun `Moves Queen`() {
        var board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pe2e4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb7b6")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Qd1e2")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb6b5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Qe2c4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb5b4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Qc4f7")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "p pppQpp"+
                    "        "+
                    "        "+
                    " p  P   "+
                    "        "+
                    "PPPP PPP"+
                    "RNB KBNR", sut.board.toStringTest() )
    }
}