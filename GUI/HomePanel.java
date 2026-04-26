package ums.gui.panels;

import ums.gui.UITheme;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HomePanel extends JPanel {

    public HomePanel(String role, String userName) {
        setLayout(new BorderLayout(0, 20));
        setBackground(UITheme.BG);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Welcome card
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 226, 255), 1),
                BorderFactory.createEmptyBorder(30, 36, 30, 36)));

        String greeting = getGreeting();
        JLabel welcome = new JLabel(greeting + ", " + userName + "!");
        welcome.setFont(UITheme.TITLE);
        welcome.setForeground(UITheme.PRIMARY);

        JLabel subtitle = new JLabel("You are logged in as  •  " + role);
        subtitle.setFont(UITheme.BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy  •  HH:mm"));
        JLabel dateLabel = new JLabel(now);
        dateLabel.setFont(UITheme.SMALL);
        dateLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setBackground(UITheme.CARD_BG);
        textBlock.add(welcome);
        textBlock.add(Box.createVerticalStrut(6));
        textBlock.add(subtitle);
        textBlock.add(Box.createVerticalStrut(10));
        textBlock.add(dateLabel);

        card.add(textBlock, BorderLayout.CENTER);
        add(card, BorderLayout.NORTH);

        // Info strip
        JPanel infoRow = new JPanel(new GridLayout(1, 3, 16, 0));
        infoRow.setBackground(UITheme.BG);
        infoRow.add(infoCard("🎓", "System", "MIS System"));
        infoRow.add(infoCard("👤", "Role", role));
        infoRow.add(infoCard("🏫", "Institution", "Faculty of Technology"));
        add(infoRow, BorderLayout.CENTER);
    }

    private JPanel infoCard(String icon, String label, String value) {
        JPanel p = new JPanel(new BorderLayout(8, 4));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 226, 255), 1),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        JLabel ico = new JLabel(icon + "  " + label);
        ico.setFont(UITheme.SMALL);
        ico.setForeground(UITheme.TEXT_MUTED);
        JLabel val = new JLabel(value);
        val.setFont(UITheme.HEADING);
        val.setForeground(UITheme.TEXT_PRIMARY);
        p.add(ico, BorderLayout.NORTH);
        p.add(val, BorderLayout.CENTER);
        return p;
    }

    private String getGreeting() {
        int hour = LocalDateTime.now().getHour();
        if (hour < 12) return "Good Morning";
        if (hour < 17) return "Good Afternoon";
        return "Good Evening";
    }
}
