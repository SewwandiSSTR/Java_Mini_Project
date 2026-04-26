package ums.roles;

import ums.Interfaces.IAttendanceViewer;
import ums.Interfaces.IGradeViewer;
import ums.dao.*;
import ums.model.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;

public final class Undergraduate extends AbstractUser implements IAttendanceViewer, IGradeViewer {

    private final IAttendanceDAO attendDAO = new AttendanceDAOImpl();
    private final IMedicalDAO    medDAO    = new MedicalDAOImpl();
    private final ICourseDAO     courseDAO = new CourseDAOImpl();
    private final IGradeDAO      gradeDAO  = new GradeDAOImpl();
    private final IMarkDAO       markDAO   = new MarkDAOImpl();
    private final INoticeDAO     noticeDAO = new NoticeDAOImpl();
    private final ITimetableDAO  ttDAO     = new TimetableDAOImpl();

    public Undergraduate(String userId, String userName) {
        super(userId, userName);
    }

    /**
     * Undergraduate can update ONLY:
     *  - Contact details (email)
     *  - Profile picture
     * Cannot change username or password via this method.
     */
    @Override
    public void updateProfile() {
        String[] opts = {"Update Contact Email", "Update Profile Picture"};
        int c = JOptionPane.showOptionDialog(null,
                "What would you like to update?", "Update Profile",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, opts, opts[0]);

        if (c == 0) {
            // Contact email update only
            String contact = JOptionPane.showInputDialog(null,
                    "Enter new contact email:", "Update Contact", JOptionPane.PLAIN_MESSAGE);
            if (contact != null && !contact.isBlank()) {
                if (updateContact(contact))
                    JOptionPane.showMessageDialog(null, "Contact updated successfully.");
                else
                    JOptionPane.showMessageDialog(null, "Failed to update contact.");
            }
        } else if (c == 1) {
            // Profile picture update
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Profile Photo");
            fc.setFileFilter(new FileNameExtensionFilter(
                    "Image files (jpg, png, gif)", "jpg", "jpeg", "png", "gif"));
            fc.setAcceptAllFileFilterUsed(false);
            if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                String path = fc.getSelectedFile().getAbsolutePath();
                if (userDAO.updateProfilePicture(userId, path))
                    JOptionPane.showMessageDialog(null, "Profile picture updated successfully.");
                else
                    JOptionPane.showMessageDialog(null, "Failed to update profile picture.");
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

    // UG-accessible data
    public List<Course>    getMyCourses()                              { return courseDAO.getByStudent(userId); }
    public List<Timetable> getTimetable()                              { return ttDAO.getFullTimetable(); }
    public List<Notice>    getNotices()                                { return noticeDAO.getAllActive(); }

    public Mark            getMyMarks(String courseId, String type)    { return markDAO.getFinalMarkByStudent(userId, courseId, type); }
    public List<Grade>     getMyGradesWithGPA()                        { return gradeDAO.getGPAWithGrade(userId); }
    public Grade           getMySGPA()                                 { return gradeDAO.getSGPAByStudent(userId); }
    public Grade           getMyGrade(String courseId, String type)    { return gradeDAO.getGradeByStudent(userId, courseId, type); }

    public List<Attendance> getMyAttendance(String code, String type)  { return attendDAO.getByStudent(userId, code, type); }
    public double getMyAttendanceRate(String code, String type)        { return attendDAO.getAttendanceRate(code, type, userId); }

    public boolean submitMedical(MedicalRecord r)                      { r.setRegno(userId); return medDAO.addMedical(r); }
    public List<MedicalRecord> getMyMedical(String code, String type, String mtype) { return medDAO.getByStudent(userId, code, type, mtype); }

    // ── IAttendanceViewer ─────────────────────────────────────────────────
    @Override public void viewAttendanceIndividual()                { }
    @Override public void viewAttendanceWholeBatch()                { }
    @Override public void viewAttendanceTheory()                    { }
    @Override public void viewAttendancePractical()                 { }
    @Override public void viewAttendancePracticaBoth()              { }
    @Override public void viewAttendanceEligibilityWithMedical()    { }
    @Override public void viewAttendanceNotEligibilityWithMedical() { }
    @Override public void viewAttendanceEqualEligibility()          { }
    @Override public void viewAttendanceAboveEligibility()          { }
    @Override public void viewAttendanceNotEligibility()            { }
    @Override public void viewEligibilityIndividual()               { }
    @Override public void viewEligibilityWholeBatch()               { }

    // ── IGradeViewer ──────────────────────────────────────────────────────
    @Override public void viewGradeIndividual()   { }
    @Override public void viewGradeWholeBatch()   { }
    @Override public void viewSGPAIndividual()    { }
    @Override public void viewSGPAWholeBatch()    { }
}
