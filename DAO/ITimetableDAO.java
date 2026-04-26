package ums.dao;

import ums.model.Timetable;
import java.util.List;

public sealed interface ITimetableDAO permits TimetableDAOImpl {
    boolean createSlot(Timetable t);
    boolean deleteSlot(String day, String C_code, String C_type);
    List<Timetable> getFullTimetable();
}
