package ui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

// Plain, flat colours and the font size used everywhere (no gradients or glow).
public class Theme {

    // CHANGE THIS NUMBER to make the whole application's text bigger or smaller.
    public static final int FONT_SIZE = 16;

    // Height of one row in tables and the tree
    public static final int ROW_HEIGHT = FONT_SIZE + 12;

    public static final Color HEADER_BACKGROUND = new Color(0, 51, 102);   // dark blue
    public static final Color HEADER_TEXT = Color.WHITE;

    // Makes every Swing component (labels, buttons, menus, tables...) use FONT_SIZE.
    // Call it once in Main, before any window is created.
    public static void setGlobalFont() {
        ArrayList<Object> keys = new ArrayList<Object>();
        Enumeration<Object> all = UIManager.getDefaults().keys();
        while (all.hasMoreElements()) {
            keys.add(all.nextElement());
        }
        for (Object key : keys) {
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                Font old = (Font) value;
                UIManager.put(key, new FontUIResource(old.deriveFont(old.getStyle(), (float) FONT_SIZE)));
            }
        }
    }

    // Creates the dark blue title bar at the top of a window
    public static JLabel makeHeader(String text) {
        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setBackground(HEADER_BACKGROUND);
        label.setForeground(HEADER_TEXT);
        label.setFont(new Font("SansSerif", Font.BOLD, FONT_SIZE + 6));
        label.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        return label;
    }
}
