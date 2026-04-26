package ums.model;

public class Course {
    private String courseName, courseCode, courseType, coordinator, Department;
    private int credit;

    public Course() {}
    public Course(String courseName, String courseCode, String courseType,
                  int credit, String coordinator, String Department) {
        this.courseName = courseName; this.courseCode = courseCode;
        this.courseType = courseType; this.credit = credit;
        this.coordinator = coordinator; this.Department = Department;
    }

    public String getCourseName()          { return courseName; }
    public void   setCourseName(String v)  { courseName = v; }
    public String getCourseCode()          { return courseCode; }
    public void   setCourseCode(String v)  { courseCode = v; }
    public String getCourseType()          { return courseType; }
    public void   setCourseType(String v)  { courseType = v; }
    public int    getCredits()             { return credit; }
    public void   setCredits(int v)        { credit = v; }
    public String getCoordinator()         { return coordinator; }
    public void   setCoordinator(String v) { coordinator = v; }
    public String getDepartment()          { return Department; }
    public void   setDepartment(String v)  { Department = v; }

    @Override public String toString() { return courseCode + " - " + courseName + " (" + courseType + ")"; }
}
