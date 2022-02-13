package model.Board

import chess.model.*
import model.Player
import java.util.*
import kotlin.math.abs

abstract class Result

data class Success(val board: Board, val check: Boolean = false, val checkmate: Boolean = false): Result() {
    override fun toString(): String {
        return board.toString()
    }
}

abstract class Error(val error: String): Result() {
    override fun toString(): String {
        return error
    }
}
/**
 * Possible errors that can happen while trying to make a move
 */
private class InvalidMove(val error_: String): Error("Invalid move $error_")
private class Finished(): Error("Game has finished")
private class BadMove(): Error("Invalid command")
private class InvalidSquare(val error_: String): Error(error_)
private class Ambiguity(): Error("Specify the command")
private class EmptySquare(): Error("Given quare is empty")
private class OpponentSquare(): Error("Given square contains a piece witch belongs to the oponent player")
private class BadPiece(): Error("Given piece type does not correspond to the given current square")
private class PromotionNotValid(): Error("Given piece type for Promotion is not valid")
private class BadPromotion(): Error("Promotion shouldnt have been made")
private class MakePromotion(): Error("Promotion should have been made")
private class MyKingInCheck(): Error("Current move puts your King in check")

class MoveType(val special: SpecialMove?, val capture: Boolean = false)
abstract class SpecialMove()
class Promotion(val newPiece: PieceType?): SpecialMove()
class Castling(): SpecialMove()
class EnPassant(): SpecialMove()

data class Move(val piece: PieceType, val curSquare: Square, val newSquare: Square, val moveType: MoveType? = null) {
    override fun toString(): String {
        var str = piece.toStr() + curSquare.column.letter + curSquare.row.digit
        if (moveType != null && moveType.capture) str += 'x'
        str += "${newSquare.column.letter}${newSquare.row.digit}"
        if (moveType != null && moveType.special is Promotion) {
            val newPiece = moveType.special.newPiece
                str += '=' + (newPiece?.toStr() ?: "inFault")
        }
        return str
    }
}

@Suppress("NAME_SHADOWING")
class Board {
    /**
     * Used to bound a piece to a player in the [boardArr]
     */
    class Piece(val type: PieceType, val player: Player)

    private val LINES = 8
    private val COLS = 8
    private val boardArr: Array<Array<Piece?>>
    operator fun get(square: Square) = boardArr[square.row.ordinal][square.column.ordinal]
    private val finished: Boolean

    /**
     * Saves the position of the king throughout the game (It is going to be used for check and checkmate)
     */
    private val whiteKingPosition: Square
    private val blackKingPosition: Square

    val currentPlayer: Player

    /**
     * To iniciate the Game
     */
    constructor() {
        finished = false
        boardArr = Array(LINES) { Array(COLS) { null } }
        // updates the white and black King positions
        init()
        whiteKingPosition = Square(Column.E,Row.ONE)
        blackKingPosition = Square(Column.E,Row.EIGHT)
        currentPlayer = Player.WHITE
    }

    /**
     * To change the [boardArr] state/make movements.
     */
    private constructor(board: Board, boardArr: Array<Array<Piece?>>) { //Sempre que este construtor é chamado foi realizada uma jogada com sucesso
        this.boardArr = boardArr
        finished = board.finished
        var whiteKingPosition: Square? = null
        var blackKingPosition: Square? = null
        Square.values.forEach {square -> //Dar update da posição do rei
            val piece = boardArr[square.row.ordinal][square.column.ordinal]
            if (piece != null) {
                if (piece.type is King) {
                    if (piece.player === Player.WHITE)
                        whiteKingPosition = square
                    else
                        blackKingPosition = square
                }
            }
        }
        // should never be null
        this.whiteKingPosition = whiteKingPosition!!
        this.blackKingPosition = blackKingPosition!!
        currentPlayer = board.currentPlayer.other()
    }

