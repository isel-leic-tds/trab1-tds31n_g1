package model.Board

import chess.model.Square
import model.Player
import java.util.HashMap

abstract class State
object NormalState: State()
class Check(val player: Player): State()
class Checkmate(val player: Player): State()

/**
 * Checks the current [board] state.
 */
fun isInCheckMate(boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square, currentPlayer: Player): Player? {
    val player = isKingInCheck(boardArr, whiteKingPosition, blackKingPosition) ?: return null
    val result = tryToMoveKing(if (player===Player.WHITE)whiteKingPosition else blackKingPosition, boardArr)

    //--------------------------------------------------------------------------------------------------------------

    if(isMyKingInCheck(boardArr, whiteKingPosition, blackKingPosition, currentPlayer))
        return Check(currentPlayer)
    val checkSquares = isAdversaryKingInCheck(boardArr, whiteKingPosition, blackKingPosition, currentPlayer).size
    // King is in check
    if(checkSquares > 0) {
        // if there's only one enemy piece threatening the King
        if(checkSquares == 1) {
            val square = isAdversaryKingInCheck(boardArr, whiteKingPosition, blackKingPosition, currentPlayer)
            // If there's no piece that can protect the King
            if (getSquaresThatProtectKing(square, boardArr, whiteKingPosition, blackKingPosition, currentPlayer).isEmpty()) {
                // If the King can't move
                if(!kingHasValidMoves(boardArr, whiteKingPosition, blackKingPosition, currentPlayer))
                    return Checkmate(currentPlayer.other())
                else {
                    //Verificar se ao fazer esses moves ao rei nao continua em check
                    if(isKingStillInCheck(boardArr,whiteKingPosition, blackKingPosition, currentPlayer)) {
                        return Checkmate(currentPlayer.other())
                    }
                }
                return Check(currentPlayer.other())
            }
            // Is some piece is able to protect the King
            else {
                val piecesThatCanEat = piecesToEatCheckPiece(move, boardArr)
                if(canSomePieceEatPieceDoingCheck(move,piece,piecesThatCanEat,boardArr,whiteKingPosition,blackKingPosition) == piecesThatCanEat.size) { // Se o contador for igual ao número de peças que podem comer a peça que esta a pôr em check o rei
                    if(!kingHasValidMoves(boardArr, whiteKingPosition, blackKingPosition, currentPlayer)) { // Nao tem movimentos validos
                        //É logo chequemate e retorna-se o board
                        return Checkmate(currentPlayer.other())
                    }
                }
                // is in check
                return Check(currentPlayer.other())
            }
        }
        // if king has more than one piece to threat him
        else {
            //  if the King can't move
            if(!kingHasValidMoves(boardArr, whiteKingPosition, blackKingPosition, currentPlayer)) {
                return Checkmate(currentPlayer.other())
            }
            // is in check
            return Check(currentPlayer.other())
        }
    }
    return NormalState
}

/**
 * Tries to move King out of danger.
 * @return true if it is possible to move him out of danger or false otherwise.
 */
fun tryToMoveKing(king: Square, boardArr: Array<Array<Board.Piece?>>): Boolean {
    val piece = boardArr[king.row.ordinal][king.column.ordinal]
    if (piece == null || piece.type !is King) return false
    val kingPlayer = piece.player
    val allKingMoves = piece.type.getAllMoves(Move(piece.type, king, king), boardArr)
    allKingMoves.forEach {
        if (kingPlayer === Player.WHITE)
            isKingInCheck(boardArr, king)
    }
}

fun piecesToEatCheckPiece(move: Move, boardArr: Array<Array<Board.Piece?>>):MutableList<Square> {
    val piecesThatCanEat = mutableListOf<Square>()
    Square.values.forEach { square ->
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null) {
            val allMoves = piece.type.getAllMoves(Move(piece.type, square, square), boardArr)
            allMoves.forEach {
                    if(it.row == move.newSquare.row && it.column == move.newSquare.column)
                        piecesThatCanEat.add(square)
            }
        }
    }
    return piecesThatCanEat
}

/**
 * Checks if one of the Players has its King in check.
 * @return the player that has its King in check or null neather King is in check.
 */
