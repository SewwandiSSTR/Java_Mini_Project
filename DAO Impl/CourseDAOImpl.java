package ums.dao;

import ums.db.DBConnection;
import ums.model.Course;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public final class CourseDAOImpl implements ICourseDAO {
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    @Override
    public boolean createCourse(Course c) {
        String sql = "INSERT INTO Course(C_code,C_type,C_name,Credit,Lec_id,Dep_id) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getCourseCode()); ps.setString(2, c.getCourseType());
            ps.setString(3, c.getCourseName()); ps.setInt(4, c.getCredits());
            ps.setString(5, c.getCoordinator()); ps.setString(6, c.getDepartment());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    @Override
    public List<Course> getAll() {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT C_code,C_type,C_name,Credit,Lec_id,Dep_id FROM Course ORDER BY C_code,C_type";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public boolean updateCourse(Course c) {
        String sql = "UPDATE Course SET C_name=?,Credit=?,Lec_id=?,Dep_id=? WHERE C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, c.getCourseName()); ps.setInt(2, c.getCredits());
            ps.setString(3, c.getCoordinator()); ps.setString(4, c.getDepartment());
            ps.setString(5, c.getCourseCode()); ps.setString(6, c.getCourseType());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    /** Delete one specific (C_code, C_type) row and all its child records in a transaction */
    @Override
    public boolean deleteCourse(String courseCode, String courseType) {
        Connection c = conn();
        try {
            c.setAutoCommit(false);
            exec2(c, "DELETE FROM Quiz               WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Assignment         WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Mid_exam           WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM End_exam           WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Project            WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Attendance         WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Medical_Mid        WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Medical_End        WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Medical_Attendence WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Course_Enrollment  WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Time_table         WHERE C_code=? AND C_type=?", courseCode, courseType);
            exec2(c, "DELETE FROM Course WHERE C_code=? AND C_type=?",             courseCode, courseType);
            c.commit();
            return true;
        } catch (SQLException e) {
            try { c.rollback(); } catch (SQLException ignored) {}
            JOptionPane.showMessageDialog(null, "Delete course failed:\n" + e.getMessage());
            return false;
        } finally {
            try { c.setAutoCommit(true); } catch (SQLException ignored) {}
        }
    }

    /** Get one course by full primary key */
    @Override
    public Course getById(String courseCode, String courseType) {
        String sql = "SELECT * FROM Course WHERE C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, courseCode); ps.setString(2, courseType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return null;
    }

    @Override
    public List<Course> getByStudent(String studentId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT c.* FROM Course c " +
                     "JOIN Course_Enrollment e ON c.C_code=e.C_code AND c.C_type=e.C_type " +
                     "WHERE e.Regno=? ORDER BY c.C_code,c.C_type";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public List<Course> getByLecturer(String lecId) {
        List<Course> list = new ArrayList<>();
        String sql = "SELECT * FROM Course WHERE Lec_id=? ORDER BY C_code,C_type";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, lecId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        Course c = new Course();
        c.setCourseCode(rs.getString("C_code")); c.setCourseType(rs.getString("C_type"));
        c.setCourseName(rs.getString("C_name")); c.setCredits(rs.getInt("Credit"));
        c.setCoordinator(rs.getString("Lec_id")); c.setDepartment(rs.getString("Dep_id"));
        return c;
    }

    private void exec2(Connection c, String sql, String p1, String p2) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p1); ps.setString(2, p2); ps.executeUpdate();
        }
    }
}
