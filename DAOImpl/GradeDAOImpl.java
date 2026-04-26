package ums.dao;

import ums.db.DBConnection;
import ums.model.Course;
import ums.model.Grade;
import ums.model.Mark;

import javax.swing.*;
import java.sql.*;
import java.util.*;

/**
 * Grade calculation rules (updated):
 *
 *  ENG2122 (English III) is a LANGUAGE COURSE.
 *    – It IS graded normally (A+…E, MC, ECA, EE).
 *    – It is EXCLUDED from SGPA calculation (credit not counted).
 *    – Students can see their grade for ENG2122 but it doesn't affect GPA.
 *
 *  MC  – Approved medical Mid or End → grade = "MC", GPA = 0, SGPA withheld = "WH"
 *  ECA – CA component < 40 % of its weight → grade = "ECA"
 *  EE  – End mark < 40 OR attendance < 80 % → grade = "EE"
 *  E   – Both CA and End fail → grade = "E"
 *  WH  – Any course (excluding ENG) = MC → SGPA label = "WH"
 *
 *  SGPA = Σ(GPA_pts × credits)  /  Σ(credits)
 *         where ENG2122 credits are NOT included in either sum.
 */
public final class GradeDAOImpl implements IGradeDAO {

    private final IMarkDAO markDAO = new MarkDAOImpl();
    private Connection conn() { return DBConnection.getInstance().getConnection(); }

    /** Courses excluded from GPA calculation */
    private static final Set<String> GPA_EXCLUDED = Set.of("ENG2122");

    // ══════════════════════════════════════════════════════════════════════
    //  GRADE FOR ONE STUDENT + COURSE
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public Grade getGradeByStudent(String studentId, String courseId, String C_type) {
        Grade g = new Grade();
        g.setRegno(studentId);
        g.setCourseId(courseId);
        g.setCourseType(C_type);

        Mark m = markDAO.getFinalMarkByStudent(studentId, courseId, C_type);
        if (m == null) {
            g.setGrade("N/A");
            g.setGpaPoints(0.0);
            return g;
        }

        // Copy eligibility data
        g.setCaWeighted(m.getCA());
        g.setEndWeighted(m.getEND());
        g.setFinalMark(m.getFinal());
        g.setCaMark(m.getCA());
        g.setAttendanceRate(m.getAttendanceRateWithMedical());
        g.setCaEligible(m.isCAPass());
        g.setEndEligible(m.isEndPass());
        g.setAttEligible(m.isAttendanceEligible());

        // ── Rule 1: Approved medical Mid or End → MC ──────────────────────
        if (m.isMedicalMid() || m.isMedicalEnd()) {
            g.setGrade("MC");
            g.setGpaPoints(0.0);
            return g;
        }

        // ── Rule 5: Attendance not eligible ──────────────────────────────
        if (!m.isAttendanceEligible()) {
            g.setGrade("EE");
            g.setGpaPoints(0.0);
            return g;
        }

        // ── Rules 2/3/4: CA and End pass/fail ────────────────────────────
        boolean caFail  = !m.isCAPass();
        boolean endFail = !m.isEndPass();

        if (caFail && endFail) { g.setGrade("E");   g.setGpaPoints(0.0); return g; }
        if (caFail)            { g.setGrade("ECA"); g.setGpaPoints(0.0); return g; }
        if (endFail)           { g.setGrade("EE");  g.setGpaPoints(0.0); return g; }

        // ── Normal alpha grade ────────────────────────────────────────────
        String alpha = findAlphaGrade(m.getFinal());
        g.setGrade(alpha);
        // ENG2122 gets a grade string but GPA points set 0 (excluded from SGPA sum)
        g.setGpaPoints(GPA_EXCLUDED.contains(courseId) ? 0.0 : findGPAPoint(alpha));
        return g;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  GRADES FOR WHOLE BATCH
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public List<Grade> getGradesByBatch(String courseId, String courseType) {
        List<Mark> marks = markDAO.getFinalMarksByBatch(courseId, courseType);
        List<Grade> grades = new ArrayList<>();
        for (Mark m : marks) {
            Grade g = getGradeByStudent(m.getRegno(), courseId, courseType);
            grades.add(g);
        }
        return grades;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SGPA  (ENG2122 excluded)
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public Grade getSGPAByStudent(String studentId) {
        List<Grade> allGrades = getGPAWithGrade(studentId);
        Grade summary = new Grade();
        summary.setRegno(studentId);

        // WH = any NON-ENG course has MC grade
        boolean withheld = allGrades.stream()
            .anyMatch(g -> "MC".equals(g.getGrade()) && !GPA_EXCLUDED.contains(g.getCourseId()));
        summary.setSgpaWithheld(withheld);

        if (withheld) {
            summary.setSGPA(0.0);
            summary.setGrade("WH");
            return summary;
        }

        double totalPoints = 0.0, totalCredits = 0.0;
        for (Grade gd : allGrades) {
            // Skip ENG2122 (and other excluded courses) from GPA calculation
            if (GPA_EXCLUDED.contains(gd.getCourseId())) continue;
            // Skip N/A (no marks yet)
            if ("N/A".equals(gd.getGrade())) continue;

            int credit = getCreditForCourse(gd.getCourseId(), gd.getCourseType());
            totalPoints  += gd.getGpaPoints() * credit;
            totalCredits += credit;
        }
        double sgpa = totalCredits > 0 ? totalPoints / totalCredits : 0.0;
        summary.setSGPA(sgpa);
        summary.setGrade(sgpaLabel(sgpa));
        return summary;
    }

    @Override
    public List<Grade> getGPAWithGrade(String studentId) {
        List<Grade> grades = new ArrayList<>();
        // Only get courses the student is enrolled in
        String sql = "SELECT DISTINCT ce.C_code, ce.C_type " +
                     "FROM Course_Enrollment ce WHERE ce.Regno=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String code = rs.getString("C_code");
                String type = rs.getString("C_type");
                Grade gd = getGradeByStudent(studentId, code, type);
                grades.add(gd);
            }
        } catch (SQLException e) { JOptionPane.showMessageDialog(null, e.getMessage()); }
        return grades;
    }

