
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import kotlinx.coroutines.flow.map

val focusRequester = FocusRequester()

fun main() {
    val width = 48
    val height = 20

    val game = Game(width, height, GameComponent.foodProducer)


    Window(size = IntSize(1400, 800), resizable = false) {
        val map = game.map.collectAsState(emptyList())
        val snakeDead = game.snakeDead.collectAsState(false)
        val gameSpeed = game.gameSpeed.map {
            when (it) {
                Game.GameSpeed.Normal -> 120L
                Game.GameSpeed.Faster -> 60L
                Game.GameSpeed.Slower -> 180L
            }
        }.collectAsState(120L)

        MaterialTheme {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()

                while (true) {
                    delay(gameSpeed.value)
                    game.tick()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.DarkGray)
                    .focusable()
                    .focusRequester(focusRequester)
                    .focusModifier()
                    .onKeyEvent {
                        when (it.key) {
                            Key.DirectionUp,
                            Key.W -> game.onDirectionChanged(Game.Direction.Up).let { true }
                            Key.DirectionDown,
                            Key.S -> game.onDirectionChanged(Game.Direction.Down).let { true }
                            Key.DirectionLeft,
                            Key.A -> game.onDirectionChanged(Game.Direction.Left).let { true }
                            Key.DirectionRight,
                            Key.D -> game.onDirectionChanged(Game.Direction.Right).let { true }
                            Key.Spacebar -> if (snakeDead.value) {
                                game.onRestartGame()
                                true
                            } else {
                                false
                            }
                            else -> false
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                val chunked = map.value.chunked(width)
                LazyColumn {
                    items(chunked.size) { rowIndex ->
                        val row = chunked[rowIndex]
                        LazyRow {
                            items(row.size) { cellIndex ->
                                when (val cell = row[cellIndex]) {
                                    is Game.Cell.Snake.Head -> {
                                        Cell(25.dp, GameColors.headColor)
                                    }
                                    is Game.Cell.Snake.Body -> {
                                        Cell(25.dp, GameColors.bodyColor)
                                    }
                                    is Game.Cell.Food -> {
                                        val color = when (cell.type) {
                                            Game.FoodType.Normal -> GameColors.normalFoodColor
                                            Game.FoodType.Accelerate -> GameColors.fasterFoodColor
                                            Game.FoodType.Decelerate -> GameColors.slowerFoodColor
                                            Game.FoodType.Reverse -> GameColors.reverseFoodColor
                                            Game.FoodType.GoThroughWalls -> GameColors.goThroughWallsColor
                                        }

                                        Cell(25.dp, color)
                                    }
                                    else -> {
                                        val color = when {
                                            rowIndex.isEven() -> {
                                                if (cellIndex.isEven()) GameColors.cellColor0 else GameColors.cellColor1
                                            }
                                            else -> {
                                                if (cellIndex.isEven()) GameColors.cellColor1 else GameColors.cellColor0
                                            }
                                        }

                                        Cell(
                                            25.dp,
                                            color
                                        )
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
