import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RandomNumberProviderMock : RandomNumberProvider {
    private var nextRandom: Int = 0

    fun mockNextRandom(nextRandomInt: Int) {
        nextRandom = nextRandomInt
    }

    override fun getRandomNumber(until: Int): Int {
        return nextRandom
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
}
