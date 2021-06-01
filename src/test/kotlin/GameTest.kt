import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
class GameTest {

    private lateinit var game: Game

    private val width = 10
    private val height = 10

    @BeforeEach
    fun setup() {
        game = Game(width = width, height = height)
    }

    private fun Game.turn(direction: Game.Direction) {
        this.onDirectionChanged(direction)
        this.tick()
    }

    private suspend fun Game.assertAtPosition(x: Int, y: Int, expected: Game.Cell) {
        val map = this.map.first()

        val actual = map[x + y * width]

        assertEquals(expected, actual)
    }

    private suspend fun Game.assertInitialSnakeState() {
        this.assertAtPosition(0, 0, Game.Cell.SnakeBody)
        this.assertAtPosition(1, 0, Game.Cell.SnakeBody)
        this.assertAtPosition(2, 0, Game.Cell.SnakeHead)
    }

    @Test
    fun `should have proper initial state`() = runBlockingTest {
        game.assertInitialSnakeState()
        for (x in 3 until width) {
            for (y in 0 until height) {
                game.assertAtPosition(x, y, Game.Cell.Empty)
            }
        }
    }

    @Test
    fun `should move snake to default direction`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.tick()

        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(2, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(3, 0, Game.Cell.SnakeHead)
    }

    @Test
    fun `should turn right`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Right)

        game.assertAtPosition(2, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(2, 1, Game.Cell.SnakeBody)
        game.assertAtPosition(3, 1, Game.Cell.SnakeHead)
    }

    @Test
    fun `should turn down`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)

        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(2, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(2, 1, Game.Cell.SnakeHead)
    }

    @Test
    fun `should turn left`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Left)

        game.assertAtPosition(2, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(2, 1, Game.Cell.SnakeBody)
        game.assertAtPosition(1, 1, Game.Cell.SnakeHead)
    }

    @Test
    fun `should turn up`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Up)

        /**
         * 0,0 next 1,0
         * 1,0 next 2,0
         * 2,0 next null
         *
         * 3,0 next null - new head
         *
         * 1,0 next 2,0 next null
         * 2,0 next nul
         *
         *
         */

        game.assertAtPosition(2, 1, Game.Cell.SnakeBody)
        game.assertAtPosition(3, 1, Game.Cell.SnakeBody)
        game.assertAtPosition(3, 0, Game.Cell.SnakeHead)
    }

    @Test
    fun `changing horizontal direction when going horizontally should have no effect`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.onDirectionChanged(Game.Direction.Right)
        game.onDirectionChanged(Game.Direction.Left)

        game.tick()

        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(2, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(3, 0, Game.Cell.SnakeHead)
    }

    @Test
    fun `changing vertical direction when going vertically should have no effect`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.onDirectionChanged(Game.Direction.Right)
        game.onDirectionChanged(Game.Direction.Down)
        game.onDirectionChanged(Game.Direction.Up)

        game.tick()

        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(2, 0, Game.Cell.SnakeBody)
        game.assertAtPosition(2, 1, Game.Cell.SnakeHead)
    }
}