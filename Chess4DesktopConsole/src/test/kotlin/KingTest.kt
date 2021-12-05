import model.Board.Board
import model.Board.Success
import model.Player
import org.junit.Test
import kotlin.test.assertEquals


class KingTest {
    @Test
    fun `Moves King`() {
        var sut = Board().makeMove("Pe2e4", Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pb7b6", Player.BLACK) as Success
        sut = sut.board.makeMove("Ke1e2", Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pb6b5", Player.BLACK) as Success
        sut = sut.board.makeMove("Ke2e3", Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pb5b4", Player.BLACK) as Success
        sut = sut.board.makeMove("Ke3f3", Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pb4b3", Player.BLACK) as Success
        sut = sut.board.makeMove("Kf3e3", Player.WHITE) as Success//W
        assertEquals(
            "rnbqkbnr"+
                    "p pppppp"+
                    "        "+
                    "        "+
                    "    P   "+
                    " p  K   "+
                    "PPPP PPP"+
                    "RNBQ BNR", sut.board.toStringTest() )
    }
}

