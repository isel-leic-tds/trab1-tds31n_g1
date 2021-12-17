import model.Board.Board
import model.Board.Success
import model.Player
import kotlin.test.assertEquals


class BishopTest {
    @Test
    fun `Move Bishop all possible ways`() {
        var sut: Success = Board().makeMove("Pd2d4",Player.WHITE) as Success//W
            sut = sut.board.makeMove("Pd7d5",Player.BLACK) as Success
            sut = sut.board.makeMove("Bc1e3",Player.WHITE) as Success//W
            sut = sut.board.makeMove("Pa7a6",Player.BLACK) as Success
            sut = sut.board.makeMove("Be3d2",Player.WHITE) as Success//W
            sut = sut.board.makeMove("Pa6a5",Player.BLACK) as Success
            sut = sut.board.makeMove("Bd2b4",Player.WHITE) as Success//W
            sut = sut.board.makeMove("Pa5a4",Player.BLACK) as Success
            sut = sut.board.makeMove("Bb4c3",Player.WHITE) as Success//W
        assertEquals(
            "rnbqkbnr"+
                    " pp pppp"+
                    "        "+
                    "   p    "+
                    "p  P    "+
                    "  B     "+
                    "PPP PPPP"+
                    "RN QKBNR", sut.board.toStringTest() )
    }
}