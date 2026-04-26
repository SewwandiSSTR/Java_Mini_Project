package ums.gui.panels;

import ums.dao.GradeDAOImpl;
import ums.dao.IGradeDAO;
import ums.gui.UITheme;
import ums.model.Grade;
import ums.roles.Undergraduate;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UGGradesPanel extends JPanel {

    private final Undergraduate ug;
    private final IGradeDAO     gradeDAO = new GradeDAOImpl();

    public UGGradesPanel(Undergraduate ug) {
        this.ug = ug;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("🎓  My Grades, SGPA & CGPA");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────
        String[] cols = {
                "Course Code", "Type", "Course Name",
                "Grade",
                "CA Eligible", "End Eligible",
                "Att % (w/Med)", "Att Eligible"
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
        table.getColumnModel().getColumn(3).setPreferredWidth(60);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(110);
        table.getColumnModel().getColumn(7).setPreferredWidth(90);

        // Colour-code Grade column (index 3)
        table.getColumnModel().getColumn(3).setCellRenderer(gradeRenderer());

        // Colour eligibility columns (4, 5, 7)
        DefaultTableCellRenderer eligRenderer = eligibilityRenderer();
        table.getColumnModel().getColumn(4).setCellRenderer(eligRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(eligRenderer);
        table.getColumnModel().getColumn(7).setCellRenderer(eligRenderer);

        // ── SGPA + CGPA summary bar ───────────────────────────────────────
        JLabel sgpaLabel = new JLabel("  SGPA: –");
        sgpaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sgpaLabel.setForeground(UITheme.PRIMARY);

        JLabel cgpaLabel = new JLabel("   CGPA: –");
        cgpaLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cgpaLabel.setForeground(new Color(13, 71, 161));

        JLabel gpaNote = new JLabel("   (ENG2122 excluded from all GPA calculations)");
        gpaNote.setFont(UITheme.SMALL);
        gpaNote.setForeground(UITheme.TEXT_MUTED);

        JPanel gpaCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        gpaCard.setBackground(new Color(227, 242, 253));
        gpaCard.setBorder(BorderFactory.createLineBorder(new Color(187, 222, 251), 1));
        gpaCard.add(sgpaLabel);
        gpaCard.add(new JSeparator(SwingConstants.VERTICAL) {{
            setPreferredSize(new Dimension(1, 20));
        }});
        gpaCard.add(cgpaLabel);
        gpaCard.add(gpaNote);

        // ── Legend ────────────────────────────────────────────────────────
        JLabel legend = new JLabel(
                "  MC = Medical  |  ECA = CA Fail  |  EE = End Fail  |  E = Both  |  WH = Withheld");
        legend.setFont(UITheme.SMALL);
        legend.setForeground(UITheme.TEXT_MUTED);

        // ── Load button ───────────────────────────────────────────────────
        JButton loadBtn = UITheme.primaryBtn("📊 Load My Grades");
        loadBtn.addActionListener(e -> {
            model.setRowCount(0);
            List<Grade> grades = gradeDAO.getGPAWithGrade(ug.getUserId());

            for (Grade g : grades) {
                boolean noMarks    = "N/A".equals(g.getGrade());
                String  courseName = getCourseName(g.getCourseId(), g.getCourseType());

                model.addRow(new Object[]{
                        g.getCourseId(),
                        g.getCourseType(),
                        courseName,
                        noMarks ? "–" : g.getGrade(),
                        noMarks ? "–" : (g.isCaEligible()  ? "✔ Pass" : "✘ Fail"),
                        noMarks ? "–" : (g.isEndEligible() ? "✔ Pass" : "✘ Fail"),
                        noMarks ? "–" : String.format("%.1f%%", g.getAttendanceRate()),
                        noMarks ? "–" : (g.isAttEligible() ? "✔ Eligible" : "✘ Not Elig.")
                });
            }

            // ── SGPA ──────────────────────────────────────────────────────
            Grade sg = gradeDAO.getSGPAByStudent(ug.getUserId());
            if (sg.isSgpaWithheld()) {
                sgpaLabel.setText("  SGPA: WH (Withheld)");
                sgpaLabel.setForeground(new Color(180, 60, 0));
            } else {
                sgpaLabel.setText(String.format("  SGPA: %.3f  [%s]", sg.getSGPA(), sg.getGrade()));
                sgpaLabel.setForeground(UITheme.PRIMARY);
            }

            // ── CGPA ──────────────────────────────────────────────────────
            Grade cg = gradeDAO.getCGPAByStudent(ug.getUserId());
            if (cg.isCgpaWithheld()) {
                cgpaLabel.setText("   CGPA: WH (Withheld)");
                cgpaLabel.setForeground(new Color(180, 60, 0));
            } else {
                cgpaLabel.setText(String.format("   CGPA: %.3f  [%s]", cg.getCGPA(), cg.getGrade()));
                cgpaLabel.setForeground(new Color(13, 71, 161));
            }
        });

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        topBar.setBackground(UITheme.BG);
        topBar.add(loadBtn);
        topBar.add(legend);

        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(UITheme.BG);
        bottomBar.add(gpaCard, BorderLayout.CENTER);

        add(topBar,                BorderLayout.NORTH);
        add(UITheme.scroll(table), BorderLayout.CENTER);
        add(bottomBar,             BorderLayout.SOUTH);
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

    private DefaultTableCellRenderer gradeRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String g = val == null ? "" : val.toString();
                setHorizontalAlignment(CENTER);
                setFont(UITheme.BUTTON);
                switch (g) {
                    case "MC"           -> { setForeground(Color.WHITE); setBackground(new Color(230,120,0)); }
                    case "ECA","EE","E" -> { setForeground(Color.WHITE); setBackground(UITheme.DANGER); }
                    case "A+","A","A-"  -> { setForeground(Color.WHITE); setBackground(UITheme.SUCCESS); }
                    case "WH"           -> { setForeground(Color.WHITE); setBackground(new Color(100,0,140)); }
                    case "N/A"          -> { setForeground(UITheme.TEXT_MUTED); setBackground(Color.WHITE); }
                    default             -> { setForeground(UITheme.TEXT_PRIMARY);
                        setBackground(sel ? t.getSelectionBackground() : Color.WHITE); }
                }
                return this;
            }
        };
    }

    private DefaultTableCellRenderer eligibilityRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                setHorizontalAlignment(CENTER);
                String s = val == null ? "" : val.toString();
                setForeground(s.startsWith("✔") ? UITheme.SUCCESS : UITheme.DANGER);
                setFont(UITheme.BUTTON);
                setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
                return this;
            }
        };
    }
}