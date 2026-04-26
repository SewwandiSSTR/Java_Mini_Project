package ums.gui;

import ums.roles.AbstractUser;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Abstract base for all role dashboards.
 * Encapsulates the sidebar-navigation + content-area layout.
 * Subclasses register menu items via registerMenu().
 */
public abstract class BaseDashboardFrame extends JFrame {

    protected final AbstractUser currentUser;
    private   final JPanel       contentArea  = new JPanel(new BorderLayout());
    private   final JPanel       sidebarPanel = new JPanel();
    private   final Map<String, Supplier<JPanel>> menuItems = new LinkedHashMap<>();
    private         JLabel       userInfoLabel;   // header label — updated on username change

    public BaseDashboardFrame(AbstractUser user, String title) {
        super("TEC-MIS  •  " + title);
        this.currentUser = user;
        buildFrame();
    }

    /** Subclasses call this in their constructor before calling finishBuild() */
    protected void registerMenu(String label, Supplier<JPanel> panelFactory) {
        menuItems.put(label, panelFactory);
    }

    /** Subclasses call this after registering all menu items */
    protected void finishBuild() {
        buildSidebar();
        // Show first panel by default
        if (!menuItems.isEmpty()) {
            Supplier<JPanel> first = menuItems.values().iterator().next();
            showPanel(first.get());
        }
        setVisible(true);
    }

    private void buildFrame() {
        setSize(1100, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);

        // Top header bar
        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        // Left sidebar
        sidebarPanel.setBackground(UITheme.SIDEBAR_BG);
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(210, 0));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JScrollPane sideScroll = new JScrollPane(sidebarPanel);
        sideScroll.setBorder(null);
        sideScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sideScroll.setPreferredSize(new Dimension(210, 0));

        // Content
        contentArea.setBackground(UITheme.BG);
        contentArea.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sideScroll, contentArea);
        split.setDividerSize(1);
        split.setEnabled(false);
        root.add(split, BorderLayout.CENTER);

        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(UITheme.PRIMARY_DARK);
        h.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        h.setPreferredSize(new Dimension(0, 56));

        JLabel brand = new JLabel("🎓 TEC-MIS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 18));
        brand.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);

        userInfoLabel = new JLabel(currentUser.getUserName() + "  [" + currentUser.getRole() + "]");
        userInfoLabel.setFont(UITheme.BODY);
        userInfoLabel.setForeground(UITheme.SECONDARY);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UITheme.BUTTON);
        logoutBtn.setBackground(UITheme.DANGER);
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> { dispose(); new LoginFrame(); });

        right.add(userInfoLabel);
        right.add(logoutBtn);

        h.add(brand, BorderLayout.WEST);
        h.add(right, BorderLayout.EAST);
        return h;
    }

    private void buildSidebar() {
        sidebarPanel.removeAll();
        sidebarPanel.add(Box.createVerticalStrut(6));

        for (Map.Entry<String, Supplier<JPanel>> entry : menuItems.entrySet()) {
            JButton btn = new JButton(entry.getKey());
            btn.setFont(UITheme.SIDEBAR);
            btn.setForeground(UITheme.SIDEBAR_TXT);
            btn.setBackground(UITheme.SIDEBAR_BG);
            btn.setOpaque(true);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(UITheme.PRIMARY); }
                public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(UITheme.SIDEBAR_BG); }
            });
            btn.addActionListener(e -> {
                JPanel panel = entry.getValue().get();
                showPanel(panel);
            });
            sidebarPanel.add(btn);
            sidebarPanel.add(Box.createVerticalStrut(2));
        }
        sidebarPanel.add(Box.createVerticalGlue());
        sidebarPanel.revalidate();
    }

    protected void showPanel(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    /** Call this after a username change to update the header label instantly. */
    public void refreshUserLabel() {
        if (userInfoLabel != null) {
            userInfoLabel.setText(currentUser.getUserName() + "  [" + currentUser.getRole() + "]");
            userInfoLabel.revalidate();
            userInfoLabel.repaint();
        }
    }
}
