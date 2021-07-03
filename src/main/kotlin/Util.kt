import kotlinx.coroutines.flow.MutableStateFlow

fun <T> MutableStateFlow<T>.updateValue(action: (T) -> T) {
    this.value = action.invoke(this.value)
}

fun positionFromIndex(index: Int, width: Int) =
    Position(
        x = index % width,
        y = index / width
    )

fun Position.toIndex(width: Int) = this.x + this.y * width

fun Int.isEven() = this % 2 == 0
fun Int.isOdd() = this % 2 != 0
