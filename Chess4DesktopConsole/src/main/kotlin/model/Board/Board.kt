package model.Board

import chess.model.*
import com.mongodb.client.model.geojson.Position
import model.Player
import java.util.*
import kotlin.math.abs

abstract class Result

data class Success(val board: Board, val str: String, val check: Boolean = false, val checkmate: Boolean = false): Result() {
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

abstract class MoveType()
class Regular(): MoveType()
class Capture(): MoveType()
class Promotion(val newPiece: PieceType?): MoveType()

data class Move(val piece: PieceType, val curSquare: Square, val newSquare: Square, val type: MoveType = Regular()) {
    override fun toString(): String {
        if (this.type is Promotion)
            return piece.toStr() + curSquare.column.letter + curSquare.row.digit + newSquare.column.letter + newSquare.row.digit + '=' + this.type.newPiece.toStr()
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
    operator fun get(square: Square) = boardArr[square.row.ordinal][square.column.ordinal]
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
    constructor(board: Board, boardArr: Array<Array<Piece?>>) { //Sempre que este construtor é chamado foi realizada uma jogada com sucesso
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
    }

    /**
     * To change the [endOfGame] state
     */
    private constructor(board: Board, boardArr: Array<Array<Piece?>>, endOfGame: Boolean) { //TODO: Não está a ser usado
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

    data class Aux(val board: Board, val check: Boolean = false, val checkmate: Boolean = false) //Apenas usado na função makeMoveWithoutCheck()
    /**
     * Used to retrieve the current state of the game hold in the database
     * Makes the move given in the [move] without checking if the move is possible.
     */
    fun makeMoveWithoutCheck(move: String): Aux { //Usado para dar restore e Join ao jogo
        val currSquare = move.substring(1, 3).toSquareOrNull()
        val newSquare = move.substring(3, 5).toSquareOrNull()
        val move1 = Move(getPieceType(move[0])!!,currSquare!!,newSquare!!)
        // tests promotion
        val player = boardArr[currSquare.row.ordinal][currSquare.column.ordinal]!!.player
        val piece = if (move.length > 5 && move[5] == '=') Piece(getPieceType(move[6])!!,player) else boardArr[currSquare.row.ordinal][currSquare.column.ordinal]!!
        val newBoardArr = boardArr.clone()
        if (doCastling(move1, piece, newBoardArr))  {
            val result = checkAndCheckmate(move1,newBoardArr,piece) as ISuccess
            return Aux(result.content as Board)
        }

        if(canEnPassant(move1,piece,newBoardArr)) {
            newBoardArr[currSquare.row.ordinal][currSquare.column.ordinal] = null
            newBoardArr[newSquare.row.ordinal][newSquare.column.ordinal] = piece
            val result = checkAndCheckmate(move1,newBoardArr,piece) as ISuccess
            return Aux(result.content as Board)
        }

        newBoardArr[currSquare.row.ordinal][currSquare.column.ordinal] = null
        newBoardArr[newSquare.row.ordinal][newSquare.column.ordinal] = piece

        if(piece.type is Pawn) {
            newBoardArr[newSquare.row.ordinal][newSquare.column.ordinal] = Piece(updatePawn(move1),piece.player)
        }

        updateKingAndRook(move1,piece,newBoardArr)

        val checkResult = checkAndCheckmate(move1,newBoardArr,piece) as ISuccess

        return Aux(checkResult.content as Board,checkResult.check, checkResult.checkmate)
    }

    /**
     * Receives a Move [move] and tries to make a move with it.
     * Returns Sucess with new Board or an Error with information about what went wrong.
     * @return Result
     */
    // TODO theres a bug when Promotion is made but it shouldt, then when we try to make a new move it says that the square is empty
    fun makeMove(move: Move, curPlayer: Player = Player.WHITE): Result {
        if (finished) return Finished()
        // checks if the given Square is valid
        var result = isValidSquare(move, curPlayer)
        if (result is Error) return result

        // tries to make the Move
        result = makeMove(move)
        if (result is Error) return result
    }

    fun test() {
        var newBoard = ((result) as ISuccess).content as Board
        val inCheck = (result).check
        val inCheckmate = (result).checkmate

        // promotion
        if (checkPromotion(move.newSquare)) {
            if (!(move.type is Promotion))
                return MakePromotion()
            result = doPromotion(newBoard.boardArr, move.newSquare, move)
            if (result is Error) return result
            newBoard = ((result) as ISuccess).content as Board
        }
        else if (move.type is Promotion)
            return BadPromotion()

        return Success(newBoard, move.toString(), inCheck, inCheckmate)
    }

    /**
     * When we need to know if that piece can make a promotion
     */
    fun isPromotionPossible(move: Move): Move? {
        val piece = boardArr[move.curSquare.row.ordinal][move.newSquare.column.ordinal]
        if (piece != null && piece.type is Pawn) {
            if (piece.player === Player.WHITE && move.newSquare.row === Row.EIGHT
                || piece.player === Player.BLACK && move.newSquare.row === Row.ONE
            )
                return move.copy(type = Promotion(null))
        }
        return null

    }

    private fun checkPromotion(newSquare: Square): Boolean {
        val piece = boardArr[newSquare.row.ordinal][newSquare.column.ordinal]
        if (piece != null && piece.type is Pawn) {
            if (piece.player === Player.WHITE && newSquare.row === Row.EIGHT
                || piece.player === Player.BLACK && newSquare.row === Row.ONE)
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

    private fun canCastle(move: Move,piece: Piece):Int {
        //Antes ainda ver se é a primeira jogada tanto do rei como da torre
        var counter = 0
        val diffCol = move.newSquare.column.ordinal - move.curSquare.column.ordinal
        if(piece.type is King) {
            if (diffCol == 2) { //ShortPath
                counter = 1
            } else if (diffCol == -2) { //LongPath
                counter = 2
            }
        }
        return counter

    }

    private fun doCastling(move:Move,piece:Piece,newBoardArr:Array<Array<Piece?>>): Boolean{
        if(canCastle(move,piece) == 1) { //Short Path
            val towerPiece = newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal+1]
            if(towerPiece != null) {
                if(piece.type is King && !piece.type.hasMoved && towerPiece.type is Rook && !towerPiece.type.hasMoved) {
                    newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null //Where King was
                    newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece //King
                    newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal + 1] = null
                    newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal - 1] = towerPiece
                    return true
                }
            }

        }
        else if(canCastle(move,piece) == 2) { //Long Path
            val towerPiece = newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal-2]
            if(towerPiece != null) {
                if(piece.type is King && !piece.type.hasMoved && towerPiece.type is Rook && !towerPiece.type.hasMoved) {
                    newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null
                    newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece //King
                    newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal - 2] = null
                    newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal - 1] = towerPiece
                    return true
                }
            }

        }
        return false
    }

    private fun canEnPassant(move:Move,piece:Piece,newBoardArr:Array<Array<Piece?>>):Boolean {
        val diffCol = move.newSquare.column.ordinal - move.curSquare.column.ordinal
        val playerPiece = piece.player
        if (diffCol == -1) {//Left
            val advPawn = newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal - 1]
            if (advPawn != null && playerPiece != advPawn.player) {
                if ((piece.type is Pawn) && (advPawn.type is Pawn) && (advPawn.type.twoSteps)) {
                    newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal - 1] = null
                    return true
                }
            }
        } else if (diffCol == 1) { //Right
            val advPawn = newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal + 1]
            if (advPawn != null && playerPiece != advPawn.player) {
                if ((piece.type is Pawn) && (advPawn.type is Pawn) && (advPawn.type.twoSteps)) {
                    newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal + 1] = null
                    return true
                }
            }
        }
        return false
    }

    private fun updatePawn(move:Move):Pawn {
        val diffRow = abs(move.newSquare.row.ordinal - move.curSquare.row.ordinal)
        return if (diffRow == 2) {
            Pawn(twoSteps = true)
        } else {
            Pawn(twoSteps = false)
        }
    }

    /**
     * Stands for internal success and should be used to report that the private functions of the Board class had sucess.
     */
    private class ISuccess(val content: Any, val check:Boolean = false, val checkmate: Boolean = false,val draw: Boolean = false): Result()
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
        val result = getMoveType(str)
        if (result is ISuccess) {
            val moveType = result.content as MoveType
            if (currSquare == null || newSquare == null || pieceType == null) return BadMove()
            return ISuccess(Move(pieceType, currSquare, newSquare, moveType))
        }
        return result // returns the error messaage
    }

    fun toMoveOrNull(pos1: Square, pos2: Square): Move? {
        val piece = boardArr[pos1.row.ordinal][pos2.column.ordinal] ?: return null
        val pieceType = piece.type
        return Move(pieceType, pos1, pos2)
    }

    private fun getMoveType(str: String): Result {
        val moveType =
            if (str[3] == 'x') Capture()
            else if (str.length > 5 && str[5] == '=' && str[0] == 'P') {
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
        if (piece.player != curPlayer) return OpponentSquare()
        return ISuccess(true)
    }

    /**
     * Checks if the given [move] is valid and if so, makes the [move].
     * @returns the new Board if the [move] was valid or null.
     */
    private fun makeMove(move: Move): Result {
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]!!
        val newBoardArr = boardArr.clone()
        if(doCastling(move,piece,newBoardArr)) return checkAndCheckmate(move,newBoardArr,piece)
        if(canEnPassant(move,piece,newBoardArr)) {
            newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece
            return checkAndCheckmate(move,newBoardArr,piece)
        }
        //Ver se é possível mover a peça para esse sitio ou se a peça para onde queremos mover é rei
        if (!isValidMove(move)) return InvalidMove(move.toString())
        newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null
        newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece

        if(piece.type is Pawn) {
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = Piece(updatePawn(move),piece.player)
        }

        updateKingAndRook(move,piece,newBoardArr)

        return checkAndCheckmate(move,newBoardArr,piece)
    }

    private fun checkAndCheckmate(move: Move,newBoardArr:Array<Array<Piece?>>,piece:Piece):Result {
        // update king position
        val whiteKingPosition = if(piece.type is King && piece.player === Player.WHITE ) move.newSquare else this.whiteKingPosition
        val blackKingPosition = if(piece.type is King && piece.player === Player.BLACK ) move.newSquare else this.blackKingPosition

        if(isMyKingInCheck(move, newBoardArr, whiteKingPosition, blackKingPosition)) { //Problema com as posições dos reis que ainda nao foram atualizadas nesta altura
            newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = piece
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = null
            return MyKingInCheck()
        }

        val checkSquares = isAdversaryKingInCheck(move, newBoardArr, whiteKingPosition, blackKingPosition).size
        val piecesThatCanEat = piecesToEatCheckPiece(move, newBoardArr)
        if(checkSquares > 0) {
            if(checkSquares == 1) {
                val square = isAdversaryKingInCheck(move, newBoardArr, whiteKingPosition, blackKingPosition)
                if (canAnyPieceProtectKing(square, move, newBoardArr, whiteKingPosition, blackKingPosition).isEmpty()) { //Se nenhuma peça conseguir proteger o rei
                    if(!kingHasValidMoves(move, newBoardArr, whiteKingPosition, blackKingPosition)) { //Ver depois se o rei tem movimentos validos e ver se continua em check
                        //Se nao tiver chequemate
                        return ISuccess(Board(this, newBoardArr), checkmate = true)
                    }
                    else if(kingHasValidMoves(move, newBoardArr, whiteKingPosition, blackKingPosition)) {
                        //Verificar se ao fazer esses moves ao rei nao continua em check
                        if(isKingStillInCheck(move,piece,newBoardArr,whiteKingPosition, blackKingPosition)) {
                            return ISuccess(Board(this, newBoardArr), checkmate = true)
                        }
                    }
                    // is in check
                    return ISuccess(Board(this, newBoardArr), check = true)
                }
                else {//Se alguma peça conseguir proteger o rei
                    if(canSomePieceEatPieceDoingCheck(move,piece,piecesThatCanEat,newBoardArr,whiteKingPosition,blackKingPosition) == piecesThatCanEat.size) { // Se o contador for igual ao número de peças que podem comer a peça que esta a pôr em check o rei
                        if(!kingHasValidMoves(move, newBoardArr, whiteKingPosition, blackKingPosition)) { // Nao tem movimentos validos
                            //É logo chequemate e retorna-se o board
                            return ISuccess(Board(this, newBoardArr), checkmate = true)
                        }
                    }
                    // is in check
                    return ISuccess(Board(this, newBoardArr), check = true)
                }
            }
            else {
                if(!kingHasValidMoves(move, newBoardArr, whiteKingPosition, blackKingPosition)) {
                    return ISuccess(Board(this, newBoardArr), checkmate = true)
                }
                // is in check
                return ISuccess(Board(this, newBoardArr), check = true)
            }
        }
        return ISuccess(Board(this, newBoardArr))
    }

    /**
     * @return if the given [move] is valid.
     */
    private fun isValidMove(move: Move): Boolean {
        if((move.newSquare.row == blackKingPosition.row && move.newSquare.column == blackKingPosition.column)
            || (move.newSquare.row == whiteKingPosition.row && move.newSquare.column == whiteKingPosition.column)) return false
        if(move.newSquare == whiteKingPosition) return false
        val pos = move.piece.getAllMoves(move, boardArr)
        if (pos.any {
                it.row == move.newSquare.row && it.column == move.newSquare.column
            }) return true
        return false
    }

    private fun updateKingAndRook(move:Move,piece: Piece,newBoardArr: Array<Array<Piece?>>) {
        if(piece.type is King) {
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = Piece(King(true),piece.player)
        }
        else if(piece.type is Rook) {
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = Piece(Rook(true),piece.player)
        }
    }

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

    /**
     * Checks if given square has a piece from [player], other player or hasnt a piece at all.
     */
    fun isFromPlayer(square: Square, player: Player): Boolean?  {
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece == null) return null
        return piece.player === player
    }

}