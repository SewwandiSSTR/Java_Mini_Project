package ums.model;

import java.sql.Date;

public class Attendance {
    private String C_code, C_type, Regno, attStatus;
    private int Week, L_hours;
    private double rate;
    private Date attDate;

    public Attendance() {}

    public String getC_code()         { return C_code; }
    public void   setC_code(String v) { C_code = v; }
    public String getC_type()         { return C_type; }
    public void   setC_type(String v) { C_type = v; }
    public String getRegno()          { return Regno; }
    public void   setRegno(String v)  { Regno = v; }
    public String getAttStatus()      { return attStatus; }
    public void   setAttStatus(String v){ attStatus = v; }
    public int    getWeek()           { return Week; }
    public void   setWeek(int v)      { Week = v; }
    public int    getL_hours()        { return L_hours; }
    public void   setL_hours(int v)   { L_hours = v; }
    public double getRate()           { return rate; }
    public void   setRate(double v)   { rate = v; }
    public Date   getAttDate()        { return attDate; }
    public void   setAttDate(Date v)  { attDate = v; }
}
