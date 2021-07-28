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
                randIndex + (snakeList.size - foodInSnakeIndex)
            }

            positionFromIndex(
                index = newIndex,
                width = width
            )

        } else {
            foodPosition
        }

        return when(randomNumberProvider.getRandomNumber(100)) {
            in 0 until 40 -> Food.Normal(position)
            in 40 until 55 -> Food.Accelerate(position)
            in 55 until 70 -> Food.Decelerate(position)
            in 70 until 85 -> Food.Reverse(position)
            else -> Food.GoThroughWalls(position)
        }

    }
}
