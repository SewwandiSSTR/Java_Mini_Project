package ums.gui;

import ums.gui.panels.*;
import ums.roles.Lecturer;

public class LecturerDashboardFrame extends BaseDashboardFrame {

    public LecturerDashboardFrame(Lecturer lecturer) {
        super(lecturer, "Lecturer Dashboard");

        registerMenu(" Home",           () -> new HomePanel("Lecturer", lecturer.getUserName()));
        registerMenu("  UG Details",     () -> new LecturerUGDetailsPanel(lecturer));   // see UG details
        registerMenu("  Attendance",     () -> new AttendancePanel(lecturer));           // see attendance
        registerMenu("  Marks & Grades", () -> new MarkGradePanel(lecturer));            // upload + view marks/grades/GPA
        registerMenu("  Medical",        () -> new MedicalPanel(lecturer));              // see + approve/reject medical
        registerMenu("  Materials",      () -> new MaterialPanel(lecturer));             // add/modify materials
        registerMenu("  Notices",        () -> new NoticePanel(lecturer));               // see notices
        registerMenu("  My Profile",     () -> new ManageProfilePanel(lecturer, this));        // contact + photo only

        finishBuild();
    }
}
