package backend

import backend.pathfinders.Algorithm
import kotlin.math.abs
import kotlin.math.pow

/*
    A data class for our world instance that simply holds the data for each node on the world grid such as its movement
    cost values and its location.
*/
data class Node constructor(val location: Pair<Int, Int>) {
    var parent: Node? = null                                // The parent node to this node for backtracking
    var hValue = 0.0                                        // The heuristic value for this node; estimate of move cost from this node to end
    var gValue = 0.0                                        // The G value for this node; move cost to this node from another node
    var isExplored = false                                  // A flag to let each node know if it is in the closed list of an algorithm
    var onFrontier = false                                  // A flag to let each node know if it is in the closed list of an algorithm
}

/*
    The actual world class that works as a 2D array of node classes. It acts as the primary backend item for our system
    as it rules over all of the nodes, altering them and calculating their costs for the algorithms to use. It also keeps
    track of the start and end nodes along with all of the blocks in the current instance of the program.
*/
class World(side: Int) {

    var grid = Array(side) { y -> Array(side) { x -> Node(Pair(x, y)) } }       // Initialize a 50 by 50 array of Nodes using indices
    val length = side                                                           // The size of a side of the world grid
    var start: Node? = null                                                     // The start node of the world grid
    var end: Node? = null                                                       // The end node of the world grid
    var algorithm: Algorithm? = null                                            // The algorithm currently solving the grid
    var blocks = mutableListOf<Node>()                                          // The list of block currently present in the world

    private fun nodeAtLoc(location: Pair<Int, Int>): Node {
        // Given a location in x, y we will return the corresponding node in that location
        val (x, y) = location
        return grid[y][x]
    }

    // Check if the given parent is better than the one we were already assigned
    fun isBetterParent(test: Node, child: Node): Boolean {
        return test.gValue < child.parent?.gValue ?: -1.0
    }

    fun calculateH(start: Node, end: Node) {
        // Get the location of both the instance we are working in and the given instance
        val (x, y) = start.location
        val (nx, ny) = end.location
        // Then use the pythagorean theorem to get the g cost for moving here
        start.hValue = (abs(nx - x).toDouble().pow(2)
                + abs(ny - y).toDouble().pow(2)).pow(.5)
    }

    fun calculateG(node: Node) {
        // Create a mutable list containing only this node
        val path = mutableListOf(node)
        // Then backtrack through its parents to find the entire path to that point
        while (path.last().parent != null)
            path.add(path.last().parent!!)
        // The set g to the number of move made
        node.gValue = path.count().toDouble()
    }

    fun changeNodeState(node: Pair<Int, Int>, type: String?) {
        // Get the current node we are changing the state of
        val change = nodeAtLoc(node)
        // Switch statement to determine what we should do with the given node
        when (type) {
            "Block" -> blocks.add(change)               // Turn it into a block
            "Start" -> start = change                   // Make it the start node
            "End" -> end = change                       // Make it the end node
            else -> println("No such operation")        // Invalid command
        }
        // After we alter the node state make sure that we haven't accidentally set an endpoint to a block node
        if (blocks.contains(change) && type != "Block")
            blocks.remove(change)
    }

    fun adjacentNodes(node: Node): MutableList<Node> {
        // Make an empty list to add adjacent nodes to
        val adjacent = mutableListOf<Node>()
        // Gets the x and y of the node in questions
        val (x, y) = node.location
        // Gets the node to the left if it exists
        if (x - 1 >= 0 && !blocks.contains(grid[y][x - 1]))
            adjacent.add(grid[y][x - 1])
        // Gets the node above if it exists
        if (y - 1 >= 0 && !blocks.contains(grid[y - 1][x]))
            adjacent.add(grid[y - 1][x])
        // Gets the node to below if it exists
        if (y + 1 < length && !blocks.contains(grid[y + 1][x]))
            adjacent.add(grid[y + 1][x])
        // Gets the node the the right if it exists
        if (x + 1 < length && !blocks.contains(grid[y][x + 1]))
            adjacent.add(grid[y][x + 1])
        // Returns the list of adjacent nodes
        return adjacent
    }

    fun resetNodesState() {
        // Iterate over every node of the grd and reset it to its default state
        for (row in grid) {
            for (node in row) {
                node.hValue = 0.0
                node.gValue = 0.0
                node.parent = null
                node.isExplored = false
                node.onFrontier = false
            }
        }
    }

    fun fullReset() {
        // Rests the world's current algorithm to null
        algorithm = null
        // Iterate over every node in the world and reset its attributes to their default state
        resetNodesState()
        // Resets the states of the blocks, start, and end nodes
        blocks.clear()
        start = null
        end = null
    }

    fun runAlgorithm() {
        // Make sure the algorithm is either not done or still has more paths to explore
        if (algorithm?.isDone == false && algorithm?.isImpossible == false)
            algorithm?.findPath()
    }
}