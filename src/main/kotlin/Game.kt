import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class Game(
    private val width: Int = 25, private val height: Int = 25,
    private val foodProducer: FoodProducer,
    private val initialFoodPosition: Position = Position(width / 2, height / 2)
) {

    private val food: MutableStateFlow<Food> = MutableStateFlow(Food.Normal(initialFoodPosition))

    private val gameSpeedFlow: MutableStateFlow<GameSpeed> = MutableStateFlow(GameSpeed.Normal)
    val gameSpeed: Flow<GameSpeed> = gameSpeedFlow

    private val scoreFlow: MutableStateFlow<Int> = MutableStateFlow(0)
    val score: Flow<Int> = scoreFlow

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
    private var canGoThroughWalls = false
    private var canDirectionBeChanged = true //only one direction change per tick is allowed

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

            val isSnakeDead = snake.isDead(currentDirection, canGoThroughWalls)
            snakeDeadFlow.value = isSnakeDead
            if (isSnakeDead) return@updateValue snake.toList()

            snake.move(currentDirection, canGoThroughWalls)

            val food = food.value
            val hasEatenFood = hasEatenFood(snake, food)

            if (hasEatenFood) {
                snake.grow(currentDirection)
                updateGameSpeed(food)
                updateCanGoThroughWalls(food)
                spawnFood(snake)
                updateGameScore()
            }

            return@updateValue if (hasEatenFood && food is Food.Reverse) {
                snake.toList().reversed().also { reversedSnake ->
                    currentDirection = getMovingDirectionFor(reversedSnake)
                }
            } else {
                snake.toList()
            }
        }
        canDirectionBeChanged = true
    }

    private fun spawnFood(snake: Snake) {
        food.value = foodProducer.spawnFood(
            width = width,
            height = height,
            snake = snake
        )
    }

    private fun getMovingDirectionFor(snakePositions: List<Position>): Direction {
        require(snakePositions.size > 1)

        val head = snakePositions[0]
        val firstBodyPart = snakePositions[1]

        return if (head.x == firstBodyPart.x) {
            if (head.y > firstBodyPart.y) {
                Direction.Down
            } else {
                Direction.Up
            }
        } else {
            if (head.x > firstBodyPart.x) {
                Direction.Right
            } else {
                Direction.Left
            }
        }
    }

    private fun updateGameSpeed(eatenFood: Food) {
        gameSpeedFlow.value = when (eatenFood) {
            is Food.Accelerate -> {
                GameSpeed.Faster
            }
            is Food.Decelerate -> {
                GameSpeed.Slower
            }
            else -> GameSpeed.Normal
        }
    }

    private fun updateCanGoThroughWalls(eatenFood: Food) {
        canGoThroughWalls = eatenFood is Food.GoThroughWalls
    }

    private fun updateGameScore() {
        scoreFlow.value++
    }

    private fun hasEatenFood(snake: Snake, food: Food): Boolean = snake.head.position == food.position

    private fun Position.isOutOfMap(): Boolean = this.x !in (0 until width) || this.y !in (0 until height)

    private fun Snake.isDead(direction: Direction, canGoTroughWalls: Boolean): Boolean {
        return if (!canGoTroughWalls && this.willHitWall(direction)) {
            true
        } else {
            val newHead = this.head.position.getNextPositionIn(direction)
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

    private fun Snake.move(direction: Direction, canGoTroughWalls: Boolean) {
        if (canGoTroughWalls && this.willHitWall(direction)) {
            this.moveHeadThroughWallIn(direction)
        } else {
            this.moveHeadIn(direction)
        }
        this.removeLast()
    }

    private fun Snake.willHitWall(direction: Direction): Boolean =
        this.head.position.getNextPositionIn(direction).isOutOfMap()

    private fun Snake.grow(currentDirection: Direction) {
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
            val oneBeforeLast = snakeList[snakeList.lastIndex - 1]
            this.append(
                if (last.x == oneBeforeLast.x) {
                    if (last.y > oneBeforeLast.y) last.copy(y = last.y + 1) else last.copy(y = last.y - 1)
                } else {
                    if (last.x > oneBeforeLast.x) last.copy(x = last.x + 1) else last.copy(x = last.x - 1)
                }
            )
        }
    }

    private fun Snake.moveHeadIn(direction: Direction) {
        this.appendHead(this.head.position.getNextPositionIn(direction))
    }

    private fun Snake.moveHeadThroughWallIn(direction: Direction) {
        this.appendHead(this.head.position.getNextPositionThroughTheWallIn(direction))
    }

    private fun Position.getNextPositionIn(direction: Direction): Position =
        this.copy(
            x = when (direction) {
                Direction.Left -> this.x - 1
                Direction.Right -> this.x + 1
                else -> this.x
            },
            y = when (direction) {
                Direction.Up -> this.y - 1
                Direction.Down -> this.y + 1
                else -> this.y
            }
        )

    private fun Position.getNextPositionThroughTheWallIn(direction: Direction): Position =
        this.copy(
            x = when (direction) {
                Direction.Left -> this.x - 1 + width
                Direction.Right -> this.x + 1 - width
                else -> this.x
            },
            y = when (direction) {
                Direction.Up -> this.y - 1 + height
                Direction.Down -> this.y + 1 - height
                else -> this.y
            }
        )

    fun onRestartGame() {
        snakeFlow.value = initialSnake
        currentDirection = Direction.Right
        food.value = Food.Normal(initialFoodPosition)
        gameSpeedFlow.value = GameSpeed.Normal
        snakeDeadFlow.value = false
        canGoThroughWalls = false
        scoreFlow.value = 0
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

        fun getOppositeDirection(): Direction =
            when (this) {
                Left -> Right
                Right -> Left
                Up -> Down
                Down -> Up
            }
    }

    enum class FoodType {
        Normal,
        Accelerate,
        Decelerate,
        Reverse,
        GoThroughWalls
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
            is Food.GoThroughWalls -> Cell.Food(FoodType.GoThroughWalls)
        }
    }
}
