package model.Board

import chess.model.Square
import model.Player
import java.util.HashMap

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

 fun piecesToEatCheckPiece(move: Move, boardArr: Array<Array<Board.Piece?>>):MutableList<Square> {
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

 fun isMyKingInCheck(move: Move, boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square):Boolean {
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

 fun isAdversaryKingInCheck(move: Move, boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square): HashMap<Square, PieceType> {
    val ret : HashMap<Square, PieceType> = HashMap<Square, PieceType> ()
    //Obter a cor do player que está a por em check
    val currentPlayerColor = boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal]!!.player
    //Iterar sobre todos os moves dessa peça para ver se me estou a mover para um sitio em que o rei está em check
    var piece: Board.Piece?
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

 fun canAnyPieceProtectKing(squareCheck: HashMap<Square, PieceType>, move: Move, boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square):MutableList<Square> {
    val piecesToProtect = mutableListOf<Square>()
    val checkSquare = squareCheck.keys.first()
    val checkPieceType = squareCheck.values.first()
    val player =  boardArr[move.newSquare.row.ordinal][move.newSquare.column.ordinal]!!.player
    var piece: Board.Piece?
    val checkPieceAllMoves = checkPieceType.getAllMoves(Move(checkPieceType, checkSquare, checkSquare), boardArr)
    val list: MutableList<Square> = if(player == Player.WHITE) {
        getPath(blackKingPosition, checkSquare, checkPieceAllMoves, boardArr)
    } else getPath(whiteKingPosition, checkSquare, checkPieceAllMoves, boardArr)

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

 fun kingHasValidMoves(move: Move, boardArr: Array<Array<Board.Piece?>>, whiteKingPosition: Square, blackKingPosition: Square):Boolean {
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