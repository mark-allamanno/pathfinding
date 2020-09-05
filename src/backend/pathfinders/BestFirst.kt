package backend.pathfinders

import backend.Node
import backend.World

class BestFirst(world: World) : Algorithm(world) {

    override fun costMap(): HashMap<Double, Node> {
        // For the Best First algorithm we set the cost of each node equal to only its h value
        val map = HashMap<Double, Node>()
        for (node in open)
            map[node.hValue] = node
        return map
    }

    override fun updateNodes(node: Node) {
        // Iterate over all nodes adjacent to the current one to update their state
        for (adjacent in world.adjacentNodes(node))
        // If the adjacent node isn't in the open and closed list then update its state
            if (!adjacent.isExplored && !adjacent.onFrontier) {
                // Add the node to the open list and set its parent to the current node and set onFrontier flag
                open.add(adjacent)
                adjacent.parent = node
                adjacent.onFrontier = true
            }
        // Then pop the node from the open list and add it to the closed list
        open.remove(node)
        closed.add(node)
        // Set the flags on the node so we can skip brute force searches for them in open/ closed lists
        node.isExplored = true
        node.onFrontier = false
    }
}