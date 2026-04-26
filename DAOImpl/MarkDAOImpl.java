package ums.dao;

import ums.db.DBConnection;
import ums.model.Mark;

import javax.swing.*;
import java.sql.*;
import java.util.*;

/**
 * All mark calculation logic.
 *
 * Quiz rule  :  3 quizzes entered, best 2 are used.
 *               Each quiz is out of 100. Best-two average = (q_hi + q_mid) / 2.
 *               That average is then scaled to the course quiz weight
 *               (e.g. *0.10 for courses where quiz contributes 10 marks).
 *
 * CA pass    :  (CA weighted / CA max weight) * 100 >= 40
 *               i.e. student earned at least 40 % of the available CA marks.
 *
 * End pass   :  raw End mark >= 35   (out of 100 before weighting)
 *
 * Medical    :  an APPROVED record in Medical_Mid / Medical_End / Medical_Attendence
 *               for this student + course sets the corresponding flag on Mark.
 *
 * Attendance :  pulled from Attendance table.  Two rates:
 *               - plain rate  (Present only)
 *               - with-medical rate (Present + Medical status)
 *               Eligible when with-medical rate >= 80.
 *
 * Course formulas (weights in marks out of 100 final):
 * ┌─────────────┬──────────┬──────────┬───────────┬───────────────┐
 * │ C_code      │ Quiz(10) │ Ass(10)  │ Mid       │ End / Project │
 * ├─────────────┼──────────┼──────────┼───────────┼───────────────┤
 * │ ICT2132     │    –     │    –     │ 20%       │ Prj 20% +     │
 * │             │          │          │           │ End 60%(P)    │
 * │ ICT2122     │  10%     │    –     │ 20%       │ End 70%(T)    │
 * │ ICT2142     │  10%     │ 20%      │    –      │ End 70%(T)    │
 * │ ICT2152     │    –     │    –     │ 20%       │ Prj 20% +     │
 * │             │          │          │           │ End 60%(P)    │
 * │ ICT2113     │  10%     │    –     │ 30%       │ End 60%(P)    │
 * │ TCS2112     │  10%     │    –     │ 20%       │ End 70%(T)    │
 * │ ENG2122     │    –     │ 10%      │ 20%       │ End 70%(T)    │
 * │ TCS2122     │    –     │ 30%      │    –      │ Prj 70%       │
 * │ default     │  10%     │    –     │ 20%       │ End 70%(T)/60%(P)│
 * └─────────────┴──────────┴──────────┴───────────┴───────────────┘
 */
public final class MarkDAOImpl implements IMarkDAO {

    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    // ══════════════════════════════════════════════════════════════════════
    //  UPLOAD
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public boolean uploadMark(Mark m) {
        // A course like TCS2122 has no quizzes/mid. Inserting 0s is fine via ON DUPLICATE KEY.
        // Only return false if ALL inserts fail (total failure).
        boolean q1  = setQuizMark(m.getC_code(), m.getC_type(), m.getRegno(), m.getQ1_mark(), 1);
        boolean q2  = setQuizMark(m.getC_code(), m.getC_type(), m.getRegno(), m.getQ2_mark(), 2);
        boolean q3  = setQuizMark(m.getC_code(), m.getC_type(), m.getRegno(), m.getQ3_mark(), 3);
        boolean ass = setAssignmentMark(m.getC_code(), m.getC_type(), m.getRegno(), m.getAssignment());
        boolean mid = setMidMark(m.getC_code(), m.getC_type(), m.getRegno(), m.getMid());
        boolean end = setEndMark(m.getC_code(), m.getC_type(), m.getRegno(), m.getEnd());
        boolean prj = setProjectMark(m.getC_code(), m.getC_type(), m.getRegno(), m.getProject());
        // Return true if at least one component was saved successfully
        return q1 || q2 || q3 || ass || mid || end || prj;
    }

