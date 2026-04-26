package ums.model;

import java.sql.Time;

public class Timetable {
    private String Day, C_code, C_type, Lec_id;
    private Time startTime, endTime;

    public Timetable() {}

    public String getDay()              { return Day; }
    public void   setDay(String v)      { Day = v; }
    public String getC_code()           { return C_code; }
    public void   setC_code(String v)   { C_code = v; }
    public String getC_type()           { return C_type; }
    public void   setC_type(String v)   { C_type = v; }
    public String getLec_id()           { return Lec_id; }
    public void   setLec_id(String v)   { Lec_id = v; }
    public Time   getStartTime()        { return startTime; }
    public void   setStartTime(Time v)  { startTime = v; }
    public Time   getEndTime()          { return endTime; }
    public void   setEndTime(Time v)    { endTime = v; }
}
