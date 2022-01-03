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

enum class MoveType{ REGULAR, CAPTURE, PROMOTION, CASTLING, EN_PASSANT }

data class Move(val piece: PieceType, val curSquare: Square, val newSquare: Square, val type: MoveType = MoveType.REGULAR) {
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
    fun makeMoveWithCorrectString(move: String): Board {
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
    fun makeMove(str: String, curPlayer: Player = Player.WHITE): Result {
        if (finished) return Finished()
        // checks if the [str] is valid
        var result = toMoveOrNull(str, curPlayer)
        if (result is Error) return result
        result = result as Aux
        val move = result.content as Move
        // checks if the given Square is valid
        result = isValidSquare(move, curPlayer)
        if (result is Error) return result
        // tries to make the Move
        val newBoard = makeMove(move) ?: return InvalidMove(move.toString())
        return Success(newBoard, move.toString())
    }

    private class Aux(val content: Any): Result()
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
            if (result is Aux)
                currSquare = result.content as Square
        }
        else {
            currSquare = cmd.substring(1, 3).toSquareOrNull()
            newSquare = cmd.substring(3, 5).toSquareOrNull()
        }
        val moveType = if (str[3] == 'x') MoveType.CAPTURE else MoveType.REGULAR
        if (currSquare == null || newSquare == null || pieceType == null) return BadMove()
        return Aux(Move(pieceType, currSquare, newSquare, moveType))
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
        return Aux(currentSquare!!)
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
        return Aux(true)
    }

    /**
     * Checks if the given [move] is valid and if so, makes the [move].
     * @returns the new Board if the [move] was valid or null.
     */
    private fun makeMove(move: Move): Board? {
        if (!isValidMove(move)) return null
        val piece = boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal]
        val newBoardArr = boardArr.clone()
        boardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null
        newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece
        if(isMyKingInCheck(move)) {
            newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = piece
            newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = null
            return null
        }
        val checkSquares = isAdversaryKingInCheck(move).size
        val piecesThatCanEat = piecesToEatCheckPiece(move)
        var counter = 0
        if(checkSquares > 0) {
            if(checkSquares == 1) {
                val square = isAdversaryKingInCheck(move) //Número de peças que metem o rei em check
                if (canAnyPieceProtectKing(square, move).isEmpty()) { //Se nenhuma peça conseguir proteger o rei
                    if(!kingHasValidMoves(move)) { //Ver depois se o rei tem movimentos validos
                        println("CHECKMATE") //Se nao tiver chequemate
                        return Board(this, newBoardArr)
                    }
                }
                else {//Se alguma peça conseguir proteger o rei
                    piecesThatCanEat.forEach { square1 -> //Iterar sobre as peças que podem comer a peça que está a pôr em check o rei
                        val pieceToEat = newBoardArr[square1.row.ordinal][square1.column.ordinal]
                        newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = pieceToEat
                        newBoardArr[square1.row.ordinal][square1.column.ordinal] = null
                        if(isAdversaryKingInCheck(move).size > 0) { //Se ao mover essa peça o rei continuar em check adiciona-se 1 ao contador
                            counter++
                        }
                        newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null //Volta-se ao estado da board que se estava
                        newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece
                        newBoardArr[square1.row.ordinal][square1.column.ordinal] = pieceToEat
                    }
                    if(counter == piecesThatCanEat.size) { // Se o contador for igual ao número de peças que podem comer a peça que esta a pôr em check o rei
                        println("CHECKMATE") //É logo chequemate e retorna-se o board
                        return Board(this, newBoardArr)
                    }
                }
            }
            else if(checkSquares > 1) {
                if(!kingHasValidMoves(move))
                    println("CHECKMATE")
                    return Board(this, newBoardArr)
            }
        }
        return Board(this, newBoardArr)
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

    /*TODO:
    Verificar primeiro se ao fazer a minha jogada o meu rei fica em check -> isMyKingInCheck() DONE
    Se ficar, dizer que o movimento não é válido -> isMyKingInCheck() DONE
    Se não ficar, verificar se o rei adversário está em check -> isAdversaryKingInCheck() DONE
    Se estiver em check ativar a mensagem a dizer CHECK -> isAdversaryKingInCheck() DONE
    Depois ver quantas peças estão a meter o rei em check -> isAdversaryKingInCheck() DONE
    Se nao houver nenhuma nao ha check -> isAdversaryKingInCheck() DONE
    Se houver 1, primeiro ver se existe alguma peça que se possa sacrificar pelo rei ou comer a peça a por em check -> canAnyPieceProtectKing() DONE
    Se nao existir nenhuma que se possa sacrificar pelo rei vemos se o rei tem para onde ir sem ficar em check -> canAnyPieceProtectKing() DONE
    Se houver 1+,nenhuma peça se pode sacrificar por isso apenas ver se o rei pode fugir -> kingHasValidMoves() DONE
    Se nao conseguir fugir aparece a mensagem de CHECKMATE e termina o jogo -> CHECKMATE

    VERIFICAR SE AO COMER A PEÇA QUE ESTA A POR EM CHEQUEMATE O REI NAO CONTINUA EM CHEQUE
    */

    private fun piecesToEatCheckPiece(move: Move):MutableList<Square> {
        val piecesThatCanEat = mutableListOf<Square>()
        Square.values.forEach { square ->
            val piece = boardArr[square.row.ordinal][square.column.ordinal]
            if (piece != null) {
                val allMoves = piece.type.getAllMoves(Move(piece.type, square, square), boardArr)
                if (allMoves.any {
                        it.row == move.newSquare.row && it.column == move.newSquare.column
                    }) piecesThatCanEat.add(square)
            }
        }
        return piecesThatCanEat
    }

    private fun isMyKingInCheck(move: Move):Boolean {
        val currentPlayerColor = boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal]!!.player
        Square.values.forEach { square ->
            val piece = boardArr[square.row.ordinal][square.column.ordinal]
            if (piece != null) {
                val allMoves = piece.type.getAllMoves(Move(piece.type,square,square),boardArr)
                if (currentPlayerColor == Player.WHITE) {
                    if (allMoves.any {
                            it.row == whiteKingPosition.row && it.column == whiteKingPosition.column
                        }) return true
                }
                else
                    if (allMoves.any {
                            it.row == blackKingPosition.row && it.column == blackKingPosition.column
                        }) return true
            }
        }
        return false
    }

    private fun isAdversaryKingInCheck(move: Move):HashMap<Square, PieceType>  {
        val ret : HashMap<Square, PieceType> = HashMap<Square, PieceType> ()
        //Obter a cor do player que está a por em check
        val currentPlayerColor = boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal]!!.player
        //Iterar sobre todos os moves dessa peça para ver se me estou a mover para um sitio em que o rei está em check
        var piece:Piece?
        Square.values.forEach { square ->
            piece = boardArr[square.row.ordinal][square.column.ordinal]
            if (piece != null) {
                val allMoves = piece!!.type.getAllMoves(Move(piece!!.type,square,square),boardArr)
                if (currentPlayerColor == Player.WHITE) {
                    if (allMoves.any {
                            it.row == blackKingPosition.row && it.column == blackKingPosition.column
                        }) ret[square] = piece!!.type
                }
                else
                    if (allMoves.any {
                            it.row == whiteKingPosition.row && it.column == whiteKingPosition.column
                        }) ret[square] = piece!!.type
            }
        }
        return ret
    }

    private fun canAnyPieceProtectKing(squareCheck: HashMap<Square, PieceType>,move: Move):MutableList<Square> {
        val piecesToProtect = mutableListOf<Square>()
        val checkSquare = squareCheck.keys.first()
        val checkPieceType = squareCheck.values.first()
        val player =  boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal]!!.player
        var piece:Piece?
        val checkPieceAllMoves = checkPieceType.getAllMoves(Move(checkPieceType, checkSquare, checkSquare), boardArr)
        val list: MutableList<Square> = if(player == Player.WHITE) {
            getPath(blackKingPosition, checkSquare, checkPieceAllMoves)
        } else getPath(whiteKingPosition, checkSquare, checkPieceAllMoves)

        Square.values.forEach { square ->
            piece = boardArr[square.row.ordinal][square.column.ordinal]
            if (piece != null && piece!!.type !is King) {
                val currPlayer = piece!!.player
                val squareMove = Move(piece!!.type, square, square)
                val allMoves = piece!!.type.getAllMoves(squareMove, boardArr)
                if (player != currPlayer) {
                    allMoves.forEach { square1 ->
                        //Trocar pelo caminho da peça a por em check até ao rei
                        if (list.any {
                                it.row == square1.row && it.column == square1.column
                            }) piecesToProtect.add(square1)
                    }
                } else if (player != currPlayer)
                    allMoves.forEach { square1 ->
                        if (list.any {
                                it.row == square1.row && it.column == square1.column
                            }) piecesToProtect.add(square1)
                    }
            }
        }
        return piecesToProtect
    }

    private fun getPath(kingSquare:Square, checkSquare: Square, checkPieceAllMoves:List<Square>):MutableList<Square> {
        val list = mutableListOf<Square>()
        val rowDiffCheck = checkSquare.row.ordinal - kingSquare.row.ordinal
        val colDiffCheck = checkSquare.column.ordinal - kingSquare.column.ordinal
        val pieceType = boardArr[checkSquare.row.ordinal][checkSquare.column.ordinal]!!.type
        if(pieceType is Knight) list.add(checkSquare)
        //Adicionar o currSquare para ver se alguma peça adversária pode comer a peça a por o rei em check
        list.add(checkSquare)
        checkPieceAllMoves.forEach { square->
            val rowDiff = square.row.ordinal - kingSquare.row.ordinal
            val colDiff = square.column.ordinal - kingSquare.column.ordinal
            if((rowDiff in 1 until rowDiffCheck) && (colDiff in (colDiffCheck + 1)..-1)) { //Move UP_RIGHT
                list.add(square)
            }
            else if((rowDiff in (rowDiffCheck + 1)..-1) && (colDiff in (colDiffCheck + 1)..-1)) { //Move DOWN_RIGHT
                list.add(square)
            }
            else if((rowDiff in 1 until rowDiffCheck) && (colDiff in 1 until colDiffCheck)) { //Move UP_LEFT
                list.add(square)
            }
            else if((rowDiff in (rowDiffCheck + 1)..-1) && (colDiff in 1 until colDiffCheck)) { //Move DOWN_LEFT
                list.add(square)
            }
            else if((rowDiff in 1 until rowDiffCheck) && square.column.ordinal == kingSquare.column.ordinal) { //Move UP
                list.add(square)
            }
            else if((rowDiff in (rowDiffCheck + 1)..-1) && square.column.ordinal == kingSquare.column.ordinal) { //Move DOWN
                list.add(square)
            }
            else if(square.row.ordinal == kingSquare.row.ordinal && (colDiff in (colDiffCheck + 1)..-1)) { //Move RIGHT
                list.add(square)
            }
            else if(square.row.ordinal == kingSquare.row.ordinal && (colDiff in 1 until colDiffCheck)) { //Move LEFT
                list.add(square)
            }
        }
        return list
    }

    private fun kingHasValidMoves(move: Move):Boolean {
        val whiteKing = boardArr[whiteKingPosition.row.ordinal][whiteKingPosition.column.ordinal]!!.type
        val blackKing = boardArr[blackKingPosition.row.ordinal][blackKingPosition.column.ordinal]!!.type
        val whiteKingMoves = whiteKing.getAllMoves(Move(whiteKing, whiteKingPosition, whiteKingPosition), boardArr)
        val blackKingMoves = blackKing.getAllMoves(Move(blackKing, blackKingPosition, blackKingPosition), boardArr)

        val player = boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal]!!.player //Current player

        var count1 = 0
        var count2 = 0
        if (player == Player.WHITE) {
            blackKingMoves.forEach {square1 ->
                Square.values.forEach { square2 ->
                    val piece = boardArr[square2.row.ordinal][square2.column.ordinal]
                    if (piece != null && count1==count2 && piece.player == Player.WHITE ) {
                        val allMoves = piece.type.getAllMoves(Move(piece.type,square2,square2),boardArr)
                        if(allMoves.any{
                            it.row == square1.row && it.column == square1.column
                            }) count1++
                    }

                }
                count2++
            }
        }
        if (player == Player.BLACK) {
            whiteKingMoves.forEach {square1 ->
                Square.values.forEach { square2 ->
                    val piece = boardArr[square2.row.ordinal][square2.column.ordinal]
                    if (piece != null && count1==count2 && piece.player == Player.WHITE) {
                        val allMoves = piece.type.getAllMoves(Move(piece.type,square2,square2),boardArr)
                        if(allMoves.any{
                                it.row == square1.row && it.column == square1.column
                            }) count1++
                    }

                }
                count2++
            }
        }
        if(count1==count2) return false
        return true
    }
}