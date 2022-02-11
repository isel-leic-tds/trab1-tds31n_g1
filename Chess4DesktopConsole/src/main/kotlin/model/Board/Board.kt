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

class MoveType(val capture: Boolean = false, val special: SpecialMove?)
abstract class SpecialMove()
class Promotion(val newPiece: PieceType): SpecialMove()
class Castling(): SpecialMove()
class EnPassant(): SpecialMove()

data class Move(val piece: PieceType, val curSquare: Square, val newSquare: Square, val moveType: MoveType) {
    override fun toString(): String {
        if (this.moveType is Promotion)
            return piece.toStr() + curSquare.column.letter + curSquare.row.digit + newSquare.column.letter + newSquare.row.digit + '=' + this.moveType.newPiece.toStr()
        return piece.toStr() + curSquare.column.letter + curSquare.row.digit + newSquare.column.letter + newSquare.row.digit
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
        var newBoard = makeMoveInternal(move)
        if (newBoard == null)
            return InvalidMove(move.toString())
        if (move.moveType is Promotion)
            newBoard = makePromotion(newBoard, move.newSquare, move.moveType.newPiece) ?: newBoard
        return Success(newBoard)
    }

    /**
     * Checks if given [move] has a type and if not, returns a new move with correct type.
     */
    private fun getMoveWithType(move: Move): Move? {
        if (!isValidMove(move)) return null
        // TODO -> porbably should check move type if its wrong
        if (move.moveType != null) return move
        val newPos = boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal]
        val capture = newPos?.player != null
        if (newPos?.player != null)
            return move.copy(moveType = MoveType(capture, null))
        /*if (isPromotionPossible(move.newSquare))
            return move.copy(moveType = MoveType(capture, Promotion))*/
        if (canCastle(move))
            return move.copy(moveType = MoveType(capture, Castling()))
        if (canEnPassant(move))
            return move.copy(moveType = MoveType(capture, EnPassant()))
        // special move not possible with given [move].
        return move.copy(moveType = MoveType(capture, null))
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
        if (makeCastling(move1, piece, newBoardArr))  {
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

    /*************************************************CASTLE******************************************************************/

    /**
     * Checks if it is possible to make a castling move.
     */
    private fun canCastle(move: Move): Boolean {
        // cheks move piece
        if (move.piece !is King) return false
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]
        // checks piece in the boardArray
        if (piece == null || piece.type !is King || (piece as King).hasMoved) return false
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
        return if (diffCol == 2) Direction.RIGHT else Direction.LEFT
    }

    /**
     * Tries to make a castling move if possible.
     * @return new Board after making castle move or null if not possible.
     */
    private fun makeCastling(move:Move): Board? {
        val direction = getCastleDirection(move) ?: return null
        val newBoardArr = boardArr.clone()
        return if (direction === Direction.LEFT) { //Short Path
            val towerPiece = newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal+1]
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal + 1] = null
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal - 1] = towerPiece
            val newBoard = Board(this, newBoardArr)
            makeRegularMove(move, newBoard)
        } else { //Long Path
            val towerPiece = newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal-2]
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal - 2] = null
            newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal - 1] = towerPiece
            val newBoard = Board(this, newBoardArr)
            makeRegularMove(move, newBoard)
        }
    }

    /************************************************************************************************************************/

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
        return if (diffCol == -1) Direction.LEFT else Direction.LEFT
    }

    /**
     * Tries to make enPassant move if possible.
     * @return new Board after making enPassant or null if not possible.
     */
    private fun makeEnPassant(move: Move): Board? {
        val direction = getEnPassantDirection(move) ?: return null
        val newBoardArr = boardArr.clone()
        val piece = newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]
        if (direction === Direction.LEFT)
            newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal - 1] = null
        else
            newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal + 1] = null
        // temporary board!
        val newBoard = Board(this, newBoardArr)
        return makeRegularMove(move, newBoard)
    }

    /**********************************************EN_PASSANT*******************************************************************/

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
            val specialMove = result.content as SpecialMove
            if (currSquare == null || newSquare == null || pieceType == null) return BadMove()
            return ISuccess(Move(pieceType, currSquare, newSquare, specialMove))
        }
        return result // returns the error messaage
    }

    /**
     * Given two squares, [pos1] and [pos2], returns a Move objetc.
     */
    fun toMoveOrNull(pos1: Square, pos2: Square): Move? {
        val piece = boardArr[pos1.row.ordinal][pos1.column.ordinal] ?: return null
        val pieceType = piece.type
        return Move(pieceType, pos1, pos2)
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
    private fun isValidSquare(move: Move): Result {
        // verifies if there's a piece in currentSquare
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] ?: return EmptySquare()
        // verifies if the piece type of the choosen square is the one in the move parameter
        if (piece.type.toStr() != move.piece.toStr()) return BadPiece()
        if (piece.player != currentPlayer) return OpponentSquare()
        return ISuccess(true)
    }

    /**
     * Makes a regular move. In other words, only changes the piece position on the board,
     * based on the curSquare and the newSquare, not checking the move type.
     */
    private fun makeRegularMove(move: Move, board: Board = this): Board? {
        if (!isValidMove(move)) return null
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]!!
        val newBoardArr = boardArr.clone()
        newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null
        newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece
        return Board(this, newBoardArr)
    }

    /**
     * Checks if the given [move] is valid and if so, makes the move.
     * Also, tries to find the move type so it can make the correct move.
     * @returns the new Board if the [move] was valid or null.
     */
    private fun makeMoveInternal(move: Move): Board? {
        if (!isValidMove(move)) return null
        val move = getMoveWithType(move) ?: return null
        val specialMove = move.moveType.special
        val newBoard =
            if (specialMove == null)
                makeRegularMove(move)
            else {
                when (specialMove) {
                    is Promotion -> makePromotion(move.newSquare, specialMove.newPiece)
                    is Castling -> makeCastling(move)
                    else -> makeEnPassant(move)
                }
            }
        return newBoard

        /*if(doCastling(move,piece,newBoardArr)) return checkAndCheckmate(move,newBoardArr,piece)
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
         */
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

    fun toPromotionMoveOrNull(move: Move, pieceType: PieceType): Move? {
        return if (needsPromotion(move))
            move.copy(moveType = Promotion(pieceType))
        else null
    }

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

    private fun makePromotion(square: Square, newPiece: PieceType): Board? {
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