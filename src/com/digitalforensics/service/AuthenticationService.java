package com.digitalforensics.service;

import com.digitalforensics.dao.UserDAO;
import com.digitalforensics.exception.ForensicsException;
import com.digitalforensics.exception.UnauthorizedAccessException;
import com.digitalforensics.model.User;
import com.digitalforensics.util.HashUtil;

import java.util.List;

public class AuthenticationService {

    private final UserDAO userDAO = new UserDAO();

    /**
     * Checks username + password.
     * The same message is used for "no such user" and "wrong password"
     * so an attacker cannot find out which usernames exist.
     */
    public User login(String username, String password) throws ForensicsException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new ForensicsException("Please enter both username and password.");
        }
        User user = userDAO.findByUsername(username.trim());
        if (user == null || !HashUtil.matches(user.getPasswordHash(), HashUtil.hashPassword(user.getSalt(), password))) {
            throw new ForensicsException("Invalid username or password.");
        }
        if (!user.isActive()) {
            throw new UnauthorizedAccessException("This account has been deactivated.");
        }
        userDAO.updateLastLogin(user.getId());
        return user;
    }

    public List<User> activeUsers() {
        return userDAO.findActive();
    }
}
