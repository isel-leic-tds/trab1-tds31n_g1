import junit.framework.Assert.assertEquals
import model.Board.Board
import org.junit.Test

class General {
    @Test
    fun `Initial position Board`() {
        val sut = Board()
        assertEquals(
            "rnbqkbnr"+
                    "pppppppp"+
                    "        ".repeat(4) +
                    "PPPPPPPP"+
                    "RNBQKBNR", sut.toStringTest() )
    }

    @Test
    fun `MakeMove in Board`() {
        val sut = Board().makeMoveWithoutCheck("Pe2e4").makeMoveWithoutCheck("Pe7e5").makeMoveWithoutCheck("Nb1c3")
        assertEquals(
            "rnbqkbnr"+
                    "pppp ppp"+
                    "        "+
                    "    p   "+
                    "    P   "+
                    "  N     "+
                    "PPPP PPP"+
                    "R BQKBNR", sut.toStringTest() )
    }

}