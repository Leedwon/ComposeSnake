class Snake(startingPosition: Position) {

    companion object {
        fun from(positions: List<Position>): Snake {
            val snake = Snake(positions[0])
            positions.drop(1).forEach { position ->
                snake.append(position)
            }
            return snake
        }
    }

    private var _head: Node =
        Node(
            position = startingPosition,
            next = null
        )

    val head: Node
        get() = _head

    fun appendHead(position: Position) {
        _head = Node(
            position = position,
            next = _head
        )
    }

    fun append(position: Position) {
        var last = head
        while (last.next != null) {
            last = last.next!!
        }

        last.next = Node(
            position = position,
            next = null
        )
    }

    fun removeLast() {
        if (head.next == null) error("Can't remove head")

        var last = head
        while (last.next != null) {
            if (last.next?.next == null) {
                last.next = null
                break
            }
            last = last.next!!
        }
    }

    fun toList(): List<Position> {
        val result = mutableListOf<Position>()
        var running: Node? = this.head

        while (running != null) {
            result.add(running.position)
            running = running.next
        }

        return result
    }

    data class Node(
        val position: Position,
        var next: Node?
    )
}
