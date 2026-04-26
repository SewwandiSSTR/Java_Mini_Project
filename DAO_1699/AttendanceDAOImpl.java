package ums.dao;

import ums.db.DBConnection;
import ums.model.Attendance;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public final class AttendanceDAOImpl implements IAttendanceDAO {
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    @Override
    public boolean addAttendance(Attendance a) {
        String sql = "INSERT INTO Attendance(C_code,C_type,Regno,Week,Date,Status,L_Hours) VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, a.getC_code()); ps.setString(2, a.getC_type());
            ps.setString(3, a.getRegno()); ps.setInt(4, a.getWeek());
            ps.setDate(5, a.getAttDate()); ps.setString(6, a.getAttStatus());
            ps.setInt(7, a.getL_hours());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    @Override
    public boolean updateAttendance(Attendance a) {
        String sql = "UPDATE Attendance SET Status=?, Date=? WHERE Regno=? AND C_code=? AND C_type=? AND Week=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, a.getAttStatus()); ps.setDate(2, a.getAttDate());
            ps.setString(3, a.getRegno()); ps.setString(4, a.getC_code());
            ps.setString(5, a.getC_type()); ps.setInt(6, a.getWeek());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    @Override
    public List<Attendance> getByStudent(String studentId, String C_code, String C_type) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE Regno=? AND C_code=? AND C_type=? ORDER BY Week ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, studentId); ps.setString(2, C_code); ps.setString(3, C_type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    /** Load both T and P attendance for a course+week combined and sorted. */
    public List<Attendance> getByBatchBothTypes(String courseId, int week) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE C_code=? AND Week=? ORDER BY C_type,Regno ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, courseId); ps.setInt(2, week);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null,"Attendance load error: "+e.getMessage()); }
        return list;
    }

    @Override
    public List<Attendance> getByRegno(String regno) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE Regno=? ORDER BY C_code,C_type,Week ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, regno);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public List<Attendance> getByRegnoAndCourse(String regno, String courseId) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE Regno=? AND C_code=? ORDER BY C_type,Week ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, regno); ps.setString(2, courseId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    public List<Attendance> getByBatch(String courseId, String c_type, int week) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE C_code=? AND C_type=? AND Week=? ORDER BY Regno ASC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, courseId); ps.setString(2, c_type); ps.setInt(3, week);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public List<Attendance> getByType(String C_code, String C_type) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE C_code=? AND C_type=? ORDER BY Week,Regno";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, C_code); ps.setString(2, C_type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public List<Attendance> getByType(String C_code) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT * FROM Attendance WHERE C_code=? ORDER BY C_type,Week,Regno";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, C_code);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public double getAttendanceRate(String courseId, String courseType, String studentId) {
        String sql = "SELECT COUNT(*) as total, SUM(CASE WHEN Status IN ('Present','Medical') THEN 1 ELSE 0 END) as counted " +
                "FROM Attendance WHERE Regno=? AND C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, studentId); ps.setString(2, courseId); ps.setString(3, courseType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total"), counted = rs.getInt("counted");
                return total > 0 ? (double) counted * 100 / total : 0.0;
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return 0.0;
    }

    @Override
    public List<Attendance> getAttendanceRateBatch(String courseId, String courseType) {
        List<Attendance> list = new ArrayList<>();
        String sql = "SELECT C_code, C_type, Regno, " +
                "ROUND(SUM(CASE WHEN Status IN ('Present','Medical') THEN 1 ELSE 0 END)*100.0/COUNT(*),2) AS rate " +
                "FROM Attendance WHERE C_code=? AND C_type=? GROUP BY C_code,C_type,Regno";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, courseId); ps.setString(2, courseType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance a = new Attendance();
                a.setC_code(rs.getString("C_code")); a.setC_type(rs.getString("C_type"));
                a.setRegno(rs.getString("Regno")); a.setRate(rs.getDouble("rate"));
                list.add(a);
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public Map<String, List<Attendance>> getEligibleStudents(String courseId, String cType) {
        List<Attendance> above = new ArrayList<>(), equal = new ArrayList<>(), less = new ArrayList<>();
        String sql = "SELECT C_code,C_type,Regno, " +
                "ROUND(SUM(CASE WHEN Status='Present' THEN 1 ELSE 0 END)*100.0/COUNT(*),2) AS rate " +
                "FROM Attendance WHERE C_code=? AND C_type=? GROUP BY C_code,C_type,Regno";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, courseId); ps.setString(2, cType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance a = new Attendance();
                a.setC_code(rs.getString("C_code")); a.setC_type(rs.getString("C_type"));
                a.setRegno(rs.getString("Regno")); double rate = rs.getDouble("rate"); a.setRate(rate);
                if (rate > 80.0) above.add(a);
                else if (Math.abs(rate - 80.0) < 0.01) equal.add(a);
                else less.add(a);
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        Map<String, List<Attendance>> result = new HashMap<>();
        result.put("above", above); result.put("equal", equal); result.put("less", less);
        return result;
    }

    @Override
    public Map<String, List<Attendance>> getEligibleMedical(String courseId, String cType) {
        List<Attendance> eligible = new ArrayList<>(), notEligible = new ArrayList<>();
        String sql = "SELECT C_code,C_type,Regno, " +
                "ROUND(SUM(CASE WHEN Status IN ('Present','Medical') THEN 1 ELSE 0 END)*100.0/COUNT(*),2) AS rate " +
                "FROM Attendance WHERE C_code=? AND C_type=? GROUP BY C_code,C_type,Regno";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, courseId); ps.setString(2, cType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Attendance a = new Attendance();
                a.setC_code(rs.getString("C_code")); a.setC_type(rs.getString("C_type"));
                a.setRegno(rs.getString("Regno")); double rate = rs.getDouble("rate"); a.setRate(rate);
                if (rate >= 80.0) eligible.add(a); else notEligible.add(a);
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        Map<String, List<Attendance>> result = new HashMap<>();
        result.put("eligible", eligible); result.put("notEligible", notEligible);
        return result;
    }

    private Attendance mapRow(ResultSet rs) throws SQLException {
        Attendance a = new Attendance();
        a.setC_code(rs.getString("C_code")); a.setC_type(rs.getString("C_type"));
        a.setRegno(rs.getString("Regno")); a.setWeek(rs.getInt("Week"));
        a.setAttDate(rs.getDate("Date")); a.setAttStatus(rs.getString("Status"));
        a.setL_hours(rs.getInt("L_Hours"));
        return a;
    }
}
