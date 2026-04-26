package ums.roles;

import ums.dao.*;
import ums.model.*;

import javax.swing.*;
import java.util.List;

public final class Admin extends AbstractUser {

    private final INoticeDAO    noticeDAO    = new NoticeDAOImpl();
    private final ICourseDAO    courseDAO    = new CourseDAOImpl();
    private final ITimetableDAO timetableDAO = new TimetableDAOImpl();

    public Admin(String userId, String userName) {
        super(userId, userName);
    }

    /** Admin can change username, password, and contact. */
    @Override
    public void updateProfile() {
        String[] options = {"Change Username", "Change Password", "Update Contact"};
        int choice = JOptionPane.showOptionDialog(null, "What would you like to update?",
                "Update Profile", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);
        if (choice == 0) updateUserName();
        else if (choice == 1) updatePassword();
        else if (choice == 2) {
            String contact = JOptionPane.showInputDialog(null, "Enter new contact email:");
            if (contact != null && !contact.isBlank()) updateContact(contact);
        }
    }

    @Override
    public void viewNotice() {
        List<Notice> list = noticeDAO.getAllActive();
        if (list.isEmpty()) { JOptionPane.showMessageDialog(null, "No notices found."); return; }
        StringBuilder sb = new StringBuilder("=== Notices ===\n");
        list.forEach(n -> sb.append("• ").append(n.getContent())
                            .append("\n  [").append(n.getCreatedAt()).append("]\n\n"));
        JOptionPane.showMessageDialog(null,
                new JScrollPane(new JTextArea(sb.toString(), 15, 50)),
                "Notices", JOptionPane.INFORMATION_MESSAGE);
    }

    // ── User management ───────────────────────────────────────────────────
    public boolean   createUserRecord(User u)       { return userDAO.createUser(u); }
    public boolean   deleteUserRecord(String id)    { return userDAO.deleteUser(id); }
    public List<User> getAllUsers()                  { return userDAO.findAll(); }

    // ── Notice management ─────────────────────────────────────────────────
    public boolean   createNotice(String content)   { return noticeDAO.createNotice(new Notice(null, content, userId)); }
    public boolean   updateNotice(Notice n)          { return noticeDAO.updateNotice(n); }
    public boolean   deleteNotice(int id)            { return noticeDAO.deleteNotice(id); }
    public List<Notice> getNotices()                 { return noticeDAO.getAllActive(); }

    // ── Course management ─────────────────────────────────────────────────
    public boolean   createCourse(Course c)                       { return courseDAO.createCourse(c); }
    public boolean   updateCourse(Course c)                       { return courseDAO.updateCourse(c); }
    public boolean   deleteCourse(String code, String type)       { return courseDAO.deleteCourse(code, type); }
    public List<Course> getAllCourses()                            { return courseDAO.getAll(); }

    // ── Timetable management ──────────────────────────────────────────────
    public boolean   createTimetableSlot(Timetable t)             { return timetableDAO.createSlot(t); }
    public boolean   deleteTimetableSlot(String day, String c, String t) { return timetableDAO.deleteSlot(day, c, t); }
    public List<Timetable> getTimetable()                         { return timetableDAO.getFullTimetable(); }
}
