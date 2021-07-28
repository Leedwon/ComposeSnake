import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnakeTest {

    private operator fun Snake.get(i: Int): Position {
        var current = this.head
        repeat(i) {
            current = current.next!!
        }
        return current.position
    }

    @Test
    fun `should properly create snake`() {
        val snake = Snake(Position(0, 0))

        assertEquals(snake[0], Position(0, 0))
    }

    @Test
    fun `should properly append to snake`() {
        val snake = Snake(Position(0, 0))

        snake.append(Position(1, 0))

        assertEquals(snake[0], Position(0, 0))
        assertEquals(snake[1], Position(1, 0))
    }

    @Test
    fun `should properly remove from snake`() {
        val snake = Snake(Position(0, 0))

        repeat(5) {
            snake.append(Position(it + 1, 0))
        }

        repeat(6) {
            assertEquals(snake[it], Position(it, 0))
        }

        repeat(5) {
            snake.removeLast()
        }

        assertEquals(snake[0], Position(0, 0))
        assertEquals(snake.head.next, null)
    }

    @Test
    fun `should append head to snake`() {
        val snake = Snake(Position(1, 1))
        snake.appendHead(Position(0, 0))

        assertEquals(snake[0], Position(0, 0))
        assertEquals(snake[1], Position(1, 1))

    }

    @Test
    fun `should create list from snake`() {
        val snake = Snake(Position(0, 0))
        snake.appendHead(Position(1, 0))
        snake.appendHead(Position(2, 0))
        snake.appendHead(Position(3, 0))
        snake.appendHead(Position(4, 0))

        val snakeList = snake.toList()

        assertEquals(
            snakeList, listOf(
                Position(4, 0),
                Position(3, 0),
                Position(2, 0),
                Position(1, 0),
                Position(0, 0)
            )
        )
    }

    @Test
    fun `should create snake from list`() {
        val list = listOf(
            Position(4,0),
            Position(3,0),
            Position(2,0),
            Position(1,0),
            Position(0,0),
        )

        val expected = Snake(Position(0,0))
        expected.appendHead(Position(1,0))
        expected.appendHead(Position(2,0))
        expected.appendHead(Position(3,0))
        expected.appendHead(Position(4,0))

        assertEquals(
            Snake.from(list).toList(),
            expected.toList()
        )
    }

    @Test
    fun `should return correctly reversed snake`() {
        val list = listOf(
            Position(0,0),
            Position(1,0),
            Position(2,0),
            Position(3,0),
            Position(4,0),
        )

        val expected = Snake(Position(0,0))
        expected.appendHead(Position(1,0))
        expected.appendHead(Position(2,0))
        expected.appendHead(Position(3,0))
        expected.appendHead(Position(4,0))

        assertEquals(
            Snake.from(list).toList(),
            expected.reversed().toList()
        )
    }

}
