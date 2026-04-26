package ums.gui.panels;

import ums.dao.IUserDAO;
import ums.dao.UserDAOImpl;
import ums.db.DBConnection;
import ums.gui.BaseDashboardFrame;
import ums.gui.UITheme;
import ums.roles.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class ManageProfilePanel extends JPanel {

    private final AbstractUser        user;
    private final IUserDAO            userDAO    = new UserDAOImpl();
    private final JLabel              photoLabel = new JLabel();
    private final BaseDashboardFrame  parentFrame;

    public ManageProfilePanel(AbstractUser user, BaseDashboardFrame parentFrame) {
        this.user        = user;
        this.parentFrame = parentFrame;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("  My Profile");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(24, 0));
        body.setBackground(UITheme.BG);
        body.add(buildPhotoPanel(), BorderLayout.WEST);
        body.add(buildFormCard(),   BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    // ── Photo panel (all roles) ───────────────────────────────────────────
    private JPanel buildPhotoPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(UITheme.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 226, 255), 1),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)));
        p.setPreferredSize(new Dimension(200, 0));

        photoLabel.setPreferredSize(new Dimension(150, 150));
        photoLabel.setMinimumSize(new Dimension(150, 150));
        photoLabel.setMaximumSize(new Dimension(150, 150));
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setVerticalAlignment(SwingConstants.CENTER);
        photoLabel.setBorder(BorderFactory.createLineBorder(new Color(207, 226, 255), 2));
        photoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadPhoto();

        JLabel nameLabel = new JLabel(user.getUserId());
        nameLabel.setFont(UITheme.SMALL);
        nameLabel.setForeground(UITheme.TEXT_MUTED);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roleLabel = new JLabel(user.getRole());
        roleLabel.setFont(UITheme.BUTTON);
        roleLabel.setForeground(UITheme.PRIMARY);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton changePhotoBtn = UITheme.primaryBtn("📷 Change Photo");
        changePhotoBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        changePhotoBtn.addActionListener(e -> changePhoto());

        p.add(photoLabel);
        p.add(Box.createVerticalStrut(12));
        p.add(nameLabel);
        p.add(Box.createVerticalStrut(4));
        p.add(roleLabel);
        p.add(Box.createVerticalStrut(16));
        p.add(changePhotoBtn);
        return p;
    }

    // ── Form card — fields vary by role ───────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(207, 226, 255), 1),
                BorderFactory.createEmptyBorder(28, 36, 28, 36)));

        // ── Identity info (read-only for everyone) ────────────────────────
        addInfoRow(card, "User ID :", user.getUserId());
        addInfoRow(card, "Role    :", user.getRole());
        card.add(Box.createVerticalStrut(20));
        addDivider(card);
        card.add(Box.createVerticalStrut(20));

        boolean isAdmin     = user instanceof Admin;
        boolean isLecOrTO   = user instanceof Lecturer || user instanceof TechnicalOfficer;
        // Student (Undergraduate) is the remaining case

        if (isAdmin) {
            // ════════════════════════════════════════════════════════
            // ADMIN — full access: username + password + contact email
            // ════════════════════════════════════════════════════════

            addSectionHeader(card, "Change Username");
            JTextField unField = styledField();
            JButton unBtn = UITheme.primaryBtn("Update Username");
            unBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            unBtn.addActionListener(e -> {
                String val = unField.getText().trim();
                if (val.isEmpty()) { showErr("Username cannot be empty."); return; }
                if (userDAO.updateUserName(user.getUserId(), val)) {
                    user.setUserName(val);                  // update in-memory
                    parentFrame.refreshUserLabel();       // refresh header label
                    JOptionPane.showMessageDialog(this, "Username updated successfully.");
                    unField.setText("");
                } else showErr("Failed to update username.");
            });
            card.add(unField); card.add(Box.createVerticalStrut(6));
            card.add(unBtn);   card.add(Box.createVerticalStrut(20));
            addDivider(card);  card.add(Box.createVerticalStrut(20));

            addSectionHeader(card, "Change Email / Contact");
            JTextField adminEmailField = styledField();
            adminEmailField.setText(userDAO.getCurrentEmail(user.getUserId()));
            JButton adminEmailBtn = UITheme.primaryBtn("Update Email");
            adminEmailBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            adminEmailBtn.addActionListener(e -> {
                String val = adminEmailField.getText().trim();
                if (val.isEmpty()) { showErr("Email cannot be empty."); return; }
                if (!val.contains("@")) { showErr("Please enter a valid email address."); return; }
                if (userDAO.updateContact(user.getUserId(), val))
                    JOptionPane.showMessageDialog(this, "Contact email updated successfully.");
                else showErr("Failed to update contact email.");
            });
            card.add(adminEmailField); card.add(Box.createVerticalStrut(6));
            card.add(adminEmailBtn);

        } else if (isLecOrTO) {
            // ════════════════════════════════════════════════════════
            // LECTURER / TO — username + password + profile photo
            // ════════════════════════════════════════════════════════

            addSectionHeader(card, "Change Username");
            JTextField unField = styledField();
            JButton unBtn = UITheme.primaryBtn("Update Username");
            unBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            unBtn.addActionListener(e -> {
                String val = unField.getText().trim();
                if (val.isEmpty()) { showErr("Username cannot be empty."); return; }
                if (userDAO.updateUserName(user.getUserId(), val)) {
                    user.setUserName(val);                  // update in-memory
                    parentFrame.refreshUserLabel();       // refresh header label
                    JOptionPane.showMessageDialog(this, "Username updated successfully.");
                    unField.setText("");
                } else showErr("Failed to update username.");
            });
            card.add(unField); card.add(Box.createVerticalStrut(6));
            card.add(unBtn);   card.add(Box.createVerticalStrut(20));
            addDivider(card);  card.add(Box.createVerticalStrut(20));

            addSectionHeader(card, "Change Password");
            JPasswordField pwField = styledPasswordField();
            JButton pwBtn = UITheme.primaryBtn("Update Password");
            pwBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            pwBtn.addActionListener(e -> {
                String val = new String(pwField.getPassword()).trim();
                if (val.isEmpty()) { showErr("Password cannot be empty."); return; }
                if (userDAO.updatePassword(user.getUserId(), val)) {
                    JOptionPane.showMessageDialog(this, "Password updated successfully.");
                    pwField.setText("");
                } else showErr("Failed to update password.");
            });
            card.add(pwField); card.add(Box.createVerticalStrut(6));
            card.add(pwBtn);   card.add(Box.createVerticalStrut(20));
            addDivider(card);  card.add(Box.createVerticalStrut(20));

            addSectionHeader(card, "Profile Picture");
            JLabel lecPicHint = new JLabel("Use the \"📷 Change Photo\" button on the left to update your photo.");
            lecPicHint.setFont(UITheme.SMALL);
            lecPicHint.setForeground(UITheme.TEXT_MUTED);
            lecPicHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(lecPicHint);

        } else {
            // ════════════════════════════════════════════════════════
            // STUDENT — contact email + profile photo only
            // ════════════════════════════════════════════════════════

            addSectionHeader(card, "Contact Email");
            JTextField contactField = styledField();
            contactField.setText(userDAO.getCurrentEmail(user.getUserId()));
            JButton contactBtn = UITheme.primaryBtn("Update Email");
            contactBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
            contactBtn.addActionListener(e -> {
                String val = contactField.getText().trim();
                if (val.isEmpty()) { showErr("Email cannot be empty."); return; }
                if (!val.contains("@")) { showErr("Please enter a valid email address."); return; }
                if (userDAO.updateContact(user.getUserId(), val))
                    JOptionPane.showMessageDialog(this, "Contact email updated successfully.");
                else showErr("Failed to update contact email.");
            });
            card.add(contactField); card.add(Box.createVerticalStrut(6));
            card.add(contactBtn);   card.add(Box.createVerticalStrut(20));
            addDivider(card);       card.add(Box.createVerticalStrut(20));

            addSectionHeader(card, "Profile Picture");
            JLabel picHint = new JLabel("Use the \"📷 Change Photo\" button on the left to update your photo.");
            picHint.setFont(UITheme.SMALL);
            picHint.setForeground(UITheme.TEXT_MUTED);
            picHint.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(picHint);   card.add(Box.createVerticalStrut(20));
            addDivider(card);    card.add(Box.createVerticalStrut(16));

            // Locked notice for students
            JPanel lockBox = new JPanel();
            lockBox.setLayout(new BoxLayout(lockBox, BoxLayout.Y_AXIS));
            lockBox.setBackground(new Color(255, 245, 245));
            lockBox.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 200, 200), 1),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            lockBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            lockBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

            JLabel lockTitle = new JLabel("🔒  Restricted Fields");
            lockTitle.setFont(UITheme.BUTTON);
            lockTitle.setForeground(new Color(180, 0, 0));
            lockTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lockNote = new JLabel("Username and password can only be changed by the Admin.");
            lockNote.setFont(UITheme.SMALL);
            lockNote.setForeground(UITheme.TEXT_MUTED);
            lockNote.setAlignmentX(Component.LEFT_ALIGNMENT);

            lockBox.add(lockTitle);
            lockBox.add(Box.createVerticalStrut(4));
            lockBox.add(lockNote);
            card.add(lockBox);
        }

        return card;
    }

    /** Styled single-line text field, full width, left-aligned. */
    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(UITheme.BODY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    /** Styled password field, full width, left-aligned. */
    private JPasswordField styledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setFont(UITheme.BODY);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        return f;
    }

    /** Thin horizontal divider line. */
    private void addDivider(JPanel card) {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        card.add(sep);
    }

    /** Small bold section sub-header label. */
    private void addSectionHeader(JPanel card, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UITheme.HEADING);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
    }

    // ── Photo helpers ─────────────────────────────────────────────────────

    /** Folder where all profile images are stored (relative to the working directory). */
    private static final String PHOTO_DIR = "profiles";

    /**
     * Load the photo from the profiles/ folder.
     * The DB stores only the filename (e.g. "001.jpg"), so we resolve the
     * full path as profiles/<filename>.
     */
    private void loadPhoto() {
        String filename = getProfilePath();       // e.g. "001.jpg" or full path (legacy)
        if (filename != null && !filename.isBlank()) {

            // Support both: plain filename stored in DB, and legacy full-path entries
            File f = new File(filename);
            if (!f.isAbsolute() || !f.exists()) {
                // Treat as a filename inside profiles/
                f = new File(PHOTO_DIR, filename);
            }

            if (f.exists()) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    if (img != null) {
                        Image scaled = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                        photoLabel.setIcon(new ImageIcon(scaled));
                        photoLabel.setText("");
                        return;
                    }
                } catch (Exception ignored) {}
            }
        }
        // Fallback placeholder
        photoLabel.setIcon(null);
        photoLabel.setText("<html><center>👤<br><small>No Photo</small></center></html>");
        photoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        photoLabel.setForeground(UITheme.TEXT_MUTED);
    }

    /**
     * Let the user pick an image file, copy it into profiles/ with a unique
     * name, save the filename to the DB, then reload the display.
     */
    private void changePhoto() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Profile Photo");
        fc.setFileFilter(new FileNameExtensionFilter(
                "Image files (jpg, jpeg, png, gif)", "jpg", "jpeg", "png", "gif"));
        fc.setAcceptAllFileFilterUsed(false);

        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File selected = fc.getSelectedFile();

        // ── Copy the chosen image into the profiles/ folder ───────────────
        File dir = new File(PHOTO_DIR);
        if (!dir.exists()) dir.mkdirs();           // create folder if not present

        // Build a unique filename: userId + original extension
        // e.g.  TG/2023/001.jpg  →  TG_2023_001.jpg
        String ext = getExtension(selected.getName());
        String safeName = user.getUserId().replaceAll("[/\\\\]", "_") + "." + ext;
        File dest = new File(dir, safeName);

        try {
            Files.copy(selected.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            showErr("Could not copy image: " + ex.getMessage());
            return;
        }

        // ── Save only the filename to the DB ──────────────────────────────
        if (saveProfilePath(safeName)) {
            JOptionPane.showMessageDialog(this, "Profile photo updated successfully.");
            loadPhoto();
        } else {
            showErr("Image copied but could not save to database.");
        }
    }

    /** Returns the lowercase file extension, defaulting to "jpg". */
    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return (dot >= 0) ? name.substring(dot + 1).toLowerCase() : "jpg";
    }

    /** Read the stored filename/path from the Profile table. */
    private String getProfilePath() {
        String sql = "SELECT Profile_path FROM Profile WHERE Profile_id = ?";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("Profile_path");
        } catch (SQLException ignored) {}
        return null;
    }

    /**
     * Upsert the Profile row with the given filename.
     * Uses ON DUPLICATE KEY so it works whether a row already exists or not.
     */
    private boolean saveProfilePath(String filename) {
        String sql = "INSERT INTO Profile(Profile_id, Created_date, Profile_path) " +
                "VALUES(?, CURDATE(), ?) " +
                "ON DUPLICATE KEY UPDATE Profile_path = VALUES(Profile_path)";
        try (PreparedStatement ps = DBConnection.getInstance().getConnection()
                .prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, filename);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "DB error: " + e.getMessage());
            return false;
        }
    }

    private void addInfoRow(JPanel parent, String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 4));
        row.setBackground(UITheme.CARD_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(label + "  ");
        lbl.setFont(UITheme.BODY); lbl.setForeground(UITheme.TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(80, 24));
        JLabel val = new JLabel(value);
        val.setFont(UITheme.HEADING); val.setForeground(UITheme.TEXT_PRIMARY);
        row.add(lbl); row.add(val);
        parent.add(row);
    }

    private void showErr(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
