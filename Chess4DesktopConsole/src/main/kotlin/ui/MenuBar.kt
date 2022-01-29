package ui

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.*

/**
 * MunuBar of the game.
 * @param onNew Called when the item Game>New is selected.
 * @param onExit Called when the item Game>Exist is selected.
 */
@Composable
fun FrameWindowScope.ChessMenuBar(onOpen: ()->Unit, onJoin: ()->Unit ) =  MenuBar {
    Menu("Game",'G') {
        Item("Open", onClick = onOpen /*{TODO open new window to receive name of the game}*/ )
        Item("Join", onClick = onJoin /*{TODO open new window to receive name of the game}*/ )
        Item("Options", onClick = {/*TODO */} )
    }
}

