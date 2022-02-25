package chess.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.*

/**
 * MunuBar of the game.
 * @param onNew Called when the item Game>New is selected.
 * @param onExit Called when the item Game>Exist is selected.
 */
@Composable
fun FrameWindowScope.ChessMenuBar(onOpen: ()->Unit, onJoin: ()->Unit, onTargets: ()->Unit, onSinglePlayer: ()->Unit ) =  MenuBar {
    Menu("Game",'G') {
        Item("Open", onClick = onOpen)
        Item("Join", onClick = onJoin)
    }
    Menu("Options", 'O') {
        Item("Show Targets", onClick = onTargets)
        Item("Single Player", onClick = onSinglePlayer)
    }
}

