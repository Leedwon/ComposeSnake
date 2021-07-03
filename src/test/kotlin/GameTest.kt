import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.lang.Thread.sleep

class FoodProducerMock : FoodProducer {

    private var foodList: List<Food> = emptyList()

    private var callCount = 0

    fun mockFoodPositions(foodList: List<Food>) {
        check(foodList.isNotEmpty()) {
            "can't mock with no values"
        }
        this.foodList = foodList
    }

    override fun spawnFood(width: Int, height: Int, snake: Snake): Food {
        check(foodList.isNotEmpty()) {
            "food positions are not mocked did you forget to call 'mockFoodPositions()'?"
        }

        return foodList.getOrNull(callCount++) ?: foodList.last()
    }
}

@ExperimentalCoroutinesApi
class GameTest {

    private lateinit var game: Game

    private lateinit var foodProducerMock: FoodProducerMock

    private lateinit var gameSpeedValues: MutableList<Game.GameSpeed>

    private val width = 10
    private val height = 5

    @BeforeEach
    fun setUp() {
        gameSpeedValues = mutableListOf()
        foodProducerMock = FoodProducerMock()
    }

    private fun TestCoroutineScope.collectGameSpeedValues(): Job {
        return launch {
            game.gameSpeed.collect {
                gameSpeedValues.add(it)
            }
        }
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
        this.assertAtPosition(0, 0, Game.Cell.Snake.Head)
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
        assertFalse(game.snakeDead.first())
        assertEquals(Game.GameSpeed.Normal, game.gameSpeed.first())
    }

    @Test
    fun `should move snake to default direction`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.tick()

