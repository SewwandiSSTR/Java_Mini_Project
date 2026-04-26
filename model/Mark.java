package ums.model;

/**
 * Holds all raw marks + derived CA / END / Final for one student–course pair.
 *
 * Special grade codes produced by GradeDAOImpl:
 *   MC   – at least one approved medical (Mid or End); no numeric grade.
 *           SGPA denominator counts the course but numerator uses 0 → SGPA shown as "WH"
 *   ECA  – CA component failed  (CA weighted mark < 40 % of its max weight)
 *   EE   – End exam failed       (End weighted mark < 40 % of its max weight)
 *   E    – both CA and End failed
 *   Normal alpha grades: A+, A, A−, B+, B, B−, C+, C, C−, D, E
 */
public class Mark {

    // ── Identity ──────────────────────────────────────────────────────────
    private String C_code;
    private String C_type;
    private String Regno;

    // ── Raw component marks (each entered out of 100) ─────────────────────
    private double Q1_mark;
    private double Q2_mark;
    private double Q3_mark;
    private double Assignment;
    private double Mid;
    private double End;
    private double Project;

    // ── Derived weighted values ────────────────────────────────────────────
    /** Best-two-quiz average, already scaled to the course's quiz weight (e.g. *0.10) */
    private double BestQuizWeighted;
    /** Continuous Assessment total after weighting (e.g. max ≈ 30) */
    private double CA;
    /** Weighted End-exam contribution (e.g. max 70 Theory / 60 Practical) */
    private double END;
    /** CA + END */
    private double Final;

    // ── CA / End-exam pass thresholds ─────────────────────────────────────
    /**
     * CAPass = true  when the raw CA percentage ≥ 40.
     * (CA weighted / CA max-weight) * 100 >= 40
     */
    private boolean CAPass;
    /**
     * EndPass = true  when the raw End mark ≥ 40.
     */
    private boolean EndPass;

    // ── Maximum weights (set during calculation for threshold checks) ──────
    private double CAMaxWeight;   // e.g. 0.30 for a 30-mark CA
    private double EndMaxWeight;  // e.g. 0.70 Theory, 0.60 Practical

    // ── Medical flags (approved medicals found in DB) ─────────────────────
    private boolean medicalMid;
    private boolean medicalEnd;
    private boolean medicalAttendance;

    // ── Attendance ────────────────────────────────────────────────────────
    /** Raw attendance rate (0–100), excluding medical absences */
    private double attendanceRate;
    /** Attendance rate counting approved medicals as present */
    private double attendanceRateWithMedical;
    /** True when attendanceRateWithMedical >= 80 */
    private boolean attendanceEligible;

    public Mark() {}

    // ── Getters / Setters ──────────────────────────────────────────────────
    public String  getC_code()                     { return C_code; }
    public void    setC_code(String v)             { this.C_code = v; }
    public String  getC_type()                     { return C_type; }
    public void    setC_type(String v)             { this.C_type = v; }
    public String  getRegno()                      { return Regno; }
    public void    setRegno(String v)              { this.Regno = v; }

    public double  getQ1_mark()                    { return Q1_mark; }
    public void    setQ1_mark(double v)            { this.Q1_mark = v; }
    public double  getQ2_mark()                    { return Q2_mark; }
    public void    setQ2_mark(double v)            { this.Q2_mark = v; }
    public double  getQ3_mark()                    { return Q3_mark; }
    public void    setQ3_mark(double v)            { this.Q3_mark = v; }
    public double  getAssignment()                 { return Assignment; }
    public void    setAssignment(double v)         { this.Assignment = v; }
    public double  getMid()                        { return Mid; }
    public void    setMid(double v)                { this.Mid = v; }
    public double  getEnd()                        { return End; }
    public void    setEnd(double v)                { this.End = v; }
    public double  getProject()                    { return Project; }
    public void    setProject(double v)            { this.Project = v; }

    public double  getBestQuizWeighted()           { return BestQuizWeighted; }
    public void    setBestQuizWeighted(double v)   { this.BestQuizWeighted = v; }
    public double  getCA()                         { return CA; }
    public void    setCA(double v)                 { this.CA = v; }
    public double  getEND()                        { return END; }
    public void    setEND(double v)                { this.END = v; }
    public double  getFinal()                      { return Final; }
    public void    setFinal(double v)              { this.Final = v; }

    public boolean isCAPass()                      { return CAPass; }
    public void    setCAPass(boolean v)            { this.CAPass = v; }
    public boolean isEndPass()                     { return EndPass; }
    public void    setEndPass(boolean v)           { this.EndPass = v; }

    public double  getCAMaxWeight()                { return CAMaxWeight; }
    public void    setCAMaxWeight(double v)        { this.CAMaxWeight = v; }
    public double  getEndMaxWeight()               { return EndMaxWeight; }
    public void    setEndMaxWeight(double v)       { this.EndMaxWeight = v; }

    public boolean isMedicalMid()                  { return medicalMid; }
    public void    setMedicalMid(boolean v)        { this.medicalMid = v; }
    public boolean isMedicalEnd()                  { return medicalEnd; }
    public void    setMedicalEnd(boolean v)        { this.medicalEnd = v; }
    public boolean isMedicalAttendance()           { return medicalAttendance; }
    public void    setMedicalAttendance(boolean v) { this.medicalAttendance = v; }

    public double  getAttendanceRate()             { return attendanceRate; }
    public void    setAttendanceRate(double v)     { this.attendanceRate = v; }
    public double  getAttendanceRateWithMedical()  { return attendanceRateWithMedical; }
    public void    setAttendanceRateWithMedical(double v) { this.attendanceRateWithMedical = v; }
    public boolean isAttendanceEligible()          { return attendanceEligible; }
    public void    setAttendanceEligible(boolean v){ this.attendanceEligible = v; }
}
