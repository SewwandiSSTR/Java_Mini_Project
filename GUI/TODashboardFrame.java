package ums.gui;

import ums.gui.panels.*;
import ums.roles.TechnicalOfficer;

public class TODashboardFrame extends BaseDashboardFrame {

    public TODashboardFrame(TechnicalOfficer to) {
        super(to, "Technical Officer Dashboard");

        registerMenu(" Home",        () -> new HomePanel("Technical Officer", to.getUserName()));
        registerMenu("  Attendance",  () -> new TOAttendancePanel(to));    // add + maintain attendance
        registerMenu("  Medical",     () -> new MedicalPanel(to));         // add + maintain medical
        registerMenu("  Notices",     () -> new NoticePanel(to));          // see notices
        registerMenu("  Timetable",   () -> new TimetablePanel(to));       // see timetable
        registerMenu("  My Profile",  () -> new ManageProfilePanel(to, this));   // contact + photo only

        finishBuild();
    }
}
