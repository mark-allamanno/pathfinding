package backend.pathfinders

import backend.Node
import backend.World

class aStar(world: World) : Algorithm(world) {

    override fun costMap(): HashMap<Double, Node> {
        // For the A* algorithm we set the cost of each node equal to its H + G value and return it
        val map = HashMap<Double, Node>()
        for (node in open)
            map[node.hValue + node.gValue] = node
        return map
    }

    override fun updateNodes(node: Node) {
        // Iterate over all nodes adjacent to the current one to update their state
        for (adjacent in world.adjacentNodes(node)) {
            // If the closed list contains the adjacent nodes then skip it
            if (adjacent.isExplored)
                continue
            // If the node isn't explored or the current node is a better parent then update its stats
            if (!adjacent.onFrontier || world.isBetterParent(node, adjacent)) {
                // Update the parent and calculate the new g cost
                adjacent.parent = node
                world.calculateG(adjacent)
                // If the node hasn't been explored then add it to the open list and set onFrontier flag
                if (!adjacent.onFrontier) {
                    open.add(adjacent)
                    adjacent.onFrontier = true
                }
            }
        }
        // Then pop the node from the open list and add it to the closed list
        open.remove(node)
        closed.add(node)
        // Set the flags on the node so we can skip brute force searches for them in open/ closed lists
        node.onFrontier = false
        node.isExplored = true
    }
}