import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FoodProducerMock : FoodProducer {

    private var foodPositions: List<Position> = emptyList()

    private var callCount = 0

    fun mockFoodPositions(positions: List<Position>) {
        check(positions.isNotEmpty()) {
            "can't mock with no values"
        }
        foodPositions = positions
    }

    override fun spawnFood(width: Int, height: Int, snake: Snake): Position {
        check(foodPositions.isNotEmpty()) {
            "food positions are not mocked did you forget to call 'mockFoodPositions()'?"
        }

        return foodPositions.getOrNull(callCount++) ?: foodPositions.last()
    }
}

@ExperimentalCoroutinesApi
class GameTest {

    private lateinit var game: Game

    private lateinit var foodProducerMock: FoodProducerMock

    private val width = 10
    private val height = 5

    @BeforeEach
    fun setUp() {
        foodProducerMock = FoodProducerMock()
    }

    private fun createGame(initialFoodPosition: Position = Position(0, 4)) {
        game = Game(width = width, height = height, foodProducerMock, initialFoodPosition = initialFoodPosition)
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
        createGame()

        game.assertInitialSnakeState()
        for (x in 3 until width) {
            for (y in 0 until height) {
                game.assertAtPosition(x, y, Game.Cell.Empty)
            }
        }
    }

    @Test
    fun `should move snake to default direction`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.tick()

        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)
    }

    @Test
    fun `should turn right`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Right)

        game.assertAtPosition(1, 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `should turn down`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)

        game.assertAtPosition(0, 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `should turn left`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.tick()
        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Left)

        game.assertAtPosition(0, 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `should turn up`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Up)

        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)
    }

    @Test
    fun `changing horizontal direction when going horizontally should have no effect`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.onDirectionChanged(Game.Direction.Right)
        game.onDirectionChanged(Game.Direction.Left)

        game.tick()

        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)
    }

    @Test
    fun `changing vertical direction when going vertically should have no effect`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.onDirectionChanged(Game.Direction.Right)
        game.onDirectionChanged(Game.Direction.Down)
        game.onDirectionChanged(Game.Direction.Up)

        game.tick()

        game.assertAtPosition(0, 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `should make a square movement`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.assertAtPosition(0, 1, Game.Cell.SnakeBody)

        game.turn(Game.Direction.Right)
        game.assertAtPosition(1, 1, Game.Cell.SnakeBody)

        game.turn(Game.Direction.Up)
        game.assertAtPosition(1, 0, Game.Cell.SnakeBody)

        game.turn(Game.Direction.Left)
        game.assertAtPosition(0, 0, Game.Cell.SnakeBody)
    }

    @Test
    fun `snake should die when going up`() = runBlockingTest {
        createGame()

        game.turn(Game.Direction.Up)

        assertTrue(game.snakeDead.first())
        game.assertAtPosition(0, 0, Game.Cell.SnakeBody)
    }

    @Test
    fun `snake should die when going right`() = runBlockingTest {
        createGame()

        repeat(width) {
            game.turn(Game.Direction.Right)
        }

        assertTrue(game.snakeDead.first())
        game.assertAtPosition(width - 1, 0, Game.Cell.SnakeBody)
    }

    @Test
    fun `snake should die when going down`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1,0))

        repeat(height) {
            game.turn(Game.Direction.Down)
        }

        assertTrue(game.snakeDead.first())
        game.assertAtPosition(0, height - 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `snake should die when going left`() = runBlockingTest {
        createGame()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Left)

        assertTrue(game.snakeDead.first())
        game.assertAtPosition(0, 1, Game.Cell.SnakeBody)
    }

    @Test
    fun `snake should die when eating itself`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))

        foodProducerMock.mockFoodPositions(
            listOf(
                Position(2, 0),
                Position(3, 0),
                Position(4, 0),
                Position(0, 1),
            )
        )

        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Left)
        game.turn(Game.Direction.Up)

        assertTrue(game.snakeDead.first())
        game.assertAtPosition(4, 0, Game.Cell.SnakeBody)
    }
}
