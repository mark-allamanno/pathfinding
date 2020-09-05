package frontend

import backend.World
import backend.pathfinders.BestFirst
import backend.pathfinders.Dijkstra
import backend.pathfinders.aStar
import java.awt.event.ActionEvent
import javax.swing.AbstractAction
import javax.swing.JMenu
import javax.swing.JMenuBar
import kotlin.math.roundToInt


class MenuBar(screen: Screen) : JMenuBar() {

    private val actions = JMenu("Grid Manipulations")           // Menu for Grid Manipulations ie adding stuff
    private val items = JMenu("Items")                          // Sub menu for the specific manipulations like adding blocks
    private val algorithms = JMenu("Algorithms")                // Menu for all the algorithms

    init {
        // Loop to add all of the grid manipulations to the items dropdown menu and add that menu to the manipulations menu
        for (elem in arrayOf("Start", "End", "Block"))
            items.add(ItemButton(elem, screen))
        actions.add(items)
        // Add the random terrain generation to the manipulations menu along with a clear screen button
        actions.add(RandomGenerateButton(screen.world))
        actions.add(ClearScreen(screen.world))
        // Add the actions menu to the menu bar
        this.add(actions)
        // Add the algorithms to the algorithms dropdown menu and add the dropdown menu to the menu bar
        for (elem in arrayOf("A*", "Best First", "Dijkstra"))
            algorithms.add(AlgorithmButton(elem, screen.world))
        this.add(algorithms)
    }
}

/*
    Below are the implementations for the Menu Bar buttons what have various actions such as the button for
    choosing algorithms or for creating a randomized grid to work with.
*/
class AlgorithmButton(private val algorithm: String, private val world : World) : AbstractAction(algorithm) {

    override fun actionPerformed(e: ActionEvent) {
        // First reset all the nodes in the world but don't clear the block list
        world.resetNodesState()
        // When clicked generate an instance of ourselves as the new algorithm for the screen
        if (world.start != null && world.end != null) {
            when (algorithm) {
                "A*" -> world.algorithm = aStar(world)                  // Create a new A* if we are A*
                "Best First" -> world.algorithm = BestFirst(world)      // Create new Best First if we are Best First
                "Dijkstra" -> world.algorithm = Dijkstra(world)         // Create a new Dijkstra if we are Dijkstra
            }
        }
    }
}

class RandomGenerateButton(private val world: World) : AbstractAction("Generate Random Grid") {

    override fun actionPerformed(e: ActionEvent) {
        // Resets the world and clears the algorithm if it is not null and invoke the garbage collector to prevent heap overflow
        if (world.algorithm != null) {
            world.fullReset()
            System.gc()
        }
        // Define the number of blocks we want to generate
        val numBlocks = (world.length * world.length * .4).roundToInt()
        // Generate the amount of random blocks
        for (x in 0 until numBlocks)
            world.changeNodeState(randomPair(world.length), "Block")
        // Generate a random start and end node for the world
        world.changeNodeState(randomPair(world.length), "Start")
        world.changeNodeState(randomPair(world.length), "End")
    }

    private fun randomPair(upperBound: Int): Pair<Int, Int> {
        // Return a random pair of numbers between 0 and the upper bound
        return Pair((0 until upperBound).random(), (0 until upperBound).random())
    }
}

class ItemButton(private val myAction: String, private val screen: Screen) : AbstractAction(myAction) {

    override fun actionPerformed(e: ActionEvent) {
        // Change the action on mouse press for the screen to the value of the button
        screen.actionOnClick = myAction
    }

}

class ClearScreen(private val world: World) : AbstractAction("Clear Screen") {

    override fun actionPerformed(e: ActionEvent) {
        // Resets the worlds state to default
        world.fullReset()
    }
}