package ums.gui.panels;

import ums.dao.GradeDAOImpl;
import ums.dao.IGradeDAO;
import ums.gui.UITheme;
import ums.model.Course;
import ums.model.Grade;
import ums.roles.Undergraduate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class UGMarksPanel extends JPanel {

    private final Undergraduate ug;
    private final IGradeDAO gradeDAO = new GradeDAOImpl();

    public UGMarksPanel(Undergraduate ug) {
        this.ug = ug;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("📋  My Eligibility Status");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        // Info banner
        JLabel info = new JLabel(
            "  ℹ  Your eligibility status per course is shown below.  " +
            "Raw marks are available from your Lecturer.");
        info.setFont(UITheme.SMALL);
        info.setForeground(UITheme.TEXT_MUTED);
        info.setOpaque(true);
        info.setBackground(new Color(227, 242, 253));
        info.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // Table
        String[] cols = {
            "Course Code", "Type", "Course Name",
            "CA Eligible", "End Eligible",
            "Attendance % (with Medical)", "Attendance Eligible",
            "Overall Status"
        };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(50);
        table.getColumnModel().getColumn(2).setPreferredWidth(240);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(180);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);
        table.getColumnModel().getColumn(7).setPreferredWidth(130);

        JButton loadBtn = UITheme.primaryBtn("📋 Load Eligibility");
        loadBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<Grade> grades = gradeDAO.getGPAWithGrade(ug.getUserId());
            for (Grade g : grades) {
                if ("N/A".equals(g.getGrade())) continue;
                String courseName = getCourseName(g.getCourseId(), g.getCourseType());

                // Overall status
                String overall;
                if ("MC".equals(g.getGrade())) {
                    overall = "⚕ Medical";
                } else if (g.isCaEligible() && g.isEndEligible() && g.isAttEligible()) {
                    overall = "✔ Eligible";
                } else {
                    overall = "✘ Not Eligible";
                }

                model.addRow(new Object[]{
                    g.getCourseId(),
                    g.getCourseType(),
                    courseName,
                    g.isCaEligible()  ? "✔ Pass" : "✘ Fail",
                    g.isEndEligible() ? "✔ Pass" : "✘ Fail",
                    String.format("%.1f%%", g.getAttendanceRate()),
                    g.isAttEligible() ? "✔ Eligible" : "✘ Not Eligible",
                    overall
                });
            }
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topBar.setBackground(UITheme.BG);
        topBar.add(loadBtn);

        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setBackground(UITheme.BG);
        infoBar.add(info, BorderLayout.CENTER);

        JPanel north = new JPanel(new BorderLayout(0, 6));
        north.setBackground(UITheme.BG);
        north.add(infoBar, BorderLayout.NORTH);
        north.add(topBar,  BorderLayout.SOUTH);

        add(north,                 BorderLayout.NORTH);
        add(UITheme.scroll(table), BorderLayout.CENTER);
    }

    private String getCourseName(String code, String type) {
        try (java.sql.Connection c = ums.db.DBConnection.getInstance().getConnection();
             java.sql.PreparedStatement ps = c.prepareStatement(
                 "SELECT C_name FROM Course WHERE C_code=? AND C_type=?")) {
            ps.setString(1, code); ps.setString(2, type);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("C_name");
        } catch (Exception ignored) {}
        return code;
    }
}
