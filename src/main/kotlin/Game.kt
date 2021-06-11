import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlin.random.Random

class Game(private val width: Int = 25, private val height: Int = 25) {

    private val snake = Snake(Position(0, 0))

    private val food: MutableStateFlow<Position> = MutableStateFlow(
        Position(
            x = Random.nextInt(1, width - 1),
            y = Random.nextInt(1, height - 1),
        )
    )

    private val tick: MutableStateFlow<Int> = MutableStateFlow(0)

    val map: Flow<List<Cell>> = tick.combine(food) { _, food ->
        val snake = snake.toList()
        List(width * height) { index ->
            val position = positionFromIndex(index)

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

    private fun Snake.toList(): List<Position> {
        val result = mutableListOf<Position>()
        var running: Snake.Node? = this.head

        while (running != null) {
            result.add(running.position)
            running = running.next
        }

        return result
    }

    private var currentDirection = Direction.Right

    fun onDirectionChanged(newDirection: Direction) {
        if (currentDirection.isHorizontal() && newDirection.isHorizontal() || currentDirection.isVertical() && newDirection.isVertical()) {
            return
        }
        currentDirection = newDirection
    }

    private fun Position.toIndex() = this.x + this.y * width

    private fun positionFromIndex(index: Int): Position =
        Position(
            x = index % width,
            y = index / width
        )

    fun tick() {
        snake.move(currentDirection)

        val hasEatenFood = hasEatenFood(snake, food.value)

        if (hasEatenFood) {
            snake.grow(currentDirection)
            food.value = spawnFood()
        }

        tick.value = if (tick.value == 0) {
            1
        } else {
            0
        }
    }

    private fun spawnFood(): Position {
        val snakeList = snake.toList()
        val randRange = width * height - snakeList.size

        val randIndex = Random.nextInt(randRange)
        val foodPosition = positionFromIndex(randIndex)

        return if (snakeList.contains(foodPosition)) {
            val foodInSnakeIndex = snakeList.indexOf(foodPosition)
            val newIndex = if (randIndex > randRange / 2) {
                randIndex - foodInSnakeIndex
            } else {
                randIndex + foodInSnakeIndex
            }

            positionFromIndex(newIndex)

        } else {
            foodPosition
        }
    }

    private fun hasEatenFood(snake: Snake, food: Position): Boolean = snake.head.position == food

    private fun Snake.move(direction: Direction) {
        this.appendHead(moveHead(snake.head.position, direction))
        this.removeLast()
    }

    private fun Snake.grow(currentDirection: Direction) {
        val last = snake.toList().last()
        snake.append(
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
