package ums.dao;

import ums.model.Course;
import java.util.List;

public sealed interface ICourseDAO permits CourseDAOImpl {
    boolean      createCourse(Course c);
    boolean      updateCourse(Course c);
    boolean      deleteCourse(String courseCode, String courseType);
    List<Course> getAll();
    Course       getById(String courseCode, String courseType);
    List<Course> getByStudent(String studentId);
    List<Course> getByLecturer(String lecId);
}
