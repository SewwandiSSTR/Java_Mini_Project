package ums.gui;

import ums.dao.IUserDAO;
import ums.dao.UserDAOImpl;
import ums.model.User;
import ums.roles.*;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final IUserDAO userDAO = new UserDAOImpl();
    private JTextField     idField;
    private JPasswordField pwField;
    private JLabel         statusLabel;

    public LoginFrame() {
        super("TEC-MIS  •  Login");
        UITheme.applyLAF();
        buildUI();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 540);
        setLocationRelativeTo(null);
        setResizable(false);
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);

        JPanel accentBar = new JPanel();
        accentBar.setBackground(UITheme.PRIMARY);
        accentBar.setPreferredSize(new Dimension(8, 0));
        root.add(accentBar, BorderLayout.WEST);

        JPanel card = new JPanel();
        card.setBackground(UITheme.CARD_BG);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        JLabel logo = new JLabel("🎓 TEC-MIS");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logo.setForeground(UITheme.PRIMARY);
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Technical Education Campus – Management Information System");
        sub.setFont(UITheme.SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        sub.setAlignmentX(CENTER_ALIGNMENT);

        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setForeground(new Color(207, 226, 255));

        JLabel idLbl = new JLabel("User ID");
        idLbl.setFont(UITheme.BODY);
        idLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        idField = new JTextField();
        idField.setFont(UITheme.BODY);
        idField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        idField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel pwLbl = new JLabel("Password");
        pwLbl.setFont(UITheme.BODY);
        pwLbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        pwField = new JPasswordField();
        pwField.setFont(UITheme.BODY);
        pwField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        pwField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton loginBtn = UITheme.primaryBtn("  Sign In  ");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> doLogin());
        pwField.addActionListener(e -> doLogin());

        statusLabel = new JLabel(" ");
        statusLabel.setFont(UITheme.SMALL);
        statusLabel.setForeground(UITheme.DANGER);
        statusLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Demo hint
        JLabel hint = new JLabel("Try: Admin/Fot/01 | Admin1@fot");
        hint.setFont(UITheme.SMALL);
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setAlignmentX(CENTER_ALIGNMENT);

        card.add(logo);
        card.add(Box.createVerticalStrut(4));
        card.add(sub);
        card.add(Box.createVerticalStrut(20));
        card.add(sep);
        card.add(Box.createVerticalStrut(24));
        card.add(idLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(idField);
        card.add(Box.createVerticalStrut(16));
        card.add(pwLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(pwField);
        card.add(Box.createVerticalStrut(24));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(statusLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(hint);

        root.add(card, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void doLogin() {
        String id = idField.getText().trim();
        String pw = new String(pwField.getPassword()).trim();

        if (id.isEmpty() || pw.isEmpty()) {
            statusLabel.setText("⚠  Please enter both User ID and Password.");
            return;
        }

        User user = userDAO.authenticate(id, pw);
        if (user == null) {
            statusLabel.setText("✗  Invalid credentials. Please try again.");
            pwField.setText("");
            return;
        }

        dispose();
        openDashboard(user);
    }

    private void openDashboard(User user) {
        String displayName = loadDisplayName(user.getUserId(), user.getRole());
        String dbRole      = user.getRole();   // exact DB value e.g. "Dean", "TO", "Student"
        SwingUtilities.invokeLater(() -> {
            switch (dbRole) {
                case "Admin" -> {
                    Admin a = new Admin(user.getUserId(), displayName);
                    a.setDbRole("Admin");
                    new AdminDashboardFrame(a);
                }
                case "Lecturer" -> {
                    Lecturer l = new Lecturer(user.getUserId(), displayName);
                    l.setDbRole("Lecturer");
                    new LecturerDashboardFrame(l);
                }
                case "TO" -> {
                    TechnicalOfficer t = new TechnicalOfficer(user.getUserId(), displayName);
                    t.setDbRole("Technical Officer");
                    new TODashboardFrame(t);
                }
                case "Student" -> {
                    Undergraduate u = new Undergraduate(user.getUserId(), displayName);
                    u.setDbRole("Student");
                    new UGDashboardFrame(u);
                }
                default -> JOptionPane.showMessageDialog(null,
                        "Unknown role: " + dbRole +
                                "\nPlease contact the system administrator.");
            }
        });
    }

    /** Load the person's real name from their role table. Falls back to userId. */
    private String loadDisplayName(String userId, String role) {
        try (java.sql.Connection c = ums.db.DBConnection.getInstance().getConnection()) {
            String sql = switch (role) {
                case "Admin"    -> "SELECT CONCAT(F_name,' ',L_name) AS name FROM Admin WHERE Admin_id=?";
                case "Lecturer"        -> "SELECT Fullname AS name FROM Lecturer WHERE Lec_id=?";
                case "TO"       -> "SELECT CONCAT(F_Name,' ',L_Name) AS name FROM Technical_officer WHERE Tec_id=?";
                case "Student"  -> "SELECT CONCAT(Fname,' ',Lname) AS name FROM Student WHERE Reg_no=?";
                default         -> null;
            };
            if (sql == null) return userId;
            try (java.sql.PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, userId);
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String name = rs.getString("name");
                    return (name != null && !name.isBlank()) ? name : userId;
                }
            }
        } catch (Exception ignored) {}
        return userId;
    }
}
