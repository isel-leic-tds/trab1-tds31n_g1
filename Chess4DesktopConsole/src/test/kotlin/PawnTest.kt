import junit.framework.Assert.assertEquals
import model.Board.Board
import model.Board.Success
import model.Player
import org.junit.Test

class PawnTest {
    @Test
    fun Masdasdas() {
        var board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pe2e3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd7d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pe3e4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd5d4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd2d4")!!) as Success
        assertEquals(
            "rnbqkbnr"+
                    "ppp pppp"+
                    "        "+
                    "        "+
                    "   PP   "+
                    "        "+
                    "PPP  PPP"+
                    "RNBQKBNR", sut.board.toStringTest() )
    }

    @Test
    fun `Makes one step forward with Pawn in Board`() {
        val sut = Board().makeMove("Pe2e3",Player.WHITE) as Success
        assertEquals(
            "rnbqkbnr"+
                    "pppppppp"+
                    "        "+
                    "        "+
                    "        "+
                    "    P   "+
                    "PPPP PPP"+
                    "RNBQKBNR", sut.board.toStringTest() )
    }

    @Test
    fun `White eats Left`() {
        var sut = Board().makeMove("Pe2e4",Player.WHITE) as Success
        sut = sut.board.makeMove("Pd7d5",Player.BLACK) as Success
        sut = sut.board.makeMove("Pe4d5",Player.WHITE) as Success
        assertEquals(
            "rnbqkbnr"+
                    "ppp pppp"+
                    "        "+
                    "   P    "+
                    "        "+
                    "        "+
                    "PPPP PPP"+
                    "RNBQKBNR", sut.board.toStringTest() )
    }

    @Test
    fun `White eats Right`() {
        var sut = Board().makeMove("Pc2c4",Player.WHITE) as Success
        sut = sut.board.makeMove("Pd7d5",Player.BLACK) as Success
        sut = sut.board.makeMove("Pc4d5",Player.WHITE) as Success
        assertEquals(
            "rnbqkbnr"+
                    "ppp pppp"+
                    "        "+
                    "   P    "+
                    "        "+
                    "        "+
                    "PP PPPPP"+
                    "RNBQKBNR", sut.board.toStringTest() )
    }

    @Test
    fun `Black eats Left`() {
        var sut = Board().makeMove("Pe2e4",Player.WHITE) as Success
        sut = sut.board.makeMove("Pd7d5",Player.BLACK) as Success
        sut = sut.board.makeMove("Pa2a3",Player.WHITE) as Success
        sut = sut.board.makeMove("Pd5e4",Player.BLACK) as Success
        assertEquals(
            "rnbqkbnr"+
                    "ppp pppp"+
                    "        "+
                    "        "+
                    "    p   "+
                    "P       "+
                    " PPP PPP"+
                    "RNBQKBNR", sut.board.toStringTest() )
    }

    @Test
    fun `Black eats Right`() {
        var sut = Board().makeMove("Pc2c4",Player.WHITE) as Success
        sut = sut.board.makeMove("Pd7d5",Player.BLACK) as Success
        sut = sut.board.makeMove("Pa2a3",Player.WHITE) as Success
        sut = sut.board.makeMove("Pd5c4",Player.BLACK) as Success
        assertEquals(
            "rnbqkbnr"+
                    "ppp pppp"+
                    "        "+
                    "        "+
                    "  p     "+
                    "P       "+
                    " P PPPPP"+
                    "RNBQKBNR", sut.board.toStringTest())
    }
}

