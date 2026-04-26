package ums.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

public class UITheme {
    // Colour palette
    public static final Color PRIMARY       = new Color(25,  118, 210);   // blue
    public static final Color PRIMARY_DARK  = new Color(13,  71, 161);
    public static final Color SECONDARY     = new Color(66,  165, 245);
    public static final Color SUCCESS       = new Color(56,  142,  60);
    public static final Color WARNING       = new Color(245, 124,   0);
    public static final Color DANGER        = new Color(211,  47,  47);
    public static final Color BG            = new Color(245, 247, 250);
    public static final Color SIDEBAR_BG    = new Color(21,  101, 192);
    public static final Color SIDEBAR_TXT   = Color.WHITE;
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color TEXT_PRIMARY  = new Color(33,  33,  33);
    public static final Color TEXT_MUTED    = new Color(117, 117, 117);
    public static final Color TABLE_HEADER  = new Color(227, 242, 253);

    // Fonts
    public static final Font TITLE    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font HEADING  = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font BUTTON   = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font SIDEBAR  = new Font("Segoe UI", Font.BOLD,  13);

    // Reusable borders
    public static Border cardBorder()  { return BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(207, 226, 255), 1), BorderFactory.createEmptyBorder(12,16,12,16)); }
    public static Border emptyBorder(int tb, int lr) { return BorderFactory.createEmptyBorder(tb,lr,tb,lr); }

    /** Style a JButton as a primary action button */
    public static JButton primaryBtn(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY); b.setForeground(Color.WHITE);
        b.setFont(BUTTON); b.setFocusPainted(false);
        b.setBorderPainted(false); b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(8,18,8,18));
        return b;
    }

    /** Style a JButton as a danger button */
    public static JButton dangerBtn(String text) {
        JButton b = primaryBtn(text);
        b.setBackground(DANGER);
        return b;
    }

    /** Style a JButton as a success button */
    public static JButton successBtn(String text) {
        JButton b = primaryBtn(text);
        b.setBackground(SUCCESS);
        return b;
    }

    /** Apply look-and-feel once at startup */
    public static void applyLAF() {
        try { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
        catch (Exception ignored) {}
    }

    /** Create a titled section panel */
    public static JPanel sectionPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(SECONDARY, 1), title,
                        javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP,
                        HEADING, PRIMARY),
                BorderFactory.createEmptyBorder(8, 10, 10, 10)));
        return p;
    }

    /** Create a styled JScrollPane */
    public static JScrollPane scroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(new Color(207, 226, 255)));
        return sp;
    }

    /** Create a labelled field row */
    public static JPanel labelField(String labelText, JComponent field) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        row.setBackground(CARD_BG);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(BODY); lbl.setForeground(TEXT_PRIMARY);
        lbl.setPreferredSize(new Dimension(140, 28));
        row.add(lbl); row.add(field);
        return row;
    }
}