    /** Upsert a single quiz mark.  ON DUPLICATE KEY handles re-entry. */
    private boolean setQuizMark(String code, String type, String regno, double mark, int no) {
        // Validate range
        if (mark < 0 || mark > 100) {
            JOptionPane.showMessageDialog(null,
                    "Quiz " + no + " mark must be 0–100. Got: " + mark);
            return false;
        }
        String sql = "INSERT INTO Quiz(C_code,C_type,Regno,Q_no,Q_mark) VALUES(?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE Q_mark=VALUES(Q_mark)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code);  ps.setString(2, type);
            ps.setString(3, regno); ps.setInt(4, no); ps.setDouble(5, mark);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Quiz insert error: " + e.getMessage());
            return false;
        }
    }

    private boolean setAssignmentMark(String code, String type, String regno, double mark) {
        String sql = "INSERT INTO Assignment(C_code,C_type,Regno,A_mark) VALUES(?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE A_mark=VALUES(A_mark)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1,code); ps.setString(2,type);
            ps.setString(3,regno); ps.setDouble(4,mark);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null,"Assign: "+e.getMessage()); return false; }
    }

    private boolean setMidMark(String code, String type, String regno, double mark) {
        String sql = "INSERT INTO Mid_exam(C_code,C_type,Regno,Mid_mark) VALUES(?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE Mid_mark=VALUES(Mid_mark)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1,code); ps.setString(2,type);
            ps.setString(3,regno); ps.setDouble(4,mark);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null,"Mid: "+e.getMessage()); return false; }
    }

    private boolean setEndMark(String code, String type, String regno, double mark) {
        String sql = "INSERT INTO End_exam(C_code,C_type,Regno,End_mark) VALUES(?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE End_mark=VALUES(End_mark)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1,code); ps.setString(2,type);
            ps.setString(3,regno); ps.setDouble(4,mark);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null,"End: "+e.getMessage()); return false; }
    }

    private boolean setProjectMark(String code, String type, String regno, double mark) {
        String sql = "INSERT INTO Project(C_code,C_type,Regno,P_mark) VALUES(?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE P_mark=VALUES(P_mark)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1,code); ps.setString(2,type);
            ps.setString(3,regno); ps.setDouble(4,mark);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { JOptionPane.showMessageDialog(null,"Project: "+e.getMessage()); return false; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  LOAD RAW MARKS FROM DB
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Loads all raw marks for every student in (courseId, courseType),
     * then calculates derived fields (BestQuizWeighted, CA, END, Final,
     * CAPass, EndPass) and enriches with medical + attendance data.
     */
    public List<Mark> calcAllMarks(String courseId, String courseType) {
        Map<String, Mark> map = new LinkedHashMap<>();

        loadQuizzes(courseId, courseType, map);
        loadAssignments(courseId, courseType, map);
        loadMidMarks(courseId, courseType, map);
        loadEndMarks(courseId, courseType, map);
        loadProjectMarks(courseId, courseType, map);

        List<Mark> result = new ArrayList<>();
        for (Mark m : map.values()) {
            calculateDerivedMarks(m);
            loadMedicalFlags(m);
            loadAttendanceData(m);
            result.add(m);
        }
        return result;
    }

    /** Load marks for BOTH T and P types merged — shows full picture per course. */
    public List<Mark> calcBothTypes(String courseId) {
        List<Mark> result = new ArrayList<>();
        result.addAll(calcAllMarks(courseId, "T"));
        result.addAll(calcAllMarks(courseId, "P"));
        return result;
    }

    /** Load marks for a single student across both T and P types. */
    public List<Mark> getMarksByStudent(String regno, String courseId) {
        List<Mark> result = new ArrayList<>();
        for (String t : new String[]{"T","P"}) {
            Mark m = calcAllMarks(courseId, t).stream()
                    .filter(mk -> regno.equals(mk.getRegno()))
                    .findFirst().orElse(null);
            if (m != null) result.add(m);
        }
        return result;
    }

    // ── individual loaders ────────────────────────────────────────────────

    private void loadQuizzes(String code, String type, Map<String,Mark> map) {
        String sql = "SELECT * FROM Quiz WHERE C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code); ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String regno = rs.getString("Regno");
                Mark m = getOrCreate(map, regno, code, type);
                int no = rs.getInt("Q_no");
                double mk = rs.getDouble("Q_mark");
                if      (no == 1) m.setQ1_mark(mk);
                else if (no == 2) m.setQ2_mark(mk);
                else               m.setQ3_mark(mk);
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Quiz load: " + e.getMessage()); }
    }

    private void loadAssignments(String code, String type, Map<String,Mark> map) {
        String sql = "SELECT * FROM Assignment WHERE C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code); ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Mark m = getOrCreate(map, rs.getString("Regno"), code, type);
                m.setAssignment(rs.getDouble("A_mark"));
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Assignment load: " + e.getMessage()); }
    }

    private void loadMidMarks(String code, String type, Map<String,Mark> map) {
        String sql = "SELECT * FROM Mid_exam WHERE C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code); ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Mark m = getOrCreate(map, rs.getString("Regno"), code, type);
                m.setMid(rs.getDouble("Mid_mark"));
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Mid load: " + e.getMessage()); }
    }

    private void loadEndMarks(String code, String type, Map<String,Mark> map) {
        String sql = "SELECT * FROM End_exam WHERE C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code); ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Mark m = getOrCreate(map, rs.getString("Regno"), code, type);
                m.setEnd(rs.getDouble("End_mark"));
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "End load: " + e.getMessage()); }
    }

    private void loadProjectMarks(String code, String type, Map<String,Mark> map) {
        String sql = "SELECT * FROM Project WHERE C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, code); ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Mark m = getOrCreate(map, rs.getString("Regno"), code, type);
                m.setProject(rs.getDouble("P_mark"));
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, "Project load: " + e.getMessage()); }
    }

    private Mark getOrCreate(Map<String,Mark> map, String regno, String code, String type) {
        return map.computeIfAbsent(regno, k -> {
            Mark nm = new Mark();
            nm.setRegno(k); nm.setC_code(code); nm.setC_type(type);
            return nm;
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DERIVED MARK CALCULATION
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Core calculation.  Uses BEST 2 of 3 quizzes.
     *
     * Steps:
     *  1. Sort the 3 quiz scores descending; keep top 2.
     *  2. Average them  (0–100 scale).
     *  3. Scale by quizWeight (0.0–1.0 fraction of final mark).
     *  4. Compute CA = quizWeighted + other weighted components.
     *  5. Compute END = end * endWeight.
     *  6. Final = CA + END.
     *  7. CAPass  = (CA / caMaxWeight) * 100 >= 40
     *     EndPass = raw End >= 35
     */
    private void calculateDerivedMarks(Mark m) {
        String code = m.getC_code();
        String type = m.getC_type();   // "T" or "P"

        double q1 = m.getQ1_mark(), q2 = m.getQ2_mark(), q3 = m.getQ3_mark();

        // ── Best-2 quiz logic ─────────────────────────────────────────────
        double[] quizzes = {q1, q2, q3};
        Arrays.sort(quizzes);                         // ascending
        double best2Avg = (quizzes[1] + quizzes[2]) / 2.0;   // top two average (0–100)

        double mid  = m.getMid();
        double end  = m.getEnd();
        double ass  = m.getAssignment();
        double prj  = m.getProject();

        double ca         = 0.0;
        double caMaxWeight = 0.0;
        double endWeight;

        // ── End weight by type ────────────────────────────────────────────
        if ("T".equals(type)) endWeight = 0.70;
        else                   endWeight = 0.60;

        // ── Course-specific CA formula ────────────────────────────────────
        // All weights expressed as fractions of 100 final marks.
        // e.g. quiz contributes 10 marks → weight = 0.10
        switch (code) {
            case "ICT2132" -> {
                // CA = Project(20%) + Mid(20%)  →  max CA = 40
                ca          = prj * 0.20 + mid * 0.20;
                caMaxWeight = 0.40;
                endWeight   = 0.60;   // Practical course
            }
            case "ICT2122" -> {
                // CA = BestTwoQuiz(10%) + Mid(20%)  → max CA = 30
                ca          = best2Avg * 0.10 + mid * 0.20;
                caMaxWeight = 0.30;
                // Theory → endWeight stays 0.70
            }
            case "ICT2142" -> {
                // CA = BestTwoQuiz(10%) + Assignment(20%) → max CA = 30
                ca          = best2Avg * 0.10 + ass * 0.20;
                caMaxWeight = 0.30;
                // Theory → endWeight stays 0.70
            }
            case "ICT2152" -> {
                // BUG FIX: DB has ICT2152 as C_type='T' (Theory).
                // endWeight uses type-based value (0.70 for T, already set above).
                // CA = Mid(20%) + Assignment(10%) — same structure as ENG2122
                // However keeping original formula: CA = Project(20%) + Mid(20%)
                // endWeight stays as already set (0.70 for T, 0.60 for P)
                ca          = prj * 0.20 + mid * 0.20;
                caMaxWeight = 0.40;
                // endWeight already set correctly above based on C_type
            }
            case "ICT2113" -> {
                // CA = BestTwoQuiz(10%) + Mid(30%) → max CA = 40
                ca          = best2Avg * 0.10 + mid * 0.30;
                caMaxWeight = 0.40;
                endWeight   = 0.60;
            }
            case "TCS2112" -> {
                // CA = BestTwoQuiz(10%) + Mid(20%) → max CA = 30
                ca          = best2Avg * 0.10 + mid * 0.20;
                caMaxWeight = 0.30;
                // Theory → endWeight stays 0.70
            }
            case "ENG2122" -> {
                // CA = Assignment(10%) + Mid(20%) → max CA = 30
                ca          = ass * 0.10 + mid * 0.20;
                caMaxWeight = 0.30;
                // Theory → endWeight stays 0.70
            }
            case "TCS2122" -> {
                // No quiz / mid.  CA = Assignment(30%), END = Project(70%)
                ca          = ass * 0.30;
                caMaxWeight = 0.30;
                endWeight   = 0.70;
                // Override END to use Project instead of End exam
                m.setEND(prj * endWeight);
                m.setCA(ca);
                m.setCAMaxWeight(caMaxWeight);
                m.setEndMaxWeight(endWeight);
                m.setBestQuizWeighted(0);
                double finalMark = ca + prj * endWeight;
                m.setFinal(finalMark);
                // Pass/fail: CA >= 40% of 30 = 12, Project >= 40
                m.setCAPass((ca / caMaxWeight) * 100.0 >= 40.0);
                m.setEndPass(prj >= 35.0);
                return;   // done for TCS2122
            }
            default -> {
                // Default: quiz(10%) + mid(20%) → max CA = 30
                ca          = best2Avg * 0.10 + mid * 0.20;
                caMaxWeight = 0.30;
            }
        }

        double endWeighted = end * endWeight;
        double finalMark   = ca + endWeighted;

        // ── Pass / Fail determination ─────────────────────────────────────
        // CA pass: student scored ≥ 40 % of the CA portion
        // CA pass: CA percentage >= 40%
        boolean caPass  = (caMaxWeight > 0) && (ca / caMaxWeight) * 100.0 >= 40.0;
        // End pass: raw end mark >= 35 (out of 100)
        boolean endPass = end >= 35.0;

        // ── Store in model ────────────────────────────────────────────────
        m.setBestQuizWeighted(best2Avg * 0.10);
        m.setCA(ca);
        m.setEND(endWeighted);
        m.setFinal(finalMark);
        m.setCAMaxWeight(caMaxWeight);
        m.setEndMaxWeight(endWeight);
        m.setCAPass(caPass);
        m.setEndPass(endPass);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MEDICAL FLAGS  (check Approved records in medical tables)
    // ══════════════════════════════════════════════════════════════════════

    private void loadMedicalFlags(Mark m) {
        m.setMedicalMid(hasMedical(m.getRegno(), m.getC_code(), m.getC_type(), "Medical_Mid"));
        m.setMedicalEnd(hasMedical(m.getRegno(), m.getC_code(), m.getC_type(), "Medical_End"));
        m.setMedicalAttendance(hasMedical(m.getRegno(), m.getC_code(), m.getC_type(), "Medical_Attendence"));
    }

    private boolean hasMedical(String regno, String code, String type, String table) {
        String sql = "SELECT COUNT(*) FROM " + table +
                " WHERE Regno=? AND C_code=? AND C_type=? AND Medical_Status='Approved'";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, regno); ps.setString(2, code); ps.setString(3, type);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { return false; }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ATTENDANCE
    // ══════════════════════════════════════════════════════════════════════

    private void loadAttendanceData(Mark m) {
        String sql = "SELECT " +
                "  COUNT(*) AS total, " +
                "  SUM(CASE WHEN Status='Present' THEN 1 ELSE 0 END) AS present_only, " +
                "  SUM(CASE WHEN Status IN ('Present','Medical') THEN 1 ELSE 0 END) AS present_med " +
                "FROM Attendance WHERE Regno=? AND C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, m.getRegno()); ps.setString(2, m.getC_code()); ps.setString(3, m.getC_type());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int total = rs.getInt("total");
                if (total > 0) {
                    double rawRate  = rs.getDouble("present_only") * 100.0 / total;
                    double medRate  = rs.getDouble("present_med")  * 100.0 / total;
                    m.setAttendanceRate(rawRate);
                    m.setAttendanceRateWithMedical(medRate);
                    m.setAttendanceEligible(medRate >= 80.0);
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,"Can Get Attendance data");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  INTERFACE METHODS
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public Mark getFinalMarkByStudent(String studentId, String courseId, String C_type) {
        return calcAllMarks(courseId, C_type).stream()
                .filter(m -> studentId.equals(m.getRegno()))
                .findFirst().orElse(null);
    }

    @Override
    public Mark getCAByStudent(String studentId, String courseId, String C_type) {
        return getFinalMarkByStudent(studentId, courseId, C_type);
    }

    @Override
    public List<Mark> getFinalMarksByBatch(String courseId, String C_type) {
        return calcAllMarks(courseId, C_type);
    }

    @Override
    public List<Mark> getCAByBatch(String courseId, String C_type) {
        return calcAllMarks(courseId, C_type);
    }
}
