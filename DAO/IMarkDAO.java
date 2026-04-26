package ums.dao;

import ums.model.Mark;
import java.util.List;

public sealed interface IMarkDAO permits MarkDAOImpl {
    boolean    uploadMark(Mark m);
    Mark       getFinalMarkByStudent(String studentId, String courseId, String C_type);
    Mark       getCAByStudent(String studentId, String courseId, String C_type);
    List<Mark> getFinalMarksByBatch(String courseId, String C_type);
    List<Mark> getCAByBatch(String courseId, String C_type);
    List<Mark> calcBothTypes(String courseId);
    List<Mark> getMarksByStudent(String regno, String courseId);
}
