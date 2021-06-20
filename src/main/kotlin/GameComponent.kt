object GameComponent {
    private val randomNumberProvider: RandomNumberProvider
        get() = RandomNumberProviderImpl()

    val foodProducer: FoodProducer
        get() = FoodProducerImpl(randomNumberProvider)
}