import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RandomNumberProviderMock : RandomNumberProvider {
    private var nextRandomNumbers: List<Int> = listOf(0)

    private var index = 0

    fun mockNextRandom(nextRandomInt: Int) {
        index = 0
        nextRandomNumbers = listOf(nextRandomInt)
    }

    fun mockNextRandoms(nextRandomInts: List<Int>) {
        index = 0
        nextRandomNumbers = nextRandomInts
    }

    override fun getRandomNumber(until: Int): Int {
        return nextRandomNumbers.getOrNull(index++) ?: nextRandomNumbers.last()
    }
}


class FoodProducerImplTest {
    private lateinit var foodProducer: FoodProducerImpl

    private lateinit var randomNumberProviderMock: RandomNumberProviderMock

    private val width = 10
    private val height = 5

    private fun createSnake(positions: List<Position>): Snake {
        return Snake.from(positions)
    }

    @BeforeEach
    fun setUp() {
        randomNumberProviderMock = RandomNumberProviderMock()
    }

    private fun createFoodProducer() {
        foodProducer = FoodProducerImpl(randomNumberProviderMock)
    }

    @Test
    fun `should correctly spawn food with random number not overlapping snake`() {
        createFoodProducer()

        val foodPosition = Position(3, 0)
        val snakePosition = Position(1, 0)
        val snake = Snake(snakePosition)

        randomNumberProviderMock.mockNextRandom(foodPosition.toIndex(width))

        val actual = foodProducer.spawnFood(width, height, snake)

        assertEquals(Food.Normal(foodPosition), actual)
    }

    @Test
    fun `should correctly spawn food with random number overlapping snake in second half of the map`() {
        createFoodProducer()

        val foodPosition = Position(4, 3)

        randomNumberProviderMock.mockNextRandom(foodPosition.toIndex(width))

        val snake = createSnake(
            listOf(
                Position(4, 3),
                Position(3, 3),
                Position(2, 3),
            )
        )

        val expectedFoodPosition = Position(1, 3)
        val actualFoodPosition = foodProducer.spawnFood(width, height, snake)

        assertEquals(Food.Normal(expectedFoodPosition), actualFoodPosition)
    }

    @Test
    fun `should correctly spawn food with random number overlapping snake in first half of the map`() {
        createFoodProducer()

        val foodPosition = Position(5, 0)

        randomNumberProviderMock.mockNextRandom(foodPosition.toIndex(width))

        val snake = createSnake(
            listOf(
                Position(5, 0),
                Position(5, 1),
                Position(5, 2),
            )
        )

        val expectedFoodPosition = Position(6, 0)
        val actualFoodPosition = foodProducer.spawnFood(width, height, snake)

        assertEquals(Food.Normal(expectedFoodPosition), actualFoodPosition)
    }

    @Test
    fun `should correctly spawn accelerate food`() {
        createFoodProducer()

        val foodPosition = Position(3, 0)
        val snakePosition = Position(1, 0)
        val snake = Snake(snakePosition)

        randomNumberProviderMock.mockNextRandoms(listOf(foodPosition.toIndex(width), 50))

        val actual = foodProducer.spawnFood(width, height, snake)

        assertEquals(Food.Accelerate(foodPosition), actual)
    }

    @Test
    fun `should correctly spawn decelerate food`() {
        createFoodProducer()

        val foodPosition = Position(3, 0)
        val snakePosition = Position(1, 0)
        val snake = Snake(snakePosition)

        randomNumberProviderMock.mockNextRandoms(listOf(foodPosition.toIndex(width), 65))

        val actual = foodProducer.spawnFood(width, height, snake)

        assertEquals(Food.Decelerate(foodPosition), actual)
    }

    @Test
    fun `should correctly spawn reverse food`() {
        createFoodProducer()

        val foodPosition = Position(3, 0)
        val snakePosition = Position(1, 0)
        val snake = Snake(snakePosition)

        randomNumberProviderMock.mockNextRandoms(listOf(foodPosition.toIndex(width), 75))

        val actual = foodProducer.spawnFood(width, height, snake)

        assertEquals(Food.Reverse(foodPosition), actual)
    }

    @Test
    fun `should correctly spawn GoThroughWalls food`() {
        createFoodProducer()

        val foodPosition = Position(3, 0)
        val snakePosition = Position(1, 0)
        val snake = Snake(snakePosition)

        randomNumberProviderMock.mockNextRandoms(listOf(foodPosition.toIndex(width), 90))

        val actual = foodProducer.spawnFood(width, height, snake)

        assertEquals(Food.GoThroughWalls(foodPosition), actual)
    }

    @Test
    fun `should correctly spawn food when there is only one place available in last map half`() {
        createFoodProducer()

        val width = 2
        val height = 2

        val expectedFoodPosition = Position(1, 1)

        val snake = createSnake(
            listOf(
                Position(0, 0),
                Position(1, 0),
                Position(0, 1)
            )
        )

        randomNumberProviderMock.mockNextRandom(0)

        val actual = foodProducer.spawnFood(width, height, snake)

        assertEquals(Food.Normal(expectedFoodPosition), actual)
    }

    @Test
    fun `should correctly spawn food when there is only one place available in first map half`() {
        createFoodProducer()

        val width = 2
        val height = 2

        val expectedFoodPosition = Position(0, 0)

        val snake = createSnake(
            listOf(
                Position(1, 0),
                Position(1, 1),
                Position(2, 0)
            )
        )

        randomNumberProviderMock.mockNextRandom(0)

        val actual = foodProducer.spawnFood(width, height, snake)

        assertEquals(Food.Normal(expectedFoodPosition), actual)
    }
}
