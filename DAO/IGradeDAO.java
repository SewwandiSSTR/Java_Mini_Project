package ums.dao;

import ums.model.Grade;
import java.util.List;

public sealed interface IGradeDAO permits GradeDAOImpl {
    Grade       getGradeByStudent(String studentId, String courseId, String C_type);
    List<Grade> getGradesByBatch(String courseId, String courseType);
    Grade       getSGPAByStudent(String studentId);
    List<Grade> getGPAWithGrade(String studentId);
    Grade getCGPAByStudent(String studentId);
    Grade       getEligibilityByStudent(String studentId, String courseId, String C_type);
}
