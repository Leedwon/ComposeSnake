class Snake(startingPosition: Position) {

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

    data class Node(
        val position: Position,
        var next: Node?
    )
}
