import model.Board.Board
import model.Board.Success
import model.Player
import org.junit.Test
import kotlin.test.assertEquals


class KingTest {
    @Test
    fun `Moves King`() {
        var board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pe2e4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb7b6")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke1e2")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb6b5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke2e3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb5b4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke3f3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb4b3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Kf3e3")!!) as Success
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

