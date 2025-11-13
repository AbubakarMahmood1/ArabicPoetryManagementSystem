package com.arabicpoetry.bll.service;

import com.arabicpoetry.dal.DAOFactory;
import com.arabicpoetry.dal.dao.UserDAO;
import com.arabicpoetry.model.User;
import com.arabicpoetry.util.PasswordUtil;

import java.sql.SQLException;

/**
 * Service class for user authentication (Singleton pattern)
 * Handles login and user session management
 */
public class AuthenticationService {
    private static AuthenticationService instance;
    private UserDAO userDAO;
    private User currentUser;

    // Private constructor for Singleton pattern
    private AuthenticationService() throws SQLException {
        this.userDAO = DAOFactory.getInstance().getUserDAO();
    }

    /**
     * Get singleton instance
     * @throws SQLException 
     */
    public static synchronized AuthenticationService getInstance() throws SQLException {
        if (instance == null) {
            instance = new AuthenticationService();
        }
        return instance;
    }

    /**
     * Login user with username and password
     * @return User object if successful, null otherwise
     */
    public User login(String username, String password) throws SQLException {
        User user = userDAO.findByUsername(username);

        if (user != null && user.isActive()) {
            if (PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
                currentUser = user;
                userDAO.updateLastLogin(user.getUserId());
                return user;
            }
        }
        return null;
    }

    /**
     * Logout current user
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Get current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Register new user
     */
    public void registerUser(String username, String password, String fullName) throws SQLException {
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setFullName(fullName);
        user.setActive(true);
        userDAO.create(user);
    }
}
