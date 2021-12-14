package ui

import androidx.compose.desktop.DesktopMaterialTheme
import androidx.compose.desktop.Window
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
fun FrameWindowScope.ChessMenuBar(onOpen: ()->Unit, onExit: ()->Unit ) =  MenuBar {
    Menu("Game",'G') {
        Item("Open", onClick = onOpen )
        Item("Exit", onClick = onExit )
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
