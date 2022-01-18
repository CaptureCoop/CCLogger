package org.capturecoop.cclogger;

import org.capturecoop.ccutils.utils.LinkUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

public class DebugConsole extends JFrame{
    private final JTextPane content = new JTextPane();
    private final ArrayList<WindowListener> listeners = new ArrayList<>();
    private int fontSize = 20;
    private final int scrollSpeed = 20;
    private final boolean[] keys = new boolean[4096];
    private BufferedImage icon;

    public DebugConsole () {
        setTitle("Debug Console");
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int)(size.getWidth()/2), (int)(size.getHeight()/2));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        try {
            icon = ImageIO.read(DebugConsole.class.getResource("/org/capturecoop/cclogger/resources/console.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        setIconImage(icon);
        content.setOpaque(true);
        content.setContentType("text/html");
        content.setEditable(false);
        content.setBackground(Color.BLACK);
        content.setFont(new Font("Consolas", Font.PLAIN, fontSize));

        JScrollPane scrollPane = new JScrollPane(content, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.setWheelScrollingEnabled(false);
        add(scrollPane);

        content.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                keys[keyEvent.getKeyCode()] = true;
                if(keys[KeyEvent.VK_PLUS] || keys[KeyEvent.VK_ADD])
                    fontSize++;
                else if(keys[KeyEvent.VK_MINUS] || keys[KeyEvent.VK_SUBTRACT])
                    fontSize--;
                content.setFont(new Font(content.getFont().getName(), Font.PLAIN, fontSize));
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
                keys[keyEvent.getKeyCode()] = false;
            }
        });

        addMouseWheelListener(e -> {
            if(keys[KeyEvent.VK_CONTROL]) {
                switch(e.getWheelRotation()) {
                    case -1: fontSize++; break;
                    case 1: fontSize--; break;
                }
            } else {
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getValue() + (e.getWheelRotation() * scrollSpeed));
            }
            content.setFont(new Font(content.getFont().getName(), Font.PLAIN, fontSize));
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for(WindowListener listener : listeners)
                    listener.windowClosing(e);
                dispose();
            }
        });

        content.addHyperlinkListener(hle -> {
            if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
                LinkUtils.openLink(hle.getURL().toString());
            }
        });

        setVisible(true);
    }

    public void update() {
        if(CCLogger.getHTMLLog() != null) {
            content.setText("<html>" + CCLogger.getHTMLLog() + "</html>");
            repaint();
        }
    }

    public void addCustomWindowListener(WindowListener listener) {
        listeners.add(listener);
    }
}
