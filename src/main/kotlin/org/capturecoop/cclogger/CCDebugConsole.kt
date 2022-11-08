package org.capturecoop.cclogger

import org.capturecoop.ccutils.utils.CCLinkUtils
import java.awt.Color
import java.awt.Font
import java.awt.Toolkit
import java.awt.event.*
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.event.HyperlinkEvent

class CCDebugConsole: JFrame() {
    private val content = JTextPane()
    private val listeners = ArrayList<WindowListener>()
    private var fontSize = 20
    private val scrollSpeed = 20
    private val keys = Array(4096) { false }
    private val icon: BufferedImage

    init {
        title = "Debug Console"
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        Toolkit.getDefaultToolkit().screenSize.also {
            setSize(it.width / 2, it.height / 2)
        }
        icon = ImageIO.read(CCDebugConsole::class.java.getResource("/org/capturecoop/cclogger/resources/console.png"))
        iconImage = icon
        content.isOpaque = true
        content.contentType = "text/html"
        content.isEditable = false
        content.background = Color.BLACK
        content.font = Font("Consolas", Font.PLAIN, fontSize)
        //https://github.com/JFormDesigner/FlatLaf/issues/165
        //This fixes an issue with the fonts being wonky and unable to change
        content.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)

        JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER).also { scrollPane ->
            scrollPane.border = BorderFactory.createEmptyBorder()
            scrollPane.verticalScrollBar.unitIncrement = 10
            scrollPane.isWheelScrollingEnabled = false
            add(scrollPane)

            addMouseWheelListener { e ->
                if(keys[KeyEvent.VK_CONTROL]) {
                    when(e.wheelRotation) {
                        -1 -> fontSize++
                        1 -> fontSize--
                    }
                } else {
                    scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.value + (e.wheelRotation * scrollSpeed)
                }
                content.font = Font(content.font.name, Font.PLAIN, fontSize)
            }
        }

        content.addKeyListener(object: KeyAdapter() {
            override fun keyPressed(keyEvent: KeyEvent) {
                keys[keyEvent.keyCode] = true
                if(keys[KeyEvent.VK_PLUS] || keys[KeyEvent.VK_ADD])
                    fontSize++
                else if(keys[KeyEvent.VK_MINUS] || keys[KeyEvent.VK_SUBTRACT])
                    fontSize--
                content.font = Font(content.font.name, Font.PLAIN, fontSize)
            }

            override fun keyReleased(keyEvent: KeyEvent) {
                keys[keyEvent.keyCode] = false
            }
        })

        addWindowListener(object: WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                listeners.forEach { it.windowClosing(e) }
                dispose()
            }
        })

        content.addHyperlinkListener { hle ->
            if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.eventType)) {
                CCLinkUtils.openLink(hle.url.toString())
            }
        }

        isVisible = true
    }

    fun update() {
        content.text = "<html>${CCLogger.htmlLog}</html>"
        repaint()
    }
}