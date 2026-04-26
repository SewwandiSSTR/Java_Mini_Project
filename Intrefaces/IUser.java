package ums.Interfaces;

/**
 * Core contract for all system users.
 * Implemented by AbstractUser (sealed class hierarchy in ums.roles).
 */
public interface IUser {
    void updateProfile();
    void updateUserName();
    void updatePassword();
    void viewNotice();
}
