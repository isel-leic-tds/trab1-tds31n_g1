import model.Board.Board
import model.Board.Success
import model.Player
import org.junit.Test
import kotlin.test.assertEquals

class QueenTest {
    @Test
    fun `Moves Queen`() {
        var sut = Board().makeMove("Pe2e4",Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pb7b6",Player.BLACK) as Success
        sut = sut.board.makeMove("Qd1e2",Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pb6b5",Player.BLACK) as Success
        sut = sut.board.makeMove("Qe2c4",Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pb5b4",Player.BLACK) as Success
        sut = sut.board.makeMove("Qc4f7",Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pb4b3",Player.BLACK) as Success
        sut = sut.board.makeMove("Qf7f8",Player.WHITE) as Success//W

        assertEquals(
            "rnbqkQnr"+
                    "p ppp pp"+
                    "        "+
                    "        "+
                    "    P   "+
                    " p      "+
                    "PPPP PPP"+
                    "RNB KBNR", sut.board.toStringTest() )
    }
}
