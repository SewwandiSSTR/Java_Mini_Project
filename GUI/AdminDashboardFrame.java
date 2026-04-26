package ums.gui;

import ums.gui.panels.*;
import ums.roles.Admin;

public class AdminDashboardFrame extends BaseDashboardFrame {

    private final Admin admin;

    public AdminDashboardFrame(Admin admin) {
        super(admin, "Admin Dashboard");
        this.admin = admin;

        registerMenu("  Home",           () -> new HomePanel("Admin", admin.getUserName()));
        registerMenu("  Manage Users",   () -> new ManageUsersPanel(admin));
        registerMenu("  Manage Courses", () -> new CoursePanel(admin));
        registerMenu("  Notices",        () -> new NoticePanel(admin));
        registerMenu("  Timetable",      () -> new TimetablePanel(admin));
        registerMenu("  My Profile",     () -> new ManageProfilePanel(admin, this));

        finishBuild();
    }
}
