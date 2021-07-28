sealed class Food {
    abstract val position: Position

    data class Normal(override val position: Position) : Food()
    data class Accelerate(override val position: Position) : Food()
    data class Decelerate(override val position: Position) : Food()
    data class Reverse(override val position: Position) : Food()
    data class GoThroughWalls(override val position: Position) : Food()
}