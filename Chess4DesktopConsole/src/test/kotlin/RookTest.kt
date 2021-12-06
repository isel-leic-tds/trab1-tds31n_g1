import model.Board.Board
import model.Board.Success
import model.Player
import org.junit.Test
import kotlin.test.assertEquals

class RookTest {
    @Test
    fun `Move Rook forward and to the side`() {
        var sut = Board().makeMove("Pa2a4",Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pa7a5",Player.BLACK) as Success
        sut = sut.board.makeMove("Ra1a3",Player.WHITE) as Success//W
        sut = sut.board.makeMove("Pb7b5",Player.BLACK) as Success
        assertEquals(
            "rnbqkbnr"+
                    "  pppppp"+
                    "        "+
                    "pp      "+
                    "P       "+
                    "R       "+
                    " PPPPPPP"+
                    " NBQKBNR", sut.board.toStringTest() )
    }
}