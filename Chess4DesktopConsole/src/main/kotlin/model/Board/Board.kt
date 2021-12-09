package model.Board

import chess.model.*
import model.Player
import java.util.*

abstract class Result
data class Success(val board: Board, val str: String): Result() {
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
private class OponentSquare(): Error("Given square contains a piece witch belongs to the oponent player")
private class BadPiece(): Error("Given piece type does not correspond to the given current square")
private class PromotionNotValid(): Error("Given piece type for Promotion is not valid")

abstract class MoveType()
class Regular(): MoveType()
class Capture(): MoveType()
class Promotion(val newPiece: PieceType): MoveType()

data class Move(val piece: PieceType, val curSquare: Square, val newSquare: Square, val type: MoveType = Regular()) {
    override fun toString(): String {
        return piece.toStr() + curSquare.column.letter + curSquare.row.digit + newSquare.column.letter + newSquare.row.digit
    }
}

class Board {
    /**
     * Used to bound a piece to a player in the [boardArr]
     */
    class Piece(val type: PieceType, val player: Player)

    private val LINES = 8
    private val COLS = 8
    private val boardArr: Array<Array<Piece?>>
    private val finished: Boolean

    /**
     * Saves the position of the king throughout the game (It is going to be used for check and checkmate)
     */
    private val whiteKingPosition: Square
    private val blackKingPosition: Square

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
    }

    /**
     * To change the [boardArr] state/make movements.
     */
    constructor(board: Board, boardArr: Array<Array<Piece?>>) {
        this.boardArr = boardArr
        finished = board.finished
        var whiteKingPosition: Square? = null
        var blackKingPosition: Square? = null
        Square.values.forEach {square ->
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
    }

    /**
     * To change the [endOfGame] state
     */
    private constructor(board: Board, boardArr: Array<Array<Piece?>>, endOfGame: Boolean) {
        whiteKingPosition = board.whiteKingPosition
        blackKingPosition = board.blackKingPosition
        this.boardArr = boardArr
        finished = endOfGame
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
     * Converts the current state of the game in a String
     * Square.values.joinToString
     */
    fun toStringTest(): String {
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
                str += " " + aux
            } else str += "  "
            oldSquare = square
        }
        str += " |\n   -----------------"
        return str
    }

    /**
     * Used to retrieve the current state of the game hold in the database
     * Makes the move given in the [move] without checking if the move is possible.
     */
    fun makeMoveWithoutCheck(move: String): Board {
        val currSquare = move.substring(1, 3).toSquareOrNull()
        val newSquare = move.substring(3, 5).toSquareOrNull()
        //val move = Move(model.Board.getPieceType(move[0])!!,Square(currCol,currRow),Square(newCol,newRow))
        val piece = boardArr[currSquare!!.row.ordinal][currSquare.column.ordinal]
        val newBoard = boardArr.clone()
        boardArr[currSquare.row.ordinal][currSquare.column.ordinal] = null
        newBoard[newSquare!!.row.ordinal][newSquare.column.ordinal] = piece
        return Board(this,newBoard, finished)
    }

    /**
     * Used to make a move with a given String [str].
     * Needs also the [curPlayer] to check if the move is possible.
     * Always returns the given complete [str], in cases where the given [str] is not complete.
     * @return Result
     */
    // TODO ORGANIZE THIS FUNCTION
    fun makeMove(str: String, curPlayer: Player = Player.WHITE): Result {
        if (finished) return Finished()
        // checks if the [str] is valid
        var result = toMoveOrNull(str, curPlayer)
        if (result is Error) return result
        result = result as ISuccess
        val move = result.content as Move
        // checks if the given Square is valid
        result = isValidSquare(move, curPlayer)
        if (result is Error) return result
        // tries to make the Move
        result = makeMove(move)
        if (result is Error) return result
        var newBoard = ((result) as ISuccess).content as Board
        if (checkPromotion(move.newSquare)) {
            result = doPromotion(newBoard.boardArr, move.newSquare, move)
            if (result is Error) return result
            newBoard = ((result) as ISuccess).content as Board
        }
        return Success(newBoard, move.toString())
    }

    private fun checkPromotion(newSquare: Square): Boolean {
        val piece = boardArr[newSquare.row.ordinal][newSquare.column.ordinal]
        if (piece != null && piece.type is Pawn) {
            if (piece.player === Player.WHITE && newSquare.row === chess.model.Row.EIGHT
                || piece.player === Player.BLACK && newSquare.row === chess.model.Row.ONE)
               return true
        }
        return false
    }

    /**
     * Checks if the PieceType for Promotion is valid.
     * Otherwise, returns an Error
     */
    private fun getPieceForPromotion(move: Move): Result {
        val newPiece = (move.type as Promotion).newPiece
        if (newPiece is King || newPiece is Pawn)
            return PromotionNotValid()
        return ISuccess(newPiece)
    }

