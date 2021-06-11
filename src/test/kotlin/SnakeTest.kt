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

}
