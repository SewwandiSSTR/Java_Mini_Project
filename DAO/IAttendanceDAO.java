package ums.dao;

import ums.model.Attendance;
import java.util.List;
import java.util.Map;

public sealed interface IAttendanceDAO permits AttendanceDAOImpl {
    boolean addAttendance(Attendance a);
    boolean updateAttendance(Attendance a);
    List<Attendance> getByStudent(String studentId, String C_code, String C_type);
    List<Attendance> getByBatch(String courseId, String c_type, int Week);
    List<Attendance> getByBatchBothTypes(String courseId, int week);
    List<Attendance> getByRegno(String regno);
    List<Attendance> getByRegnoAndCourse(String regno, String courseId);
    List<Attendance> getByType(String C_code, String C_type);
    List<Attendance> getByType(String C_code);
    Map<String, List<Attendance>> getEligibleStudents(String courseId, String cType);
    Map<String, List<Attendance>> getEligibleMedical(String courseId, String cType);
    double getAttendanceRate(String courseId, String courseType, String studentId);
    List<Attendance> getAttendanceRateBatch(String courseId, String courseType);
}
