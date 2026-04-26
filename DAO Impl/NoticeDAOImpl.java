package ums.dao;

import ums.db.DBConnection;
import ums.model.Notice;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public final class NoticeDAOImpl implements INoticeDAO {
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    @Override
    public boolean createNotice(Notice n) {
        String sql = "INSERT INTO Notice(Notice_description, Active) VALUES(?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, n.getContent());
            ps.setString(2, "Yes");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    @Override
    public List<Notice> getAllActive() {
        List<Notice> list = new ArrayList<>();
        String sql = "SELECT * FROM Notice WHERE Active='Yes' ORDER BY Created_at DESC";
        try (Statement st = conn().createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Notice no = new Notice();
                no.setNoticeId(String.valueOf(rs.getInt("Notice_id")));
                no.setContent(rs.getString("Notice_description"));
                no.setCreatedAt(rs.getTimestamp("Created_at"));
                no.setActive(true);
                list.add(no);
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public boolean deleteNotice(int noticeId) {
        String sql = "DELETE FROM Notice WHERE Notice_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, noticeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    @Override
    public boolean updateNotice(Notice n) {
        String sql = "UPDATE Notice SET Notice_description=? WHERE Notice_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, n.getContent());
            ps.setInt(2, Integer.parseInt(n.getNoticeId()));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }
}