        game.assertAtPosition(1, 0, Game.Cell.Snake.Head)
    }

    @Test
    fun `should turn right`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Right)

        game.assertAtPosition(1, 1, Game.Cell.Snake.Head)
    }

    @Test
    fun `should turn down`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)

        game.assertAtPosition(0, 1, Game.Cell.Snake.Head)
    }

    @Test
    fun `should turn left`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.tick()
        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Left)

        game.assertAtPosition(0, 1, Game.Cell.Snake.Head)
    }

    @Test
    fun `should turn up`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Up)

        game.assertAtPosition(1, 0, Game.Cell.Snake.Head)
    }

    @Test
    fun `snake should grow when eating food`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(listOf(Food.Normal(Position(0, 1))))

        game.turn(Game.Direction.Right)

        game.assertAtPosition(0, 0, Game.Cell.Snake.Body)
        game.assertAtPosition(1, 0, Game.Cell.Snake.Head)
    }

    @Test
    fun `changing horizontal direction when going horizontally should have no effect`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.onDirectionChanged(Game.Direction.Right)
        game.onDirectionChanged(Game.Direction.Left)

        game.tick()

        game.assertAtPosition(1, 0, Game.Cell.Snake.Head)
    }

    @Test
    fun `changing vertical direction when going vertically should have no effect`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.onDirectionChanged(Game.Direction.Right)
        game.onDirectionChanged(Game.Direction.Down)
        game.onDirectionChanged(Game.Direction.Up)

        game.tick()

        game.assertAtPosition(0, 1, Game.Cell.Snake.Head)
    }

    @Test
    fun `should make a square movement`() = runBlockingTest {
        createGame()

        game.assertInitialSnakeState()

        game.turn(Game.Direction.Down)
        game.assertAtPosition(0, 1, Game.Cell.Snake.Head)

        game.turn(Game.Direction.Right)
        game.assertAtPosition(1, 1, Game.Cell.Snake.Head)

        game.turn(Game.Direction.Up)
        game.assertAtPosition(1, 0, Game.Cell.Snake.Head)

        game.turn(Game.Direction.Left)
        game.assertAtPosition(0, 0, Game.Cell.Snake.Head)
    }

    @Test
    fun `should propagate normal food correctly`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))

        game.assertAtPosition(1, 0, Game.Cell.Food(Game.FoodType.Normal))
    }


    @Test
    fun `should propagate reverse food correctly`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(listOf(Food.Reverse(Position(2, 0))))

        game.turn(Game.Direction.Right)

        game.assertAtPosition(2, 0, Game.Cell.Food(Game.FoodType.Reverse))
    }


    @Test
    fun `should propagate accelerate food correctly`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(listOf(Food.Accelerate(Position(2, 0))))

        game.turn(Game.Direction.Right)

        game.assertAtPosition(2, 0, Game.Cell.Food(Game.FoodType.Accelerate))
    }


    @Test
    fun `should propagate decelerate food correctly`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(listOf(Food.Decelerate(Position(2, 0))))

        game.turn(Game.Direction.Right)

        game.assertAtPosition(2, 0, Game.Cell.Food(Game.FoodType.Decelerate))
    }

    @Test
    fun `should propagate accelerated game speed after eating accelerate food`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(listOf(Food.Accelerate(Position(2, 0))))

        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)

        assertEquals(Game.GameSpeed.Faster, game.gameSpeed.first())
    }

    @Test
    fun `should propagate decelerated game speed after eating decelerate food`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(listOf(Food.Decelerate(Position(2, 0))))

        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)

        assertEquals(Game.GameSpeed.Slower, game.gameSpeed.first())
    }

    @Test
    fun `should reset game speed from faster to normal by eating normal food`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(
            listOf(
                Food.Accelerate(Position(2, 0)),
                Food.Normal(Position(3, 0)),
            )
        )

        val collectingJob = collectGameSpeedValues()

        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)


        assertEquals(
            listOf(
                Game.GameSpeed.Normal,
                Game.GameSpeed.Faster,
                Game.GameSpeed.Normal,
            ),
            gameSpeedValues
        )
        collectingJob.cancel()
    }

    @Test
    fun `should reset game speed from slower to normal by eating normal food`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(
            listOf(
                Food.Decelerate(Position(2, 0)),
                Food.Normal(Position(3, 0)),
            )
        )

        val collectingJob = collectGameSpeedValues()

        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)


        assertEquals(
            listOf(
                Game.GameSpeed.Normal,
                Game.GameSpeed.Slower,
                Game.GameSpeed.Normal,
            ),
            gameSpeedValues
        )
        collectingJob.cancel()
    }

    @Test
    fun `should reset game speed from faster to normal by eating reverse food`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(
            listOf(
                Food.Accelerate(Position(2, 0)),
                Food.Reverse(Position(3, 0)),
            )
        )

        val collectingJob = collectGameSpeedValues()

        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)


        assertEquals(
            listOf(
                Game.GameSpeed.Normal,
                Game.GameSpeed.Faster,
                Game.GameSpeed.Normal,
            ),
            gameSpeedValues
        )
        collectingJob.cancel()
    }

    @Test
    fun `should reset game speed from slower to normal by eating reverse food`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))
        foodProducerMock.mockFoodPositions(
            listOf(
                Food.Decelerate(Position(2, 0)),
                Food.Reverse(Position(3, 0)),
            )
        )

        val collectingJob = collectGameSpeedValues()

        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Right)


        assertEquals(
            listOf(
                Game.GameSpeed.Normal,
                Game.GameSpeed.Slower,
                Game.GameSpeed.Normal,
            ),
            gameSpeedValues
        )
        collectingJob.cancel()
    }

    @Test
    fun `snake should die when going up`() = runBlockingTest {
        createGame()

        game.turn(Game.Direction.Up)

        assertTrue(game.snakeDead.first())
        game.assertAtPosition(0, 0, Game.Cell.Snake.Head)
    }

    @Test
    fun `snake should die when going right`() = runBlockingTest {
        createGame()

        repeat(width) {
            game.turn(Game.Direction.Right)
        }

        assertTrue(game.snakeDead.first())
        game.assertAtPosition(width - 1, 0, Game.Cell.Snake.Head)
    }

    @Test
    fun `snake should die when going down`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))

        repeat(height) {
            game.turn(Game.Direction.Down)
        }

        assertTrue(game.snakeDead.first())
        game.assertAtPosition(0, height - 1, Game.Cell.Snake.Head)
    }

    @Test
    fun `snake should die when going left`() = runBlockingTest {
        createGame()

        game.turn(Game.Direction.Down)
        game.turn(Game.Direction.Left)

        assertTrue(game.snakeDead.first())
        game.assertAtPosition(0, 1, Game.Cell.Snake.Head)
    }

    @Test
    fun `snake should die when eating itself`() = runBlockingTest {
        createGame(initialFoodPosition = Position(1, 0))

        foodProducerMock.mockFoodPositions(
            listOf(
                Food.Normal(Position(2, 0)),
                Food.Normal(Position(3, 0)),
                Food.Normal(Position(4, 0)),
                Food.Normal(Position(0, 1)),
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
        game.assertAtPosition(3, 1, Game.Cell.Snake.Head)
    }

    @Test
    fun `should correctly reverse snake and change current direction to left after eating reverse food`() =
        runBlockingTest {
            foodProducerMock.mockFoodPositions(
                listOf(
                    Food.Reverse(Position(3, 0)),
                    Food.Normal(Position(6, 0))
                )
            )
            createGame(initialFoodPosition = Position(2, 0))

            game.turn(Game.Direction.Right)
            game.turn(Game.Direction.Right)
            game.turn(Game.Direction.Right)

            game.assertAtPosition(1, 0, Game.Cell.Snake.Head)
            game.assertAtPosition(2, 0, Game.Cell.Snake.Body)

            game.tick()

            game.assertAtPosition(0, 0, Game.Cell.Snake.Head)
            game.assertAtPosition(1, 0, Game.Cell.Snake.Body)
        }

    @Test
    fun `should correctly reverse snake and change current direction to right after eating reverse food`() =
        runBlockingTest {
            foodProducerMock.mockFoodPositions(
                listOf(
                    Food.Normal(Position(2, 0)),
                    Food.Reverse(Position(4, 1)),
                    Food.Reverse(Position(9, 0)),
                )
            )

            createGame(initialFoodPosition = Position(1, 0))

            repeat(7) {
                game.turn(Game.Direction.Right) //head 7,0
            }

            game.turn(Game.Direction.Down)  //head 7,1
            game.turn(Game.Direction.Left)  //head 6,1
            game.turn(Game.Direction.Left)  //head 5,1
            game.turn(Game.Direction.Left)  //head 4,1

            game.assertAtPosition(7, 1, Game.Cell.Snake.Head)
            game.assertAtPosition(6, 1, Game.Cell.Snake.Body)
            game.assertAtPosition(5, 1, Game.Cell.Snake.Body)
            game.assertAtPosition(4, 1, Game.Cell.Snake.Body)

            game.tick() //head 8,1

            game.assertAtPosition(8, 1, Game.Cell.Snake.Head)
            game.assertAtPosition(7, 1, Game.Cell.Snake.Body)
            game.assertAtPosition(6, 1, Game.Cell.Snake.Body)
            game.assertAtPosition(5, 1, Game.Cell.Snake.Body)
        }

    @Test
    fun `should correctly reverse snake and change current direction to up after eating reverse food`() =
        runBlockingTest {
            foodProducerMock.mockFoodPositions(
                listOf(
                    Food.Reverse(Position(2, 2)),
                    Food.Reverse(Position(0, 0)),
                )
            )
            createGame(initialFoodPosition = Position(1, 0))

            game.turn(Game.Direction.Right) //head 1,0
            game.turn(Game.Direction.Right) //head 2,0
            game.turn(Game.Direction.Down)  //head 2,1
            game.turn(Game.Direction.Down)  //head 2,2

            game.assertAtPosition(2, 1, Game.Cell.Snake.Head)
            game.assertAtPosition(2, 2, Game.Cell.Snake.Body)

            game.tick()

            game.assertAtPosition(2, 0, Game.Cell.Snake.Head)
            game.assertAtPosition(2, 1, Game.Cell.Snake.Body)
        }

    @Test
    fun `should correctly reverse snake and change current direction to down after eating reverse food`() =
        runBlockingTest {
            foodProducerMock.mockFoodPositions(
                listOf(
                    Food.Reverse(Position(2, 0))
                )
            )
            createGame(initialFoodPosition = Position(1, 0))

            game.turn(Game.Direction.Right)
            game.turn(Game.Direction.Down)
            game.turn(Game.Direction.Right)
            game.turn(Game.Direction.Up)

            game.assertAtPosition(2, 1, Game.Cell.Snake.Head)
            game.assertAtPosition(2, 0, Game.Cell.Snake.Body)

            game.tick()

            game.assertAtPosition(2, 2, Game.Cell.Snake.Head)
            game.assertAtPosition(2, 1, Game.Cell.Snake.Body)
        }

    @Test
    fun `should correctly restart game`() = runBlockingTest {
        foodProducerMock.mockFoodPositions(listOf(Food.Reverse(Position(3, 0))))
        createGame(initialFoodPosition = Position(1, 0))

        game.turn(Game.Direction.Right)
        game.turn(Game.Direction.Up)

        assertTrue(game.snakeDead.first())

        game.onRestartGame()
        assertFalse(game.snakeDead.first())
        game.assertAtPosition(0, 0, Game.Cell.Snake.Head)
        game.assertAtPosition(1, 0, Game.Cell.Food(Game.FoodType.Normal))
    }
}
