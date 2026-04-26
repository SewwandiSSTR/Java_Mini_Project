package ums.model;

import java.sql.Date;

public class MedicalRecord {
    private String Medical_Id, C_code, C_type, Regno, Medical_status, Handle_at, Medi_type, Reason;
    private Date Submitted_Date, Medical_Start_Date, Medical_End_Date;

    public MedicalRecord() {}

    public String getMedical_Id()          { return Medical_Id; }
    public void   setMedical_Id(String v)  { Medical_Id = v; }
    public String getC_code()              { return C_code; }
    public void   setC_code(String v)      { C_code = v; }
    public String getC_type()              { return C_type; }
    public void   setC_type(String v)      { C_type = v; }
    public String getRegno()               { return Regno; }
    public void   setRegno(String v)       { Regno = v; }
    public String getMedical_status()      { return Medical_status; }
    public void   setMedical_status(String v){ Medical_status = v; }
    public String getHandle_at()           { return Handle_at; }
    public void   setHandle_at(String v)   { Handle_at = v; }
    public String getMedi_type()           { return Medi_type; }
    public void   setMedi_type(String v)   { Medi_type = v; }
    public Date   getSubmitted_Date()      { return Submitted_Date; }
    public void   setSubmitted_Date(Date v){ Submitted_Date = v; }
    public Date   getMedical_Start_Date()  { return Medical_Start_Date; }
    public void   setMedical_Start_Date(Date v){ Medical_Start_Date = v; }
    public Date   getMedical_End_Date()    { return Medical_End_Date; }
    public void   setMedical_End_Date(Date v){ Medical_End_Date = v; }
}
