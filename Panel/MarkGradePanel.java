package ums.gui.panels;

import ums.dao.*;
import ums.gui.UITheme;
import ums.model.*;
import ums.roles.Lecturer;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class MarkGradePanel extends JPanel {

    private final Lecturer   lecturer;
    private final IMarkDAO   markDAO   = new MarkDAOImpl();
    private final IGradeDAO  gradeDAO  = new GradeDAOImpl();
    private final ICourseDAO courseDAO = new CourseDAOImpl();

    private JTextField codeF, typeF, regnoF, q1F, q2F, q3F, assF, midF, endF, prjF;

    public MarkGradePanel(Lecturer lecturer) {
        this.lecturer = lecturer;
        setBackground(UITheme.BG);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        buildUI();
    }

    private void buildUI() {
        JLabel title = new JLabel("  Marks & Grades");
        title.setFont(UITheme.TITLE);
        title.setForeground(UITheme.PRIMARY);
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.BODY);
        tabs.addTab("Upload Marks",   buildUploadPanel());
        tabs.addTab("View Marks",     buildViewMarksPanel());
        tabs.addTab("View Grades",    buildViewGradesPanel());
        tabs.addTab("SGPA & CGPA",    buildSGPAPanel());
        add(tabs, BorderLayout.CENTER);
    }

    // ── Upload ────────────────────────────────────────────────────────────
    private JPanel buildUploadPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        // Show assigned courses as a hint
        List<Course> myCourses = courseDAO.getByLecturer(lecturer.getUserId());
        String courseHint = myCourses.isEmpty()
                ? "  ⚠ No courses assigned in DB — you can still enter any course code manually."
                : "  📚 Your courses: " + myCourses.stream()
                .map(c -> c.getCourseCode() + "(" + c.getCourseType() + ")")
                .reduce((a, b) -> a + "  " + b).orElse("");
        JLabel hintLbl = new JLabel(courseHint);
        hintLbl.setFont(UITheme.SMALL);
        hintLbl.setForeground(myCourses.isEmpty() ? new Color(180, 60, 0) : UITheme.TEXT_MUTED);
        p.add(hintLbl, BorderLayout.NORTH);

        JPanel form = UITheme.sectionPanel("Enter Student Marks  (all fields 0–100)  |  Enter EXACT course code (e.g. ICT2113)");
        form.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets  = new Insets(6, 8, 6, 8);
        gc.fill    = GridBagConstraints.HORIZONTAL;
        gc.anchor  = GridBagConstraints.WEST;

        codeF  = new JTextField(10);
        typeF  = new JTextField(5);
        regnoF = new JTextField(14);
        q1F    = new JTextField(8);
        q2F    = new JTextField(8);
        q3F    = new JTextField(8);
        assF   = new JTextField(8);
        midF   = new JTextField(8);
        endF   = new JTextField(8);
        prjF   = new JTextField(8);

        // Row 0: Course Code | Type (T/P) | Reg No
        addFormRow(form, gc, 0,
                "Course Code:", codeF,
                "Type (T/P):",  typeF,
                "Reg No:",      regnoF);

        // Row 1: Quiz 1 | Quiz 2 | Quiz 3
        addFormRow(form, gc, 1,
                "Quiz 1 /100:", q1F,
                "Quiz 2 /100:", q2F,
                "Quiz 3 /100:", q3F);

        // Row 2: Assignment | Mid Exam | End Exam
        addFormRow(form, gc, 2,
                "Assignment:", assF,
                "Mid Exam:",   midF,
                "End Exam:",   endF);

        // Row 3: Project (first column only)
        gc.gridx = 0; gc.gridy = 3; gc.weightx = 0;
        form.add(lbl("Project:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(prjF, gc);

        JLabel noteLabel = new JLabel(
                "  ℹ  Best 2 of 3 quizzes are used for CA calculation automatically.");
        noteLabel.setFont(UITheme.SMALL);
        noteLabel.setForeground(UITheme.TEXT_MUTED);

        JButton loadMarksBtn = UITheme.primaryBtn("🔍 Load Marks");
        loadMarksBtn.setToolTipText("Enter Course Code, Type and Reg No then click to load existing marks for editing");
        loadMarksBtn.addActionListener(e -> {
            String code  = codeF.getText().trim();
            String type  = typeF.getText().trim().toUpperCase();
            String regno = regnoF.getText().trim();
            if (code.isEmpty() || type.isEmpty() || regno.isEmpty()) {
                JOptionPane.showMessageDialog(p, "Enter Course Code, Type (T/P) and Reg No first.");
                return;
            }
            Mark existing = markDAO.getFinalMarkByStudent(regno, code, type);
            if (existing == null) {
                JOptionPane.showMessageDialog(p, "No marks found for " + regno + " in " + code + " (" + type + ").\nFields cleared — ready for new entry.");
                q1F.setText(""); q2F.setText(""); q3F.setText("");
                assF.setText(""); midF.setText(""); endF.setText(""); prjF.setText("");
            } else {
                q1F.setText(fmt(existing.getQ1_mark()));
                q2F.setText(fmt(existing.getQ2_mark()));
                q3F.setText(fmt(existing.getQ3_mark()));
                assF.setText(fmt(existing.getAssignment()));
                midF.setText(fmt(existing.getMid()));
                endF.setText(fmt(existing.getEnd()));
                prjF.setText(fmt(existing.getProject()));
                JOptionPane.showMessageDialog(p, "Marks loaded for " + regno + ". Edit and click Upload Marks to update.", "Marks Loaded", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton saveBtn = UITheme.primaryBtn("Upload Marks");
        saveBtn.addActionListener(e -> uploadMark());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        btnRow.setBackground(UITheme.BG);
        btnRow.add(loadMarksBtn);
        btnRow.add(saveBtn);
        btnRow.add(noteLabel);

        JPanel formArea = new JPanel(new BorderLayout(0, 6));
        formArea.setBackground(UITheme.BG);
        formArea.add(form,   BorderLayout.NORTH);
        formArea.add(btnRow, BorderLayout.SOUTH);

        p.add(formArea, BorderLayout.CENTER);
        return p;
    }

    /** Add one row of 3 label+field pairs to a GridBagLayout form. */
    private void addFormRow(JPanel form, GridBagConstraints gc, int row,
                            String l1, JComponent f1,
                            String l2, JComponent f2,
                            String l3, JComponent f3) {
        gc.gridy = row;
        gc.weightx = 0; gc.gridx = 0; form.add(lbl(l1), gc);
        gc.weightx = 1; gc.gridx = 1; form.add(f1,       gc);
        gc.weightx = 0; gc.gridx = 2; form.add(lbl(l2), gc);
        gc.weightx = 1; gc.gridx = 3; form.add(f2,       gc);
        gc.weightx = 0; gc.gridx = 4; form.add(lbl(l3), gc);
        gc.weightx = 1; gc.gridx = 5; form.add(f3,       gc);
    }

    private void uploadMark() {
        try {
            Mark m = new Mark();
            m.setC_code(codeF.getText().trim());
            m.setC_type(typeF.getText().trim().toUpperCase());
            m.setRegno(regnoF.getText().trim());
            m.setQ1_mark(parseD(q1F)); m.setQ2_mark(parseD(q2F)); m.setQ3_mark(parseD(q3F));
            m.setAssignment(parseD(assF)); m.setMid(parseD(midF));
            m.setEnd(parseD(endF)); m.setProject(parseD(prjF));

            if (m.getC_code().isEmpty() || m.getRegno().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Course code and Reg No are required.");
                return;
            }
            if (lecturer.uploadMark(m)) {
                Mark calc = markDAO.getFinalMarkByStudent(m.getRegno(), m.getC_code(), m.getC_type());
                if (calc != null) {
                    double[] qs = {m.getQ1_mark(), m.getQ2_mark(), m.getQ3_mark()};
                    Arrays.sort(qs);
                    double best2 = (qs[1] + qs[2]) / 2.0;
                    String msg = String.format(
                            "Marks uploaded successfully.\n\n" +
                                    "Best-2-quiz average: %.1f\n" +
                                    "CA (weighted):       %.2f   %s\n" +
                                    "End (weighted):      %.2f   %s\n" +
                                    "Final:               %.2f",
                            best2,
                            calc.getCA(),  calc.isCAPass()  ? "CA Pass"  : " CA Fail",
                            calc.getEND(), calc.isEndPass() ? " End Pass" : " End Fail",
                            calc.getFinal());
                    JOptionPane.showMessageDialog(this, msg, "Upload Result",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Marks uploaded.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Upload failed – check DB.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "All mark fields must be numbers 0–100.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── View Marks ────────────────────────────────────────────────────────
    private JPanel buildViewMarksPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        String[] cols = {"Regno","Q1","Q2","Q3","Best2Avg","Assign","Mid","End","Project",
                "CA","End(w)","Final","CA Pass","End Pass","Att%(Med)","Att Elig"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(UITheme.BODY); table.setRowHeight(26);
        table.getTableHeader().setFont(UITheme.HEADING);
        table.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Cols: Regno,Q1,Q2,Q3,Best2Avg,Assign,Mid,End,Project,CA,End(w),Final,CA Pass,End Pass,Att%(Med),Att Elig
        int[] mColWidths = {120, 60, 60, 60, 70, 65, 65, 65, 65, 65, 65, 65, 65, 65, 80, 90};
        for (int i = 0; i < mColWidths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(mColWidths[i]);
            table.getColumnModel().getColumn(i).setMinWidth(mColWidths[i]);
        }

        JTextField vCode = new JTextField(8), vType = new JTextField(3), vRegno = new JTextField(12);
        JButton batchBtn = UITheme.primaryBtn("Load");
        JButton regnoBtn = UITheme.primaryBtn("Search by Regno");

        // ── Load full batch ────────────────────────────────────────────────
        batchBtn.addActionListener(e -> {
            String code = vCode.getText().trim();
            String type = vType.getText().trim().toUpperCase();
            if (code.isEmpty()) { JOptionPane.showMessageDialog(p, "Enter a Course Code."); return; }
            model.setRowCount(0);

            if (!type.isEmpty()) {
                // ── Single type (T or P): show rows as before ─────────────
                for (Mark m : markDAO.getFinalMarksByBatch(code, type)) {
                    double[] qs = {m.getQ1_mark(), m.getQ2_mark(), m.getQ3_mark()};
                    Arrays.sort(qs);
                    double best2 = (qs[1] + qs[2]) / 2.0;
                    model.addRow(new Object[]{
                            m.getRegno(),
                            fmt(m.getQ1_mark()), fmt(m.getQ2_mark()), fmt(m.getQ3_mark()),
                            fmt(best2),
                            fmt(m.getAssignment()), fmt(m.getMid()), fmt(m.getEnd()), fmt(m.getProject()),
                            fmt(m.getCA()), fmt(m.getEND()), fmt(m.getFinal()),
                            m.isCAPass()             ? "Pass" : "Fail",
                            m.isEndPass()            ? "Pass" : "Fail",
                            String.format("%.1f%%", m.getAttendanceRateWithMedical()),
                            m.isAttendanceEligible() ? "Eligible" : "Not Eligible"
                    });
                }
            } else {
                // ── Both T and P: combine into one row per student ────────
                java.util.Map<String, Mark> tMap = new java.util.LinkedHashMap<>();
                for (Mark m : markDAO.getFinalMarksByBatch(code, "T")) tMap.put(m.getRegno(), m);

                java.util.Map<String, Mark> pMap = new java.util.LinkedHashMap<>();
                for (Mark m : markDAO.getFinalMarksByBatch(code, "P")) pMap.put(m.getRegno(), m);

                java.util.Set<String> allRegnos = new java.util.LinkedHashSet<>(tMap.keySet());
                allRegnos.addAll(pMap.keySet());

                for (String regno : allRegnos) {
                    Mark t = tMap.get(regno);
                    Mark pp = pMap.get(regno);

                    double q1  = val(t, "q1")  + val(pp, "q1");
                    double q2  = val(t, "q2")  + val(pp, "q2");
                    double q3  = val(t, "q3")  + val(pp, "q3");
                    double ass = val(t, "ass") + val(pp, "ass");
                    double mid = val(t, "mid") + val(pp, "mid");
                    double end = val(t, "end") + val(pp, "end");
                    double prj = val(t, "prj") + val(pp, "prj");

                    double[] qs = {q1, q2, q3};
                    Arrays.sort(qs);
                    double best2 = (qs[1] + qs[2]) / 2.0;

                    double ca    = val(t, "ca")    + val(pp, "ca");
                    double endW  = val(t, "endw")  + val(pp, "endw");
                    double finalM = val(t, "final") + val(pp, "final");

                    boolean caPass  = (t == null || t.isCAPass())  && (pp == null || pp.isCAPass());
                    boolean endPass = (t == null || t.isEndPass()) && (pp == null || pp.isEndPass());

                    double attRate = 0.0; int attCount = 0;
                    if (t  != null) { attRate += t.getAttendanceRateWithMedical();  attCount++; }
                    if (pp != null) { attRate += pp.getAttendanceRateWithMedical(); attCount++; }
                    if (attCount > 0) attRate /= attCount;
                    boolean attElig = (t == null || t.isAttendanceEligible()) && (pp == null || pp.isAttendanceEligible());

                    model.addRow(new Object[]{
                            regno,
                            fmt(q1), fmt(q2), fmt(q3), fmt(best2),
                            fmt(ass), fmt(mid), fmt(end), fmt(prj),
                            fmt(ca), fmt(endW), fmt(finalM),
                            caPass  ? "Pass" : "Fail",
                            endPass ? "Pass" : "Fail",
                            String.format("%.1f%%", attRate),
                            attElig ? "Eligible" : "Not Eligible"
                    });
                }
            }
            if (model.getRowCount() == 0)
                JOptionPane.showMessageDialog(p, "No marks found for course: " + code + (type.isEmpty() ? "" : " (" + type + ")"));
        });

        // ── Search by Regno ────────────────────────────────────────────────
        regnoBtn.addActionListener(e -> {
            String regno = vRegno.getText().trim();
            String code  = vCode.getText().trim();
            String type  = vType.getText().trim().toUpperCase();
            if (regno.isEmpty()) { JOptionPane.showMessageDialog(p, "Enter a Reg No."); return; }
            if (code.isEmpty())  { JOptionPane.showMessageDialog(p, "Enter a Course Code."); return; }
            model.setRowCount(0);

            java.util.List<String> types = new java.util.ArrayList<>();
            if (type.isEmpty()) { types.add("T"); types.add("P"); }
            else types.add(type);

            for (String t : types) {
                Mark m = markDAO.getFinalMarkByStudent(regno, code, t);
                if (m == null) continue;
                double[] qs = {m.getQ1_mark(), m.getQ2_mark(), m.getQ3_mark()};
                Arrays.sort(qs);
                double best2 = (qs[1] + qs[2]) / 2.0;
                model.addRow(new Object[]{
                        m.getRegno(),
                        fmt(m.getQ1_mark()), fmt(m.getQ2_mark()), fmt(m.getQ3_mark()),
                        fmt(best2),
                        fmt(m.getAssignment()), fmt(m.getMid()), fmt(m.getEnd()), fmt(m.getProject()),
                        fmt(m.getCA()), fmt(m.getEND()), fmt(m.getFinal()),
                        m.isCAPass()             ? "Pass" : "Fail",
                        m.isEndPass()            ? "Pass" : "Fail",
                        String.format("%.1f%%", m.getAttendanceRateWithMedical()),
                        m.isAttendanceEligible() ? "Eligible" : "Not Eligible"
                });
            }
            if (model.getRowCount() == 0)
                JOptionPane.showMessageDialog(p, "No marks found for " + regno + " in " + code);
        });

        JLabel hint = new JLabel("  Type: blank = T+P combined (summed)  |  T or P = single type");
        hint.setFont(UITheme.SMALL);
        hint.setForeground(UITheme.TEXT_MUTED);

        // Row 1: batch load
        JPanel filterRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterRow1.setBackground(UITheme.BG);
        filterRow1.add(new JLabel("Course:")); filterRow1.add(vCode);
        filterRow1.add(new JLabel("Type (opt.):")); filterRow1.add(vType);
        filterRow1.add(batchBtn); filterRow1.add(hint);

        // Row 2: regno search
        JPanel filterRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterRow2.setBackground(UITheme.BG);
        JLabel regHint = new JLabel("(course + type required)");
        regHint.setFont(UITheme.SMALL); regHint.setForeground(UITheme.TEXT_MUTED);
        filterRow2.add(new JLabel("Reg No:")); filterRow2.add(vRegno);
        filterRow2.add(regnoBtn); filterRow2.add(regHint);

        JPanel filter = new JPanel(new GridLayout(2, 1));
        filter.setBackground(UITheme.BG);
        filter.add(filterRow1);
        filter.add(filterRow2);

        p.add(filter, BorderLayout.NORTH);
        p.add(UITheme.scroll(table), BorderLayout.CENTER);
        return p;
    }

    /** Helper: extract a named component from a Mark (returns 0.0 if mark is null). */
    private double val(Mark m, String field) {
        if (m == null) return 0.0;
        switch (field) {
            case "q1":    return m.getQ1_mark();
            case "q2":    return m.getQ2_mark();
            case "q3":    return m.getQ3_mark();
            case "ass":   return m.getAssignment();
            case "mid":   return m.getMid();
            case "end":   return m.getEnd();
            case "prj":   return m.getProject();
            case "ca":    return m.getCA();
            case "endw":  return m.getEND();
            case "final": return m.getFinal();
            default:      return 0.0;
        }
    }

    // ── View Grades ───────────────────────────────────────────────────────
    private JPanel buildViewGradesPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        String[] cols = {"Regno","Course","Type","CA(w)","End(w)","Final","Grade",
                "CA Elig","End Elig","Att%","Att Elig"};
        DefaultTableModel gModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable gTable = new JTable(gModel);
        gTable.setFont(UITheme.BODY); gTable.setRowHeight(26);
        gTable.getTableHeader().setFont(UITheme.HEADING);
        gTable.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        gTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Fix column widths so Regno is always fully visible
        // Cols: Regno,Course,Type,CA(w),End(w),Final,Grade,CA Elig,End Elig,Att%,Att Elig
        int[] gColWidths = {120, 80, 55, 70, 70, 70, 70, 70, 70, 70, 90};
        for (int i = 0; i < gColWidths.length; i++) {
            gTable.getColumnModel().getColumn(i).setPreferredWidth(gColWidths[i]);
            gTable.getColumnModel().getColumn(i).setMinWidth(gColWidths[i]);
        }
        gTable.getColumnModel().getColumn(6).setCellRenderer(gradeColourRenderer());

        JTextField gCode  = new JTextField(8);
        JTextField gType  = new JTextField(4);   // optional — blank = both T and P
        JTextField gRegno = new JTextField(12);  // optional — blank = all students
        JButton viewBtn = UITheme.primaryBtn("Load Grades");

        viewBtn.addActionListener(e -> {
            String code  = gCode.getText().trim();
            String type  = gType.getText().trim().toUpperCase();
            String regno = gRegno.getText().trim();

            if (code.isEmpty()) { JOptionPane.showMessageDialog(null, "Enter a Course Code."); return; }
            gModel.setRowCount(0);

            // Determine which types to load
            java.util.List<String> types = new java.util.ArrayList<>();
            if (type.isEmpty()) { types.add("T"); types.add("P"); }
            else                  types.add(type);

            for (String t : types) {
                java.util.List<Grade> grades;
                if (!regno.isEmpty()) {
                    // Single student
                    Grade g = gradeDAO.getGradeByStudent(regno, code, t);
                    grades = (g != null) ? java.util.List.of(g) : java.util.List.of();
                } else {
                    grades = gradeDAO.getGradesByBatch(code, t);
                }
                for (Grade g : grades) {
                    if ("N/A".equals(g.getGrade())) continue;
                    gModel.addRow(new Object[]{
                            g.getRegno(), g.getCourseId(), g.getCourseType(),
                            fmt(g.getCaWeighted()), fmt(g.getEndWeighted()), fmt(g.getFinalMark()),
                            g.getGrade(),
                            g.isCaEligible()  ? "Pass" : "Fail",
                            g.isEndEligible() ? "Pass" : "Fail",
                            String.format("%.1f%%", g.getAttendanceRate()),
                            g.isAttEligible() ? "Eligible" : "Not Eligible"
                    });
                }
            }
        });

        JLabel legend = new JLabel("  ECA=CA Fail  |  EE=End Fail  |  E=Both  |  MC=Medical  |  Type: blank=both T&P");
        legend.setFont(UITheme.SMALL); legend.setForeground(UITheme.TEXT_MUTED);

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filter.setBackground(UITheme.BG);
        filter.add(new JLabel("Course:"));      filter.add(gCode);
        filter.add(new JLabel("Type (opt.):")); filter.add(gType);
        filter.add(new JLabel("Reg No (opt.):")); filter.add(gRegno);
        filter.add(viewBtn); filter.add(legend);

        p.add(filter, BorderLayout.NORTH);
        p.add(UITheme.scroll(gTable), BorderLayout.CENTER);
        return p;
    }

    // ── SGPA & CGPA ───────────────────────────────────────────────────────
    private JPanel buildSGPAPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UITheme.BG);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        String[] cols = {"Regno","Course","Type","Grade"};
        DefaultTableModel sModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable sTable = new JTable(sModel);
        sTable.setFont(UITheme.BODY); sTable.setRowHeight(26);
        sTable.getTableHeader().setFont(UITheme.HEADING);
        sTable.getTableHeader().setBackground(UITheme.TABLE_HEADER);
        sTable.getColumnModel().getColumn(3).setCellRenderer(gradeColourRenderer());
        sTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        sTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        sTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        sTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        sTable.getColumnModel().getColumn(3).setPreferredWidth(70);

        // ── Result labels ─────────────────────────────────────────────────
        JLabel sgpaResult = new JLabel("SGPA: –");
        sgpaResult.setFont(UITheme.HEADING);
        sgpaResult.setForeground(UITheme.PRIMARY);

        JLabel cgpaResult = new JLabel("   CGPA: –");
        cgpaResult.setFont(UITheme.HEADING);
        cgpaResult.setForeground(new Color(13, 71, 161));

        JLabel gpaNote = new JLabel("   (ENG2122 excluded from all CGPA calculations)");
        gpaNote.setFont(UITheme.SMALL);
        gpaNote.setForeground(UITheme.TEXT_MUTED);

        JPanel resultBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        resultBar.setBackground(new Color(227, 242, 253));
        resultBar.setBorder(BorderFactory.createLineBorder(new Color(187, 222, 251), 1));
        resultBar.add(sgpaResult);
        resultBar.add(new JSeparator(SwingConstants.VERTICAL) {{
            setPreferredSize(new Dimension(1, 20));
        }});
        resultBar.add(cgpaResult);
        resultBar.add(gpaNote);

        // ── Input ─────────────────────────────────────────────────────────
        JTextField sRegno = new JTextField(12);
        JButton viewBtn = UITheme.primaryBtn("View SGPA & CGPA");
        viewBtn.addActionListener(e -> {
            String regno = sRegno.getText().trim();
            if (regno.isEmpty()) { JOptionPane.showMessageDialog(p, "Enter Reg No."); return; }
            sModel.setRowCount(0);

            // Per-course grades
            List<Grade> all = gradeDAO.getGPAWithGrade(regno);
            for (Grade g : all) {
                if ("N/A".equals(g.getGrade())) continue;
                sModel.addRow(new Object[]{
                        g.getRegno(), g.getCourseId(), g.getCourseType(),
                        g.getGrade()
                });
            }

            // SGPA
            Grade sg = gradeDAO.getSGPAByStudent(regno);
            if (sg.isSgpaWithheld()) {
                sgpaResult.setText("SGPA: WH (Withheld – MC grade present)");
                sgpaResult.setForeground(new Color(180, 60, 0));
            } else {
                sgpaResult.setText(String.format("SGPA: %.3f  (%s)", sg.getSGPA(), sg.getGrade()));
                sgpaResult.setForeground(UITheme.PRIMARY);
            }

            // CGPA
            Grade cg = gradeDAO.getCGPAByStudent(regno);
            if (cg.isCgpaWithheld()) {
                cgpaResult.setText("   CGPA: WH (Withheld – MC grade present)");
                cgpaResult.setForeground(new Color(180, 60, 0));
            } else {
                cgpaResult.setText(String.format("   CGPA: %.3f  (%s)", cg.getCGPA(), cg.getGrade()));
                cgpaResult.setForeground(new Color(13, 71, 161));
            }
        });

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterRow.setBackground(UITheme.BG);
        filterRow.add(new JLabel("Reg No:")); filterRow.add(sRegno);
        filterRow.add(viewBtn);

        JPanel north = new JPanel(new BorderLayout(0, 6));
        north.setBackground(UITheme.BG);
        north.add(filterRow, BorderLayout.NORTH);
        north.add(resultBar, BorderLayout.SOUTH);

        p.add(north,               BorderLayout.NORTH);
        p.add(UITheme.scroll(sTable), BorderLayout.CENTER);
        return p;
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private JLabel lbl(String t) { JLabel l = new JLabel(t); l.setFont(UITheme.BODY); return l; }
    private double parseD(JTextField f) {
        String t = f.getText().trim();
        return t.isEmpty() ? 0.0 : Double.parseDouble(t);
    }
    private String fmt(double v) { return String.format("%.2f", v); }

    private DefaultTableCellRenderer gradeColourRenderer() {
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
                    default             -> { setForeground(UITheme.TEXT_PRIMARY);
                        setBackground(sel ? t.getSelectionBackground() : Color.WHITE); }
                }
                return this;
            }
        };
    }}