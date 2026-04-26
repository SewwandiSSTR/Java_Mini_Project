package ums.dao;

import ums.db.DBConnection;
import ums.model.User;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public final class UserDAOImpl implements IUserDAO {
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    @Override
    public User findById(String userId) {
        String sql = "SELECT * FROM USER WHERE User_id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User u = new User();

                u.setUserId(rs.getString("User_id"));
                u.setPasswordHash(rs.getString("Password"));
                u.setRole(rs.getString("Role"));

                return u;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Can not get User");
        }
        return null;
    }

    @Override
    public User authenticate(String userId, String password) {
        String sql = "SELECT * FROM USER WHERE User_id = ? AND Password = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {

            ps.setString(1, userId);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User u = new User();
                String uid  = rs.getString("User_id");
                String role = rs.getString("Role");

                u.setUserId(uid);
                u.setPasswordHash(rs.getString("Password"));
                u.setRole(role);
                // Load display name NOW while still on the login_user connection
                // (before DBConnection.init() switches to role-specific user)
                u.setUserName(loadDisplayName(uid, role));

                return u;
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Login error: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<User> findAll() {

        List<User> list = new ArrayList<>();

        try (Statement st = conn().createStatement()) {

            ResultSet rs = st.executeQuery("SELECT * FROM USER");

            while (rs.next()) {
                User u = new User();

                u.setUserId(rs.getString("User_id"));
                u.setRole(rs.getString("Role"));
                u.setUserName(loadDisplayName(rs.getString("User_id"), rs.getString("Role")));

                list.add(u);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Can not find All User");
        }
        return list;
    }

    @Override
    public boolean createUser(User u) {

        String sql = "INSERT INTO USER(User_id, Role, Password) VALUES(?,?,?)";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {

            ps.setString(1, u.getUserId());
            ps.setString(2, u.getRole());
            ps.setString(3, u.getPasswordHash());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Can not Create User");
            return false;
        }
    }

    private String loadDisplayName(String userId, String role) {
        String sql = switch (role) {

            case "Admin"    -> "SELECT CONCAT(F_name,' ',L_name) AS name FROM Admin WHERE Admin_id=?";
            case "Lecturer",
                 "Dean"     -> "SELECT Fullname AS name FROM Lecturer WHERE Lec_id=?";
            case "TO"       -> "SELECT CONCAT(F_Name,' ',L_Name) AS name FROM Technical_officer WHERE Tec_id=?";
            case "Student"  -> "SELECT CONCAT(Fname,' ',Lname) AS name FROM Student WHERE Reg_no=?";
            default         -> null;
        };
        if (sql == null) return userId;
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                return (name != null && !name.isBlank()) ? name : userId;
            }

        } catch (SQLException ignored) {}
        return userId;
    }

    @Override
    public boolean deleteUser(String userId) {
        Connection c = conn();
        try {
            c.setAutoCommit(false);

            // 1. Mark / exam / attendance / medical / enrollment records
            exec(c, "DELETE FROM Quiz               WHERE Regno=?",     userId);
            exec(c, "DELETE FROM Assignment         WHERE Regno=?",     userId);
            exec(c, "DELETE FROM Mid_exam           WHERE Regno=?",     userId);
            exec(c, "DELETE FROM End_exam           WHERE Regno=?",     userId);
            exec(c, "DELETE FROM Project            WHERE Regno=?",     userId);
            exec(c, "DELETE FROM Attendance         WHERE Regno=?",     userId);
            exec(c, "DELETE FROM Medical_Mid        WHERE Regno=?",     userId);
            exec(c, "DELETE FROM Medical_End        WHERE Regno=?",     userId);
            exec(c, "DELETE FROM Medical_Attendence WHERE Regno=?",     userId);
            exec(c, "DELETE FROM Course_Enrollment  WHERE Regno=?",     userId);

            // 2. If user is a Lecturer: remove timetable + courses they own first
            //    (Lecturer FK → USER has no ON DELETE CASCADE in the DDL)
            exec(c, "DELETE FROM Time_table WHERE Lec_id=?",            userId);
            exec(c, "DELETE FROM Course     WHERE Lec_id=?",            userId);
            exec(c, "DELETE FROM Lecturer   WHERE Lec_id=?",            userId);

            // 3. Other role-specific rows
            exec(c, "DELETE FROM Student           WHERE Reg_no=?",     userId);
            exec(c, "DELETE FROM Technical_officer WHERE Tec_id=?",     userId);
            exec(c, "DELETE FROM Admin             WHERE Admin_id=?",   userId);
            exec(c, "DELETE FROM Profile           WHERE Profile_id=?", userId);

            // 4. Finally the USER row itself
            exec(c, "DELETE FROM USER WHERE User_id=?",                 userId);

            c.commit();
            return true;

        } catch (SQLException e) {
            try { c.rollback(); } catch (SQLException ignored) {}

            JOptionPane.showMessageDialog(null, "Delete failed:\n");
            return false;
        } finally {
            try { c.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /** Execute a single-param DELETE inside an existing transaction. */
    private void exec(Connection c, String sql, String param) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, param);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean updateUserName(String userId, String name) {
        boolean updated = false;

        // Try Student table
        try (PreparedStatement ps = conn().prepareStatement("UPDATE Student SET Fname = ? WHERE Reg_no = ?")) {

            ps.setString(1, name); ps.setString(2, userId);

            if (ps.executeUpdate() > 0) updated = true;

        } catch (SQLException ignored) {}

        // Try Lecturer table
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE Lecturer SET Fullname = ? WHERE Lec_id = ?")) {
            ps.setString(1, name); ps.setString(2, userId);

            if (ps.executeUpdate() > 0)
                updated = true;

        } catch (SQLException ignored) {}

        // Try Technical_officer table
        try (PreparedStatement ps = conn().prepareStatement("UPDATE Technical_officer SET F_Name = ? WHERE Tec_id = ?")) {
            ps.setString(1, name);
            ps.setString(2, userId);

            if (ps.executeUpdate() > 0) updated = true;

        } catch (SQLException ignored) {}

        // Try Admin table
        try (PreparedStatement ps = conn().prepareStatement("UPDATE Admin SET F_name = ? WHERE Admin_id = ?")) {
            ps.setString(1, name);
            ps.setString(2, userId);

            if (ps.executeUpdate() > 0)
                updated = true;

        } catch (SQLException ignored) {}
        return updated;
    }

    @Override
    public boolean updatePassword(String userId, String hash) {
        String sql = "UPDATE USER SET Password = ? WHERE User_id = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setString(2, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Can not Update Password");
            return false;
        }
    }

    // for students. Generalise to also handle Lecturer and Technical_officer.
    @Override
    public boolean updateContact(String userId, String contact) {
        boolean updated = false;

        // Try every role table — only the one matching this user will update a row
        updated |= tryUpdate("UPDATE Student           SET email = ? WHERE Reg_no   = ?", contact, userId);
        updated |= tryUpdate("UPDATE Lecturer          SET email = ? WHERE Lec_id   = ?", contact, userId);
        updated |= tryUpdate("UPDATE Technical_officer SET Email = ? WHERE Tec_id   = ?", contact, userId);
        updated |= tryUpdate("UPDATE Admin             SET Email = ? WHERE Admin_id = ?", contact, userId);

        if (!updated) JOptionPane.showMessageDialog(null, "No contact record found for: " + userId);
        return updated;
    }

    /** Fetch the current email/contact for any role. Returns empty string if not found. */
    @Override
    public String getCurrentEmail(String userId) {
        String[][] queries = {
                {"SELECT email FROM Student           WHERE Reg_no   = ?"},
                {"SELECT email FROM Lecturer          WHERE Lec_id   = ?"},
                {"SELECT Email FROM Technical_officer WHERE Tec_id   = ?"},
                {"SELECT Email FROM Admin             WHERE Admin_id = ?"}
        };
        for (String[] q : queries) {
            try (PreparedStatement ps = conn().prepareStatement(q[0])) {
                ps.setString(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String val = rs.getString(1);
                    if (val != null && !val.isBlank()) return val;
                }
            } catch (SQLException ignored) {}
        }
        return "";
    }

    /** Run a single UPDATE; returns true if at least one row changed, silently ignores SQL errors. */
    private boolean tryUpdate(String sql, String p1, String p2) {
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, p1);
            ps.setString(2, p2);

            return ps.executeUpdate() > 0;

        } catch (SQLException ignored) {
            return false;
        }
    }

    @Override
    public boolean updateProfilePicture(String userId, String path) {
        // Use upsert so it works whether or not a Profile row already exists.
        // (Plain UPDATE would silently return 0 rows for users who have no row yet.)
        String sql = "INSERT INTO Profile(Profile_id, Created_date, Profile_path) " +
                "VALUES(?, CURDATE(), ?) " +
                "ON DUPLICATE KEY UPDATE Profile_path = VALUES(Profile_path)";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, path);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Cannot update profile picture: " + e.getMessage());
            return false;
        }
    }


}