fun isKingInCheck(boardArr: Array<Array<Board.Piece?>>, kingSquare: Square): Boolean {
    val piece = boardArr[kingSquare.row.ordinal][kingSquare.column.ordinal]
    if (piece == null || piece.type !is King) return false
    Square.values.forEach { square ->
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null) {
            val allMoves = piece.type.getAllMoves(Move(piece.type,square,square), boardArr)
            allMoves.forEach {
                // if it can eat Black King
                if(it.row == kingSquare.row && it.column == kingSquare.column)
                    return true
            }
        }
    }
    return false
}

fun isMyKingInCheck(boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square, currentPlayer: Player): Boolean {
    Square.values.forEach { square ->
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null) {
            val allMoves = piece.type.getAllMoves(Move(piece.type,square,square), boardArr)
            if (currentPlayer == Player.WHITE)
                allMoves.forEach {
                    if(it.row == whiteKingPosition.row && it.column == whiteKingPosition.column)
                        return true
                }
            else
                allMoves.forEach {
                    if (it.row == blackKingPosition.row && it.column == blackKingPosition.column)
                        return true
                }
        }
    }
    return false
}

 fun isAdversaryKingInCheck(boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square, currentPlayer: Player): HashMap<Square, PieceType> {
    val ret : HashMap<Square, PieceType> = HashMap<Square, PieceType> ()
    //Obter a cor do player que está a por em check
    //Iterar sobre todos os moves dessa peça para ver se me estou a mover para um sitio em que o rei está em check
    var piece: Board.Piece?
    Square.values.forEach { square ->
        piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null) {
            val allMoves = piece!!.type.getAllMoves(Move(piece!!.type,square,square), boardArr)
            if (currentPlayer == Player.WHITE) {
                allMoves.forEach {
                    if(it.row == blackKingPosition.row && it.column == blackKingPosition.column)
                        ret[square] = piece!!.type
                }
            }
            else {
                allMoves.forEach {
                    if (it.row == whiteKingPosition.row && it.column == whiteKingPosition.column)
                        ret[square] = piece!!.type
                }
            }
        }
    }
    return ret
}

 fun getSquaresThatProtectKing(squareCheck: HashMap<Square, PieceType>, boardArr: Array<Array<Board.Piece?>>,
                               whiteKingPosition: Square, blackKingPosition: Square, currentPlayer: Player):MutableList<Square> {
    val piecesToProtect = mutableListOf<Square>()
    val checkSquare = squareCheck.keys.first()
    val checkPieceType = squareCheck.values.first()
    var piece: Board.Piece?
    val checkPieceAllMoves = checkPieceType.getAllMoves(Move(checkPieceType, checkSquare, checkSquare), boardArr)
    val list: MutableList<Square> = if(currentPlayer == Player.WHITE) {
        getPath(blackKingPosition, checkSquare, checkPieceAllMoves, boardArr)
    } else getPath(whiteKingPosition, checkSquare, checkPieceAllMoves, boardArr)

    Square.values.forEach { square ->
        piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null && piece!!.type !is King) {
            val currPlayer = piece!!.player
            val squareMove = Move(piece!!.type, square, square)
            val allMoves = piece!!.type.getAllMoves(squareMove, boardArr)
            if (currPlayer != currPlayer) {
                allMoves.forEach { square1 ->
                    //Trocar pelo caminho da peça a por em check até ao rei
                   list.forEach {
                       if(it.row == square1.row && it.column == square1.column)
                            piecesToProtect.add(square1)
                   }
                }
            } else if (currPlayer != currPlayer)
                allMoves.forEach { square1 ->
                    list.forEach {
                        if(it.row == square1.row && it.column == square1.column)
                            piecesToProtect.add(square1)
                    }
                }
        }
    }
    return piecesToProtect
}

 fun getPath(kingSquare: Square, checkSquare: Square, checkPieceAllMoves:List<Square>, boardArr: Array<Array<Board.Piece?>>):MutableList<Square> {
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

fun kingHasValidMoves(boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square, currentPlayer: Player):Boolean {
    val whiteKing = boardArr[whiteKingPosition.row.ordinal][whiteKingPosition.column.ordinal]!!.type
    val blackKing = boardArr[blackKingPosition.row.ordinal][blackKingPosition.column.ordinal]!!.type
    val whiteKingMoves = whiteKing.getAllMoves(Move(whiteKing, whiteKingPosition, whiteKingPosition), boardArr)
    val blackKingMoves = blackKing.getAllMoves(Move(blackKing, blackKingPosition, blackKingPosition), boardArr)

    var count1 = 0
    var count2 = 0
    if (currentPlayer == Player.WHITE) {
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
    if (currentPlayer == Player.BLACK) {
        whiteKingMoves.forEach {square1 ->
            Square.values.forEach { square2 ->
                val piece = boardArr[square2.row.ordinal][square2.column.ordinal]
                if (piece != null && count1==count2 && piece.player == Player.BLACK) {
                    val allMoves = piece.type.getAllMoves(Move(piece.type,square2,square2),boardArr)
                    if(allMoves.any{
                            it.row == square1.row && it.column == square1.column
                        }) count1++
                }

            }
            count2++
        }
    }
    if(count1==count2) return false // Nao tem movimentos válidos
    return true
}

/**
 * Makes all possible moves with a King to check if is still in check.
 */
fun isKingStillInCheck(newBoardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square, currentPlayer: Player): Boolean {
    var count1 = 0
    if(currentPlayer == Player.BLACK) {
        val kingPiece = newBoardArr[whiteKingPosition.row.ordinal][whiteKingPosition.column.ordinal]
        val allMoves = kingPiece!!.type.getAllMoves(Move(kingPiece.type, whiteKingPosition, whiteKingPosition), newBoardArr)
        allMoves.forEach { square1 ->
            newBoardArr[whiteKingPosition.row.ordinal][whiteKingPosition.column.ordinal] = null
            newBoardArr[square1.row.ordinal][square1.column.ordinal] = kingPiece
            val auxKingPos = Square(square1.column, square1.row)
            if(isMyKingInCheck(newBoardArr, auxKingPos, auxKingPos, currentPlayer))
                count1++ //Checkmate
            newBoardArr[square1.row.ordinal][square1.column.ordinal] = null
            newBoardArr[whiteKingPosition.row.ordinal][whiteKingPosition.column.ordinal] = kingPiece
        }
        if(count1 == allMoves.size) return true
        return false
    }
    else {
        val kingPiece = newBoardArr[blackKingPosition.row.ordinal][blackKingPosition.column.ordinal]
        val allMoves = kingPiece!!.type.getAllMoves(Move(kingPiece.type, blackKingPosition, blackKingPosition), newBoardArr)
        allMoves.forEach { square1 ->
            newBoardArr[blackKingPosition.row.ordinal][blackKingPosition.column.ordinal] = null
            newBoardArr[square1.row.ordinal][square1.column.ordinal] = kingPiece
            val auxKingPos = Square(square1.column, square1.row)
            if(isMyKingInCheck(newBoardArr, auxKingPos, auxKingPos, currentPlayer))
                count1++ //Checkmate
            newBoardArr[square1.row.ordinal][square1.column.ordinal] = null
            newBoardArr[blackKingPosition.row.ordinal][blackKingPosition.column.ordinal] = kingPiece
        }
        if(count1 == allMoves.size) return true
        return false
    }

}

fun canSomePieceEatPieceDoingCheck(move: Move,piece: Board.Piece,piecesThatCanEat:MutableList<Square>,newBoardArr: Array<Array<Board.Piece?>>,whiteKingPosition: Square,blackKingPosition: Square):Int {
    var counter = 0
    piecesThatCanEat.forEach { square1 -> //Iterar sobre as peças que podem comer a peça que está a pôr em check o rei
        val pieceToEat = newBoardArr[square1.row.ordinal][square1.column.ordinal]
        newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = pieceToEat
        newBoardArr[square1.row.ordinal][square1.column.ordinal] = null
        if(isMyKingInCheck(move,newBoardArr,whiteKingPosition,blackKingPosition)) { //Se ao mover essa peça o rei continuar em check adiciona-se 1 ao contador
            counter++
        }
        newBoardArr[move.curSquare.row.ordinal][move.curSquare.column.ordinal] = null //Volta-se ao estado da board que se estava
        newBoardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal] = piece
        newBoardArr[square1.row.ordinal][square1.column.ordinal] = pieceToEat
    }
    return counter
}

