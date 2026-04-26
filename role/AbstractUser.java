package ums.roles;

import ums.Interfaces.IUser;
import ums.dao.IUserDAO;
import ums.dao.UserDAOImpl;

import javax.swing.*;

public abstract sealed class AbstractUser implements IUser
        permits Admin, Lecturer, TechnicalOfficer, Undergraduate {

    protected final String   userId;
    protected       String   userName;
    protected       String   dbRole;
    protected final IUserDAO userDAO = new UserDAOImpl();

    public AbstractUser(String userId, String userName) {
        this.userId   = userId;
        this.userName = userName;
        this.dbRole   = getClass().getSimpleName();  // default; overridden via setDbRole()
    }

    // ── Concrete shared behaviour ─────────────────────────────────────────

    @Override
    public void updateUserName() {
        String newName = JOptionPane.showInputDialog(null, "Enter new username:", userName);
        if (newName != null && !newName.isBlank()) {
            if (userDAO.updateUserName(userId, newName)) {
                this.userName = newName;
                JOptionPane.showMessageDialog(null, "Username updated successfully.");
            }
        }
    }

    @Override
    public void updatePassword() {
        JPasswordField pf = new JPasswordField();
        int ok = JOptionPane.showConfirmDialog(null, pf,
                "Enter new password:", JOptionPane.OK_CANCEL_OPTION);
        if (ok == JOptionPane.OK_OPTION) {
            String hash = new String(pf.getPassword()).trim();
            if (!hash.isBlank()) {
                if (userDAO.updatePassword(userId, hash))
                    JOptionPane.showMessageDialog(null, "Password updated successfully.");
                else
                    JOptionPane.showMessageDialog(null, "Failed to update password.");
            }
        }
    }

    /** Update contact email — works for all roles (Student / Lecturer / TO / Admin). */
    public boolean updateContact(String contact) {
        return userDAO.updateContact(userId, contact);
    }

    // ── Abstract methods each role must implement (polymorphism) ──────────
    @Override public abstract void updateProfile();
    @Override public abstract void viewNotice();

    // ── Getters ───────────────────────────────────────────────────────────
    public String getUserId()            { return userId; }
    public String getUserName()          { return userName; }
    public void   setUserName(String n)  { this.userName = n; }
    public String getRole()              { return dbRole != null ? dbRole : getClass().getSimpleName(); }
    public void   setDbRole(String role) { this.dbRole = role; }
}
