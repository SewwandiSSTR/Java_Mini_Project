package ums.model;

import java.sql.Timestamp;

/**
 * Represents a PDF material uploaded by a Lecturer for a course.
 * Stored in the Material table.
 */
public class Material {
    private int    materialId;
    private String C_code;
    private String C_type;
    private String Lec_id;
    private String title;
    private String description;
    private String filePath;       // absolute path to the PDF file on disk
    private String fileName;       // original file name shown to users
    private Timestamp uploadedAt;

    public Material() {}

    public int       getMaterialId()              { return materialId; }
    public void      setMaterialId(int v)         { this.materialId = v; }
    public String    getC_code()                  { return C_code; }
    public void      setC_code(String v)          { this.C_code = v; }
    public String    getC_type()                  { return C_type; }
    public void      setC_type(String v)          { this.C_type = v; }
    public String    getLec_id()                  { return Lec_id; }
    public void      setLec_id(String v)          { this.Lec_id = v; }
    public String    getTitle()                   { return title; }
    public void      setTitle(String v)           { this.title = v; }
    public String    getDescription()             { return description; }
    public void      setDescription(String v)     { this.description = v; }
    public String    getFilePath()                { return filePath; }
    public void      setFilePath(String v)        { this.filePath = v; }
    public String    getFileName()                { return fileName; }
    public void      setFileName(String v)        { this.fileName = v; }
    public Timestamp getUploadedAt()              { return uploadedAt; }
    public void      setUploadedAt(Timestamp v)   { this.uploadedAt = v; }

    @Override
    public String toString() { return title + " (" + fileName + ")"; }
}
