package ums.roles;

import ums.Interfaces.IAttendanceViewer;
import ums.Interfaces.IGradeViewer;
import ums.dao.*;
import ums.model.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import java.util.Map;

public final class Lecturer extends AbstractUser implements IAttendanceViewer, IGradeViewer {

    private final IAttendanceDAO attendDAO = new AttendanceDAOImpl();
    private final IMarkDAO       markDAO   = new MarkDAOImpl();
    private final IGradeDAO      gradeDAO  = new GradeDAOImpl();
    private final IMedicalDAO    medDAO    = new MedicalDAOImpl();
    private final INoticeDAO     noticeDAO = new NoticeDAOImpl();
    private final ICourseDAO     courseDAO = new CourseDAOImpl();

    private String currentCourseId;
    private String currentCourseType;
    private String currentStudentId;
    private int    currentWeek;

    public Lecturer(String userId, String userName) {
        super(userId, userName);
    }

    /**
     * Lecturer can update their profile EXCEPT username and password.
     * Allowed: contact email, profile picture.
     */
    @Override
    public void updateProfile() {
        String[] opts = {"Update Contact Email", "Update Profile Picture"};
        int c = JOptionPane.showOptionDialog(null,
                "What would you like to update?", "Update Profile",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opts, opts[0]);
        if (c == 0) {
            String contact = JOptionPane.showInputDialog(null,
                    "Enter new contact email:", "Update Contact", JOptionPane.PLAIN_MESSAGE);
            if (contact != null && !contact.isBlank()) {
                if (updateContact(contact))
                    JOptionPane.showMessageDialog(null, "Contact updated successfully.");
            }
        } else if (c == 1) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Profile Photo");
            fc.setFileFilter(new FileNameExtensionFilter(
                    "Image files (jpg, png, gif)", "jpg", "jpeg", "png", "gif"));
            fc.setAcceptAllFileFilterUsed(false);
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                if (userDAO.updateProfilePicture(userId, fc.getSelectedFile().getAbsolutePath()))
                    JOptionPane.showMessageDialog(null, "Profile picture updated successfully.");
            }
        }
    }

    @Override
    public void viewNotice() {
        List<Notice> list = noticeDAO.getAllActive();
        StringBuilder sb = new StringBuilder();
        list.forEach(n -> sb.append("• ").append(n.getContent()).append("\n"));
        JOptionPane.showMessageDialog(null,
                sb.length() > 0 ? sb.toString() : "No notices.", "Notices",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ── Marks ──────────────────────────────────────────────────────────────
    public boolean    uploadMark(Mark m)           { return markDAO.uploadMark(m); }
    public Mark       viewFinalMarkIndividual()    { return markDAO.getFinalMarkByStudent(currentStudentId, currentCourseId, currentCourseType); }
    public List<Mark> viewFinalMarkWholeBatch()    { return markDAO.getFinalMarksByBatch(currentCourseId, currentCourseType); }
    public Mark       viewCAIndividual()           { return markDAO.getCAByStudent(currentStudentId, currentCourseId, currentCourseType); }
    public List<Mark> viewCAWholeBatch()           { return markDAO.getCAByBatch(currentCourseId, currentCourseType); }

    // ── Medical ────────────────────────────────────────────────────────────
    public List<MedicalRecord> viewEndMedical()    { return medDAO.getAll("end"); }
    public List<MedicalRecord> viewMidMedical()    { return medDAO.getAll("mid"); }
    public List<MedicalRecord> viewAttendanceMed() { return medDAO.getAll("attendance"); }
    public boolean approveMedical(String id, String type) { return medDAO.updateStatus(id, type, "Approved"); }
    public boolean rejectMedical(String id, String type)  { return medDAO.updateStatus(id, type, "Not-approved"); }

    // ── Courses ────────────────────────────────────────────────────────────
    public List<Course> getMyCourses() { return courseDAO.getByLecturer(userId); }

    // ── IAttendanceViewer ──────────────────────────────────────────────────
    @Override public void viewAttendanceIndividual()                { attendDAO.getByStudent(currentStudentId, currentCourseId, currentCourseType); }
    @Override public void viewAttendanceWholeBatch()                { attendDAO.getByBatch(currentCourseId, currentCourseType, currentWeek); }
    @Override public void viewAttendanceTheory()                    { attendDAO.getByType(currentCourseId, "T"); }
    @Override public void viewAttendancePractical()                 { attendDAO.getByType(currentCourseId, "P"); }
    @Override public void viewAttendancePracticaBoth()              { attendDAO.getByType(currentCourseId); }
    @Override public void viewAttendanceEligibilityWithMedical()    { attendDAO.getEligibleMedical(currentCourseId, currentCourseType); }
    @Override public void viewAttendanceNotEligibilityWithMedical() { attendDAO.getEligibleMedical(currentCourseId, currentCourseType); }
    @Override public void viewAttendanceEqualEligibility()          { attendDAO.getEligibleStudents(currentCourseId, currentCourseType); }
    @Override public void viewAttendanceAboveEligibility()          { attendDAO.getEligibleStudents(currentCourseId, currentCourseType); }
    @Override public void viewAttendanceNotEligibility()            { attendDAO.getEligibleStudents(currentCourseId, currentCourseType); }
    @Override public void viewEligibilityIndividual()               { attendDAO.getAttendanceRate(currentCourseId, currentCourseType, currentStudentId); }
    @Override public void viewEligibilityWholeBatch()               { attendDAO.getAttendanceRateBatch(currentCourseId, currentCourseType); }

    public List<Attendance>           getAttendanceByStudent() { return attendDAO.getByStudent(currentStudentId, currentCourseId, currentCourseType); }
    public List<Attendance>           getAttendanceByBatch()   { return attendDAO.getByBatch(currentCourseId, currentCourseType, currentWeek); }
    public Map<String,List<Attendance>> getEligibility()       { return attendDAO.getEligibleStudents(currentCourseId, currentCourseType); }
    public Map<String,List<Attendance>> getMedicalElig()       { return attendDAO.getEligibleMedical(currentCourseId, currentCourseType); }
    public double                     getIndividualRate()      { return attendDAO.getAttendanceRate(currentCourseId, currentCourseType, currentStudentId); }
    public List<Attendance>           getBatchRate()           { return attendDAO.getAttendanceRateBatch(currentCourseId, currentCourseType); }

    // ── IGradeViewer ───────────────────────────────────────────────────────
    @Override public void viewGradeIndividual()  { gradeDAO.getGradeByStudent(currentStudentId, currentCourseId, currentCourseType); }
    @Override public void viewGradeWholeBatch()  { gradeDAO.getGradesByBatch(currentCourseId, currentCourseType); }
    @Override public void viewSGPAIndividual()   { gradeDAO.getSGPAByStudent(currentStudentId); }
    @Override public void viewSGPAWholeBatch()   { gradeDAO.getGradesByBatch(currentCourseId, currentCourseType); }

    public Grade           getGradeIndividual()  { return gradeDAO.getGradeByStudent(currentStudentId, currentCourseId, currentCourseType); }
    public List<Grade>     getGradeBatch()       { return gradeDAO.getGradesByBatch(currentCourseId, currentCourseType); }
    public Grade           getSGPA()             { return gradeDAO.getSGPAByStudent(currentStudentId); }
    public List<Grade>     getGPAWithGrade()     { return gradeDAO.getGPAWithGrade(currentStudentId); }

    public void   setCurrentCourseId(String v)   { currentCourseId = v; }
    public void   setCurrentCourseType(String v) { currentCourseType = v; }
    public void   setCurrentStudentId(String v)  { currentStudentId = v; }
    public void   setCurrentWeek(int v)          { currentWeek = v; }
    public String getCurrentCourseId()           { return currentCourseId; }
    public String getCurrentCourseType()         { return currentCourseType; }
}
