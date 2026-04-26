package ums.model;

/**
 * Represents the final grade outcome for one student–course pair.
 *
 * Grade codes:
 *   Normal : A+, A, A−, B+, B, B−, C+, C, C−, D, E
 *   ECA    : CA component failed
 *   EE     : End exam failed
 *   E      : both failed
 *   MC     : Medical (approved medical for Mid or End exam)
 *
 * SGPA rules:
 *   If ANY course grade is MC → overall SGPA label = "WH" (withheld)
 *   Otherwise computed normally weighted by credits.
 *
 * Eligibility (CA + End shown to student):
 *   caEligible    true when CA mark ≥ 40 % of CA max weight
 *   endEligible   true when End mark ≥ 40
 *   attEligible   true when attendance rate (with medical) ≥ 80 %
 */
public class
Grade {

    // ── Identity ──────────────────────────────────────────────────────────
    private String  Regno;
    private String  courseId;
    private String  courseType;

    // ── Grade outcome ──────────────────────────────────────────────────────
    /** Alpha/special grade string (A+, MC, ECA, EE, E …) */
    private String  grade;
    /** GPA point (0.0 – 4.0); 0.0 for ECA/EE/E/MC */
    private double  gpaPoints;

    // ── SGPA ──────────────────────────────────────────────────────────────
    /** Calculated SGPA value (only meaningful on a "summary" Grade object) */
    private double  SGPA;
    /**
     * True when at least one course this semester has grade "MC".
     * When true the SGPA should be displayed as "WH" (withheld).
     */
    private boolean sgpaWithheld;

    // ── Eligibility flags (shown to student on their result panel) ─────────
    private boolean caEligible;
    private boolean endEligible;
    private boolean attEligible;

    private double  CGPA;           // cumulative GPA value
    private boolean cgpaWithheld;   // true when any course = MC

    /** Weighted CA mark */
    private double  caWeighted;
    /** Weighted End mark */
    private double  endWeighted;
    /** Attendance rate with medical (0–100) */
    private double  attendanceRate;

    // ── Raw marks (optional, for display) ─────────────────────────────────
    private double  finalMark;
    private double  caMark;

    public Grade() {}

    // ── Getters / Setters ──────────────────────────────────────────────────
    public String  getRegno()                      { return Regno; }
    public void    setRegno(String v)              { this.Regno = v; }
    public String  getCourseId()                   { return courseId; }
    public void    setCourseId(String v)           { this.courseId = v; }
    public String  getCourseType()                 { return courseType; }
    public void    setCourseType(String v)         { this.courseType = v; }

    public String  getGrade()                      { return grade; }
    public void    setGrade(String v)              { this.grade = v; }
    public double  getGpaPoints()                  { return gpaPoints; }
    public void    setGpaPoints(double v)          { this.gpaPoints = v; }

    public double  getSGPA()                       { return SGPA; }
    public void    setSGPA(double v)               { this.SGPA = v; }
    public boolean isSgpaWithheld()                { return sgpaWithheld; }
    public void    setSgpaWithheld(boolean v)      { this.sgpaWithheld = v; }

    public boolean isCaEligible()                  { return caEligible; }
    public void    setCaEligible(boolean v)        { this.caEligible = v; }
    public boolean isEndEligible()                 { return endEligible; }
    public void    setEndEligible(boolean v)       { this.endEligible = v; }
    public boolean isAttEligible()                 { return attEligible; }
    public void    setAttEligible(boolean v)       { this.attEligible = v; }

    public double  getCaWeighted()                 { return caWeighted; }
    public void    setCaWeighted(double v)         { this.caWeighted = v; }
    public double  getEndWeighted()                { return endWeighted; }
    public void    setEndWeighted(double v)        { this.endWeighted = v; }
    public double  getAttendanceRate()             { return attendanceRate; }
    public void    setAttendanceRate(double v)     { this.attendanceRate = v; }

    public double  getFinalMark()                  { return finalMark; }
    public void    setFinalMark(double v)          { this.finalMark = v; }
    public double  getCaMark()                     { return caMark; }
    public void    setCaMark(double v)             { this.caMark = v; }

    public double  getCGPA()                     { return CGPA; }
    public void    setCGPA(double v)             { this.CGPA = v; }

    public boolean  isCgpaWithheld()                     { return cgpaWithheld; }
    public void   setCgpaWithheld(boolean v)             { this.cgpaWithheld = v; }
}
