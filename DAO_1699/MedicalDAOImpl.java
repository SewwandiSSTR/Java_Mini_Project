package ums.dao;

import ums.db.DBConnection;
import ums.model.MedicalRecord;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public final class MedicalDAOImpl implements IMedicalDAO {
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    private String resolveTable(String type) {
        return switch (type.toLowerCase()) {
            case "end"        -> "Medical_End";
            case "mid"        -> "Medical_Mid";
            default           -> "Medical_Attendence";   // matches DB spelling
        };
    }

    @Override
    public boolean addMedical(MedicalRecord r) {
        String table = resolveTable(r.getMedi_type());
        String sql = "INSERT INTO " + table +
                     "(C_code,C_type,Regno,Medical_id,Medical_Status,Submited_Date,Medical_Start_Date,Medical_End_Date) " +
                     "VALUES(?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, r.getC_code()); ps.setString(2, r.getC_type());
            ps.setString(3, r.getRegno()); ps.setString(4, r.getMedical_Id());
            ps.setString(5, "Pending");
            ps.setDate(6, r.getSubmitted_Date());
            ps.setDate(7, r.getMedical_Start_Date());
            ps.setDate(8, r.getMedical_End_Date());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    @Override
    public boolean updateStatus(String medId, String type, String status) {
        String table = resolveTable(type);
        String sql = "UPDATE " + table + " SET Medical_Status=? WHERE Medical_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, status); ps.setString(2, medId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    @Override
    public List<MedicalRecord> getByStudent(String studentId, String C_code, String C_type, String Medi_type) {
        List<MedicalRecord> list = new ArrayList<>();
        String table = resolveTable(Medi_type);
        String sql = "SELECT * FROM " + table + " WHERE Regno=? AND C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, studentId); ps.setString(2, C_code); ps.setString(3, C_type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs, Medi_type));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public List<MedicalRecord> getAll(String Medi_type) {
        List<MedicalRecord> list = new ArrayList<>();
        String table = resolveTable(Medi_type);
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM " + table + " ORDER BY Submited_Date DESC")) {
            while (rs.next()) list.add(mapRow(rs, Medi_type));
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return list;
    }

    @Override
    public boolean deleteById(String medId, String Medi_type) {
        String table = resolveTable(Medi_type);
        String sql = "DELETE FROM " + table + " WHERE Medical_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, medId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); return false; }
    }

    private MedicalRecord mapRow(ResultSet rs, String type) throws SQLException {
        MedicalRecord mr = new MedicalRecord();
        mr.setMedical_Id(rs.getString("Medical_id"));
        mr.setC_code(rs.getString("C_code")); mr.setC_type(rs.getString("C_type"));
        mr.setRegno(rs.getString("Regno"));
        mr.setMedical_status(rs.getString("Medical_Status"));
        mr.setMedi_type(type);
        mr.setSubmitted_Date(rs.getDate("Submited_Date"));
        mr.setMedical_Start_Date(rs.getDate("Medical_Start_Date"));
        mr.setMedical_End_Date(rs.getDate("Medical_End_Date"));
        return mr;
    }
}
