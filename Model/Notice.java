package ums.model;

import java.sql.Timestamp;

public class Notice {
    private String noticeId, content, createdBy;
    private boolean active;
    private Timestamp createdAt;

    public Notice() {}
    public Notice(String noticeId, String content, String createdBy) {
        this.noticeId = noticeId; this.content = content;
        this.createdBy = createdBy; this.active = true;
    }

    public String    getNoticeId()          { return noticeId; }
    public void      setNoticeId(String v)  { noticeId = v; }
    public String    getContent()           { return content; }
    public void      setContent(String v)   { content = v; }
    public String    getCreatedBy()         { return createdBy; }
    public void      setCreatedBy(String v) { createdBy = v; }
    public boolean   isActive()             { return active; }
    public void      setActive(boolean v)   { active = v; }
    public Timestamp getCreatedAt()         { return createdAt; }
    public void      setCreatedAt(Timestamp v){ createdAt = v; }

    @Override public String toString() { return content; }
}
