package ums.gui;

import ums.gui.panels.*;
import ums.roles.Undergraduate;

public class UGDashboardFrame extends BaseDashboardFrame {

    public UGDashboardFrame(Undergraduate ug) {
        super(ug, "Student Dashboard");

        registerMenu("  Home",             () -> new HomePanel("Student", ug.getUserName()));
        registerMenu("  My Courses",       () -> new UGCoursesPanel(ug));      // see course details
        registerMenu("  My Attendance",    () -> new UGAttendancePanel(ug));    // see attendance
        registerMenu("  My Eligibility",   () -> new UGMarksPanel(ug));         // CA/End/Att eligibility
        registerMenu("  My Grades & SGPA", () -> new UGGradesPanel(ug));        // grades + GPA
        registerMenu("  Medical",          () -> new UGMedicalPanel(ug));        // see + submit medical
        registerMenu("  Notices",          () -> new NoticePanel(ug));           // see notices
        registerMenu("  Timetable",        () -> new TimetablePanel(ug));        // see timetable
        registerMenu("  My Profile",       () -> new ManageProfilePanel(ug, this));    // contact + photo only

        finishBuild();
    }
}
