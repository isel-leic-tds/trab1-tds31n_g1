package ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar

/**
 * MunuBar of the game.
 * @param onNew Called when the item Game>New is selected.
 * @param onExit Called when the item Game>Exist is selected.
 */
@Composable
fun FrameWindowScope.GaloMenuBar(onNew: ()->Unit, onExit: ()->Unit ) =  MenuBar {
    Menu("Game",'G') {
        Item("New", onClick =  onNew )
        Item("Exit", onClick = onExit )
    }
}