package ums.dao;

import ums.db.DBConnection;
import ums.model.Material;

import javax.swing.*;
import java.sql.*;
import java.util.*;

/*
 * ── Run this SQL once in your tec_mis database before using this class ──────
 *
 * CREATE TABLE IF NOT EXISTS Material (
 *     Material_id   INT           NOT NULL AUTO_INCREMENT,
 *     C_code        CHAR(8)       NOT NULL,
 *     C_type        ENUM('P','T') NOT NULL,
 *     Lec_id        CHAR(10)      NOT NULL,
 *     Title         VARCHAR(100)  NOT NULL,
 *     Description   VARCHAR(255),
 *     File_path     VARCHAR(512)  NOT NULL,
 *     File_name     VARCHAR(255)  NOT NULL,
 *     Uploaded_at   TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
 *
 *     PRIMARY KEY (Material_id),
 *
 *     CONSTRAINT fk_mat_course
 *         FOREIGN KEY (C_code, C_type) REFERENCES Course(C_code, C_type)
 *         ON UPDATE CASCADE ON DELETE CASCADE,
 *
 *     CONSTRAINT fk_mat_lec
 *         FOREIGN KEY (Lec_id) REFERENCES Lecturer(Lec_id)
 *         ON UPDATE CASCADE ON DELETE CASCADE
 * );
 *
 * ────────────────────────────────────────────────────────────────────────────
 */
public final class MaterialDAOImpl implements IMaterialDAO {

    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    @Override
    public boolean addMaterial(Material m) {
        String sql = "INSERT INTO Material(C_code, C_type, Lec_id, Title, Description, File_path, File_name) " +
                "VALUES(?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, m.getC_code());
            ps.setString(2, m.getC_type());
            ps.setString(3, m.getLec_id());
            ps.setString(4, m.getTitle());
            ps.setString(5, m.getDescription());
            ps.setString(6, m.getFilePath());
            ps.setString(7, m.getFileName());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Add material error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateMaterial(Material m) {
        String sql = "UPDATE Material SET Title=?, Description=?, File_path=?, File_name=? " +
                "WHERE Material_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, m.getTitle());
            ps.setString(2, m.getDescription());
            ps.setString(3, m.getFilePath());
            ps.setString(4, m.getFileName());
            ps.setInt(5, m.getMaterialId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Update material error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMaterial(int materialId) {
        String sql = "DELETE FROM Material WHERE Material_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, materialId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Delete material error: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Material> getByCourse(String C_code, String C_type) {
        List<Material> list = new ArrayList<>();
        String sql = "SELECT * FROM Material WHERE C_code=? AND C_type=? ORDER BY Uploaded_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, C_code);
            ps.setString(2, C_type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Load material error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<Material> getByLecturer(String lecId) {
        List<Material> list = new ArrayList<>();
        String sql = "SELECT * FROM Material WHERE Lec_id=? ORDER BY C_code, Uploaded_at DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, lecId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Load material error: " + e.getMessage());
        }
        return list;
    }

    @Override
    public Material getById(int materialId) {
        String sql = "SELECT * FROM Material WHERE Material_id=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, materialId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return null;
    }

    private Material mapRow(ResultSet rs) throws SQLException {
        Material m = new Material();
        m.setMaterialId(rs.getInt("Material_id"));
        m.setC_code(rs.getString("C_code"));
        m.setC_type(rs.getString("C_type"));
        m.setLec_id(rs.getString("Lec_id"));
        m.setTitle(rs.getString("Title"));
        m.setDescription(rs.getString("Description"));
        m.setFilePath(rs.getString("File_path"));
        m.setFileName(rs.getString("File_name"));
        m.setUploadedAt(rs.getTimestamp("Uploaded_at"));
        return m;
    }
}
