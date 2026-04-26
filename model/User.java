package ums.model;

public class User {
    private String userId, userName, passwordHash, role, email, contact, profilePic;

    public User() {}

    public User(String userId, String userName, String passwordHash, String role, String email) {
        this.userId = userId; this.userName = userName;
        this.passwordHash = passwordHash; this.role = role; this.email = email;
    }

    public String getUserId()          { return userId; }
    public void   setUserId(String v)  { userId = v; }
    public String getUserName()        { return userName; }
    public void   setUserName(String v){ userName = v; }
    public String getPasswordHash()    { return passwordHash; }
    public void   setPasswordHash(String v){ passwordHash = v; }
    public String getRole()            { return role; }
    public void   setRole(String v)    { role = v; }
    public String getEmail()           { return email; }
    public void   setEmail(String v)   { email = v; }
    public String getContact()         { return contact; }
    public void   setContact(String v) { contact = v; }
    public String getProfilePic()      { return profilePic; }
    public void   setProfilePic(String v){ profilePic = v; }

    @Override public String toString() { return userName + " [" + role + "]"; }
}
