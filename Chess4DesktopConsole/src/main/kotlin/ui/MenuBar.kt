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
        Item("Join", onClick = {/*TODO open new window to receive name of the game*/} )
        Item("Options", onClick = {/*TODO */} )
    }
}

@Composable
fun ApplicationScope.myWindow() {
    val winState = WindowState(width = Dp.Unspecified, height = Dp.Unspecified)
    androidx.compose.ui.window.Window(
        onCloseRequest = ::exitApplication,
        state = winState,
        title = "Jogo do Galo"
    ) {}
}