    /**
     * To change the [endOfGame] state
     */
    private constructor(board: Board, boardArr: Array<Array<Piece?>>, endOfGame: Boolean) { //TODO: Não está a ser usado
        whiteKingPosition = board.whiteKingPosition
        blackKingPosition = board.blackKingPosition
        this.boardArr = boardArr
        finished = endOfGame
        currentPlayer = board.currentPlayer
    }

    /**
     * Initiates the board
     */
    private fun init() {
        initPlayer(Player.WHITE)
        initPlayer(Player.BLACK)
    }

    /**
     * Initiate player pieces
     */
    private fun initPlayer(player: Player) {
        var firstRow = Row.ONE.ordinal
        var currCol = Column.A.ordinal
        var secondRow = Row.TWO.ordinal
        if (player == Player.BLACK) {
            firstRow = Row.EIGHT.ordinal
            secondRow = Row.SEVEN.ordinal
        }
        boardArr[firstRow][currCol++] = Piece(Rook(), player)
        boardArr[firstRow][currCol++] = Piece(Knight(), player)
        boardArr[firstRow][currCol++] = Piece(Bishop(), player)
        boardArr[firstRow][currCol++] = Piece(Queen(), player)
        boardArr[firstRow][currCol++] = Piece(King(), player)
        boardArr[firstRow][currCol++] = Piece(Bishop(), player)
        boardArr[firstRow][currCol++] = Piece(Knight(), player)
        boardArr[firstRow][currCol] = Piece(Rook(), player)
        for (i in 0..7)
            boardArr[secondRow][i] = Piece(Pawn(), player)
    }

    /**
     * The chess board as a String
     */
    override fun toString(): String {
        var str = "    a b c d e f g h\n   -----------------\n"
        str += "" + Square.values[0].row.digit + " |"
        var oldSquare: Square? = null
        Square.values.forEach { square ->
            val piece = boardArr[square.row.ordinal][square.column.ordinal]
            if (oldSquare != null && square.row.ordinal == oldSquare!!.row.ordinal + 1) {
                str += " |\n"
                str += "" + square.row.digit + " |"
            }
            if (piece != null) {
                var aux = piece.type.toStr()
                if (piece.player == Player.BLACK)
                    aux = aux.lowercase(Locale.getDefault())
                str += " $aux"
            } else str += "  "
            oldSquare = square
        }
        str += " |\n   -----------------"
        return str
    }

    /**
     * Receives a Move [move] and tries to make a move with it.
     * Returns Sucess with new Board or an Error with information about what went wrong.
     * @return Result
     */
    fun makeMove(move: Move): Result {
        if (finished) return Finished()
        //isValidSquare() // TODO -> maybe its no necessary
        // checks move type and also if the move is valid
        val move = getMoveWithType(move) ?: return InvalidMove(move.toString())
        val newBoard = makeOneMove(move) ?: return InvalidMove(move.toString())
        return Success(newBoard)
    }

    /**
     * Given two squares, [pos1] and [pos2], returns a Move objetc.
     * Also, tries to find the move type.
     */
    fun toMoveOrNull(pos1: Square, pos2: Square): Move? {
        val piece = boardArr[pos1.row.ordinal][pos1.column.ordinal] ?: return null
        val pieceType = piece.type
        val move = Move(pieceType, pos1, pos2)
        return getMoveWithType(move)
    }

    /**
     * Given [move], converts it to a Move if is valid.
     */
    fun toMoveOrNull(move: String): Move? {
        val cmd = move.trim()
        val pieceType = getPieceType(cmd[0]) ?: return null
        val currSquare = cmd.substring(1, 3).toSquareOrNull() ?: return null
        val capture = move[3] == 'x'
        // tries to assert second square
        val n1 = if (capture) 4 else 3
        val n2 = if (capture) 6 else 5
        val newSquare = cmd.substring(n1,n2).toSquareOrNull() ?: return null
        // tests promotion
        if (!capture && move.length == 7 && move[5] == '=')
            return Move(pieceType, currSquare, newSquare, MoveType(Promotion(getPieceType(move[6]))))
        else if (capture && move.length == 8 && move[6] == '=')
            return Move(pieceType, currSquare, newSquare, MoveType(Promotion(getPieceType(move[7]))))
        val move = Move(pieceType, currSquare, newSquare)
        return getMoveWithType(move)
    }

