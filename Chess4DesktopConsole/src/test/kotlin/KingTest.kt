import model.Board
import org.junit.Test
import kotlin.test.assertEquals


class KingTest {
    @Test
    fun `Moves King`() {
        val sut = Board()
            .makeMove("Pe2e4")!!.first//W
            .makeMove("Pb7b6")!!.first
            .makeMove("Ke1e2")!!.first//W
            .makeMove("Pb6b5")!!.first
            .makeMove("Ke2e3")!!.first//W
            .makeMove("Pb5b4")!!.first
            .makeMove("Ke3f3")!!.first//W
            .makeMove("Pb4b3")!!.first
            .makeMove("Kf3e3")!!.first//W

        assertEquals(
            "rnbqkbnr"+
                    "p pppppp"+
                    "        "+
                    "        "+
                    "    P   "+
                    " p  K   "+
                    "PPPP PPP"+
                    "RNBQ BNR", sut.toStringTest() )
    }
}