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

        assertEquals(foodPosition, actual)
    }

    @Test
    fun `should correctly spawn food with random number overlapping snake`() {
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

        assertEquals(expectedFoodPosition, actualFoodPosition)
    }
}