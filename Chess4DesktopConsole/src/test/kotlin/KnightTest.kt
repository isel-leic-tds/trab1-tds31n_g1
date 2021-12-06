import model.Board.Board
import model.Board.Success
import model.Player
import org.junit.Test
import kotlin.test.assertEquals


class KnightTest {
    @Test
    fun `Moves Knight`() {
        var sut = Board().makeMove("Nb1c3",Player.WHITE) as Success//W
        sut = sut.board.makeMove("Nb8a6",Player.BLACK) as Success
        sut = sut.board.makeMove("Nc3d5",Player.WHITE) as Success//W
        sut = sut.board.makeMove("Na6c5",Player.BLACK) as Success
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

