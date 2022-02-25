import chess.model.Board.Board
import chess.model.Board.Success
import org.junit.Test
import kotlin.test.*
class BishopTest {
    @Test
    fun `Move Bishop in all possible ways`() {
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

    @Test
    fun `Trying to move Bishop to an enemy Piece`() {
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pc2c4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd7d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb2b4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pc7c5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pc4d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pc5b4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pa2a3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb4a3")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "pp  pppp"+
                    "        "+
                    "   P    "+
                    "        "+
                    "p       "+
                    "   PPPPP"+
                    "RNBQKBNR", sut.board.toStringTest() )
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Bc1a3")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "pp  pppp"+
                    "        "+
                    "   P    "+
                    "        "+
                    "B       "+
                    "   PPPPP"+
                    "RN QKBNR", sut.board.toStringTest() )

    }

    @Test
    fun `Trying to move Bishop to another friendly Piece`() {
        val board = Board()
        val sut = board.toMoveOrNull("Bc1b2")
        assertNull(sut)
    }

    @Test
    fun `Trying to move Bishop horizontally and vertically`() {
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pc2c4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd7d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb2b4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pc7c5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pc4d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pc5b4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pa2a3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb4a3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Bc1a3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ph7h6")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ba3b4")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "pp  ppp "+
                    "       p"+
                    "   P    "+
                    " B      "+
                    "        "+
                    "   PPPPP"+
                    "RN QKBNR", sut.board.toStringTest() )
        // moves right
        val sut1 = sut.board.toMoveOrNull("Bb4c4")
        assertNull(sut1, "Should not move right")
        // moves left
        val sut2 = sut.board.toMoveOrNull("Bb4a4")
        assertNull(sut2, "Should not move left")
        // moves up
        val sut3 = sut.board.toMoveOrNull("Bb4b3")
        assertNull(sut3, "Should not move up")
        // moves down
        val sut4 = sut.board.toMoveOrNull("Ba3b5")
        assertNull(sut4, "Should not move down")
    }
}


