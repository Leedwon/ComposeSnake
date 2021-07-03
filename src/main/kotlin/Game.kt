import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

// TODO: 26/06/2021 test gameSpeed implement and test reverse behaviour
class Game(
    private val width: Int = 25, private val height: Int = 25,
    private val foodProducer: FoodProducer,
    private val initialFoodPosition: Position = Position(width / 2, height / 2)
) {

    private val food: MutableStateFlow<Food> = MutableStateFlow(Food.Normal(initialFoodPosition))

    private val gameSpeedFlow: MutableStateFlow<GameSpeed> = MutableStateFlow(GameSpeed.Normal)
    val gameSpeed: Flow<GameSpeed> = gameSpeedFlow

    private val initialSnake = listOf(Position(0, 0))

    private val snakeFlow: MutableStateFlow<List<Position>> = MutableStateFlow(initialSnake)
    private val snakeDeadFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val snakeDead: Flow<Boolean> = snakeDeadFlow

    val map: Flow<List<Cell>> = snakeFlow
        .combine(food) { snake, food ->
            List(width * height) { index ->
                val position = positionFromIndex(index, width)

                when {
                    snake.first() == position -> Cell.Snake.Head
                    snake.contains(position) -> {
                        Cell.Snake.Body
                    }
                    position == food.position -> {
                        food.toCell()
                    }
                    else -> {
                        Cell.Empty
                    }
                }
            }
        }

    private var currentDirection = Direction.Right
    private var canDirectionBeChanged = true

    fun onDirectionChanged(newDirection: Direction) {
        if (!canDirectionBeChanged || currentDirection.isHorizontal() && newDirection.isHorizontal() || currentDirection.isVertical() && newDirection.isVertical()) {
            return
        }
        canDirectionBeChanged = false
        currentDirection = newDirection
    }

    fun tick() {
        if (snakeDeadFlow.value) return
        snakeFlow.updateValue { snakePositions ->
            val snake = Snake.from(snakePositions)

            val isSnakeDead = snake.isDead(currentDirection)
            snakeDeadFlow.value = isSnakeDead
            if (isSnakeDead) return@updateValue snake.toList()

            snake.move(currentDirection)

            val currentFood = food.value

            val hasEatenFood = hasEatenFood(snake, currentFood.position)
            if (hasEatenFood) {
                snake.grow(currentDirection).also {
                    updateGameSpeed(currentFood)
                }

                food.value = foodProducer.spawnFood(
                    width = width,
                    height = height,
                    snake = snake
                )
            }

            return@updateValue if (hasEatenFood && currentFood is Food.Reverse) {
                val reversedSnake = snake.toList().reversed()

                val head = reversedSnake[0]
                val second = reversedSnake[1]

                currentDirection =
                    if (head.x == second.x) {
                        if (head.y > second.y) {
                            Direction.Down
                        } else {
                            Direction.Up
                        }
                    } else {
                        if (head.x > second.x) {
                            Direction.Right
                        } else {
                            Direction.Left
                        }
                    }

                reversedSnake
            } else {
                snake.toList()
            }
        }
        canDirectionBeChanged = true
    }

    private fun updateGameSpeed(eatenFood: Food) {
        when (eatenFood) {
            is Food.Accelerate -> {
                gameSpeedFlow.value = GameSpeed.Faster
            }
            is Food.Decelerate -> {
                gameSpeedFlow.value = GameSpeed.Slower
            }
            is Food.Normal,
            is Food.Reverse -> {
                gameSpeedFlow.value = GameSpeed.Normal
            }
        }
    }

    private fun hasEatenFood(snake: Snake, food: Position): Boolean = snake.head.position == food

    private fun Snake.isDead(direction: Direction): Boolean {
        val newHead = moveHead(this.head.position, direction)

        val isOutOfMap = newHead.x !in (0 until width) || newHead.y !in (0 until height)
        return if (isOutOfMap) {
            true
        } else {
            var running = this.head
            while (running.next != null) {
                if (running.position == newHead) {
                    return true
                }
                running = running.next!!
            }

            return false
        }
    }

    private fun Snake.move(direction: Direction) {
        this.appendHead(moveHead(this.head.position, direction))
        this.removeLast()
    }

    private fun Snake.grow(currentDirection: Direction) {
        //todo test more carefully
        val snakeList = this.toList()
        val last = snakeList.last()
        if (snakeList.size == 1) {
            this.append(
                when (currentDirection) {
                    Direction.Left -> last.copy(x = last.x + 1)
                    Direction.Right -> last.copy(x = last.x - 1)
                    Direction.Up -> last.copy(y = last.y + 1)
                    Direction.Down -> last.copy(y = last.y - 1)
                }
            )
        } else {
            val penultimate = snakeList[snakeList.lastIndex - 1]
            this.append(
                if (last.x == penultimate.x) {
                    if (last.y > penultimate.y) last.copy(y = last.y + 1) else last.copy(y = last.y - 1)
                } else {
                    if (last.x > penultimate.x) last.copy(x = last.x + 1) else last.copy(x = last.x - 1)
                }
            )
        }
    }

    private fun moveHead(head: Position, direction: Direction): Position =
        head.copy(
            x = when (direction) {
                Direction.Left -> head.x - 1
                Direction.Right -> head.x + 1
                else -> head.x
            },
            y = when (direction) {
                Direction.Up -> head.y - 1
                Direction.Down -> head.y + 1
                else -> head.y
            }
        )

    fun onRestartGame() {
        snakeFlow.value = initialSnake
        currentDirection = Direction.Right
        food.value = Food.Normal(initialFoodPosition)
        gameSpeedFlow.value = GameSpeed.Normal
        snakeDeadFlow.value = false
    }

    enum class GameSpeed {
        Normal,
        Faster,
        Slower
    }

    enum class Direction {
        Left,
        Right,
        Up,
        Down;

        fun isHorizontal() = this == Left || this == Right

        fun isVertical() = this == Up || this == Down
    }

    enum class FoodType {
        Normal,
        Accelerate,
        Decelerate,
        Reverse
    }

    sealed class Cell {
        object Empty : Cell()
        data class Food(val type: FoodType) : Cell()
        sealed class Snake : Cell() {
            object Body : Snake()
            object Head : Snake()
        }
    }

    private fun Food.toCell(): Cell.Food {
        return when (this) {
            is Food.Accelerate -> Cell.Food(FoodType.Accelerate)
            is Food.Decelerate -> Cell.Food(FoodType.Decelerate)
            is Food.Normal -> Cell.Food(FoodType.Normal)
            is Food.Reverse -> Cell.Food(FoodType.Reverse)
        }
    }
}
