package ums.roles;

import ums.dao.*;
import ums.model.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;

public final class TechnicalOfficer extends AbstractUser {

    private final IAttendanceDAO attendDAO = new AttendanceDAOImpl();
    private final IMedicalDAO    medDAO    = new MedicalDAOImpl();
    private final INoticeDAO     noticeDAO = new NoticeDAOImpl();
    private final ITimetableDAO  ttDAO     = new TimetableDAOImpl();

    public TechnicalOfficer(String userId, String userName) {
        super(userId, userName);
    }

    /**
     * TO can update their profile EXCEPT username and password.
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

    public boolean addAttendance(Attendance a)    { return attendDAO.addAttendance(a); }
    public boolean updateAttendance(Attendance a) { return attendDAO.updateAttendance(a); }

    public List<Attendance> getAttendanceByBatch(String courseId, String type, int week) {
        return attendDAO.getByBatch(courseId, type, week);
    }

    public boolean submitMedical(MedicalRecord r)              { return medDAO.addMedical(r); }
    public List<MedicalRecord> getPendingMedical(String type)  { return medDAO.getAll(type); }
    public boolean deleteMedical(String id, String type)       { return medDAO.deleteById(id, type); }

    public List<Timetable> getTimetable()                      { return ttDAO.getFullTimetable(); }
}
