import model.Board.Board
import model.Board.Success
import org.junit.Test
import kotlin.test.assertEquals


class BishopTest {
    @Test
    fun `Move Bishop all possible ways`() {
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pd2d4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd7d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Bc1e3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pa7a6")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Be3d2")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pa6a5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Bd2b4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pa5a4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Bb4c3")!!) as Success
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