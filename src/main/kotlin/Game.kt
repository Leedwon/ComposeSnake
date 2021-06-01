import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class Game(private val width: Int = 50, private val height: Int = 50) {

    private val _map: MutableStateFlow<List<Element>> = MutableStateFlow(List(height * width) { index ->
        val snakeHead = Element.SnakeBody(Position(2, 0), null)
        val snakeBody1 = Element.SnakeBody(Position(1, 0), snakeHead)
        val snakeBody2 = Element.SnakeBody(Position(0, 0), snakeBody1)

        when (index) {
            0 -> snakeBody2
            1 -> snakeBody1
            2 -> snakeHead
            else -> Element.Empty
        }
    })

    val map: Flow<List<Cell>> = _map.map { map ->
        map.map { element ->
            when (element) {
                Element.Empty -> Cell.Empty
                is Element.SnakeBody -> if (element.next == null) {
                    Cell.SnakeHead
                } else {
                    Cell.SnakeBody
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

    private fun Position.toIndex() = this.x + this.y * width

    private fun positionFromIndex(index: Int): Position =
        Position(
            x = index % width,
            y = index / height
        )

    fun tick() {
        val currentMap = _map.value

        val snake = getSnake(currentMap)

        val head = getHead(snake)
        val body = getBody(snake)

        val newHead = moveHead(head, currentDirection)
        val newBody = moveBody(body, newHead)

        val newMap = MutableList<Element>(width * height) { Element.Empty }

        newMap[newHead.position.toIndex()] = newHead
        newBody.forEach { newMap[it.position.toIndex()] = it }

        _map.value = newMap
    }

    private fun getSnake(map: List<Element>) = map.filterIsInstance<Element.SnakeBody>()

    private fun getHead(snake: List<Element.SnakeBody>) = snake.first { it.next == null }

    private fun getBody(snake: List<Element.SnakeBody>) = snake.filter { it.next != null }

    private fun moveHead(head: Element.SnakeBody, direction: Direction): Element.SnakeBody =
        head.copy(
            position = head.position.copy(
                x = when (direction) {
                    Direction.Left -> head.position.x - 1
                    Direction.Right -> head.position.x + 1
                    else -> head.position.x
                },
                y = when (direction) {
                    Direction.Up -> head.position.y - 1
                    Direction.Down -> head.position.y + 1
                    else -> head.position.y
                }
            )
        )

    private fun moveBody(body: List<Element.SnakeBody>, newHead: Element.SnakeBody): List<Element.SnakeBody> {
        val newBody = mutableListOf<Element.SnakeBody>()
        body.reversed().mapIndexed { index, element ->
            check(element.next != null) { "Snake body must have next element, only head is not having next element " }

            val nextElement = element.next
            if(index == 0) {
                newBody.add(0, nextElement.copy(next = newHead))
            } else {
                newBody.add(0, nextElement.copy(next = newBody[0]))
            }
        }

        return newBody
    }

    enum class Direction {
        Left,
        Right,
        Up,
        Down;

        fun isHorizontal() = this == Left || this == Right

        fun isVertical() = this == Up || this == Down
    }

    private sealed class Element {
        data class SnakeBody(val position: Position, val next: SnakeBody?) : Element() {
            fun isHead(): Boolean = next == null
        }

        object Empty : Element()
    }

    sealed class Cell {
        object Empty : Cell()
        object Food : Cell() //todo more type of food
        object SnakeBody : Cell()
        object SnakeHead : Cell()
    }

    data class Position(val x: Int, val y: Int)
}