package ums.dao;

import ums.model.User;
import java.util.List;

public sealed interface IUserDAO permits UserDAOImpl {
    User   findById(String userId);
    User   authenticate(String userId, String password);
    List<User> findAll();
    boolean createUser(User user);
    boolean deleteUser(String userId);
    boolean updateUserName(String userId, String name);
    boolean updatePassword(String userId, String hash);
    boolean updateContact(String userId, String contact);
    boolean updateProfilePicture(String userId, String path);
    String  getCurrentEmail(String userId);
}
