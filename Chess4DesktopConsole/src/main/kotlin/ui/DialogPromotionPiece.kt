package ui

import Chess
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import model.Board.*

/**
 * Dialog window to read the name of the game entered by the user.
 * @param onOk Function called when OK button is pressed. Its parameter is the edited name.
 * @param onCancel Function called when the user tries to close the window.
 */
@Composable
fun DialogPromotionPiece( onOk: (PieceType)->Unit, onCancel: ()->Unit ) = Dialog(
    onCloseRequest = onCancel,
    title = "Game name",
    state = DialogState(width = Dp.Unspecified, height = Dp.Unspecified)
) {
    var name by remember { mutableStateOf("") }
    Column {
        Text("Select promotion piece")
        Row {
            Button(onClick = { onOk(King()) }) {
                Image(painterResource("kingB.png"), "king", Modifier)
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