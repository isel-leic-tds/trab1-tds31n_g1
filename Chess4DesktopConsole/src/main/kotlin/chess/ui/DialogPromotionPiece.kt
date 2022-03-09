package chess.ui

import androidx.compose.foundation.Image
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import chess.model.board.*

/**
 * Dialog window to read the name of the game entered by the user.
 * @param onOk Function called when OK button is pressed. Its parameter is the edited name.
 * @param onCancel Function called when the user tries to close the window.
 */
@Composable
fun DialogPromotionPiece( onOk: (PieceType)->Unit) = Dialog(
    onCloseRequest = {  },
    title = "Game name",
    state = DialogState(width = Dp.Unspecified, height = Dp.Unspecified)
) {
    Column {
        Text("Select promotion piece")
        Row {
            Button(onClick = { onOk(Queen()) }) {
                Image(painterResource("queenB.png"), "queen", Modifier)
            }
            Button(onClick = { onOk(Rook()) }) {
                Image(painterResource("rookB.png"), "rook", Modifier)
            }
            Button(onClick = { onOk(Bishop()) }) {
                Image(painterResource("bishopB.png"), "bishop", Modifier)
            }
            Button(onClick = { onOk(Knight()) }) {
                Image(painterResource("knightB.png"), "knight", Modifier)
            }
        }
    }
}