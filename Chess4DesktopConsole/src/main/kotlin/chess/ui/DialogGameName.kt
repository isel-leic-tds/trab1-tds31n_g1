package chess.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState

/**
 * Dialog window to read the name of the game entered by the user.
 * @param onOk Function called when OK button is pressed and game name is not empty. Its parameter is the edited name.
 * @param onCancel Function called when the user tries to close the window.
 */
@Composable
fun DialogGameName( onOk: (String)->Unit, onCancel: ()->Unit ) = Dialog(
    onCloseRequest = onCancel,
    title = "Game name",
    state = DialogState(width = Dp.Unspecified, height = Dp.Unspecified)
) {
    var name by remember { mutableStateOf("") }
    Column {
        Text("Enter the name of game")
        Row {
            TextField(name, onValueChange = { name = it })
            Button(onClick = { if (name.isNotBlank()) onOk(name) }) {
                Text("OK")
            }
        }
    }
}