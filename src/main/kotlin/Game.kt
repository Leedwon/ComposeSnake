import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class Game(
    private val width: Int = 25, private val height: Int = 25,
    private val foodProducer: FoodProducer
) {

    private val food: MutableStateFlow<Position> = MutableStateFlow(
        Position(
            x = Random.nextInt(1, width - 1),
            y = Random.nextInt(1, height - 1),
        )
    )

    private val snakeFlow: MutableStateFlow<List<Position>> = MutableStateFlow(listOf(Position(0, 0)))

    val map: Flow<List<Cell>> = snakeFlow
        .map { it.toList() }
        .combine(food) { snake, food ->
            List(width * height) { index ->
                val position = positionFromIndex(index, width)

                when {
                    snake.contains(position) -> {
                        Cell.SnakeBody
                    }
                    position == food -> {
                        Cell.Food
                    }
                    else -> {
                        Cell.Empty
                    }
                }
            }
        }

    private var currentDirection = Direction.Right

    fun onDirectionChanged(newDirection: Direction) {
        if (currentDirection.isHorizontal() && newDirection.isHorizontal() || currentDirection.isVertical() && newDirection.isVertical()) {
            return
        }
        currentDirection = newDirection
    }

    fun tick() {
        snakeFlow.updateValue { snakePositions ->
            val snake = Snake.from(snakePositions)
            snake.move(currentDirection)

            val hasEatenFood = hasEatenFood(snake, food.value)

            if (hasEatenFood) {
                snake.grow(currentDirection)
                food.value = foodProducer.spawnFood(
                    width = width,
                    height = height,
                    snake = snake
                )
            }

            snake.toList()
        }
    }

    private fun hasEatenFood(snake: Snake, food: Position): Boolean = snake.head.position == food

    private fun Snake.move(direction: Direction) {
        this.appendHead(moveHead(this.head.position, direction))
        this.removeLast()
    }

    private fun Snake.grow(currentDirection: Direction) {
        val last = this.toList().last()
        this.append(
            when (currentDirection) {
                Direction.Left -> last.copy(x = last.x + 1)
                Direction.Right -> last.copy(x = last.x - 1)
                Direction.Up -> last.copy(y = last.y + 1)
                Direction.Down -> last.copy(y = last.y - 1)
            }
        )
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

    enum class Direction {
        Left,
        Right,
        Up,
        Down;

        fun isHorizontal() = this == Left || this == Right

        fun isVertical() = this == Up || this == Down
    }

    sealed class Cell {
        object Empty : Cell()
        object Food : Cell() //todo more type of food
        object SnakeBody : Cell()
        object SnakeHead : Cell()
    }

}
