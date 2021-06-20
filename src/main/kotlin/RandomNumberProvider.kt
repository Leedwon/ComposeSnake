import kotlin.random.Random

interface RandomNumberProvider {
    fun getRandomNumber(until: Int): Int
}

class RandomNumberProviderImpl: RandomNumberProvider {
    override fun getRandomNumber(until: Int): Int = Random.nextInt(until)
}