@file:Suppress("DEPRECATION")

import junit.framework.Assert.assertEquals
import chess.model.Board.Board
import chess.model.Board.Success
import org.junit.Test

class PawnTest {
    @Test
    fun `Valid Moves`() {
        val board = Board()
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
        val board = Board()
        val sut = board.makeMove(board.toMoveOrNull("Pe2e3")!!) as Success
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
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pe2e4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd7d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pe4d5")!!) as Success
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
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pc2c4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd7d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pc4d5")!!) as Success
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
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pe2e4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd7d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pa2a3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd5e4")!!) as Success
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
        val board = Board()
        var sut = board.makeMove(board.toMoveOrNull("Pc2c4")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd7d5")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pa2a3")!!) as Success
        sut = sut.board.makeMove(sut.board.toMoveOrNull("Pd5c4")!!) as Success
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

