interface FoodProducer {
    fun spawnFood(width: Int, height: Int, snake: Snake): Position
}

class FoodProducerImpl(private val randomNumberProvider: RandomNumberProvider) : FoodProducer {
    override fun spawnFood(width: Int, height: Int, snake: Snake): Position {
        val snakeList = snake.toList()
        val randRange = width * height - snakeList.size

        val randIndex = randomNumberProvider.getRandomNumber(randRange)
        val foodPosition = positionFromIndex(
            index = randIndex,
            width = width
        )

        return if (snakeList.contains(foodPosition)) {
            val foodInSnakeIndex = snakeList.size - snakeList.indexOf(foodPosition)
            val newIndex = if (randIndex > randRange / 2) {
                randIndex - foodInSnakeIndex
            } else {
                randIndex + foodInSnakeIndex
            }

            positionFromIndex(
                index = newIndex,
                width = width
            )

        } else {
            foodPosition
        }
    }
}