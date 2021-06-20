import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

val focusRequester = FocusRequester()

fun main() {
    val width = 48
    val height = 32

    val game = Game(width, height, GameComponent.foodProducer)

    Window(size = IntSize(1200, 800), resizable = false) {
        val map = game.map.collectAsState(emptyList())

        MaterialTheme {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()

                while (true) {
                    delay(120)
                    game.tick()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .focusable()
                    .focusRequester(focusRequester)
                    .focusModifier()
                    .onKeyEvent {
                        when (it.key) {
                            Key.W -> game.onDirectionChanged(Game.Direction.Up).let { true }
                            Key.S -> game.onDirectionChanged(Game.Direction.Down).let { true }
                            Key.A -> game.onDirectionChanged(Game.Direction.Left).let { true }
                            Key.D -> game.onDirectionChanged(Game.Direction.Right).let { true }
                            else -> false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                val chunked = map.value.chunked(width)
                LazyColumn {
                    items(chunked) { row ->
                        LazyRow {
                            items(row) { cell ->
                                when (cell) {
                                    is Game.Cell.SnakeBody -> {
                                        Cell(25.dp, Color.Green)
                                    }
                                    is Game.Cell.Food -> {
                                        Cell(25.dp, Color.Red)
                                    }
                                    else -> {
                                        Cell(25.dp, Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Cell(size: Dp, color: Color) {
    Box(
        Modifier.size(size).background(color = color)
    )
}
