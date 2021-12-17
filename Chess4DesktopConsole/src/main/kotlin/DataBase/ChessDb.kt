package DataBase


import Moves
import mongoDb.*

/**
 * The Chess basic operations.
 * Contract to be implemented by any concrete database.
 */
interface ChessDb {
    /**
     * replaces the moves in another document by [move]
     */
    fun replaceDocument(moves: Moves): Boolean

    /**
     * Posts the [moves] in a document
     */
    fun insertDocument(moves: Moves): Boolean
    /**
     * Retrieves the document identified by [gameId]
     */
    fun getDocument(gameId: String): Moves?
}

// Name of the collection that holds all the chess games
const val COLLECTION = "Chess"

/**
 * Implements the chess operations using a MongoDB instance.
 * @property driver to access MongoDb
 */
class MongoChessCommands(val driver: MongoDriver): ChessDb {
   override fun replaceDocument(moves: Moves) =
        driver.getCollection<Moves>(COLLECTION).replaceDocument(moves)

    override fun insertDocument(moves: Moves) =
        driver.getCollection<Moves>(COLLECTION).insertDocument(moves)

    override fun getDocument(gameId: String) = driver.getCollection<Moves>(COLLECTION).getDocument(gameId)

}

class LocalDb(): ChessDb {
    val map = HashMap<String,Moves>()

    override fun replaceDocument(moves: Moves): Boolean {
        map[moves._id] = moves
        return true
    }

    override fun insertDocument(moves: Moves): Boolean {
        map[moves._id] = moves
        return true
    }

    override fun getDocument(gameId: String): Moves? = map[gameId]
}