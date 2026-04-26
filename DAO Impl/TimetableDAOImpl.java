package ums.dao;

import ums.db.DBConnection;
import ums.model.Timetable;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public final class TimetableDAOImpl implements ITimetableDAO {
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    @Override
    public boolean createSlot(Timetable t) {
        String sql = "INSERT INTO Time_table(Day,Start_time,End_time,C_code,C_type,Lec_id) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, t.getDay()); ps.setTime(2, t.getStartTime());
            ps.setTime(3, t.getEndTime()); ps.setString(4, t.getC_code());
            ps.setString(5, t.getC_type()); ps.setString(6, t.getLec_id());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    @Override
    public boolean deleteSlot(String day, String C_code, String C_type) {
        String sql = "DELETE FROM Time_table WHERE Day=? AND C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, day); ps.setString(2, C_code); ps.setString(3, C_type);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    @Override
    public List<Timetable> getFullTimetable() {
        List<Timetable> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM Time_table ORDER BY FIELD(Day,'Monday','Tuesday','Wednesday','Thursday','Friday'), Start_time")) {
            while (rs.next()) {
                Timetable t = new Timetable();
                t.setDay(rs.getString("Day")); t.setC_code(rs.getString("C_code"));
                t.setC_type(rs.getString("C_type")); t.setStartTime(rs.getTime("Start_time"));
                t.setEndTime(rs.getTime("End_time")); t.setLec_id(rs.getString("Lec_id"));
                list.add(t);
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }
}
