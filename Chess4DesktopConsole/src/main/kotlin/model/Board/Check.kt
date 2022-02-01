package model.Board

import chess.model.Square
import model.Player
import java.util.HashMap

 fun piecesToEatCheckPiece(movePos: MovePos, boardArr: Array<Array<Board.Piece?>>):MutableList<Square> {
    val piecesThatCanEat = mutableListOf<Square>()
    Square.values.forEach { square ->
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null) {
            val allMoves = piece.type.getAllMoves(MovePos(piece.type, square, square), boardArr)
            allMoves.forEach {
                    if(it.row == movePos.newSquare.row && it.column == movePos.newSquare.column)
                        piecesThatCanEat.add(square)
            }
        }
    }
    return piecesThatCanEat
}

 fun isMyKingInCheck(movePos: MovePos, boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square):Boolean {
    val currentPlayerColor = boardArr[movePos.newSquare.row.ordinal][movePos.newSquare.column.ordinal]!!.player
    Square.values.forEach { square ->
        val piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null) {
            val allMoves = piece.type.getAllMoves(MovePos(piece.type,square,square),boardArr)
            if (currentPlayerColor == Player.WHITE) {
                allMoves.forEach {
                    if(it.row == whiteKingPosition.row && it.column == whiteKingPosition.column) {
                        return true
                    }
                }
            }
            else
                allMoves.forEach {
                    if(it.row == blackKingPosition.row && it.column == blackKingPosition.column) {
                        return true
                    }
                }
        }
    }
    return false
}

 fun isAdversaryKingInCheck(movePos: MovePos, boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square): HashMap<Square, PieceType> {
    val ret : HashMap<Square, PieceType> = HashMap<Square, PieceType> ()
    //Obter a cor do player que está a por em check
    val currentPlayerColor = boardArr[movePos.newSquare.row.ordinal][movePos.newSquare.column.ordinal]!!.player
    //Iterar sobre todos os moves dessa peça para ver se me estou a mover para um sitio em que o rei está em check
    var piece: Board.Piece?
    Square.values.forEach { square ->
        piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null) {
            val allMoves = piece!!.type.getAllMoves(MovePos(piece!!.type,square,square),boardArr)
            if (currentPlayerColor == Player.WHITE) {
                allMoves.forEach {
                    if(it.row == blackKingPosition.row && it.column == blackKingPosition.column) {
                        ret[square] = piece!!.type
                    }
                }
            }
            else {
                allMoves.forEach {
                    if (it.row == whiteKingPosition.row && it.column == whiteKingPosition.column) {
                        ret[square] = piece!!.type
                    }
                }
            }
        }
    }
    return ret
}

 fun canAnyPieceProtectKing(squareCheck: HashMap<Square, PieceType>, movePos: MovePos, boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square):MutableList<Square> {
    val piecesToProtect = mutableListOf<Square>()
    val checkSquare = squareCheck.keys.first()
    val checkPieceType = squareCheck.values.first()
    val player =  boardArr[movePos.newSquare.row.ordinal][movePos.newSquare.column.ordinal]!!.player
    var piece: Board.Piece?
    val checkPieceAllMoves = checkPieceType.getAllMoves(MovePos(checkPieceType, checkSquare, checkSquare), boardArr)
    val list: MutableList<Square> = if(player == Player.WHITE) {
        getPath(blackKingPosition, checkSquare, checkPieceAllMoves, boardArr)
    } else getPath(whiteKingPosition, checkSquare, checkPieceAllMoves, boardArr)

    Square.values.forEach { square ->
        piece = boardArr[square.row.ordinal][square.column.ordinal]
        if (piece != null && piece!!.type !is King) {
            val currPlayer = piece!!.player
            val squareMovePos = MovePos(piece!!.type, square, square)
            val allMoves = piece!!.type.getAllMoves(squareMovePos, boardArr)
            if (player != currPlayer) {
                allMoves.forEach { square1 ->
                    //Trocar pelo caminho da peça a por em check até ao rei
                   list.forEach {
                       if(it.row == square1.row && it.column == square1.column)
                            piecesToProtect.add(square1)
                   }
                }
            } else if (player != currPlayer)
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

fun kingHasValidMoves(movePos: MovePos, boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square):Boolean {
    val whiteKing = boardArr[whiteKingPosition.row.ordinal][whiteKingPosition.column.ordinal]!!.type
    val blackKing = boardArr[blackKingPosition.row.ordinal][blackKingPosition.column.ordinal]!!.type
    val whiteKingMoves = whiteKing.getAllMoves(MovePos(whiteKing, whiteKingPosition, whiteKingPosition), boardArr)
    val blackKingMoves = blackKing.getAllMoves(MovePos(blackKing, blackKingPosition, blackKingPosition), boardArr)

    val player = boardArr[movePos.newSquare.row.ordinal][movePos.newSquare.column.ordinal]!!.player //Current player

    var count1 = 0
    var count2 = 0
    if (player == Player.WHITE) {
        blackKingMoves.forEach {square1 ->
            Square.values.forEach { square2 ->
                val piece = boardArr[square2.row.ordinal][square2.column.ordinal]
                if (piece != null && count1==count2 && piece.player == Player.WHITE ) {
                    val allMoves = piece.type.getAllMoves(MovePos(piece.type,square2,square2),boardArr)
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
                if (piece != null && count1==count2 && piece.player == Player.BLACK) {
                    val allMoves = piece.type.getAllMoves(MovePos(piece.type,square2,square2),boardArr)
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

fun isKingStillInCheck(movePos: MovePos, piece: Board.Piece, newBoardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square):Boolean {
    //Verificar se ao fazer esses moves ao rei nao continua em check
    var count1 = 0
    if(piece.player == Player.BLACK) {
        val kingPiece = newBoardArr[whiteKingPosition.row.ordinal][whiteKingPosition.column.ordinal]
        val allMoves = kingPiece!!.type.getAllMoves(MovePos(kingPiece.type, whiteKingPosition, whiteKingPosition), newBoardArr)
        allMoves.forEach { square1 ->
            newBoardArr[whiteKingPosition.row.ordinal][whiteKingPosition.column.ordinal] = null
            newBoardArr[square1.row.ordinal][square1.column.ordinal] = kingPiece
            val auxKingPos = Square(square1.column, square1.row)
            if(isMyKingInCheck(movePos,newBoardArr,auxKingPos,auxKingPos)) {
                count1++ //Checkmate
            }
            newBoardArr[square1.row.ordinal][square1.column.ordinal] = null
            newBoardArr[whiteKingPosition.row.ordinal][whiteKingPosition.column.ordinal] = kingPiece
            newBoardArr[movePos.newSquare.row.ordinal][movePos.newSquare.column.ordinal] = piece
        }
        if(count1 == allMoves.size) return true
        return false
    }
    else {
        val kingPiece = newBoardArr[blackKingPosition.row.ordinal][blackKingPosition.column.ordinal]
        val allMoves = kingPiece!!.type.getAllMoves(MovePos(piece.type, blackKingPosition, blackKingPosition), newBoardArr)
        allMoves.forEach { square1 ->
            newBoardArr[blackKingPosition.row.ordinal][blackKingPosition.column.ordinal] = null
            newBoardArr[square1.row.ordinal][square1.column.ordinal] = kingPiece
            val auxKingPos = Square(square1.column, square1.row)
            if(isMyKingInCheck(movePos,newBoardArr,auxKingPos,auxKingPos)) {
                count1++ //Checkmate
            }
            newBoardArr[square1.row.ordinal][square1.column.ordinal] = null
            newBoardArr[blackKingPosition.row.ordinal][blackKingPosition.column.ordinal] = kingPiece
            newBoardArr[movePos.newSquare.row.ordinal][movePos.newSquare.column.ordinal] = piece
        }
        if(count1 == allMoves.size) return true
        return false
    }

}

fun canSomePieceEatPieceDoingCheck(movePos: MovePos, piece: Board.Piece, piecesThatCanEat:MutableList<Square>, newBoardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square):Int {
    var counter = 0
    piecesThatCanEat.forEach { square1 -> //Iterar sobre as peças que podem comer a peça que está a pôr em check o rei
        val pieceToEat = newBoardArr[square1.row.ordinal][square1.column.ordinal]
        newBoardArr[movePos.newSquare.row.ordinal][movePos.newSquare.column.ordinal] = pieceToEat
        newBoardArr[square1.row.ordinal][square1.column.ordinal] = null
        if(isMyKingInCheck(movePos,newBoardArr,whiteKingPosition,blackKingPosition)) { //Se ao mover essa peça o rei continuar em check adiciona-se 1 ao contador
            counter++
        }
        newBoardArr[movePos.curSquare.row.ordinal][movePos.curSquare.column.ordinal] = null //Volta-se ao estado da board que se estava
        newBoardArr[movePos.newSquare.row.ordinal][movePos.newSquare.column.ordinal] = piece
        newBoardArr[square1.row.ordinal][square1.column.ordinal] = pieceToEat
    }
    return counter
}

