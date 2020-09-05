package frontend

import backend.Node
import backend.World
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.event.*
import javax.swing.*

/*
    The screen to actually display the algorithm working. Is in charge of drawing all of the items: blocks, start, end,
    and algorithm attributes to the screen for the end user to see.
*/
class Screen : JPanel(), ActionListener, MouseMotionListener, MouseListener {

    private val frame = JFrame("Pathfinding")      // The Frame for our screen to reside in
    private var timer = Timer(5, this)     // The timer used to regulate screen refreshes
    private var toggleDraw: ContinuousDraw? = null      // A helper class to implement continuous drawing when mouse is held
    var mouseX = 0                                      // The current position of the mouse's x coordinate
    var mouseY = 0                                      // The current position of the mouse's y coordinate
    val world = World(100)                         // The world for the screen to use
    val tileSize = 900 / world.length                   // The tile size for the world. Gotten by dividing 900 / 50
    var actionOnClick: String? = null                   // The current action to use on a mouse click

    init {
        // Set the look and feel of the current window to be the system theme
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        // Set the preferred size of the window and add the required listeners
        this.preferredSize = Dimension(900, 900)
        addMouseMotionListener(this)
        addMouseListener(this)
        this.isFocusable = true
        // Set all of the important attributes for the JFrame before we start
        frame.setLocation(100, 100)
        frame.jMenuBar = MenuBar(this)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isResizable = false
        // Set the content pane and then pack it to get correct dimensions on the window
        frame.contentPane = this
        frame.pack()
        // Make it visible
        frame.isVisible = true
        // Start the timer
        timer.start()
    }

    companion object {
        @JvmStatic
        // Create a companion object of itself for the screen to be able to self instanciate
        fun main(args: Array<String>) {
            Screen()
        }
    }

    // Standard override methods that must be here to make the interfaces happy
    override fun mouseMoved(e: MouseEvent) {
        mouseX = e.x; mouseY = e.y
    }

    override fun mouseDragged(e: MouseEvent) {
        mouseX = e.x; mouseY = e.y
    }

    override fun mouseClicked(e: MouseEvent) {}
    override fun mouseEntered(e: MouseEvent) {}
    override fun mouseExited(e: MouseEvent) {}

    override fun mousePressed(e: MouseEvent) {
        toggleDraw = ContinuousDraw(this)
    }

    override fun mouseReleased(e: MouseEvent) {
        toggleDraw?.cancel()
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        // If the algorithm isn't finished and there is still the possibility of a path then attempt to find it
        world.runAlgorithm()
        // Renders the current state of the algorithm
        renderAlgorithm(g)
        // Renders the current state of the world
        renderWorld(g)
    }

    private fun renderAlgorithm(g: Graphics) {
        // If the algorithm is done then show what it found to be the optimal path
        when {
            world.algorithm?.isDone == true -> {
                for (node in world.algorithm!!.bestPath())
                    drawTile(g, node, Color.BLUE)
            }
            world.algorithm?.isImpossible == true -> {
                world.algorithm = null
                JOptionPane.showMessageDialog(this,
                        "There is no valid path to find for the given world state", "Pathfinding Impossible",
                        JOptionPane.WARNING_MESSAGE)
            }
            // If the algorithm isn't done and it is non-null then draw its current open and closed lists
            world.algorithm != null -> {
                for (node in world.algorithm!!.open)
                    drawTile(g, node, Color.RED)
                for (node in world.algorithm!!.closed)
                    drawTile(g, node, Color.GREEN)
            }
        }
    }

    private fun renderWorld(g: Graphics) {
        // Then draw all of the blocks for the current world
        for (block in world.blocks)
            drawTile(g, block, Color.BLACK)
        // Draw the start and end points for the world if they are non-null
        if (world.start != null)
            drawTile(g, world.start!!, Color.ORANGE)
        if (world.end != null)
            drawTile(g, world.end!!, Color.MAGENTA)
        // Finally draw the overlay grid to the screen to show the individual nodes graphically
        g.color = Color.BLACK
        for (x in 0 until width step tileSize) {
            for (y in 0 until height step tileSize) {
                g.drawLine(x, 0, x, height)
                g.drawLine(0, y, width, y)
            }
        }
    }

    private fun drawTile(g: Graphics, node: Node, color: Color) {
        // Get the location of the node onscreen and change the color to the desired color before drawing
        val (x, y) = node.location; g.color = color
        g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize)
    }

    override fun actionPerformed(e: ActionEvent) {
        repaint()
    }      // Redraw the frame every 17ms for ~60fps
}

/*
    Below is a simple helper class that allows for use to have continuous drawing for the free placement of elements.
    Without this the user would have to click multiple times to place multiple blocks, however this is annoying so we
    just want to be able to press and hold to continuously draw elements. However swing doesn't support this natively so
    we need to take matters into our own hands. So we make a non swing timer task and have it self instanciate a timer to
    call itself to check for continuous key presses. This timer is activated on key press and ends on key release.
*/
class ContinuousDraw(private val screen: Screen) : java.util.TimerTask() {

    private val timer = java.util.Timer()       // A new non swing timer

    init {
        // Start a scheduled task on the timer that is ourselves that is classed at the same rate is screen redraws
        timer.scheduleAtFixedRate(this, 0, 5)
    }

    override fun run() {
        // When running make a par of the current mouse position and change the nodes state that contains the mouse
        val selected = Pair(screen.mouseX / screen.tileSize, screen.mouseY / screen.tileSize)
        screen.world.changeNodeState(selected, screen.actionOnClick)
    }
}