    /**
     * Checks if given [move] has a type and if not, returns a new move with correct type.
     * If the type is Promotion and given [move] is not Promotion, returns a new Move with
     * Promotion but the new Piece is empty.
     * @return new Move with special move if it exists. If move is not valid, returns null.
     */
    private fun getMoveWithType(move: Move): Move? {
        if (!isValidMove(move)) return null
        val newPos = boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal]
        val capture = newPos?.player != null
        if (needsPromotion(move)) {
            val special = move.moveType?.special
            if (special != null && special is Promotion)
                return move.copy(moveType = MoveType(Promotion(special.newPiece), capture))
            // new piece for promotion is null
            return move.copy(moveType = MoveType(Promotion(null), capture))
        }
        if (canCastle(move))
            return move.copy(moveType = MoveType(Castling(), capture))
        if (canEnPassant(move))
            return move.copy(moveType = MoveType(EnPassant(), capture))
        // special move not possible with given [move].
        return move.copy(moveType = MoveType(null, capture))
    }

    data class Aux(val board: Board, val check: Boolean = false, val checkmate: Boolean = false) //Apenas usado na função makeMoveWithoutCheck()
    /**
     * Used to retrieve the current state of the game hold in the database
     * Makes the move given in the [str] without checking if the move is possible.
     */
    fun makeMoveWithoutCheck(str: String): Aux {
        val move = toMoveOrNull(str)!!
        val newBoard = makeOneMove(move)!!
        return Aux(newBoard)
    }

    /*************************************************CASTLE******************************************************************/

    /**
     * Checks if it is possible to make a castling move.
     */
    private fun canCastle(move: Move): Boolean {
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]
        // checks piece in the boardArray
        if (piece == null || piece.type !is King || piece.type.hasMoved) return false
        val diffCol = move.newSquare.column.ordinal - move.curSquare.column.ordinal
        if (diffCol == 2) {
            val piece = boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal+1]
            if (piece != null && piece.type is Rook && !piece.type.hasMoved)
                return true
        } else if (diffCol == -2) {
            val piece = boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal-2]
            if (piece != null && piece.type is Rook && !piece.type.hasMoved)
                return true
        }
        return false
    }

    enum class Direction { LEFT, RIGHT }
    /**
     * Checks wich is the direction to make a castling move.
     * @return the direction or null if it's not possible to castle.
     */
    private fun getCastleDirection(move: Move): Direction? {
        if (!canCastle(move)) return null
        val diffCol = move.newSquare.column.ordinal - move.curSquare.column.ordinal
        if (diffCol == 2)
            return Direction.RIGHT
        else if (diffCol == -2)
            return Direction.LEFT
        return null
    }

    /**
     * Tries to make a castling move if possible.
     * @return new Board after making castle move or null if not possible.
     */
    private fun makeCastling(move:Move): Board? {
        val direction = getCastleDirection(move) ?: return null
        val newBoardArr = boardArr.clone()
        if (direction === Direction.RIGHT) { //Short Path
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal+1] = null
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal - 1] = Piece(Rook(true), currentPlayer)
        } else { //Long Path
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal-2] = null
            newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal - 1] = Piece(Rook(true), currentPlayer)
        }
        // makes the King move and updates him be hasMoved
        return makeMoveWithoutChecking(move, Board(this, newBoardArr))
    }

    /**********************************************EN_PASSANT*******************************************************************/

    /**
     * Checks if it is possible to make enPassant with given [move].
     */
    private fun canEnPassant(move: Move): Boolean {
        // if given move is not valid (for some reason)
        if (isValidSquare(move) is Error) return false
        val diffCol = move.newSquare.column.ordinal - move.curSquare.column.ordinal
        // playerPiece will never be null because we called isValidSquare()
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]!!
        if (diffCol == -1) { //Left
            val advPawn = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal - 1]
            if (advPawn != null && piece.player != advPawn.player)
                if ((piece.type is Pawn) && (advPawn.type is Pawn) && (advPawn.type.twoSteps)) {
                    return true
            }
        } else if (diffCol == 1) { //Right
            val advPawn = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal + 1]
            if (advPawn != null && piece.player != advPawn.player)
                if ((piece.type is Pawn) && (advPawn.type is Pawn) && (advPawn.type.twoSteps)) {
                    return true
            }
        }
        return false
    }

    /**
     * @return direction in wich it's possible to make enPassant with given [move].
     */
    private fun getEnPassantDirection(move: Move): Direction? {
        if (!canEnPassant(move)) return null
        val diffCol = move.newSquare.column.ordinal - move.curSquare.column.ordinal
        return if (diffCol == -1) Direction.LEFT else Direction.RIGHT
    }

    /**
     * Tries to make enPassant move if possible.
     * @return new Board after making enPassant or null if not possible.
     */
    private fun makeEnPassant(move: Move): Board? {
        val direction = getEnPassantDirection(move) ?: return null
        val newBoardArr = boardArr.clone()
        if (direction === Direction.LEFT)
            newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal - 1] = null
        else
            newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal + 1] = null
        return makeMoveWithoutChecking(move, Board(this, newBoardArr))
    }

    /**
     * Stands for internal success and should be used to report that the private functions of the Board class had sucess.
     */
    private class ISuccess(val content: Any, val check:Boolean = false, val checkmate: Boolean = false,val draw: Boolean = false): Result()

    /**
     * Checks if there's actually a Piece in the given Square
     * Checks also if the given Move is correct for the current player.
     */
    private fun isValidSquare(move: Move): Result {
        // verifies if there's a piece in currentSquare
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] ?: return EmptySquare()
        // verifies if the piece type of the choosen square is the one in the move parameter
        if (piece.type.toStr() != move.piece.toStr()) return BadPiece()
        if (piece.player != currentPlayer) return OpponentSquare()
        return ISuccess(true)
    }

    /**
     * Makes a regular move if is valid, excluding special moves. In other words, only changes the piece position on the board,
     * based on the curSquare and the newSquare, not checking the move type.
     */
    private fun makeRegularMove(move: Move, board: Board = this): Board? {
        if (!isValidMove(move)) return null
        return makeMoveWithoutChecking(move, board)
    }

    /**
     * Maves [move] without checking if its valid
     * Warning!!! Use with caution.
     */
    private fun makeMoveWithoutChecking(move: Move, board: Board = this): Board {
        val boardArr = board.boardArr
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]!!
        val newBoardArr = boardArr.clone()
        newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null
        newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece
        return updatePieces(move, Board(this, newBoardArr))!!
    }

    /**
     * Checks if the given [move] is valid and if so, makes the move.
     * Also, tries to find the move type so it can make the correct move.
     * @returns the new Board if the [move] was valid or null.
     */
    private fun makeOneMove(move: Move): Board? {
        val gameState1 = getBoardState(this, whiteKingPosition, blackKingPosition)
        if (gameState1 === State.CHECKMATE) return null
        //if (!isValidMove(move)) return null // TODO -> maybe its not necessary
        val move = getMoveWithType(move) ?: return null
        val specialMove = move.moveType?.special
        val newBoard =
            if (specialMove == null)
                makeRegularMove(move)
            else {
                when (specialMove) {
                    is Promotion -> makePromotion(move.newSquare, specialMove.newPiece, makeRegularMove(move)?:return null)
                    is Castling -> makeCastling(move)
                    else -> makeEnPassant(move)
                }
            }
        val gameState2 = getBoardState(this, whiteKingPosition, blackKingPosition)
        if (gameState2 === State.CHECK) return null
        return newBoard
    }

    /**
     * @return if the given [move] is valid.
     */
    private fun isValidMove(move: Move, board: Board = this): Boolean {
        val boardArr = board.boardArr
        if((move.newSquare.row == blackKingPosition.row && move.newSquare.column == blackKingPosition.column)
            || (move.newSquare.row == whiteKingPosition.row && move.newSquare.column == whiteKingPosition.column)) return false
        if(move.newSquare == whiteKingPosition) return false
        val pos = move.piece.getAllMoves(move, boardArr)
        if (pos.any {
                it.row == move.newSquare.row && it.column == move.newSquare.column
            }) return true
        if (canCastle(move) || canEnPassant(move))
            return true
        return false
    }

    /**
     * Updates the peaces in the board that need to be updated.
     */
    private fun updatePieces(move:Move, board: Board = this): Board? {
        val boardArr = board.boardArr
        val piece = boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] ?: return null
        val newBoardArr = boardArr.clone()
        if(piece.type is King && move.piece is King)
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = Piece(King(true), currentPlayer)
        else if(piece.type is Rook && move.piece is Rook)
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = Piece(Rook(true), currentPlayer)
        else if(piece.type is Pawn && move.piece is Pawn)
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = Piece(updatePawn(move), currentPlayer)
        return Board(this, newBoardArr)
    }

    private fun updatePawn(move:Move): Pawn {
        val diffRow = abs(move.newSquare.row.ordinal - move.curSquare.row.ordinal)
        return if (diffRow == 2) {
            Pawn(twoSteps = true)
        } else {
            Pawn(twoSteps = false)
        }
    }

    /**
     * Checks if given square has a piece from [player], other player or hasnt a piece at all.
     */
    fun isFromPlayer(square: Square, player: Player): Boolean?  {
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece == null) return null
        return piece.player === player
    }

    /*************************************************PROMOTION******************************************************************/

    /**
     * Checks if it is possible to make a promotion with given [move].
     */
    fun needsPromotion(move: Move): Boolean {
        if (!isValidMove(move)) return false
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]
        if (piece != null && piece.type is Pawn) {
            if (piece.player === Player.WHITE && move.newSquare.row === Row.EIGHT
                || piece.player === Player.BLACK && move.newSquare.row === Row.ONE
            )
                return true
        }
        return false
    }

    /**
     * Given a [move], returns a new Move for promotion.
     */
    fun toPromotionMoveOrNull(move: Move, pieceType: PieceType): Move? {
        return if (needsPromotion(move))
            move.copy(moveType = MoveType(Promotion(pieceType)))
        else null
    }

    /**
     * Checks if promotion is possible in given square.
     */
    private fun isPromotionPossible(square: Square): Boolean {
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null && piece.type is Pawn) {
            if (piece.player === Player.WHITE && square.row === Row.EIGHT
                || piece.player === Player.BLACK && square.row === Row.ONE
            )
                return true
        }
        return false
    }

    /**
     * Makes promotion if possible.
     * @return a new Board after promotion.
     */
    private fun makePromotion(square: Square, newPiece: PieceType?, board: Board = this): Board? {
        if (newPiece == null) return board
        val boardArr = board.boardArr
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece == null || !isPromotionPossible(square)) return null
        val newBoardArr = boardArr.clone()
        newBoardArr[square.row.ordinal][square.column.ordinal] = Piece(newPiece, piece.player)
        return Board(this, newBoardArr)
    }

    /****************************************************************************************************************************/

    /*************************************************ONLY USED ON TESTS******************************************************************/

    /**
     * Converts the current state of the game in a String
     * Square.values.joinToString
     */
    fun toStringTest(): String { //Apenas usado nos testes
        var str = ""
        Square.values.forEach { square ->
            val piece = boardArr[square.row.ordinal][square.column.ordinal]
            if (piece != null) {
                var aux = piece.type.toStr()
                if (piece.player == Player.BLACK)
                    aux = aux.lowercase(Locale.getDefault())
                str += aux
            } else str += ' '
        }
        return str
    }
    /*******************************************************************************************************************************/

}