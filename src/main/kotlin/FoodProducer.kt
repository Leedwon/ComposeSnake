interface FoodProducer {
    fun spawnFood(width: Int, height: Int, snake: Snake): Food
}

class FoodProducerImpl(private val randomNumberProvider: RandomNumberProvider) : FoodProducer {
    override fun spawnFood(width: Int, height: Int, snake: Snake): Food {
        val snakeList = snake.toList()
        val randRange = width * height - snakeList.size

        val randIndex = randomNumberProvider.getRandomNumber(randRange)
        val foodPosition = positionFromIndex(
            index = randIndex,
            width = width
        )

        val position = if (snakeList.contains(foodPosition)) {
            val foodInSnakeIndex = snakeList.indexOf(foodPosition)
            val newIndex = if (randIndex > randRange / 2) {
                randIndex - (snakeList.size - foodInSnakeIndex)
            } else {
                randIndex + foodInSnakeIndex + 1
            }

            positionFromIndex(
                index = newIndex,
                width = width
            )

        } else {
            foodPosition
        }

        return when(randomNumberProvider.getRandomNumber(100)) {
            in 0 until 50 -> Food.Normal(position)
            in 50 until 66 -> Food.Accelerate(position)
            in 66 until 82 -> Food.Decelerate(position)
            else -> Food.Reverse(position)
        }

    }
}
