import chess.model.board.Board
import chess.model.board.Success
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs


class KingTest {
    @Test
    fun `Moves King in all possible directions`() {
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pe2e4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb7b6")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke1e2")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb6b5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke2e3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb5b4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke3f3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb4b3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Kf3e3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ph7h6")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke3d4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ph6h5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Kd4c5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ph5h4")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "p ppppp "+
                    "        "+
                    "  K     "+
                    "    P  p"+
                    " p      "+
                    "PPPP PPP"+
                    "RNBQ BNR", sut.board.toStringTest() )
        // moves left
        val sut1 = sut.board.makeMove(sut.board.toMoveOrNull("Kc5b5")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "p ppppp "+
                    "        "+
                    " K      "+
                    "    P  p"+
                    " p      "+
                    "PPPP PPP"+
                    "RNBQ BNR", sut1.board.toStringTest(), "Should move left")
        // moves right
        val sut2 = sut.board.makeMove(sut.board.toMoveOrNull("Kc5d5")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "p ppppp "+
                    "        "+
                    "   K    "+
                    "    P  p"+
                    " p      "+
                    "PPPP PPP"+
                    "RNBQ BNR", sut2.board.toStringTest(), "Should move right")
        // moves down
        val sut3 = sut.board.makeMove(sut.board.toMoveOrNull("Kc5c4")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "p ppppp "+
                    "        "+
                    "        "+
                    "  K P  p"+
                    " p      "+
                    "PPPP PPP"+
                    "RNBQ BNR", sut3.board.toStringTest(), "Should move down")
        // moves down-left
        val sut4 = sut.board.makeMove(sut.board.toMoveOrNull("Kc5b4")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "p ppppp "+
                    "        "+
                    "        "+
                    " K  P  p"+
                    " p      "+
                    "PPPP PPP"+
                    "RNBQ BNR", sut4.board.toStringTest(), "Should move down-left")
        // moves down-right
        val sut5 = sut.board.makeMove(sut.board.toMoveOrNull("Kc5d4")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "p ppppp "+
                    "        "+
                    "        "+
                    "   KP  p"+
                    " p      "+
                    "PPPP PPP"+
                    "RNBQ BNR", sut5.board.toStringTest(), "Should move down-right")

    }

    @Test
    fun `Puts King in Check`() {
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pe2e4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb7b6")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke1e2")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb6b5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke2e3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb5b4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke3f3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pb4b3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Kf3e3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ph7h6")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ke3d4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ph6h5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Kd4c5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Ph5h4")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "p ppppp "+
                    "        "+
                    "  K     "+
                    "    P  p"+
                    " p      "+
                    "PPPP PPP"+
                    "RNBQ BNR", sut.board.toStringTest() )
        // moves up
        val res = sut.board.makeMove(sut.board.toMoveOrNull("Kc5c6")!!)
        assertIs<chess.model.board.Error>(res, "Should be check")
    }
}

