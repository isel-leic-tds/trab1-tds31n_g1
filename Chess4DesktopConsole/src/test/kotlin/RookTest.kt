import model.Board.Board
import model.Board.Success
import org.junit.Test
import kotlin.test.assertEquals

class RookTest {
    @Test
    fun `Move Rook forward and to the side`() {
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pa2a4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pa7a5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ra1a3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb7b5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ra3h3")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "  pppppp"+
                    "        "+
                    "pp      "+
                    "P       "+
                    "       R"+
                    " PPPPPPP"+
                    " NBQKBNR", sut.board.toStringTest() )
    }
}