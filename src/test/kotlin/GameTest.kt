import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FoodProducerMock : FoodProducer {
    override fun spawnFood(width: Int, height: Int, snake: Snake): Position {
        TODO("Not yet implemented")
    }
}

@ExperimentalCoroutinesApi
class GameTest {

    private lateinit var game: Game

    private val width = 10
    private val height = 5

    @BeforeEach
    fun setup() {
        game = Game(width = width, height = height, FoodProducerMock()) //todo
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
    }

    @Test
    fun `should turn right`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Right)

        game.assertAtPosition(1, 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `should turn down`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)

        game.assertAtPosition(0, 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `should turn left`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.tick()
        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Left)

        game.assertAtPosition(0, 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `should turn up`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Up)

        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)
    }

    @Test
    fun `changing horizontal direction when going horizontally should have no effect`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.onDirectionChanged(Game.Direction.Right)
        game.onDirectionChanged(Game.Direction.Left)

        game.tick()

        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)
    }

    @Test
    fun `changing vertical direction when going vertically should have no effect`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.onDirectionChanged(Game.Direction.Right)
        game.onDirectionChanged(Game.Direction.Down)
        game.onDirectionChanged(Game.Direction.Up)

        game.tick()

        game.assertAtPosition(0, 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `should make a square movement`() = runBlockingTest {
        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.assertAtPosition(0,1, Game.Cell.SnakeBody)

        game.turn(Game.Direction.Right)
        game.assertAtPosition(1,1, Game.Cell.SnakeBody)

        game.turn(Game.Direction.Up)
        game.assertAtPosition(1,0, Game.Cell.SnakeBody)

        game.turn(Game.Direction.Left)
        game.assertAtPosition(0,0, Game.Cell.SnakeBody)
    }
}