    @Override
    public Grade getCGPAByStudent(String studentId) {
        Grade summary = new Grade();
        summary.setRegno(studentId);

        // Load ALL courses the student has ever enrolled in (full history)
        List<Grade> allGrades = new ArrayList<>();
        String sql = "SELECT DISTINCT ce.C_code, ce.C_type " +
                "FROM Course_Enrollment ce WHERE ce.Regno=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, studentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String code = rs.getString("C_code");
                String type = rs.getString("C_type");
                Grade gd = getGradeByStudent(studentId, code, type);
                allGrades.add(gd);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            summary.setCGPA(0.0);
            return summary;
        }

        // Check WH: any non-ENG course with MC grade across all history
        boolean withheld = allGrades.stream()
                .anyMatch(g -> "MC".equals(g.getGrade()) && !GPA_EXCLUDED.contains(g.getCourseId()));
        summary.setCgpaWithheld(withheld);

        if (withheld) {
            summary.setCGPA(0.0);
            summary.setGrade("WH");
            return summary;
        }

        double totalPoints  = 0.0;
        double totalCredits = 0.0;

        for (Grade gd : allGrades) {
            // Skip ENG2122 and any course with no marks yet
            if (GPA_EXCLUDED.contains(gd.getCourseId())) continue;
            if ("N/A".equals(gd.getGrade()))             continue;

            int credit = getCreditForCourse(gd.getCourseId(), gd.getCourseType());
            totalPoints  += gd.getGpaPoints() * credit;
            totalCredits += credit;
        }

        double cgpa = totalCredits > 0 ? totalPoints / totalCredits : 0.0;
        summary.setCGPA(cgpa);
        summary.setGrade(sgpaLabel(cgpa));  // reuse same classification labels
        return summary;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ELIGIBILITY  (CA + End + Attendance shown to student)
    // ══════════════════════════════════════════════════════════════════════

    @Override
    public Grade getEligibilityByStudent(String studentId, String courseId, String C_type) {
        return getGradeByStudent(studentId, courseId, C_type);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════

    private int getCreditForCourse(String courseId, String courseType) {
        String sql = "SELECT Credit FROM Course WHERE C_code=? AND C_type=?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, courseId); ps.setString(2, courseType);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("Credit");
        } catch (SQLException ignored) {}
        return 1;
    }

    // BUG FIX: UGC Commission Circular No. 12-2024 grade boundaries
    // A+ >= 85, A >= 75, A- >= 70, B+ >= 65, B >= 60, B- >= 55
    // C+ >= 50, C >= 45, C- >= 40, D >= 35, E < 35
    public String findAlphaGrade(double mark) {
        if (mark >= 85) return "A+";
        if (mark >= 75) return "A";
        if (mark >= 70) return "A-";
        if (mark >= 65) return "B+";
        if (mark >= 60) return "B";
        if (mark >= 55) return "B-";
        if (mark >= 50) return "C+";
        if (mark >= 45) return "C";
        if (mark >= 40) return "C-";
        if (mark >= 35) return "D";
        if (mark >= 0)  return "E";
        return "N/A";
    }

    public double findGPAPoint(String grade) {
        return switch (grade) {
            case "A+", "A" -> 4.0;
            case "A-"      -> 3.7;
            case "B+"      -> 3.3;
            case "B"       -> 3.0;
            case "B-"      -> 2.7;
            case "C+"      -> 2.3;
            case "C"       -> 2.0;
            case "C-"      -> 1.7;
            case "D"       -> 1.3;
            default        -> 0.0;
        };
    }

    private String sgpaLabel(double sgpa) {
        if (sgpa >= 3.7) return "First Class";
        if (sgpa >= 3.0) return "Second Class (Upper)";
        if (sgpa >= 2.0) return "Second Class (Lower)";
        if (sgpa >= 1.0) return "Pass";
        return "Fail";
    }
}