    /**
     * Does explicitly a Promotion move given in [square] with [newPiece].
     * @Throws IllegalCallerException if the [square] is empty.
     */
    private fun doPromotion(board: Array<Array<Piece?>>, square: Square, move: Move): Result {
        val result = getPieceForPromotion(move)
        if (result is ISuccess) {
            val newPiece = result.content as PieceType
            val newBoardArr = board.clone()
            val piece = newBoardArr[square.row.ordinal][square.column.ordinal] ?: throw IllegalCallerException()
            newBoardArr[square.row.ordinal][square.column.ordinal] = Piece(newPiece, piece.player)
            return ISuccess(Board(this, newBoardArr))
        }
        return result
    }

    /**
     * Stands for internal success and should be used to report that the private functions of the Board class had sucess.
     */
    private class ISuccess(val content: Any): Result()
    /**
     * Transforms a given [str] in a Move dataType to facilitate the operation in the makeMove().
     * Also checks if the [str] is incomplete and tries to reconstruct the complete [str].
     * @returns Result witch can be an Error or a valid Move
     */
    private fun toMoveOrNull(str: String, curPlayer: Player): Result {
        val cmd = str.trim()
        val pieceType = getPieceType(cmd[0])
        var currSquare: Square? = null
        val newSquare: Square?
        // omitting currentSquare
        if (str.length == 3) {
            newSquare = cmd.substring(1, 3).toSquareOrNull()
            if (newSquare == null) return BadMove()
            val result = getOmittedCurrentSquare(newSquare, curPlayer)
            if (result is Error) return result
            if (result is ISuccess)
                currSquare = result.content as Square
        }
        else {
            currSquare = cmd.substring(1, 3).toSquareOrNull()
            newSquare = cmd.substring(3, 5).toSquareOrNull()
        }
        val result = getPieceForPromotion(str)
        if (result is ISuccess) {
            val moveType = result.content as MoveType
            if (currSquare == null || newSquare == null || pieceType == null) return BadMove()
            return ISuccess(Move(pieceType, currSquare, newSquare, moveType))
        }
        return result // returns the error messaage
    }

    private fun getPieceForPromotion(str: String): Result {
        val moveType =
            if (str[3] == 'x') Capture()
            else if (str.length > 5 && str[5] == '=') {
                when (str[6]) {
                    'B' -> Promotion(Bishop())
                    'N' -> Promotion(Knight())
                    'R' -> Promotion(Rook())
                    'Q' -> Promotion(Queen())
                    'K' -> Promotion(King())
                    else -> Promotion(Pawn())//return PromotionNotValid()
                }
            }
            else Regular()
        return ISuccess(moveType)

    }

    private fun getOmittedCurrentSquare(newSquare: Square, curPlayer: Player): Result {
        var counter = 0
        var currentSquare: Square? = null
        // tries to find a valid corespondency
        Square.values.forEach { square ->
            val piece = boardArr[square.row.ordinal][square.column.ordinal]
            if (piece != null && piece.player == curPlayer) {
                val possibleSquares = piece.type.getAllMoves(Move(piece.type,square,newSquare),boardArr)
                if (possibleSquares.any{it.row == newSquare.row && it.column == newSquare.column}) {
                    currentSquare = square
                    ++counter
                }
            }
        }
        // there's ambiguity
        if (counter > 1) return Ambiguity()
        // no correspondicy found
        if (counter == 0) return InvalidMove("")
        return ISuccess(currentSquare!!)
    }

    /**
     * Checks if there's actually a Piece in the given Square
     * Checks also if the given Move is correct for the current player.
     */
    private fun isValidSquare(move: Move, curPlayer: Player): Result {
        // verifies if there's a piece in currentSquare
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] ?: return EmptySquare()
        // verifies if the piece type of the chooses square is the one in the str command
        if (piece.type.toStr() != move.piece.toStr()) return BadPiece()
        if (piece.player != curPlayer) return OponentSquare()
        return ISuccess(true)
    }

    /**
     * Checks if the given [move] is valid and if so, makes the [move].
     * @returns the new Board if the [move] was valid or null.
     */
    // TODO MAYBE THIS FUNCTION SHOULD GET A BOARD ARRAY?
    private fun makeMove(move: Move): Result {
        if (!isValidMove(move)) return InvalidMove(move.toString())
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]
        val newBoardArr = boardArr.clone()
        newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null
        newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece
        return ISuccess(Board(this, newBoardArr))
    }

    /**
     * @return if the given [move] is valid.
     */
    private fun isValidMove(move: Move): Boolean {
        val pos = move.piece.getAllMoves(move, boardArr)
        if (pos.any {
                it.row == move.newSquare.row && it.column == move.newSquare.column
            }) return true
        return false
    }

}

